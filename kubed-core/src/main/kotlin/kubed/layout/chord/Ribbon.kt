package kubed.layout.chord

import kubed.path.Context
import kubed.path.PathContext
import kubed.shape.PathShape
import kubed.util.MoreMath

class Ribbon : PathShape<Ribbon, Chord>() {
    var radius: (Group, Int) ->  Double = { _, _ -> throw IllegalStateException("radius must be specified") }

    override fun generate(d: Chord, i: Int): Context? {
        val context = PathContext()

        val sr = radius(d.source, i)
        val sa0 = d.source.startAngle - MoreMath.HALF_PI
        val sa1 = d.source.endAngle - MoreMath.HALF_PI
        val sx0 = sr * Math.cos(sa0)
        val sy0 = sr * Math.sin(sa0)
        val tr = radius(d.target, i)
        val ta0 = d.target.startAngle - MoreMath.HALF_PI
        val ta1 = d.target.endAngle - MoreMath.HALF_PI

        context.moveTo(sx0, sy0)
        context.arc(0.0, 0.0, sr, sa0, sa1, false)
        if (sa0 != ta0 || sa1 != ta1) { // TODO sr !== tr?
            context.quadraticCurveTo(0.0, 0.0, tr * Math.cos(ta0), tr * Math.sin(ta0))
            context.arc(0.0, 0.0, tr, ta0, ta1, false)
        }
        context.quadraticCurveTo(0.0, 0.0, sx0, sy0)
        context.closePath();

        return context
    }
}
