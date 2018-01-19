package kubed

import java.util.Arrays

import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.geometry.Orientation
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.ScrollBar
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.shape.Rectangle
import javafx.scene.transform.Affine
import kubed.math.lerp
import kubed.math.norm
import kubed.transform.scaleX

/**
 * An [InfiniteCanvas] provides a means to render a portion of a
 * hypothetically infinite canvas, on which arbitrary contents can be placed.
 *
 * <pre>
 * +----------------+
 * |content area    |
 * |                |
 * |         +----------------+
 * |         |visible area    |
 * |         |                |
 * |         +----------------+
 * |                |
 * +----------------+
</pre> *
 *
 *
 * The size of the [InfiniteCanvas] itself determines the visible area,
 * i.e. it is reflected in its [.layoutBoundsProperty]. The content area
 * is determined by the (visible) bounds of the [.getContentGroup] that
 * contains the content elements. These bounds can be accessed via the
 * [.contentBoundsProperty].
 *
 *
 * By default, scrollbars are shown when the content area exceeds the visible
 * area. They allow to navigate the [.scrollableBoundsProperty], which
 * resembles the union of the content area and the visible area. The horizontal
 * and vertical scroll offsets are controlled by the
 * [.horizontalScrollOffsetProperty] and
 * [.verticalScrollOffsetProperty]. The appearance of scrollbars can be
 * controlled with the following properties:
 *
 *  * The [.horizontalScrollBarPolicyProperty] determines the
 * horizontal [ScrollBarPolicy].
 *  * The [.verticalScrollBarPolicyProperty] determines the vertical
 * [ScrollBarPolicy].
 *
 *
 *
 * An arbitrary transformation can be applied to the contents that is controlled
 * by the [.contentTransformProperty]. It is unrelated to scrolling,
 * i.e. translating the content does not change the scroll offset.
 *
 *
 * Internally, an [InfiniteCanvas] consists of three layers:
 *
 * <pre>
 * +--------------------------------+
 * |scrollbar group                 |
 * +--------------------------------+
 * |overlay group                   |
 * +--------------------------------+
 * |scrolled pane (with sub-layers) |
 * +--------------------------------+
 * |underlay group                  |
 * +--------------------------------+
</pre> *
 *
 *  * The [.getUnderlayGroup] is rendered at the bottom, it is neither
 * affected by the [.horizontalScrollOffsetProperty] and
 * [.verticalScrollOffsetProperty] nor by the
 * [.contentTransformProperty].
 *  * The [.getScrolledPane] is rendered above the
 * [.getUnderlayGroup] and contains sub-layers. The
 * [.getScrolledPane] and its sub-layers are affected by the
 * [.horizontalScrollOffsetProperty] and
 * [.verticalScrollOffsetProperty].
 *  * The [.getOverlayGroup] is rendered above the
 * [.getScrolledPane]. It is neither affected by the
 * [.horizontalScrollOffsetProperty] and
 * [.verticalScrollOffsetProperty] nor by the
 * [.contentTransformProperty].
 *  * The [.getScrollBarGroup] is rendered above the
 * [.getOverlayGroup]. It contains the scrollbars.
 *
 * The [.getScrolledPane] internally consists of the following four
 * sub-layers:
 *
 * <pre>
 * +--------------------------------+
 * |scrolled overlay group          |
 * +--------------------------------+
 * |content group                   |
 * +--------------------------------+
 * |scrolled underlay group         |
 * +--------------------------------+
 * |grid canvas                     |
 * +--------------------------------+
</pre> *
 *
 *  * The [.getScrolledUnderlayGroup] is rendered at the bottom of the
 * [.getScrolledPane].
 *  * The [.getContentGroup] is rendered above the
 * [.getScrolledUnderlayGroup]. It is affected by the
 * [.contentTransformProperty].
 *  * The [.getScrolledOverlayGroup] is rendered above the
 * [.getContentGroup].
 *
 *
 * @author anyssen
 * @author mwienand
 */
class CanvasPane : Region() {
    // clipping
    private val clippingRectangle = Rectangle()
    private val clipContentProperty = SimpleBooleanProperty(
            true)

    // scrollbars
    /**
     * Returns the [Group] designated for holding the [ScrollBar]s.
     *
     * @return The [Group] designated for holding the [ScrollBar]s.
     */
    private val scrollBarGroup: Group

