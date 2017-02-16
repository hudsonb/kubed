package kubed.shape.offset

import kubed.shape.Series
import kubed.util.isTruthy

fun stackOffsetNone(): (List<Series<*, *>>, List<Int>) -> Unit = { series, order ->
    if(!series.isEmpty()) {
        var s1 = series[order[0]]
        for(i in 1 until series.size) {
            val s0 = s1
            s1 = series[order[i]]
            for(j in 0 until s1.size) {
                s1[j].y0 = if(s0[j].y1.isNaN()) s0[j].y0 else s0[j].y1
                s1[j].y1 += s1[j].y0
            }
        }
    }
}

fun stackOffsetExpand(): (List<Series<*, *>>, List<Int>) -> Unit = { series, order ->
    if(!series.isEmpty()) {
        var j = 0
        val n = series.size
        val m = series[0].size
        while(j < m) {
            var i = 0
            var y = 0.0
            while(i < n) {
                y += if (series[i][j].y1.isNaN()) 0.0 else series[i][j].y1
                ++i
            }
            if(y.isTruthy()) {
                i = 0
                while(i < n) {
                    series[i][j].y1 /= y
                    ++i
                }
            }
            ++j
        }

        stackOffsetNone()(series, order)
    }
}

fun stackOffsetSilhouette(): (List<Series<*, *>>, List<Int>) -> Unit = { series, order ->
    if(!series.isEmpty()) {
        val s0 = series[order[0]]
        val m = s0.size
        for(j in 0 until m) {
            val y = (0 until series.size).sumByDouble { if(series[it][j].y1.isNaN()) 0.0 else series[it][j].y1 }
            s0[j].y0 = -y / 2
            s0[j].y1 += s0[j].y0
        }

        stackOffsetNone()(series, order)
    }
}

fun stackOffsetWiggle(): (List<Series<*, *>>, List<Int>) -> Unit = { series, order ->
    if(!series.isEmpty()) {
        val s0 = series[order[0]]
        if(s0.size > 0) {
            val n = series.size
            val m = s0.size
            var y = 0.0
            var j = 1
            while(j < m) {
                var i = 0
                var s1 = 0.0
                var s2 = 0.0
                while(i < n) {
                    val si = series[order[i]]
                    val sij0 = if(si[j].y1.isNaN()) 0.0 else si[j].y1
                    val sij1 = if(si[j - 1].y1.isNaN()) 0.0 else si[j - 1].y1
                    var s3 = (sij0 - sij1) / 2
                    for(k in 0 until i) {
                        val sk = series[order[k]]
                        val skj0 = if(sk[j].y1.isNaN()) 0.0 else sk[j].y1
                        val skj1 = if(sk[j - 1].y1.isNaN()) 0.0 else sk[j - 1].y1
                        s3 += skj0 - skj1
                    }
                    s1 += sij0
                    s2 += s3 * sij0
                    ++i
                }
                s0[j - 1].y0 = y
                s0[j - 1].y1 += s0[j - 1].y0
                if(s1.isTruthy()) y -= s2 / s1
                ++j
            }
            s0[j - 1].y0 = y
            s0[j - 1].y1 += s0[j - 1].y0
            stackOffsetWiggle()(series, order)
        }
    }
}



