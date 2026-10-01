package app.riyaspullur.personalmoneymanagement.core.export

import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupArchive
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupData
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import javax.inject.Inject

/** Versioned, lossless spreadsheet backup. All Excel cells are text to preserve 64-bit amounts/IDs. */
class BackupCodec @Inject constructor() {
    private val json = Json { encodeDefaults = true }
    private val sections = listOf("accounts", "categories", "transactions", "budgets",
        "budgetCategoryLimits", "savingsGoals", "accountGroups")

    fun write(archive: BackupArchive, format: BackupFormat, output: OutputStream) {
        val rows = mutableListOf(listOf("PMM_BACKUP", "1", "part", "json"))
        fun record(section: String, index: Int, value: String) {
            var start = 0
            var part = 0
            while (start < value.length) {
                var end = minOf(start + 8_000, value.length)
                if (end < value.length && value[end - 1].isHighSurrogate()) end--
                rows += listOf(section, index.toString(), (part++).toString(), value.substring(start, end))
                start = end
            }
        }
        record("metadata", 0, json.encodeToString(archive.copy(data = BackupData(), receipts = emptyMap())))
        val data = json.parseToJsonElement(json.encodeToString(archive.data)).jsonObject
        sections.forEach { section ->
            (data.getValue(section) as JsonArray).forEachIndexed { index, value -> record(section, index, value.toString()) }
        }
        archive.receipts.entries.forEachIndexed { index, entry ->
            record("receipts", index, JsonObject(mapOf(entry.key to JsonPrimitive(entry.value))).toString())
        }
        require(rows.sumOf { row -> row.sumOf { it.length.toLong() } } <= MAX_BYTES)
        rows += listOf("END", (rows.size - 1).toString(), "SHA256", checksum(rows))
        when (format) {
            BackupFormat.CSV -> output.writer(Charsets.UTF_8).apply {
                rows.forEach { row ->
                    write(row.joinToString(",") { "\"'${it.replace("\"", "\"\"")}\"" })
                    write("\r\n")
                }
                flush()
            }
            BackupFormat.XLSX -> BackupWorkbook.write(rows, output)
        }
    }

    fun read(input: InputStream): BackupArchive {
        val buffered = input.buffered()
        buffered.mark(4)
        val isWorkbook = buffered.read() == 'P'.code && buffered.read() == 'K'.code
        buffered.reset()
        val rows = if (isWorkbook) BackupWorkbook.read(buffered) else {
            val bytes = buffered.readBytesLimited(MAX_BYTES)
            parseCsv(bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF")).map { row ->
                row.map { cell -> require(cell.startsWith("'")); cell.drop(1) }
            }
        }
        require(rows.size >= 3 && rows.first() == listOf("PMM_BACKUP", "1", "part", "json"))
        val footer = rows.last()
        require(footer.size == 4 && footer[0] == "END" && footer[1] == (rows.size - 2).toString() && footer[2] == "SHA256")
        require(footer[3] == checksum(rows.dropLast(1))) { "Backup is incomplete or has been edited" }
        val records = linkedMapOf<Pair<String, Int>, StringBuilder>()
        val parts = mutableMapOf<Pair<String, Int>, Int>()
        rows.subList(1, rows.lastIndex).forEach { row ->
            require(row.size == 4 && row[0] in sections + listOf("metadata", "receipts"))
            val key = row[0] to row[1].toInt()
            require(key.second >= 0 && row[2].toInt() == parts.getOrDefault(key, 0))
            records.getOrPut(key) { StringBuilder() }.append(row[3])
            parts[key] = parts.getOrDefault(key, 0) + 1
        }
        fun values(section: String) = records.filterKeys { it.first == section }.entries
            .sortedBy { it.key.second }.mapIndexed { index, entry ->
                require(entry.key.second == index)
                json.parseToJsonElement(entry.value.toString())
            }
        val metadata = values("metadata").single()
        val data = JsonObject(sections.associateWith { JsonArray(values(it)) })
        val receipts = linkedMapOf<String, kotlinx.serialization.json.JsonElement>()
        values("receipts").forEach { element ->
            val entry = element.jsonObject.entries.single()
            require(receipts.put(entry.key, entry.value) == null)
        }
        val root = JsonObject(metadata.jsonObject + mapOf("data" to data, "receipts" to JsonObject(receipts)))
        return json.decodeFromString<BackupArchive>(root.toString()).also {
            require(it.format == "PersonalMoneyManagementBackup" && it.version == 1)
        }
    }

    private fun checksum(rows: List<List<String>>): String {
        val digest = MessageDigest.getInstance("SHA-256")
        rows.forEach { row -> row.forEach { cell ->
            val bytes = cell.toByteArray(Charsets.UTF_8)
            digest.update("${bytes.size}:".toByteArray(Charsets.US_ASCII))
            digest.update(bytes)
        } }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun parseCsv(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val value = StringBuilder()
        var quoted = false
        var closed = false
        var i = 0
        while (i < text.length) {
            val c = text[i++]
            if (quoted) {
                if (c == '"') {
                    if (i < text.length && text[i] == '"') { value.append('"'); i++ }
                    else { quoted = false; closed = true }
                } else value.append(c)
            } else when (c) {
                '"' -> { require(value.isEmpty() && !closed); quoted = true }
                ',' -> { row.add(value.toString()); value.clear(); closed = false }
                '\r', '\n' -> {
                    if (c == '\r' && i < text.length && text[i] == '\n') i++
                    row.add(value.toString()); rows.add(row); row = mutableListOf(); value.clear(); closed = false
                }
                else -> { require(!closed); value.append(c) }
            }
        }
        require(!quoted)
        if (row.isNotEmpty() || value.isNotEmpty() || closed) { row.add(value.toString()); rows.add(row) }
        return rows
    }

    companion object { const val MAX_BYTES = 32 * 1024 * 1024 }
}

internal fun InputStream.readBytesLimited(limit: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    var total = 0
    while (true) {
        val size = read(buffer)
        if (size < 0) break
        total += size
        require(total <= limit) { "Backup exceeds supported size" }
        output.write(buffer, 0, size)
    }
    return output.toByteArray()
}