    /**
     * Returns the horizontal [ScrollBar], or `null` if the
     * horizontal [ScrollBar] was not yet created.
     *
     * @return The horizontal [ScrollBar].
     */
    lateinit var horizontalScrollBar: ScrollBar
        private set

    /**
     * Returns the vertical [ScrollBar], or `null` if the
     * vertical [ScrollBar] was not yet created.
     *
     * @return The vertical [ScrollBar].
     */
    lateinit var verticalScrollBar: ScrollBar
        private set

    private val horizontalScrollBarPolicyProperty = SimpleObjectProperty(ScrollBarPolicy.AS_NEEDED)
    private val verticalScrollBarPolicyProperty = SimpleObjectProperty(ScrollBarPolicy.AS_NEEDED)

    // contents
    /**
     * Returns the [Group] designated for holding the scrolled content.
     *
     * @return The [Group] designated for holding the scrolled content.
     */
    val contentGroup = Group()
    private val contentTransformProperty = ReadOnlyObjectWrapper(
            Affine())

    // content and scrollable bounds
    private var contentBounds = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
    private var scrollableBounds = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
    private val contentBoundsBinding = object : ObjectBinding<Bounds>() {
        override fun computeValue(): Bounds {
            return BoundingBox(contentBounds[0], contentBounds[1],
                    contentBounds[2] - contentBounds[0],
                    contentBounds[3] - contentBounds[1])
        }
    }
    private val scrollableBoundsBinding = object : ObjectBinding<Bounds>() {
        override fun computeValue(): Bounds {
            return BoundingBox(scrollableBounds[0], scrollableBounds[1],
                    scrollableBounds[2] - scrollableBounds[0],
                    scrollableBounds[3] - scrollableBounds[1])
        }
    }
    private val contentBoundsProperty = ReadOnlyObjectWrapper<Bounds>()
    private val scrollableBoundsProperty = ReadOnlyObjectWrapper<Bounds>()

    // layers within the visualization
    /**
     * Returns the [Pane] which is translated when scrolling. This
     * [Pane] contains the [.getContentGroup], therefore, the
     * [.getContentTransform] does not influence the scroll offset.
     *
     * @return The [Pane] that is translated when scrolling.
     */
    private val scrolledPane = Pane()

    /**
     * Returns the underlay [Group].
     *
     * @return The underlay [Group].
     */
    val underlayGroup = Group()

    /**
     * Returns the scrolled underlay [Group].
     *
     * @return The scrolled underlay [Group].
     */
    val scrolledUnderlayGroup = Group()

    /**
     * Returns the scrolled overlay [Group].
     *
     * @return The scrolled overlay [Group].
     */
    val scrolledOverlayGroup = Group()

    /**
     * Returns the overlay [Group] that is rendered above the contents but
     * below the scrollbars.
     *
     * @return The overlay [Group] that is rendered above the contents but
     * below the scrollbars.
     */
    val overlayGroup = Group()

    // Listener to update the scrollbars in response to Number changes (e.g.
    // width and height).
    private val updateScrollBarsOnSizeChangeListener = ChangeListener<Number> { _, _, _ -> updateScrollBars() }
    // Listener to update the scrollbars in response to Bounds changes (e.g.
    // scrolled pane bounds and content group bounds).
    private val updateScrollBarsOnBoundsChangeListener = ChangeListener<Bounds> { _, _, _ -> updateScrollBars() }
    // Listener to update the scrollbars in response to ScrollBarPolicy
    // changes.
    private val updateScrollBarsOnPolicyChangeListener = ChangeListener<ScrollBarPolicy> { _, _, _ -> updateScrollBars() }

    private val horizontalScrollBarValueChangeListener = ChangeListener<Number> { _, _, newValue ->
        if (horizontalScrollBar.isVisible) {
            scrolledPane.translateX = computeTx(newValue.toDouble())
        }
    }

    private val verticalScrollBarValueChangeListener = ChangeListener<Number> { _, _, newValue ->
        if (verticalScrollBar.isVisible) {
            scrolledPane.translateY = computeTy(newValue.toDouble())
        }
    }

