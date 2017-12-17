package kubed.transform

import javafx.scene.transform.Affine

val Affine.scaleX: Double
    get() = mxx

val Affine.scaleY: Double
    get() = myy

val Affine.shearX: Double
    get() = mxy

val Affine.shearY: Double
    get() = myx

val Affine.translateX: Double
    get() = mxz

val Affine.translateY: Double
    get() = myz




