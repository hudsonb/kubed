package kubed.data

class HexBin<T>(val x: (T) -> Double, val y: (T) -> Double) {
    private var x0 = 0.0
    private var y0 = 0.0
    private var x1 = 1.0
    private var y1 = 1.0

    operator fun invoke(data: List<T>) {
        var px: Double
        var py: Double

        for(i in data.indices) {
            px = x(data[i])
            py = y(data[i])

            if(px.isNaN() || py.isNaN()) continue

            //py = py / dy
           // var pj = Math.round()
            //if(Math.abs())
        }
    }
}