    /**
     * Returns the transformation that is applied to the
     * [content group][.getContentGroup].
     *
     * @return The transformation that is applied to the
     * [content group][.getContentGroup].
     */
    /**
     * Sets the transformation matrix of the [ viewport transform][.getContentTransform] to the values specified by the given [Affine].
     *
     * @param tx
     * The [Affine] determining the new
     * [viewport transform][.getContentTransform].
     */
    // Unregister bounds listeners so that transformation changes do not
    // cause updates. Use flag to be aware if the transformation changed.
    // Update scrollbars if the transformation changed.
    // Register previously unregistered listeners.
    var contentTransform: Affine
        get() = contentTransformProperty.get()
        set(tx) {
            val viewportTransform = contentTransformProperty.get()
            unregisterUpdateScrollBarsOnBoundsChanges()
            var valuesChanged = false
            if (viewportTransform.mxx != tx.mxx) {
                viewportTransform.mxx = tx.mxx
                valuesChanged = true
            }
            if (viewportTransform.mxy != tx.mxy) {
                viewportTransform.mxy = tx.mxy
                valuesChanged = true
            }
            if (viewportTransform.myx != tx.myx) {
                viewportTransform.myx = tx.myx
                valuesChanged = true
            }
            if (viewportTransform.myy != tx.myy) {
                viewportTransform.myy = tx.myy
                valuesChanged = true
            }
            if (viewportTransform.tx != tx.tx) {
                viewportTransform.tx = tx.tx
                valuesChanged = true
            }
            if (viewportTransform.ty != tx.ty) {
                viewportTransform.ty = tx.ty
                valuesChanged = true
            }
            if (valuesChanged) {
                updateScrollBars()
            }
            registerUpdateScrollBarsOnBoundsChanges()
        }

    /**
     * Returns the [ScrollBarPolicy] that is currently used to decide when
     * to show a horizontal scrollbar.
     *
     * @return The [ScrollBarPolicy] that is currently used to decide when
     * to show a horizontal scrollbar.
     */
    /**
     * Sets the value of the [.horizontalScrollBarPolicyProperty] to the
     * given [ScrollBarPolicy].
     *
     * @param horizontalScrollBarPolicy
     * The new [ScrollBarPolicy] for the horizontal scrollbar.
     */
    var horizontalScrollBarPolicy: ScrollBarPolicy
        get() = horizontalScrollBarPolicyProperty.get()
        set(horizontalScrollBarPolicy) = horizontalScrollBarPolicyProperty.set(horizontalScrollBarPolicy)

    /**
     * Returns the current horizontal scroll offset.
     *
     * @return The current horizontal scroll offset.
     */
    /**
     * Sets the horizontal scroll offset to the given value.
     *
     * @param scrollOffsetX
     * The new horizontal scroll offset.
     */
    var horizontalScrollOffset: Double
        get() = scrolledPane.translateX
        set(scrollOffsetX) {
            scrolledPane.translateX = scrollOffsetX
        }

    /**
     * Returns the [ScrollBarPolicy] that is currently used to decide when
     * to show a vertical scrollbar.
     *
     * @return The [ScrollBarPolicy] that is currently used to decide when
     * to show a vertical scrollbar.
     */
    /**
     * Sets the value of the [.verticalScrollBarPolicyProperty] to the
     * given [ScrollBarPolicy].
     *
     * @param verticalScrollBarPolicy
     * The new [ScrollBarPolicy] for the vertical scrollbar.
     */
    var verticalScrollBarPolicy: ScrollBarPolicy
        get() = verticalScrollBarPolicyProperty.get()
        set(verticalScrollBarPolicy) = verticalScrollBarPolicyProperty.set(verticalScrollBarPolicy)

    /**
     * Returns the current vertical scroll offset.
     *
     * @return The current vertical scroll offset.
     */
    /**
     * Sets the vertical scroll offset to the given value.
     *
     * @param scrollOffsetY
     * The new vertical scroll offset.
     */
    var verticalScrollOffset: Double
        get() = scrolledPane.translateY
        set(scrollOffsetY) {
            scrolledPane.translateY = scrollOffsetY
        }

    /**
     * Returns the value of the [.clipContentProperty].
     *
     * @return The value of the [.clipContentProperty].
     */
    /**
     * Sets the value of the [.clipContentProperty] to the given value.
     *
     * @param clipContent
     * The new value for the [.clipContentProperty].
     */
    var clipContent: Boolean
        get() = clipContentProperty.get()
        set(clipContent) = clipContentProperty.set(clipContent)

