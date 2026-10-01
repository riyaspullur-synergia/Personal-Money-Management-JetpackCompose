package app.riyaspullur.personalmoneymanagement.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class AiPreferencesSerializer @Inject constructor() : Serializer<AiPreferences> {
    override val defaultValue: AiPreferences = AiPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AiPreferences {
        try {
            return AiPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AiPreferences, output: OutputStream) = t.writeTo(output)
}
