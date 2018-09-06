package kubed.shapefile.io

import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import java.io.IOException

class MixedEndianInputStream(stream: InputStream) : DataInputStream(stream) {
    fun readIntLe(): Int {
        val b1 = readAndCheckByte()
        val b2 = readAndCheckByte()
        val b3 = readAndCheckByte()
        val b4 = readAndCheckByte()
        return b4 shl 24 or ((b3 and 0xFF) shl 16) or ((b2 and 0xFF) shl 8) or (b1 and 0xFF)
    }

    fun readLongLe(): Long {
        val b1 = readAndCheckByte().toLong()
        val b2 = readAndCheckByte().toLong()
        val b3 = readAndCheckByte().toLong()
        val b4 = readAndCheckByte().toLong()
        val b5 = readAndCheckByte().toLong()
        val b6 = readAndCheckByte().toLong()
        val b7 = readAndCheckByte().toLong()
        val b8 = readAndCheckByte().toLong()

        return ((b8 and 0xFFL) shl 56
                or ((b7 and 0xFFL) shl 48)
                or ((b6 and 0xFFL) shl 40)
                or ((b5 and 0xFFL) shl 32)
                or ((b4 and 0xFFL) shl 24)
                or ((b3 and 0xFFL) shl 16)
                or ((b2 and 0xFFL) shl 8)
                or (b1 and 0xFFL))
    }

    fun readDoubleLe() = Double.fromBits(readLongLe())

    fun readShortLe(): Short {
        val b1 = readAndCheckByte()
        val b2 = readAndCheckByte()
        return ((b2 shl 8) or (b1 and 0xFF)).toShort()
    }

    /**
     * Reads a byte from the input stream checking that the end of file (EOF) has not been
     * encountered.
     *
     * @return byte read from input
     * @throws IOException if an error is encountered while reading
     * @throws EOFException if the end of file (EOF) is encountered.
     */
    @Throws(IOException::class, EOFException::class)
    private fun readAndCheckByte(): Int {
        val b = `in`.read()

        if(b == -1) throw EOFException()

        return b
    }
}