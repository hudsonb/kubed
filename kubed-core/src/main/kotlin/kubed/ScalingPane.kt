package kubed

/**
 * ScalableContentPane.java
 *
 * Copyright (c) 2011-2015, JFXtras
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.geometry.Bounds
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.transform.Scale

/**
 * Scales content to always fit in the bounds of this pane. Useful for workflows
 * with lots of windows.
 *
 * @author Michael Hoffer <info></info>@michaelhoffer.de>
 */
class ScalingPane : Pane() {

    /**
     * Returns the content scale transform.
     *
     * @return the content scale transform
     */
    var contentScaleTransform: Scale? = null
        private set
    private val contentPaneProperty = SimpleObjectProperty<Pane>()

    /**
     * Defines whether to keep aspect ration when scaling content.
     *
     * @return `true` if keeping aspect ratio of the content;
     * `false` otherwise
     */
    /**
     * Defines whether to keep aspect ration of the content.
     *
     * @param aspectScale the state to set
     */
    var isAspectScale = true

    /**
     * Indicates whether content is automatically scaled.
     *
     * @return `true` if content is automatically scaled;
     * `false` otherwise
     */
    /**
     * Defines whether to automatically rescale content.
     *
     * @param autoRescale the state to set
     */
    var isAutoRescale = true
    private val minScaleXProperty = SimpleDoubleProperty(java.lang.Double.MIN_VALUE)
    private val maxScaleXProperty = SimpleDoubleProperty(java.lang.Double.MAX_VALUE)
    private val minScaleYProperty = SimpleDoubleProperty(java.lang.Double.MIN_VALUE)
    private val maxScaleYProperty = SimpleDoubleProperty(java.lang.Double.MAX_VALUE)

    /**
     * @return the content pane
     */
    /**
     * Defines the content pane of this scalable pane.
     *
     * @param contentPane pane to define
     */
    var contentPane: Pane
        get() = contentPaneProperty.value
        set(contentPane) {
            contentPaneProperty.value = contentPane
            contentPane.isManaged = false
            initContentPaneListener()

            val scale = Scale(1.0, 1.0)
            scale.pivotX = 0.0
            scale.pivotY = 0.0
            scale.pivotZ = 0.0
            contentScaleTransform = scale
            contentPane.transforms.add(contentScaleTransform)

            children.add(contentPane)
        }

    var minScaleX: Double
        get() = minScaleXProperty.get()
        set(s) = minScaleXProperty.set(s)

    var maxScaleX: Double
        get() = maxScaleXProperty.get()
        set(s) = maxScaleXProperty.set(s)

    var minScaleY: Double
        get() = minScaleYProperty.get()
        set(s) = minScaleYProperty.set(s)

    var maxScaleY: Double
        get() = maxScaleYProperty.get()
        set(s) = maxScaleYProperty.set(s)

    init {
        contentPane = Pane()

        prefWidth = Region.USE_PREF_SIZE
        prefHeight = Region.USE_PREF_SIZE

        needsLayoutProperty().addListener { _, _, t1 ->
            if(t1!!) computeScale()
        }
    }

    private fun computeScale() {
        val realWidth = contentPane.prefWidth(height)
        var realHeigh = contentPane.prefHeight(width)

        if(applyJDK7Fix) {
            realHeigh += 0.01 // does not paint without it
        }

        val hpad = insets.left + insets.right
        val vpad = insets.top + insets.bottom

        val contentWidth = width - hpad
        val contentHeight = height - vpad

        var contentScaleWidth = contentWidth / realWidth
        var contentScaleHeight = contentHeight / realHeigh

        contentScaleWidth = Math.max(contentScaleWidth, minScaleX)
        contentScaleWidth = Math.min(contentScaleWidth, maxScaleX)

        contentScaleHeight = Math.max(contentScaleHeight, minScaleY)
        contentScaleHeight = Math.min(contentScaleHeight, maxScaleY)

        if(isAspectScale) {
            val s = Math.min(contentScaleWidth, contentScaleHeight)

            val scale = contentScaleTransform
            if(scale != null) {
                scale.x = s
                scale.y = s
            }
        }
        else {
            val scale = contentScaleTransform
            if(scale != null) {
                scale.x = contentScaleWidth
                scale.y = contentScaleHeight
            }
        }

        contentPane.relocate(insets.left, insets.top)

        contentPane.resize(contentWidth / contentScaleWidth, contentHeight / contentScaleHeight)
    }

    override fun computeMinWidth(d: Double): Double = insets.left + insets.right + 1.0
    override fun computeMinHeight(d: Double): Double = insets.top + insets.bottom + 1.0
    override fun computePrefWidth(d: Double): Double = 1.0
    override fun computePrefHeight(d: Double): Double = 1.0

    private fun initContentPaneListener() {
        val boundsListener = ChangeListener<Bounds> { _, _, _ ->
            if(isAutoRescale) {
                isNeedsLayout = false
                contentPane.requestLayout()
                requestLayout()
            }
        }

        val numberListener = ChangeListener<Number> { _, _, _ ->
            if(isAutoRescale) {
                isNeedsLayout = false
                contentPane.requestLayout()
                requestLayout()
            }
        }

        contentPane.children.addListener(ListChangeListener { c ->
            while(c.next()) {
                if(c.wasPermutated()) {
                    for (i in c.from until c.to) {
                        //permutate
                    }
                }
                else if(c.wasUpdated()) {
                    //update item
                }
                else {
                    if(c.wasRemoved()) {
                        for (n in c.removed) {
                            n.boundsInLocalProperty().removeListener(boundsListener)
                            n.layoutXProperty().removeListener(numberListener)
                            n.layoutYProperty().removeListener(numberListener)
                        }
                    }
                    else if (c.wasAdded()) {
                        for (n in c.addedSubList) {
                            n.boundsInLocalProperty().addListener(boundsListener)
                            n.layoutXProperty().addListener(numberListener)
                            n.layoutYProperty().addListener(numberListener)
                        }
                    }
                }
            }
        })
    }

    companion object {
        private var applyJDK7Fix = false

        init {
            // JDK7 fix:
            applyJDK7Fix = System.getProperty("java.version").startsWith("1.7")
        }
    }
}