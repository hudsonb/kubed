package kubed.selection

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Point3D
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.Tooltip
import javafx.scene.effect.Effect
import javafx.scene.paint.Paint
import javafx.scene.shape.Shape
import javafx.scene.transform.Rotate
import javafx.scene.transform.Transform
import kubed.fx.setTooltip
import java.awt.SystemColor.text
import java.util.*
import kotlin.comparisons.compareBy

fun <T> select(node: Node): Selection<T> {
    val sel = Selection<T>()
    val group = Group(node)
    sel += group

    return sel
}

open class Selection<T>() : AbstractSelection<Selection<T>, T>() {
    val groups = ArrayList<Group>()

    var appender: (parent: Node, i: Int, child: Node) -> Unit = ::addNode

    private var enter: Selection<T>? = null
    private var exit: Selection<T>? = null

    fun enter(): Selection<T> {
        if(enter == null)
            throw IllegalStateException()

        return Selection(enter!!)
    }

    fun exit(): Selection<T> {
        if(exit == null)
            throw IllegalStateException()

        return Selection(exit!!)
    }

    /**
     * Copy constructor.
     */
    constructor(other: Selection<T>) : this() {
        for(g in other.groups) {
            val group = Group(g.parent)
            for(e in g)
                group += e

            groups += group
        }

        enter = if(other.enter != null) other.enter() else null
        exit = if(other.exit != null) other.exit() else null
    }

    /**
     * Returns true if this selection contains any non-null elements.
     */
    val empty: Boolean
        get() = groups.any { !it.empty }

    /**
     * For each selected element, selects the first descendant element that matches the specified [selector]. If no
     * descendant matches the specified [selector] for the current element, the element at the current index will be
     * null in the returned selection.
     *
     * If the [selector] is null, every element in the returned selection will be null.
     *
     * If the current element has associated data, this data is propagated to the corresponding selected element. If
     * multiple elements match the [selector], only the first matching element in scenegraph order is selected.
     *
     * Unlike [selectAll], [select] does not affect grouping: it preserves the existing group structure and indexes.
     */
    fun select(selector: String?): Selection<T> {
        val sel = Selection<T>()

        if(selector == null) {
            for(g in groups) {
                val newGroup = Group(g.parent)
                sel += newGroup
                for(i in sel.groups.indices)
                    newGroup.add(null)
            }
        }
        else {
            for(g in groups) {
                val group = Group(g.parent)
                sel.groups += group

                for(i in g.indices) {
                    val e = g[i]
                    when(e) {
                        is Node -> {
                            val child = e.lookupChildren(selector)
                            if(child != null) {
                                child.datum = e.datum
                                group += child
                            }
                            else group.add(null)
                        }
                        else -> group.add(null)
                    }
                }
            }
        }

        return sel
    }

    fun select(selector: String?, f: Selection<T>.() -> Unit) {
        f(select(selector))
    }

    /**
     * For each selected element, selects the first descendant element returned by the specified [selector]. If no
     * descendant matches the specified [selector] for the current element, the element at the current index will be
     * null in the returned selection.
     *
     * If the current element has associated data, this data is propagated to the corresponding selected element. If
     * multiple elements match the [selector], only the first matching element in scenegraph order is selected.
     *
     * Unlike [selectAll], [select] does not affect grouping: it preserves the existing group structure and indexes.
     */
    fun select(selector: (d: T, i: Int, nodes: List<Node?>) -> Node?): Selection<T> {
        val sel = Selection<T>()

        for(g in groups) {
            val newGroup = Group(g.parent)
            sel.groups += newGroup

            for(i in g.indices) {
                val e = g[i]
                if(e is Node) {
                    val nodes = g.map { it as? Node }
                    val selNode = selector(e.datum as T, i, nodes)
                    if(selNode != null) {
                        selNode.datum = e.datum
                        newGroup += selNode
                        break
                    }
                }
                if(e == null || e is Placeholder) {
                    newGroup.add(null)
                }
            }
        }

        return sel
    }

    fun select(selector: (d: T, i: Int, nodes: List<Node?>) -> Node?, f: Selection<T>.() -> Unit) {
        f(select(selector))
    }