    init {
        // bind bounds properties to predefined bindings
        contentBoundsProperty.bind(contentBoundsBinding)
        scrollableBoundsProperty.bind(scrollableBoundsBinding)

        // create scrollbars
        scrollBarGroup = createScrollBarGroup()

        // create visualization
        children.addAll(createLayers())
        scrolledPane.children.addAll(createScrolledLayers())

        // add content transformation to content group
        contentGroup.transforms.add(contentTransform)

        // register listeners for updating the scrollbars
        registerUpdateScrollBarsOnBoundsChanges()
        registerUpdateScrollBarsOnSizeChanges()
        registerUpdateScrollBarsOnPolicyChanges()

        // enable content clipping
        if (clipContentProperty.get()) {
            clipContent()
        }
        // register for "clipContent" changes to enable/disable content clipping
        clipContentProperty.addListener { _, _, newValue ->
            if (newValue!!) {
                clipContent()
            } else {
                unclipContent()
            }
        }
    }

    /**
     * Enables content clipping for this [InfiniteCanvas].
     */
    protected fun clipContent() {
        clippingRectangle.widthProperty().bind(widthProperty())
        clippingRectangle.heightProperty().bind(heightProperty())
        clip = clippingRectangle
    }

    /**
     * Returns the [BooleanProperty] that determines if this
     * [InfiniteCanvas] does clipping, i.e. restricts its visibility to
     * its [.layoutBoundsProperty].
     *
     * @return The [BooleanProperty] that determines if this
     * [InfiniteCanvas] does clipping.
     */
    fun clipContentProperty(): BooleanProperty {
        return clipContentProperty
    }

    /**
     * Computes the bounds `[min-x, min-y, max-x, max-y]` surrounding
     * the [content group][.getContentGroup] within the coordinate system
     * of this [InfiniteCanvas].
     *
     * @return The bounds `[min-x, min-y, max-x, max-y]` surrounding
     * the [content group][.getContentGroup] within the
     * coordinate system of this [InfiniteCanvas].
     */
    private fun computeContentBoundsInLocal(): DoubleArray {
        val contentBoundsInScrolledPane = contentGroup
                .boundsInParent
        val minX = contentBoundsInScrolledPane.minX
        val maxX = contentBoundsInScrolledPane.maxX
        val minY = contentBoundsInScrolledPane.minY
        val maxY = contentBoundsInScrolledPane.maxY

        val minInScrolled = scrolledPane.localToParent(minX, minY)
        val realMinX = minInScrolled.x
        val realMinY = minInScrolled.y
        val realMaxX = realMinX + (maxX - minX)
        val realMaxY = realMinY + (maxY - minY)

        return doubleArrayOf(realMinX, realMinY, realMaxX, realMaxY)
    }

    /**
     * Converts a horizontal translation distance into the corresponding
     * horizontal scrollbar value.
     *
     * @param tx
     * The horizontal translation distance.
     * @return The horizontal scrollbar value corresponding to the given
     * translation.
     */
    private fun computeHv(tx: Double): Double {
        return lerp(horizontalScrollBar.min, horizontalScrollBar.max,
                norm(scrollableBounds[0], scrollableBounds[2] - width,
                        -tx))
    }

    /**
     * Computes and returns the bounds of the scrollable area within this
     * [InfiniteCanvas].
     *
     * @return The bounds of the scrollable area, i.e.
     * `[minx, miny, maxx, maxy]`.
     */
    private fun computeScrollableBoundsInLocal(): DoubleArray {
        val cb = Arrays.copyOf(contentBounds, contentBounds.size)
        val db = contentGroup.boundsInParent

        // factor in the viewport extending the content bounds
        if (cb[0] < 0) {
            cb[0] = 0.0
        }
        if (cb[1] < 0) {
            cb[1] = 0.0
        }
        if (cb[2] > width) {
            cb[2] = 0.0
        } else {
            cb[2] = width - cb[2]
        }
        if (cb[3] > height) {
            cb[3] = 0.0
        } else {
            cb[3] = height - cb[3]
        }

        return doubleArrayOf(db.minX - cb[0], db.minY - cb[1], db.maxX + cb[2], db.maxY + cb[3])
    }

    /**
     * Converts a horizontal scrollbar value into the corresponding horizontal
     * translation distance.
     *
     * @param hv
     * The horizontal scrollbar value.
     * @return The horizontal translation distance corresponding to the given
     * scrollbar value.
     */
    private fun computeTx(hv: Double): Double {
        return -lerp(scrollableBounds[0], scrollableBounds[2] - width,
                norm(horizontalScrollBar.min, horizontalScrollBar.max,
                        hv))
    }

    /**
     * Converts a vertical scrollbar value into the corresponding vertical
     * translation distance.
     *
     * @param vv
     * The vertical scrollbar value.
     * @return The vertical translation distance corresponding to the given
     * scrollbar value.
     */
    private fun computeTy(vv: Double): Double {
        return -lerp(scrollableBounds[1], scrollableBounds[3] - height,
                norm(verticalScrollBar.min, verticalScrollBar.max,
                        vv))
    }

