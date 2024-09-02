package com.marmatsan.dev.core_data.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.marmatsan.core_domain.ProtoPreferences
import java.io.InputStream
import java.io.OutputStream

object PreferencesDataSerializer : Serializer<ProtoPreferences> {

    override val defaultValue: ProtoPreferences = ProtoPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProtoPreferences {
        try {
            return ProtoPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ProtoPreferences, output: OutputStream) {
        t.writeTo(output)
    }
}