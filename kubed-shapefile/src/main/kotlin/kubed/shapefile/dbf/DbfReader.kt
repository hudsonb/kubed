package kubed.shapefile.dbf

import kubed.shapefile.io.MixedEndianInputStream
import java.io.File
import java.io.InputStream

private const val END = 0x1A
private const val DELETED = 0x2A

class DbfReader(input: InputStream) {
    private val stream: MixedEndianInputStream = MixedEndianInputStream(input)
    private val header = readHeader(stream)

    fun nextRecord(): Map<String, Any>? {
        val type = nextType()
        return when(type) {
            END -> null
            else -> {
                val record = HashMap<String, Any>()
                header.fields?.forEach { field ->
                    val value = readFieldValue(field, stream)
                    if(value != null) record[field.fieldName] = value
                }
                record
            }
        }
    }

    private fun nextType(): Int {
        var type: Int
        do {
            type = stream.readByte().toInt()
            if(type == END) break
            else if(type == DELETED) skipRecord()
        } while(type == DELETED)

        return type
    }

    private fun skipRecord() = stream.skipBytes(header.recordLength.toInt())
}

fun main(args: Array<String>) {
    val reader = DbfReader(File("/Users/hudsonb/TM_WORLD_BORDERS_SIMPL-0.3/TM_WORLD_BORDERS_SIMPL-0.3.dbf").inputStream())
    var record = reader.nextRecord()
    while(record != null) {
        println(record)
        record = reader.nextRecord()
    }
}