    /**
     * Converts a vertical translation distance into the corresponding vertical
     * scrollbar value.
     *
     * @param ty
     * The vertical translation distance.
     * @return The vertical scrollbar value corresponding to the given
     * translation.
     */
    private fun computeVv(ty: Double): Double {
        return lerp(verticalScrollBar.min, verticalScrollBar.max,
                norm(scrollableBounds[1], scrollableBounds[3] - height,
                        -ty))
    }

    /**
     * Provides the visual bounds of the content group in the local coordinate
     * system of this [InfiniteCanvas] as a (read-only) property.
     *
     * @return The bounds of the content group, i.e.
     * `minx, miny, maxx, maxy` as
     * [ReadOnlyObjectProperty].
     */
    fun contentBoundsProperty(): ReadOnlyObjectProperty<Bounds> {
        return contentBoundsProperty.readOnlyProperty
    }

    /**
     * Returns the viewport transform as a (read-only) property.
     *
     * @return The viewport transform as [ReadOnlyObjectProperty].
     */
    fun contentTransformProperty(): ReadOnlyObjectProperty<Affine> {
        return contentTransformProperty.readOnlyProperty
    }

    /**
     * Returns a list containing the top level layers in the visualization of
     * this [InfiniteCanvas]. Per default, the underlay group, the
     * scrolled pane, the overlay group, and the scrollbar group are returned in
     * that order.
     *
     * @return A list containing the top level layers in the visualization of
     * this [InfiniteCanvas].
     */
    private fun createLayers(): List<Node> {
        return Arrays.asList<Parent>(underlayGroup, scrolledPane,
                overlayGroup, scrollBarGroup)
    }

    /**
     * Creates the [Group] designated for holding the scrollbars and
     * places the scrollbars in it. Furthermore, event listeners are registered
     * to update the scroll offset upon scrollbar movement.
     *
     * @return The [Group] designated for holding the scrollbars.
     */
    private fun createScrollBarGroup(): Group {
        // create horizontal scrollbar
        horizontalScrollBar = ScrollBar()
        horizontalScrollBar.isVisible = false
        horizontalScrollBar.opacity = 0.5

        // create vertical scrollbar
        verticalScrollBar = ScrollBar()
        verticalScrollBar.orientation = Orientation.VERTICAL
        verticalScrollBar.isVisible = false
        verticalScrollBar.opacity = 0.5

        // bind horizontal size
        val vWidth = object : DoubleBinding() {
            init {
                bind(verticalScrollBar.visibleProperty(), verticalScrollBar.widthProperty())
            }

            override fun computeValue(): Double = if(verticalScrollBar.isVisible) verticalScrollBar.width else 0.0
        }

        horizontalScrollBar.prefWidthProperty().bind(widthProperty().subtract(vWidth))

        // bind horizontal y position
        horizontalScrollBar.layoutYProperty().bind(heightProperty().subtract(horizontalScrollBar.heightProperty()))

        // bind vertical size
        val hHeight = object : DoubleBinding() {
            init {
                bind(horizontalScrollBar.visibleProperty(), horizontalScrollBar.heightProperty())
            }

            override fun computeValue(): Double = if(horizontalScrollBar.isVisible) horizontalScrollBar.height else 0.0
        }

        verticalScrollBar.prefHeightProperty().bind(heightProperty().subtract(hHeight))

        // bind vertical x position
        verticalScrollBar.layoutXProperty().bind(widthProperty().subtract(verticalScrollBar.widthProperty()))

        horizontalScrollBar.valueProperty().addListener(horizontalScrollBarValueChangeListener)
        verticalScrollBar.valueProperty().addListener(verticalScrollBarValueChangeListener)

        return Group(horizontalScrollBar, verticalScrollBar)
    }

    /**
     * Returns a list containing the scrolled layers in the visualization of
     * this [InfiniteCanvas]. Per default, the grid canvas, the scrolled
     * underlay group, the content group, and the scrolled overlay group are
     * returned in that order.
     *
     * @return A list containing the top level layers in the visualization of
     * this [InfiniteCanvas].
     */
    private fun createScrolledLayers(): List<Node> {
        return Arrays.asList(scrolledUnderlayGroup,
                contentGroup, scrolledOverlayGroup)
    }

