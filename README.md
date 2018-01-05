# Kubed: A Kotlin DSL for data visualization

Kubed is a data visualization DSL embedded within the Kotlin programming language. Kubed facilitates the creation of interactive visualizations through data-driven transformations of the JavaFX scenegraph. With Kubed, developers can construct complex data visualizations through the composition of geometric primitives, such as rectangles, lines and text, whose visual properties are defined by functions over the underlying data.

Kubed is *heavily* inspired by [D3.js](https://d3js.org/), and supports many of the features found in D3, including: selections, transitions, scales, colorspaces, easing, and interpolators. Additional features coming soon!

## Example

Below is an example of the Kubed DSL; a simple bar chart is constructed.

```kotlin
val values = listOf(4.0, 8.0, 15.0, 16.0, 23.0, 28.0)

val width = 420.0
val barHeight = 20.0

val x = scaleLinear<Double> {
    domain(0.0, values.max!!)
    range(0.0, width)
}

val chart = Group()

val rect = rect<Double> {
    width { d, _ -> x(d) } height(barHeight - 1)
    translateY { _, i, _ -> i * barHeight}
}

chart.selectAll<Double>()
     .data(values)
     .enter()
     .append { d, i, _ -> rect(d, i) }
```
---

This is an experimental API and is subject to breaking changes until a first major release.

---

## Documentation

* Selection
* Transitions
* Scales
* Colorspaces
* Easing
* Interpolators
* Chord Diagrams

## Roadmap

The following features are being added to Kubed in the near future:
* Voronoi
* Force Layout
* Hierarchical Layout
