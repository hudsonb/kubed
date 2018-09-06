package kubed.shapefile

import kubed.geo.Feature
import kubed.shapefile.dbf.DbfReader
import kubed.shapefile.shp.ShpReader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class ShapefileReader(shpInput: InputStream, dbfInput: InputStream? = null) {
    constructor(shp: String, dbf: String = shp.removeSuffix(".shp") + ".dbf") : this(File(shp).inputStream(),
            try { File(dbf).inputStream() } catch(e: FileNotFoundException) { null })

    private val shpReader = ShpReader(shpInput)
    private val dbfReader = if(dbfInput != null) DbfReader(dbfInput) else null

    fun nextFeature(): Feature? {
        val geo = shpReader.nextGeometry() ?: return null
        return Feature(geo, dbfReader?.nextRecord() ?: emptyMap<String, Any>())
    }
}

fun main(args: Array<String>) {
    val reader = ShapefileReader("/Users/hudsonb/TM_WORLD_BORDERS_SIMPL-0.3/TM_WORLD_BORDERS_SIMPL-0.3.shp")

    var feature = reader.nextFeature()
    while(feature != null) {
        println(feature)
    }
}