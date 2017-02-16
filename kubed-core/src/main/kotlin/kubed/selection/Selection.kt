package kubed.selection

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Point3D
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.effect.Effect
import javafx.scene.paint.Paint
import javafx.scene.shape.Shape
import javafx.scene.transform.Rotate
import javafx.scene.transform.Transform
import java.util.*
import kotlin.comparisons.compareBy

open class Selection() : BaseSelection<Selection> {
    companion object {
        val UNDEFINED = Any()
        internal val EMPTY_SELECTION = Selection()
        internal const val DATA_PROPERTY: String = "__data__"
    }

    val groups = ArrayList<Group>()

    var undefined = UNDEFINED

    private var enter: Selection? = null
    private var exit: Selection? = null

    fun enter(): Selection {
        if(enter == null)
            return EMPTY_SELECTION

        return Selection(enter!!)
    }

    fun exit(): Selection {
        if(exit == null)
            return EMPTY_SELECTION

        return Selection(exit!!)
    }

    /**
     * Copy constructor.
     */
    constructor(other: Selection) : this() {
        for(g in other.groups) {
            val group = Group(g.parent)
            for(e in g)
                group += e

            groups += group
        }

        enter = other.enter()
        exit = other.exit()
    }

    /**
     * Returns true if this selection contains any non-null elements.
     */
    val empty: Boolean
        get() = groups.any { !it.empty }