    /**
     * Adjusts the [.horizontalScrollOffsetProperty], the
     * [.verticalScrollOffsetProperty], and the
     * [.contentTransformProperty], so that the
     * [.getContentGroup] is fully visible within the bounds of this
     * [InfiniteCanvas] if possible. The content will be centered, but the
     * given *zoomMin* and *zoomMax* values restrict the zoom factor,
     * so that the content might exceed the canvas, or does not fill it
     * completely.
     *
     *
     * Note, that the [.contentTransformProperty] is set to a pure scale
     * transformation by this method.
     *
     *
     * Note, that fit-to-size cannot be performed in all situations. If the
     * content area is 0 or the canvas area is 0, then this method cannot fit
     * the content to the canvas size, and therefore, throws an
     * [IllegalStateException]. The following condition can be used to
     * test if fit-to-size can be performed:
     *
     * <pre>
     * if (infiniteCanvas.getWidth() &gt; 0 &amp;&amp; infiniteCanvas.getHeight() &gt; 0
     * &amp;&amp; infiniteCanvas.getContentBounds().getWidth() &gt; 0
     * &amp;&amp; infiniteCanvas.getContentBounds().getHeight() &gt; 0) {
     * // save to call fit-to-size here
     * infiniteCanvas.fitToSize();
     * }
    </pre> *
     *
     * @param zoomMin
     * The minimum zoom level.
     * @param zoomMax
     * The maximum zoom level.
     * @throws IllegalStateException
     * when the content area is zero or the canvas area is zero.
     */
    fun fitToSize(zoomMin: Double, zoomMax: Double) {
        // validate content size is not 0
        val contentBounds = getContentBounds()
        val contentWidth = contentBounds.width
        if (java.lang.Double.isNaN(contentWidth) || java.lang.Double.isInfinite(contentWidth)
                || contentWidth <= 0) {
            throw IllegalStateException("Content area is zero.")
        }
        val contentHeight = contentBounds.height
        if (java.lang.Double.isNaN(contentHeight) || java.lang.Double.isInfinite(contentHeight)
                || contentHeight <= 0) {
            throw IllegalStateException("Content area is zero.")
        }

        // validate canvas size is not 0
        if (width <= 0 || height <= 0) {
            throw IllegalStateException("Canvas area is zero.")
        }

        // compute zoom factor
        var zf = Math.min(width / contentWidth,
                height / contentHeight)

        // validate zoom factor
        if (java.lang.Double.isInfinite(zf) || java.lang.Double.isNaN(zf) || zf <= 0) {
            throw IllegalStateException("Invalid zoom factor.")
        }

        // compute content center
        val cx = contentBounds.minX + contentBounds.width / 2
        val cy = contentBounds.minY + contentBounds.height / 2

        // compute visible area center
        val vx = width / 2
        val vy = height / 2

        // scroll to center position
        horizontalScrollOffset = horizontalScrollOffset + vx - cx
        verticalScrollOffset = verticalScrollOffset + vy - cy

        // compute pivot point for zoom within content coordinates
        val pivot = contentGroup.sceneToLocal(vx, vy)

        val transform = Affine(contentTransform)

        // restrict zoom factor to [zoomMin, zoomMax] range
        val realZoomFactor = transform.scaleX * zf
        if (realZoomFactor > zoomMax) {
            zf = zoomMax / transform.scaleX
        }
        if (realZoomFactor < zoomMin) {
            zf = zoomMin / transform.scaleX
        }

        // compute scale transformation (around visible center)
        val scaleTransform = Affine().apply {
            appendTranslation(pivot.x, pivot.y)
            appendScale(zf, zf)
            appendTranslation(-pivot.x, -pivot.y)
        }

        // concatenate old transformation and scale transformation to yield the
        // new transformation
        transform.append(scaleTransform)
        contentTransform = transform
    }

    /**
     * Returns the value of the [.contentBoundsProperty].
     *
     * @return The value of the [.contentBoundsProperty].
     */
    fun getContentBounds(): Bounds {
        return contentBoundsProperty.get()
    }

    /**
     * Returns the value of the [.scrollableBoundsProperty].
     *
     * @return The value of the [.scrollableBoundsProperty].
     */
    fun getScrollableBounds(): Bounds {
        return scrollableBoundsProperty.get()
    }