    /**
     * For each selected element, selects the descendant elements that match the specified [selector]. The elements in
     * the returned selection are grouped by their corresponding parent node in this selection. If no element matches
     * the specified [selector] for the current element, or if the [selector] is null, the group at the current index
     * will be emptySelection. The selected elements do not inherit data from this selection, use [data] to propagate data to
     * children.
     *
     * Unlike [select], [selectAll] does affect grouping: each selected descendant is grouped by
     * the parent element in the originating selection.
     */
    fun <T2> selectAll(selector: String?): Selection<T2> {
        val sel = Selection<T2>()

        for(g in groups) {
            for(i in g.indices) {
                val e = g[i]
                if(e is Node) {
                    val newGroup = Group(e)
                    sel.groups += newGroup

                    if(selector != null) {
                        val nodes = e.lookupAllChildren(selector)
                        newGroup.addAll(nodes.sortedWith(compareBy(Node::depth, Node::pos)))
                    }
                }

                // Consider: What about Placeholders?
            }
        }

        return sel
    }

    fun <T2> selectAll(selector: String?, f: Selection<T2>.() -> Unit) {
        f(selectAll(selector))
    }

    /**
     * Sets the bound data for each selected element to [value]. Unlike [data] this method does not compute a join and
     * does not affect indexes or the enter and exit selections.
     *
     * If [value] is null, the bound data will be removed from the selected elements.
     */
    fun datum(value: T): Selection<T> {
        for(g in groups) {
            for(e in g) {
                when(e) {
                    is Node -> e.datum = value
                    is Placeholder -> e.datum = value as Any // Huh?
                }
            }
        }

        return this
    }

    /**
     * Evaluates the function [value] for each selected element, in order, being passed:
     *   * the current datum (d)
     *   * the current index (i)
     *   * the current group (nodes)
     *
     * The value returned from the [value] function is then bound to the element..
     */
    fun datum(value: (d: Any?, i: Int, nodes: List<Node?>) -> T?): Selection<T> {
        for(g in groups) {
            for(i in g.indices) {
                val e = g[i]
                if(e is Node) {
                    val nodes = g.map { it as? Node }
                    val datum = value(e.datum, i, nodes)
                    e.datum = datum
                }
            }
        }

        return this
    }

    /**
     * Joins the specified array of data with the selected elements, returning a new selection that represents the
     * _update_ selection: the elements successfully bound to data. Also defines the enter and exit selections on the
     * returned selection, which can be used to add or remove elements to correspond to the new data. The specified
     * [data] is a [List] of arbitrary values (_e.g._, numbers or objects).
     *
     * When data is assigned to a [Node] it is stored in the [Node.properties] at [DATA_PROPERTY], thus making the
     * "data" sticky and available on re-selection.
     *
     * The data is specified *for each group* in the selection.
     */
    fun data(data: List<T>, key: (d: T, i: Int, groupDatum: Any?) -> Any = { _, i, _ -> i }) =
            data<Any?>({ _, _, _ -> data }, key)