    /**
     * Sets the undefined value; the value returned when there is no data associated with a [Node] in the scenegraph.
     */
    fun undefined(value: Any): Selection {
        undefined = value
        return this
    }

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
    fun select(selector: String?): Selection {
        val sel = Selection()

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
    fun select(selector: (d: Any, i: Int, nodes: List<Node?>) -> Node?): Selection {
        val sel = Selection()

        for(g in groups) {
            val newGroup = Group(g.parent)
            sel.groups += newGroup

            for(i in g.indices) {
                val e = g[i]
                if(e is Node) {
                    val nodes = g.map { if(it is Node) it else null }
                    val selNode = selector(e.datum ?: undefined, i, nodes)
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

    fun selectAll() = selectAll("*")

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
    fun selectAll(selector: String?): Selection {
        val sel = Selection()

        groups.flatMap { it }
              .filterIsInstance<Node>()
              .forEach {
                  if(selector != null) {
                      val group = Group(it)
                      sel += group

                      val nodes = it.lookupAllChildren(selector)
                      group.addAll(nodes.sortedWith(compareBy(Node::depth, Node::pos)))
                  }
              }

        return sel
    }

    /**
     * Sets the bound data for each selected element to [value]. Unlike [data] this method does not compute a join and
     * does not affect indexes or the enter and exit selections.
     *
     * If [value] is null, the bound data will be removed from the selected elements.
     */
    fun datum(value: Any?): Selection {
        for(g in groups) {
            for(e in g) {
                when(e) {
                    is Node -> e.datum = value
                    is Placeholder -> e.datum = value ?: undefined
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
     * The value returned from the [value] function is then bound to the element. If null is returned, the element will
     * be bound to [undefined].
     */
    fun datum(value: (d: Any, i: Int, nodes: List<Node?>) -> Any?): Selection {
        for(g in groups) {
            for(i in g.indices) {
                val e = g[i]
                if(e is Node) {
                    val nodes = g.map { if(it is Node) it else null }
                    val datum = value(e.datum ?: undefined, i, nodes) ?: undefined
                    e.datum = datum
                }
            }
        }

        return this
    }

    /**
     * Joins the specified array of data with the selected elements, returning a new selection that it represents the
     * _update_ selection: the elements successfully bound to data. Also defines the enter and exit selections on the
     * returned selection, which can be used to add or remove elements to correspond to the new data. The specified
     * [data] is a [List] of arbitrary values (_e.g._, numbers or objects).
     *
     * When data is assigned to a [Node] it is stored in the [Node.properties] at [DATA_PROPERTY], thus making the
     * "data" sticky and available on re-selection.
     *
     * The data is specified *for each group* in the selection.
     */
    fun data(data: List<List<*>>, key: (d: Any?, i: Int, groupDatum: Any) -> Any = { _, i, _ -> i }) = data( {
        _, i, _ -> data[i]
    }, key)

    fun data(data: (d: Any, i: Int, nodes: List<Node?>) -> List<*>,
             key: (d: Any?, i: Int, groupDatum: Any) -> Any = { _, i, _ -> i }): Selection {
        val sel = Selection()
        val enter = Selection()
        val exit = Selection()
        sel.enter = enter
        sel.exit = exit

        val parentNodes = groups.map { it.parent }

        val dataList = ArrayList<List<*>>(groups.size)
        for(i in groups.indices) {
            val g = groups[i]
            val d = g.parent.datum ?: undefined
            dataList.add(data(d, i, parentNodes))
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
                    is Node -> key(e.datum ?: undefined, j, g)
                    is Placeholder -> key(e.datum, j, g)
                    else -> null
                }
                oldKeys += k
            }

            val groupData = dataList[i]
            val newKeys = ArrayList<Any?>(groupData.size)
            for(j in groupData.indices)
                newKeys += key(groupData[j], j, g.parent.datum ?: undefined)

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
                    enterGroup += Placeholder(g.parent, groupData[j]!!)
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

    fun append(node: () -> Node, append: (parent: Node, i: Int, child: Node) -> Unit = ::addNode): Selection {
        val sel = Selection()

        for(g in groups) {
            val newGroup = Group(g.parent)
            sel.groups += newGroup

            if(g.isEmpty()) {
                val newNode = node()
                append(g.parent, -1, newNode)
                newGroup += newNode
            }
            else {
                for(i in g.indices) {
                    val e = g[i]
                    when(e) {
                        is Node -> {
                            val newNode = node()
                            newNode.datum = e.datum
                            append(e, -1, newNode)
                            newGroup += node
                        }
                        is Placeholder -> {
                            val newNode = node()
                            newNode.datum = e.datum
                            append(e.parent, -1, newNode)
                            newGroup += newNode
                        }
                        else -> newGroup.add(null)
                    }
                }
            }
        }

        return sel
    }

    fun append(node: (datum: Any, index: Int, nodes: List<Node?>) -> Node) = append(node, ::addNode)

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
    open fun append(node: (datum: Any, index: Int, nodes: List<Node?>) -> Node,
                    append: (parent: Node, i: Int, child: Node) -> Unit): Selection {
        val sel = Selection()

        for(g in groups) {
            val newGroup = Group(g.parent)
            sel.groups += newGroup

            for(i in g.indices) {
                val e = g[i]
                val nodes = g.map { if(it is Node) it else null }
                when(e) {
                    is Node -> {
                        val newNode = node(e.datum ?: undefined, i, nodes)
                        newNode.datum = e.datum
                        append(e, -1, newNode)
                        newGroup += newNode
                    }
                    is Placeholder -> {
                        val newNode = node(e.datum, i, nodes)
                        newNode.datum = e.datum
                        append(e.parent, -1, newNode)
                        newGroup += newNode
                    }
                    else -> newGroup.add(null)
                }
            }
        }

        return sel
    }

    fun classed(vararg styleClasses: String, apply: Boolean = true): Selection {
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

    fun classed(vararg styleClasses: String, apply: (d: Any, i: Int, nodes: List<Node?>) -> Boolean) =
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
    fun style(key: String, value: String?): Selection {
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
    fun style(key: String, vararg values: Number, units: String = ""): Selection {
        val joiner = StringJoiner(" ")
        values.forEach { joiner.add(it.toString() + units) }
        return style(key, joiner.toString())
    }

    /**
     *
     */
    fun style(key: String, value: (d: Any, i: Int, nodes: List<Node?>) -> String?) =
            forEach<Node> { d, i, group -> style(this, key, value(d, i, group)) }

    fun effect(effect: Effect): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.effect = effect }

        return this
    }

    fun effect(value: (d: Any, i: Int, group: List<Node?>) -> Effect) =
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
    fun merge(other: Selection): Selection {
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

    inline fun <reified T : Node> forEach(action: T.(d: Any, i: Int, group: List<Node?>) -> Unit): Selection {
        for(i in groups.indices) {
            val group = groups[i]
            val nodes = group.map { if(it is T) it else null }
            for(j in group.indices) {
                val e = group[j]
                if(e is T) action(e, e.datum ?: undefined, j, nodes)
            }
        }

        return this
    }

    fun cursor(value: (d: Any, i: Int, group: List<Node?>) -> Cursor) =
        forEach<Node> { d, i, group -> cursor = value(d, i, group)}

    fun cursor(cursor: Cursor): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.cursor = cursor }

        return this
    }

    override fun opacity(value: (d: Any, i: Int, group: List<Node?>) -> Double): Selection =
            forEach<Node> { d, i, group -> opacity = value(d, i, group) }

    override fun opacity(value: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.opacity = value }

        return this
    }

    fun rotate(rotate: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.rotate = rotate }

        return this
    }

    fun rotate(angle: (d: Any, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> rotate = angle(d, i, group) }

    fun rotationAxis(axis: Point3D): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.rotationAxis = axis }

        return this
    }

    fun rotationAxis(axis: (d: Any, i: Int, group: List<Node?>) -> Point3D) =
            forEach<Node> { d, i, group -> rotationAxis = axis(d, i, group) }

    override fun rotateX(angle: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    it.rotationAxis = Rotate.X_AXIS
                    it.rotate = angle
                }

        return this
    }

    override fun rotateX(angle: (d: Any, i: Int, group: List<Node?>) -> Double) =
        forEach<Node> { d, i, group ->
            rotationAxis = Rotate.X_AXIS
            rotate = angle(d, i, group)
        }

    override fun rotateY(angle: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    it.rotationAxis = Rotate.Y_AXIS
                    it.rotate = angle
                }

        return this
    }

    override fun rotateY(angle: (d: Any, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group ->
                rotationAxis = Rotate.Y_AXIS
                rotate = angle(d, i, group)
            }

    override fun rotateZ(angle: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach {
                    it.rotationAxis = Rotate.Z_AXIS
                    it.rotate = angle
                }

        return this
    }

    override fun rotateZ(angle: (d: Any, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group ->
                rotationAxis = Rotate.Z_AXIS
                rotate = angle(d, i, group)
            }

    override fun scaleX(x: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.scaleX = x }

        return this
    }

    override fun scaleX(x: (d: Any, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> scaleX = x(d, i, group) }

    override fun scaleY(y: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.scaleY = y }

        return this
    }

    override fun scaleY(y: (d: Any, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> scaleY = y(d, i, group) }

    override fun scaleZ(z: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.scaleZ = z }

        return this
    }

    override fun scaleZ(z: (d: Any, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> scaleZ = z(d, i, group)}

    override fun translateX(x: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.translateX = x }

        return this
    }

    override fun translateX(x: (d: Any, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> translateX = x(d, i, group) }


    override fun translateY(y: Double): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.translateY = y }

        return this
    }

    override fun translateY(y: (d: Any, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> translateY = y(d, i, group) }

    override fun translateZ(z: Double): Selection {
        groups.flatMap { it }
              .filterIsInstance<Node>()
              .forEach { it.translateZ = z }

        return this
    }

    override fun translateZ(z: (d: Any, i: Int, group: List<Node?>) -> Double) =
            forEach<Node> { d, i, group -> translateZ = z(d, i, group) }

    fun transform(transforms: List<Transform>): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.transforms.setAll(transforms) }

        return this
    }

    fun transform(values: (d: Any, i: Int, group: List<Node?>) -> List<Transform>) =
            forEach<Node> { d, i, group -> transforms.setAll(values(d, i, group))}

    fun visible(visible: Boolean): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.isVisible = visible }

        return this
    }

