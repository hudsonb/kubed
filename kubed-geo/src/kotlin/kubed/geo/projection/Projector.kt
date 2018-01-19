package kubed.geo.projection

typealias Projector = Transform

typealias InvertableProjector = InvertableTransform

interface ProjectorFactory {
    fun create(): Projector
}