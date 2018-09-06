package kubed.shapefile.dbf

import kubed.shapefile.io.MixedEndianInputStream
import java.io.DataInputStream
import java.util.*

private const val TERMINATOR = 0x0D

internal data class DbfField(val fieldName: String,
                             val dataType: Char,
                             val fieldLength: Int,
                             val decimalCount: Int,
                             val workAreaId: Byte,
                             val setFieldsFlag: Byte)

internal fun readField(stream: MixedEndianInputStream): DbfField? {
    val firstByte = stream.readUnsignedByte()
    if(firstByte == TERMINATOR) return null

    val bytes = ByteArray(11)                      /* 1-10  */
    stream.readFully(bytes, 1, 10)
    bytes[0] = firstByte.toByte()

    var nonZeroIndex = bytes.size - 1
    while(nonZeroIndex >= 0 && bytes[nonZeroIndex].toInt() == 0) {
        nonZeroIndex--
    }

    val fieldName = String(bytes, 0, nonZeroIndex + 1)
    val dataType = stream.readByte().toChar()
    stream.skipBytes(4)
    val fieldLength = stream.readByte().toInt()
    val decimalCount = stream.readByte().toInt()
    stream.skipBytes(2) // Reserved
    val workAreaId = stream.readByte()
    stream.skipBytes(2) // Reserved
    val setFieldsFlag = stream.readByte()
    stream.skipBytes(8) // Reserved

    return DbfField(fieldName, dataType, fieldLength, decimalCount, workAreaId, setFieldsFlag)
}

internal fun readFieldValue(field: DbfField, stream: MixedEndianInputStream): Any? {
    val bytes = ByteArray(field.fieldLength)
    stream.readFully(bytes)

    return when(field.dataType) {
        'C' -> readString(bytes)
        'D' -> readTimeInMillis(bytes)
        'L' -> readBoolean(bytes)
        'N' -> readNumber(field, bytes)
        else -> throw IllegalStateException("Unrecognized data type: ${field.dataType}")
    }
}

private fun readTimeInMillis(bytes: ByteArray): Long {
    val s = DataInputStream(bytes.inputStream())
    val year = s.readInt()
    val month = s.readInt()
    val day = s.readInt()
    return GregorianCalendar(year, month - 1, day).timeInMillis
}

private fun readString(bytes: ByteArray): String? {
    val s = String(bytes).trim()
    if(s.isEmpty()) return null

    return s
}

private fun readBoolean(bytes: ByteArray): Boolean? {
    val c = bytes[0].toChar()
    return when(c) {
        'Y', 'y', 'T', 't' -> true
        '?', ' ' -> null
        else -> false
    }
}

private fun readNumber(field: DbfField, bytes: ByteArray): Number? = when (field.decimalCount) {
    0 -> when {
        field.fieldLength < 5 -> readShort(bytes)
        field.fieldLength < 8 -> readInt(bytes)
        else -> readLong(bytes)
    }
    else -> readDouble(bytes)
}

private fun readShort(bytes: ByteArray) = String(bytes).trim().toShortOrNull()
private fun readInt(bytes: ByteArray) = String(bytes).trim().toIntOrNull()
private fun readLong(bytes: ByteArray) = String(bytes).trim().toLongOrNull()
private fun readDouble(bytes: ByteArray) = String(bytes).trim().toDoubleOrNull()