    fun <G> data(data: (d: G?, i: Int, nodes: List<Node?>) -> List<T>,
             key: (d: T, i: Int, groupDatum: Any?) -> Any = { _, i, _ -> i }): Selection<T> {
        val sel = Selection<T>()
        val enter = Selection<T>()
        val exit = Selection<T>()
        sel.enter = enter
        sel.exit = exit

        val parentNodes = groups.map { it.parent }

        val dataList = ArrayList<List<T>>(groups.size)
        for(i in groups.indices) {
            val g = groups[i]
            val d = g.parent.datum
            dataList.add(data(d as? G, i, parentNodes))
        }

        for(i in groups.indices) {
            val g = groups[i]
            val updateGroup = Group(g.parent)
            val enterGroup = Group(g.parent)
            val exitGroup = Group(g.parent)
            sel += updateGroup
            enter.groups.add(enterGroup)
            exit.groups.add(exitGroup)

            val oldKeys = ArrayList<Any?>(g.size)
            for(j in g.indices) {
                val e = g[j]

                val k = when(e) {
                    is Node -> e.key//key(e.datum as G, j, g)
                    is Placeholder -> e.key//key(e.datum as G, j, g)
                    else -> null
                }
                oldKeys += k
            }

            val groupData = dataList[i]
            val newKeys = ArrayList<Any?>(groupData.size)
            for(j in groupData.indices)
                newKeys += key(groupData[j], j, g.parent.datum)

            val remove = BooleanArray(oldKeys.size, { true })
            for(j in newKeys.indices) {
                val k = newKeys[j]
                val index = oldKeys.indexOf(k)
                if(index != -1) {
                    val e = g[index]
                    when(e) {
                        is Node -> e.datum = groupData[j]
                        is Placeholder -> e.datum = groupData[j]!!
                    }
                    updateGroup += e
                    enterGroup.add(null)
                    remove[index] = false
                }
                else {
                    enterGroup += Placeholder(g.parent, k, groupData[j])
                    updateGroup.add(null)
                }
            }

            for(j in g.indices) {
                if(remove[j])
                    exitGroup += g[j]
                else
                    exitGroup.add(null)
            }
        }

        return sel
    }

//    fun append(node: () -> Node): Selection<T> {
//        val sel = Selection<T>()
//
//        for(g in groups) {
//            val newGroup = Group(g.parent)
//            sel.groups += newGroup
//
//            if(g.isEmpty()) {
//                val newNode = node()
//                newNode.datum = g.parent.datum
//                append(g.parent, -1, newNode)
//                newGroup += newNode
//            }
//            else {
//                for(i in g.indices) {
//                    val e = g[i]
//                    when(e) {
//                        is Node -> {
//                            val newNode = node()
//                            newNode.datum = e.datum
//                            append(e, -1, newNode)
//                            newGroup += newNode
//                        }
//                        is Placeholder -> {
//                            val newNode = node()
//                            newNode.datum = e.datum
//                            append(e.parent, -1, newNode)
//                            newGroup += newNode
//                        }
//                        else -> newGroup.add(null)
//                    }
//                }
//            }
//        }
//
//        return sel
//    }

    /**
     * Evaluates the [node] function for each selected element, in order, being passed:
     *   * the current datum (d)
     *   * the current index (i)
     *   * the current nodes (nodes)
     *
     * This function should return the node to be appended.
     *
     * However, the parent node may not expose a public API for adding children. For this reason, an [append] callback
     * function maybe also be provided. This is invoked for each node to be appended, being passed:
     *   * the parent node (parent)
     *   * the child nodes desired index within the parents children list (i)
     *     * For enter selections, this is particularly important as this allows nodes to be inserted into the
     *       scenegraph in an order consistent with the new bound data.
     *     * For other selections, this is equal to -1; indicating that the node should be appended to the end of the
     *       list.
     *   * the node to be appended (node)
     *
     * A default [append] callback is provided which handles appending nodes to parents which are a subclass of
     * [javafx.scene.Group] or [javafx.scene.layout.Pane]. If parent is not a subclass of either of these types an
     * [IllegalArgumentException] is thrown.
     */
    open fun append(node: (datum: T, index: Int, nodes: List<Node?>) -> Node): Selection<T> {
        val sel = Selection<T>()

        for(i in groups.indices) {
            val g = groups[i]
            val newGroup = Group(g.parent)
            sel.groups += newGroup

            val nodes = g.filterIsInstance<Node>()
            val newNodes = ArrayList<Node>(g.size)
            for(j in g.indices) {
                val e = g[j]
                when(e) {
                    is Node -> {
                        val newNode = node(e.datum as T, j, nodes)
                        newNode.datum = e.datum
                        newNode.key = e.key
                        appender(e, -1, newNode)
                        newNodes += newNode
                        newGroup += newNode
                    }
                    is Placeholder -> {
                        if(e.datum != null) {
                            val newNode = node(e.datum as T, j, nodes)
                            newNode.datum = e.datum
                            newNode.key = e.key
                            newNodes += newNode
                            appender(e.parent, -1, newNode)
                            newGroup += newNode
                        }
                    }
                    else -> newGroup.add(null)
                }
            }
        }

        return sel
    }

