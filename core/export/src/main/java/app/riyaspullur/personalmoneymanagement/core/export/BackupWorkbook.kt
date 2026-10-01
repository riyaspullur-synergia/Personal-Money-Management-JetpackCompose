package app.riyaspullur.personalmoneymanagement.core.export

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.xml.parsers.SAXParserFactory

/** Minimal OOXML text workbook; uses Android-supported APIs without desktop Java dependencies. */
internal object BackupWorkbook {
    fun write(rows: List<List<String>>, output: OutputStream) {
        require(rows.size <= 1_048_576)
        val zip = ZipOutputStream(output)
        fun entry(name: String, text: String) {
            zip.putNextEntry(ZipEntry(name))
            zip.write(text.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        entry("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""")
        entry("_rels/.rels", """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
        entry("xl/workbook.xml", """<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Full backup" sheetId="1" r:id="rId1"/></sheets></workbook>""")
        entry("xl/_rels/workbook.xml.rels", """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""")
        zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
        val writer = zip.writer(Charsets.UTF_8)
        writer.write("""<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
        rows.forEachIndexed { index, row ->
            writer.write("<row r=\"${index + 1}\">")
            row.forEachIndexed { column, cell ->
                writer.write("<c r=\"${'A' + column}${index + 1}\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                writer.write(cell.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\r", "&#13;"))
                writer.write("</t></is></c>")
            }
            writer.write("</row>")
        }
        writer.write("</sheetData></worksheet>")
        writer.flush()
        zip.closeEntry()
        zip.finish()
    }

    fun read(input: InputStream): List<List<String>> {
        var sheet: ByteArray? = null
        var total = 0
        var entries = 0
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                require(++entries <= 16)
                val bytes = zip.readBytesLimited(BackupCodec.MAX_BYTES - total)
                total += bytes.size
                if (entry.name == "xl/worksheets/sheet1.xml") {
                    require(sheet == null)
                    sheet = bytes
                }
            }
        }
        val bytes = requireNotNull(sheet)
        // No DTD/entity declarations are part of this format. Reject before parsing on all Android versions.
        val xml = bytes.toString(Charsets.UTF_8)
        require(!xml.contains("<!DOCTYPE", ignoreCase = true) && !xml.contains("<!ENTITY", ignoreCase = true))
        val rows = mutableListOf<List<String>>()
        val factory = SAXParserFactory.newInstance().apply { isNamespaceAware = true }
        val reader = factory.newSAXParser().xmlReader
        reader.entityResolver = org.xml.sax.EntityResolver { _, _ -> throw IllegalArgumentException("External entity") }
        reader.contentHandler = object : DefaultHandler() {
            var row = mutableListOf<String>()
            val cell = StringBuilder()
            var inText = false
            override fun startElement(uri: String?, localName: String, qName: String?, attributes: Attributes) {
                when (localName) {
                    "row" -> { require(rows.size < 1_048_576); row = mutableListOf() }
                    "c" -> { require(attributes.getValue("t") == "inlineStr" && row.size < 4); cell.clear() }
                    "t" -> inText = true
                    "f" -> throw IllegalArgumentException("Formulas are not backup data")
                }
            }
            override fun characters(ch: CharArray, start: Int, length: Int) {
                if (inText) { cell.append(ch, start, length); require(cell.length <= 32_767) }
            }
            override fun endElement(uri: String?, localName: String, qName: String?) {
                when (localName) {
                    "t" -> inText = false
                    "c" -> row.add(cell.toString())
                    "row" -> { require(row.size == 4); rows.add(row) }
                }
            }
        }
        reader.parse(InputSource(xml.reader()))
        return rows
    }
}