    fun visible(value: (d: Any, i: Int, group: List<Node?>) -> Boolean) =
            forEach<Node> { d, i, group -> isVisible = value(d, i, group )}

    fun <T> bind(accessor: Node.(d: Any, i: Int, group: List<Node?>) -> Property<T>?, value: ObservableValue<T>) =
        forEach<Node> { d, i, group -> accessor(d, i, group)?.bind(value) }

    fun <T> unbind(accessor: Node.(d: Any, i: Int, group: List<Node?>) -> Property<T>?) =
            forEach<Node> { d, i, group -> accessor(d, i, group)?.unbind() }

    override fun fill(paint: Paint): Selection {
        groups.flatMap { it }
                .filterIsInstance<Shape>()
                .forEach { it.fill = paint }

        return this
    }

    override fun fill(paint: (d: Any, i: Int, group: List<Node?>) -> Paint) =
            forEach<Shape> { d, i, group -> fill = paint(d, i, group) }

    override fun stroke(paint: Paint): Selection {
        groups.flatMap { it }
                .filterIsInstance<Shape>()
                .forEach { it.stroke = paint }

        return this
    }

    override fun stroke(value: (d: Any, i: Int, group: List<Node?>) -> Paint) =
        forEach<Shape> { d, i, group -> stroke = value(d, i, group) }

    fun <E : Event, T : EventType<E>> on(type: T, handler: (E) -> Unit): Selection {
        groups.flatMap { it }
                .filterIsInstance<Node>()
                .forEach { it.addEventHandler(type, handler) }

        return this
    }

    fun <E : Event, T : EventType<E>> on(type: T, name: String, handler: (E) -> Unit): Selection {
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

    fun <E : Event, T : EventType<E>> off(type: T, name: String): Selection {
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

private class Placeholder(val parent: Node, var datum: Any)