    fun append(node: () -> Node): Selection<T> {
        val sel = Selection<T>()

        for(g in groups) {
            val newGroup = Group(g.parent)
            sel.groups += newGroup

            if(g.isEmpty()) {
                val newNode = node()
                newNode.datum = g.parent.datum
                appender(g.parent, -1, newNode)
                newGroup += newNode
            }
            else {
                for(i in g.indices) {
                    val e = g[i]
                    when(e) {
                        is Node -> {
                            val newNode = node()
                            newNode.datum = e.datum
                            appender(e, -1, newNode)
                            newGroup += newNode
                        }
                        is Placeholder -> {
                            val newNode = node()
                            newNode.datum = e.datum
                            appender(e.parent, -1, newNode)
                            newGroup += newNode
                        }
                        else -> newGroup.add(null)
                    }
                }
            }
        }

        return sel
    }

    fun styleClasses(values: (datum: T, index: Int, nodes: List<Node?>) -> List<String>): Selection<T> =
            forEach<Node> { d, i, group -> styleClass += values(d, i, group) }

    fun classed(vararg styleClasses: String, apply: Boolean = true): Selection<T> {
        if(styleClasses.isNotEmpty()) {
            groups.flatMap { it }
                    .filterIsInstance<Node>()
                    .forEach {
                        if(apply) it.styleClass += styleClasses
                        else it.styleClass -= styleClasses
                    }
        }

        return this
    }

    fun classed(vararg styleClasses: String, apply: (d: T, i: Int, nodes: List<Node?>) -> Boolean) =
            forEach<Node> { d, i, group ->
                if(apply(d, i, group))
                    styleClass += styleClasses
                else
                    styleClass -= styleClasses
            }

    /**
     * Appends the given key/value pair to the [Node]'s [Node.style] property.
     *
     * If the value is null, the given key is removed from the [Node]'s [Node.style] property.
     */
    fun style(key: String, value: String?): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { style(it, key, value) }

