package kubed.shape.curve

class RadialCurve(val curve: Curve) : Curve {
    override fun areaStart() = curve.areaStart()
    override fun areaEnd() = curve.areaEnd()
    override fun lineStart() = curve.lineStart()
    override fun lineEnd() = curve.lineEnd()
    override fun point(x: Double, y: Double) = curve.point(y * Math.sin(x), y * -Math.cos(x))
}