    /**
     * Returns the [ObjectProperty] that controls the
     * [ScrollBarPolicy] that decides when to show a horizontal scrollbar.
     *
     * @return The [ObjectProperty] that controls the
     * [ScrollBarPolicy] that decides when to show a horizontal
     * scrollbar.
     */
    fun horizontalScrollBarPolicyProperty(): ObjectProperty<ScrollBarPolicy> {
        return horizontalScrollBarPolicyProperty
    }

    /**
     * Returns the horizontal scroll offset as a property.
     *
     * @return A [DoubleProperty] representing the horizontal scroll
     * offset.
     */
    fun horizontalScrollOffsetProperty(): DoubleProperty {
        return scrolledPane.translateXProperty()
    }

    /**
     * Registers listeners on the bounds-in-local property of the
     * [.getScrolledPane] and on the bounds-in-parent property of the
     * [.getContentGroup] that will call [.updateScrollBars]
     * when one of the bounds is changed.
     */
    private fun registerUpdateScrollBarsOnBoundsChanges() {
        scrolledPane.boundsInParentProperty()
                .addListener(updateScrollBarsOnBoundsChangeListener)
        contentGroup.boundsInParentProperty()
                .addListener(updateScrollBarsOnBoundsChangeListener)
    }

    /**
     * Registers listeners on the [.horizontalScrollBarPolicyProperty]
     * and on the [.verticalScrollBarPolicyProperty] that will call
     * [.updateScrollBars] when one of the [ScrollBarPolicy]s
     * changes.
     */
    private fun registerUpdateScrollBarsOnPolicyChanges() {
        horizontalScrollBarPolicyProperty
                .addListener(updateScrollBarsOnPolicyChangeListener)
        verticalScrollBarPolicyProperty
                .addListener(updateScrollBarsOnPolicyChangeListener)
    }

    /**
     * Registers listeners on the [.widthProperty] and on the
     * [.heightProperty] that will call [.updateScrollBars] when
     * the size of this [InfiniteCanvas] changes.
     */
    private fun registerUpdateScrollBarsOnSizeChanges() {
        widthProperty().addListener(updateScrollBarsOnSizeChangeListener)
        heightProperty().addListener(updateScrollBarsOnSizeChangeListener)
    }

    /**
     * Ensures that the specified child [Node] is visible to the user by
     * scrolling to its position. The effect and style of the node are taken
     * into consideration. After revealing a node, it will be fully visible if
     * it fits within the current viewport bounds.
     *
     *
     * When the child node's left side is left to the viewport, it will touch
     * the left border of the viewport after revealing. When the child node's
     * right side is right to the viewport, it will touch the right border of
     * the viewport after revealing. When the child node's top side is above the
     * viewport, it will touch the top border of the viewport after revealing.
     * When the child node's bottom side is below the viewport, it will touch
     * the bottom border of the viewport after revealing.
     *
     *
     * The top and left sides have preference over the bottom and right sides,
     * i.e. when the top side is aligned with the viewport, the bottom side will
     * not be aligned, and when the left side is aligned with the viewport, the
     * right side will not be aligned.
     *
     * @param child
     * The child [Node] to reveal.
     */
    fun reveal(child: Node) {
        val bounds = sceneToLocal(
                child.localToScene(child.boundsInLocal))
        if (bounds.height <= height) {
            if (bounds.minY < 0) {
                verticalScrollOffset -= bounds.minY
            } else if (bounds.maxY > height) {
                verticalScrollOffset = verticalScrollOffset + height - bounds.maxY
            }
        }
        if (bounds.width <= width) {
            if (bounds.minX < 0) {
                horizontalScrollOffset -= bounds.minX
            } else if (bounds.maxX > width) {
                horizontalScrollOffset = horizontalScrollOffset + width - bounds.maxX
            }
        }
    }

    /**
     * Returns the bounds of the scrollable area in local coordinates of this
     * [InfiniteCanvas] as a (read-only) property. The scrollable area
     * corresponds to the visual bounds of the content group, which is expanded
     * to cover at least the area of this [InfiniteCanvas] (i.e. the
     * viewport) if necessary. It is thereby also the area that can be navigated
     * via the scroll bars.
     *
     * @return The bounds of the scrollable area, i.e.
     * `minx, miny, maxx, maxy` as
     * [ReadOnlyObjectProperty].
     */
    fun scrollableBoundsProperty(): ReadOnlyObjectProperty<Bounds> {
        return scrollableBoundsProperty.readOnlyProperty
    }

    /**
     * Disables content clipping for this [InfiniteCanvas].
     */
    private fun unclipContent() {
        clippingRectangle.widthProperty().unbind()
        clippingRectangle.heightProperty().unbind()
        clip = null
    }

