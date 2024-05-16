package com.marmatsan.dev.core_data.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.marmatsan.core_domain.PreferencesData
import java.io.InputStream
import java.io.OutputStream

object PreferencesDataSerializer : Serializer<PreferencesData> {

    override val defaultValue: PreferencesData = PreferencesData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PreferencesData {
        try {
            return PreferencesData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: PreferencesData, output: OutputStream) {
        t.writeTo(output)
    }
}