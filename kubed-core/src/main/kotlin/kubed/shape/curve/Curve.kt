package kubed.shape.curve

interface Curve {
    /**
     * Indicates the start of  new area segment. Each area segmenet consists of exactly two line segments: the topline,
     * followed by the baseline, with baseline points in reverse order.
     */
    fun areaStart()

    /**
     * Indicates the end of the current area segment
     */
    fun areaEnd()

    /**
     * Indicates the start of a new line segment Zero or more points will follow.
     */
    fun lineStart()

    /**
     * Indicates the end of the current line segment.
     */
    fun lineEnd()

    /**
     * Indicates a new point in the current line segment with the given x- and y-values.
     */
    fun point(x: Double, y: Double)
}
