package kubed.shapefile.dbf

import kubed.shapefile.io.MixedEndianInputStream
import java.util.ArrayList

internal data class DbfHeader(val version: Byte,
                             val year: Byte,
                             var month: Byte,
                              var day: Byte,
                              var numberOfRecords: Int,
                              var headerLength: Short,
                              var recordLength: Short,
                              val incompleteTransaction: Byte,
                              val encryptionFlag: Byte,
                              val mdxFlag: Byte,
                              val languageDriver: Byte,
                              var fields: List<DbfField>?,
                              var numberOfFields: Int)

internal fun readHeader(stream: MixedEndianInputStream): DbfHeader {
    val version = stream.readByte()
    val year = stream.readByte()
    val month = stream.readByte()
    val day = stream.readByte()
    val numRecords = stream.readIntLe()
    val headerLength = stream.readShortLe()
    val recordLength = stream.readShortLe()
    stream.skipBytes(3) // Reserved
    stream.skipBytes(13) // Reserved
    stream.skipBytes(4) // Reserved

    val fields = ArrayList<DbfField>()
    var field = readField(stream)
    while(field != null) {
        fields += field
        field = readField(stream)
    }

    return DbfHeader(version, year, month, day,
            numRecords, headerLength, recordLength,
            0.toByte(), 0.toByte(), 0.toByte(), 0.toByte(),
            fields, fields.size)
}