    /**
     * Unregisters the listeners that were previously registered within
     * [.registerUpdateScrollBarsOnBoundsChanges].
     */
    private fun unregisterUpdateScrollBarsOnBoundsChanges() {
        scrolledPane.boundsInParentProperty()
                .removeListener(updateScrollBarsOnBoundsChangeListener)
        contentGroup.boundsInParentProperty()
                .removeListener(updateScrollBarsOnBoundsChangeListener)
    }

    /**
     * Updates the [ScrollBar]s' visibilities, value ranges and value
     * increments based on the [content][.computeContentBoundsInLocal] and the [scrollable][.computeScrollableBoundsInLocal]. The update is not done if any of the [ScrollBar]s is
     * currently in use.
     */
    private fun updateScrollBars() {
        // do not update while a scrollbar is pressed, so that the scrollable
        // area does not change while using a scrollbar
        if(horizontalScrollBar.isPressed || verticalScrollBar.isPressed) return

        // determine current content bounds
        val oldContentBounds = Arrays.copyOf(contentBounds, contentBounds.size)
        contentBounds = computeContentBoundsInLocal()
        if(!Arrays.equals(oldContentBounds, contentBounds))
            contentBoundsBinding.invalidate()

        // show/hide horizontal scrollbar
        val hbarPolicy = horizontalScrollBarPolicyProperty.get()
        val hbarIsNeeded = contentBounds[0] < -0.01 || contentBounds[2] > width + 0.01
        horizontalScrollBar.isVisible = hbarPolicy == ScrollBarPolicy.ALWAYS || hbarPolicy == ScrollBarPolicy.AS_NEEDED && hbarIsNeeded

        // show/hide vertical scrollbar
        val vbarPolicy = verticalScrollBarPolicyProperty.get()
        val vbarIsNeeded = contentBounds[1] < -0.01 || contentBounds[3] > height + 0.01
        verticalScrollBar.isVisible = vbarPolicy == ScrollBarPolicy.ALWAYS || vbarPolicy == ScrollBarPolicy.AS_NEEDED && vbarIsNeeded

        // determine current scrollable bounds
        val oldScrollableBounds = Arrays.copyOf(scrollableBounds,
                scrollableBounds.size)
        scrollableBounds = computeScrollableBoundsInLocal()
        if (!Arrays.equals(oldScrollableBounds, scrollableBounds)) {
            scrollableBoundsBinding.invalidate()
        }

        // update scrollbar ranges
        horizontalScrollBar.min = scrollableBounds[0]
        horizontalScrollBar.max = scrollableBounds[2]
        horizontalScrollBar.visibleAmount = width
        horizontalScrollBar.blockIncrement = width / 2
        horizontalScrollBar.unitIncrement = width / 10
        verticalScrollBar.min = scrollableBounds[1]
        verticalScrollBar.max = scrollableBounds[3]
        verticalScrollBar.visibleAmount = height
        verticalScrollBar.blockIncrement = height / 2
        verticalScrollBar.unitIncrement = height / 10

        // compute scrollbar values from canvas translation (in case the
        // scrollbar values are incorrect)
        // XXX: Remove scroll bar value listeners when adapting the values to
        // prevent infinite recursion.
        horizontalScrollBar.valueProperty().removeListener(horizontalScrollBarValueChangeListener)
        verticalScrollBar.valueProperty().removeListener(verticalScrollBarValueChangeListener)

        horizontalScrollBar.value = computeHv(scrolledPane.translateX)
        verticalScrollBar.value = computeVv(scrolledPane.translateY)

        horizontalScrollBar.valueProperty().addListener(horizontalScrollBarValueChangeListener)
        verticalScrollBar.valueProperty().addListener(verticalScrollBarValueChangeListener)
    }

    /**
     * Returns the [ObjectProperty] that controls the
     * [ScrollBarPolicy] that decides when to show a vertical scrollbar.
     *
     * @return The [ObjectProperty] that controls the
     * [ScrollBarPolicy] that decides when to show a vertical
     * scrollbar.
     */
    fun verticalScrollBarPolicyProperty(): ObjectProperty<ScrollBarPolicy> = verticalScrollBarPolicyProperty

    /**
     * Returns the vertical scroll offset as a property.
     *
     * @return A [DoubleProperty] representing the vertical scroll offset.
     */
    fun verticalScrollOffsetProperty(): DoubleProperty = scrolledPane.translateYProperty()
}