package kubed.shape.curve

import kubed.path.Context
import kubed.util.isTruthy

fun curveBasis(): (Context) -> Curve = ::BasisCurve
fun curveBasisClosed(): (Context) -> Curve = ::BasisClosedCurve
fun curveBasisOpen(): (Context) -> Curve = ::BasisOpenCurve

fun curveLinear(): (Context) -> Curve = ::LinearCurve
fun curveLinearClosed(): (Context) -> Curve = ::LinearClosedCurve

fun curveMonotoneX(): (Context) -> Curve = ::MonotoneXCurve
fun curveMonotoneY(): (Context) -> Curve = ::MonotoneYCurve

fun curveCardinal(tension: Double = 0.0): (Context) -> Curve = { context -> CardinalCurve(context, tension) }
fun curveCardinalClosed(tension: Double = 0.0): (Context) -> Curve = { context -> CardinalClosedCurve(context, tension) }
fun curveCardinalOpen(tension: Double = 0.0): (Context) -> Curve = { context -> CardinalOpenCurve(context, tension) }

fun curveCatmullRom(alpha: Double = 0.5): (Context) -> Curve {
    return { context ->
        if(alpha.isTruthy())
            CatmullRomCurve(context, alpha)
        else
            CardinalCurve(context)
    }
}
fun curveCatmullRomClosed(alpha: Double = 0.5): (Context) -> Curve {
    return { context ->
        if(alpha.isTruthy())
            CatmullRomClosedCurve(context, alpha)
        else
            CardinalClosedCurve(context, 0.0)
    }
}
fun curveCatmullRomOpen(alpha: Double = 0.5): (Context) -> Curve {
    return { context ->
        if(alpha.isTruthy())
            CatmullRomOpenCurve(context, alpha)
        else
            CardinalOpenCurve(context, 0.0)
    }
}

fun curveNatural(): (Context) -> Curve = ::NaturalCurve

fun curveRadial(curve: (Context) -> Curve = curveLinear()): (Context) -> Curve = { context -> RadialCurve(curve(context)) }

fun curveStep(): (Context) -> Curve = { context -> StepCurve(context, 0.0) }
fun curveStepBefore(): (Context) -> Curve = { context -> StepCurve(context, 0.0) }
fun curveStepAfter(): (Context) -> Curve = { context -> StepCurve(context, 1.0) }