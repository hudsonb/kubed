package kubed.color

import javafx.scene.paint.Color


/**
 * Returns a representation of this color in the Rgb color space
 */
fun Color.rgb(): Rgb = Rgb((red * 255).toInt(),
                           (green * 255).toInt(),
                           (blue * 255).toInt(),
                           opacity)

/**
 * Returns a representation of this color in the Hsl color space
 */
fun Color.hsl(): Hsl = rgb().hsl()

/**
 * Returns a representation of this color in the Cubehelix color space
 */
fun Color.cubehelix(): Cubehelix = rgb().cubehelix()

/**
 * Returns a representation of this color in the CIELAB color space
 */
fun Color.lab(): Lab = rgb().lab()