        return this
    }

    /**
     * Appends the given key/value pair to the [Node.style] property.
     *
     * @param key The CSS key to set.
     * @param values Value(s)
     * @param units The units to append to each numeric value. Defaults to an emptySelection string.
     */
    fun style(key: String, vararg values: Number, units: String = ""): Selection<T> {
        val joiner = StringJoiner(" ")
        values.forEach { joiner.add(it.toString() + units) }
        return style(key, joiner.toString())
    }

    /**
     * Appends the given key and value returned by [value] to the [Node.style] property.
     *
     * @param key The CSS key to set.
     * @param value A function which calculates the value.
     */
    fun style(key: String, value: (d: T, i: Int, nodes: List<Node?>) -> String?) =
            forEach<Node> { d, i, group -> style(this, key, value(d, i, group)) }

    /**
     * Applies the given [effect] to each [Node] in the [Selection],
     *
     * @param effect The effect to be applied.
     */
    fun effect(effect: Effect): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.effect = effect }

        return this
    }

    fun effect(value: (d: T, i: Int, group: List<Node?>) -> Effect) =
            forEach<Node> { d, i, group -> effect = value(d, i, group) }

    /**
     *
     */
    fun remove(remove: (node: Node) -> Unit = ::removeNode) {
        for(g in groups) {
            g.filterIsInstance<Node>()
                    .forEach { remove(it) }
        }
    }

    /**
     *
     */
    fun merge(other: Selection<T>): Selection<T> {
        val sel = Selection(this)

        for(i in sel.groups.indices) {
            if(i >= other.groups.size)
                return sel

            val g = sel.groups[i]
            val og = other.groups[i]
            for(j in g.indices) {
                if(j >= og.size)
                    return sel

                if(g[j] == null)
                    g[j] = og[j]
            }
        }

        return sel
    }

    /**
     * Invokes [action] on each [Node] in the selection.
     *
     * @param action The function/action to be invoke on each node.
     *
     * @return The selection.
     */
    inline fun <reified N : Node> forEach(action: N.(d: T, i: Int, group: List<Node?>) -> Unit): Selection<T> {
        for(i in groups.indices) {
            val group = groups[i]
            val nodes = group.map { it as? N }
            for(j in group.indices) {
                val e = group[j]
                if(e is N) action(e, e.datum as T, j, nodes)
            }
        }

        return this
    }

    fun cursor(value: (d: T, i: Int, group: List<Node?>) -> Cursor) =
            forEach<Node> { d, i, group -> cursor = value(d, i, group)}

    fun cursor(cursor: Cursor): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.cursor = cursor }

        return this
    }

    override fun opacity(value: (d: T, i: Int, group: List<Node?>) -> Double): Selection<T> =
            forEach<Node> { d, i, group -> opacity = value(d, i, group) }

    override fun opacity(value: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.opacity = value }

        return this
    }

    fun rotate(rotate: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.rotate = rotate }

        return this
    }

    fun rotate(angle: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> rotate = angle(d, i, group) }

    fun rotationAxis(axis: Point3D): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.rotationAxis = axis }

        return this
    }

    fun rotationAxis(axis: (d: T, i: Int, group: List<Node?>) -> Point3D) =
            forEach<Node> { d, i, group -> rotationAxis = axis(d, i, group) }

    override fun rotateX(angle: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    it.rotationAxis = Rotate.X_AXIS
                    it.rotate = angle
                }

        return this
    }

    override fun rotateX(angle: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group ->
                rotationAxis = Rotate.X_AXIS
                rotate = angle(d, i, group)
            }

    override fun rotateY(angle: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    it.rotationAxis = Rotate.Y_AXIS
                    it.rotate = angle
                }

        return this
    }

    override fun rotateY(angle: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group ->
                rotationAxis = Rotate.Y_AXIS
                rotate = angle(d, i, group)
            }

    override fun rotateZ(angle: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    it.rotationAxis = Rotate.Z_AXIS
                    it.rotate = angle
                }

        return this
    }

    override fun rotateZ(angle: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group ->
                rotationAxis = Rotate.Z_AXIS
                rotate = angle(d, i, group)
            }

    override fun scaleX(x: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.scaleX = x }

        return this
    }

    override fun scaleX(x: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> scaleX = x(d, i, group) }

    override fun scaleY(y: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.scaleY = y }

        return this
    }

    override fun scaleY(y: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> scaleY = y(d, i, group) }

    override fun scaleZ(z: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.scaleZ = z }

        return this
    }

    override fun scaleZ(z: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> scaleZ = z(d, i, group)}

    override fun translateX(x: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.translateX = x }

        return this
    }

    override fun translateX(x: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> translateX = x(d, i, group) }


    override fun translateY(y: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.translateY = y }

        return this
    }

    override fun translateY(y: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> translateY = y(d, i, group) }

    override fun translateZ(z: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.translateZ = z }

        return this
    }

    override fun translateZ(z: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> translateZ = z(d, i, group) }

    fun transform(transforms: List<Transform>): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.transforms.setAll(transforms) }

        return this
    }

    fun transform(values: (d: T, i: Int, group: List<Node?>) -> List<Transform>) =
            forEach<Node> { d, i, group -> transforms.setAll(values(d, i, group))}

    fun visible(visible: Boolean): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.isVisible = visible }

        return this
    }

    fun visible(value: (d: T, i: Int, group: List<Node?>) -> Boolean) =
            forEach<Node> { d, i, group -> isVisible = value(d, i, group )}

    fun <P> bind(accessor: Node.(d: T, i: Int, group: List<Node?>) -> Property<P>?, value: ObservableValue<P>) =
            forEach<Node> { d, i, group -> accessor(d, i, group)?.bind(value) }

    fun <P> unbind(accessor: Node.(d: T, i: Int, group: List<Node?>) -> Property<P>?) =
            forEach<Node> { d, i, group -> accessor(d, i, group)?.unbind() }

    override fun fill(paint: Paint?): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Shape>()
                .forEach { it.fill = paint }

        return this
    }

    override fun fill(paint: (d: T, i: Int, group: List<Node?>) -> Paint?) =
            forEach<Shape> { d, i, group -> fill = paint(d, i, group) }

    override fun stroke(paint: Paint?): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Shape>()
                .forEach { it.stroke = paint }

        return this
    }

    fun strokeWidth(width: (d: T, i: Int, group: List<Node?>) -> Double) =
            forEach<Shape> { d, i, group -> strokeWidth = width(d, i, group) }

    fun strokeWidth(width: Double): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Shape>()
                .forEach { it.strokeWidth = width }

        return this
    }

    override fun stroke(paint: (d: T, i: Int, group: List<Node?>) -> Paint?) =
            forEach<Shape> { d, i, group -> stroke = paint(d, i, group) }

    fun tooltip(tooltip: Tooltip?): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    if(text != null) it.setTooltip(tooltip)
                    else it.setTooltip(null)
                }

        return this
    }

    fun tooltip(tooltip: (d: T, i: Int, group: List<Node?>) -> Tooltip?) =
            forEach<Node> { d, i, group -> setTooltip(tooltip(d, i, group)) }

    fun <E : Event, ET : EventType<E>> on(type: ET, handler: Node.(E) -> Unit): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.addEventHandler(type, { e -> it.handler(e) }) }

        return this
    }

    fun <E : Event, ET : EventType<E>> on(type: ET, name: String, handler: (E) -> Unit): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    // We can't simply use automatic SAM conversion because each conversion seems to create a different
                    // instance, which causes removal to fail in off()
                    val eventHandler = EventHandler<E> { e -> handler.invoke(e) }
                    it.addEventHandler(type, eventHandler)

                    var handlers: MutableMap<EventType<*>, MutableMap<String, Any>>? =
                            it.properties["__handlers__"] as MutableMap<EventType<*>, MutableMap<String, Any>>?

                    if(handlers == null) {
                        handlers = HashMap()
                        it.properties["__handlers__"] = handlers
                    }

                    var map: MutableMap<String, Any>? = handlers[type]
                    if(map == null) {
                        map = HashMap()
                        handlers[type] = map
                    }

                    try {
                        val oldHandler = map[name] as ((E) -> Unit)?
                        if (oldHandler != null)
                            it.removeEventHandler(type, oldHandler)
                    }
                    catch(cce : ClassCastException) {
                        // Failed to remove existing handler
                    }

                    map[name] = eventHandler
                }

        return this
    }

    fun <E : Event, ET : EventType<E>> off(type: ET, name: String): Selection<T> {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    val handlers: MutableMap<EventType<*>, MutableMap<String, Any>>? =
                            it.properties["__handlers__"] as MutableMap<EventType<*>, MutableMap<String, Any>>?

                    if(handlers != null) {
                        val map: MutableMap<String, Any>? = handlers[type]
                        if(map != null) {
                            val handler = map[name] as EventHandler<E>?
                            if(handler != null) {
                                it.removeEventHandler(type, handler)
                                map.remove(name)
                                if(map.isEmpty()) {
                                    handlers.remove(type)
                                }
                            }
                        }
                    }
                }

        return this
    }

    operator fun plusAssign(group: Group) {
        groups += group
    }

    internal operator fun minusAssign(group: Group) {
        groups -= group
    }

    private fun style(node: Node, key: String, value: String?) {
        if(value == null) {
            if(node.style.isNotEmpty()) {
                val styleMap = styleToMap(node.style)
                styleMap.remove(key)
                node.style = mapToStyle(styleMap)
            }
        }
        else {
            if (node.style.isEmpty())
                node.style = "$key: $value"
            else {
                val styleMap = styleToMap(node.style)
                styleMap[key] = value
                node.style = mapToStyle(styleMap)
            }
        }
    }

    private fun styleToMap(style: String): MutableMap<String, String> {
        val map = HashMap<String, String>()
        style.splitToSequence(';').forEach {
            val kv = it.split(':')
            map[kv[0]] = kv[1]
        }

        return map
    }

    private fun mapToStyle(map: Map<String, String>): String {
        val joiner = StringJoiner(";")
        joiner.setEmptyValue("")

        map.forEach { k, v ->
            joiner.add("$k: $v")
        }

        return joiner.toString()
    }
}

private data class Placeholder(val parent: Node, var key: Any?, var datum: Any?)