package kubed.fx

import javafx.scene.Node
import javafx.scene.control.Tooltip

private val TOOLTIP_PROP_KEY = "javafx.scene.control.Tooltip"

fun Node.getTooltip() = properties[TOOLTIP_PROP_KEY]
fun Node.setTooltip(tooltip: Tooltip?) {
    if(tooltip == null) Tooltip.uninstall(this, tooltip)
    else Tooltip.install(this, tooltip)
}
