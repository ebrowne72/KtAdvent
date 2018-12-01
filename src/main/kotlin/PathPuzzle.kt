import kotlin.math.pow
import kotlin.math.sqrt

fun String.toPathPuzzle() : PathPuzzle {
	lateinit var start: Node
	lateinit var end: Node

	val nodes = lines().mapIndexed { rowIdx, row ->
		row.mapIndexed { colIdx, column ->
			when (column) {
				'.', 'S', 'X' -> {
					val node = Node(rowIdx, colIdx)
					if (column == 'S') {
						start = node
					}
					else if (column == 'X') {
						end = node
					}
					node
				}
				'B' -> EmptyNode
				else -> EmptyNode
			}
		}
	}
	return PathPuzzle(start, end, Graph(nodes))
}

data class PathPuzzle(val start: Node, val end: Node, val graph: Graph) {

	fun getPath(): String {
		start.distance = 0F
		start.guess = graph.hDistance(start, end)
		end.guess = 0F
		val openSet = mutableListOf<Node>()
		val closedSet = mutableSetOf<Node>()

		openSet.add(start)

		while (!openSet.isEmpty()) {
			val current = openSet.removeAt(0)
			if ( current === end )
				break
			closedSet.add(current)

			for ( edge in graph.edges(current) ) {
				if (closedSet.contains(edge.neighbor))
					continue
				val newDistance = current.distance + edge.distance

				if ( !openSet.contains(edge.neighbor) ) {
					openSet.add(edge.neighbor)
				}
				else if ( newDistance >= edge.neighbor.distance) {
					continue
				}

				edge.neighbor.apply {
					cameFrom = current
					distance = newDistance
					if ( guess == Float.NEGATIVE_INFINITY ) {
						guess = graph.hDistance(edge.neighbor, end)
					}
				}
			}

			openSet.sortWith(Comparator { first, second -> (first.distance + first.guess).compareTo(second.distance + second.guess) })
		}

		var currentPathNode: Node? = end
		while ( currentPathNode != null ) {
			currentPathNode.partOfPath = true
			currentPathNode = currentPathNode.cameFrom
		}

		return graph.getOutput()
	}
}

data class Edge(val distance: Float, val neighbor: Node)

open class Node(val row: Int, val col: Int) {
	var distance = Float.NEGATIVE_INFINITY
	var guess = Float.NEGATIVE_INFINITY
	var cameFrom: Node? = null
	var partOfPath = false
	override fun toString() = "Node[$row,$col]"
}

object EmptyNode : Node(-1, -1)

class Graph(private val nodes: List<List<Node>>) {

	fun hDistance(first: Node, second: Node) = sqrt((second.row - first.row).toFloat().pow(2) +(second.col - first.col).toFloat().pow(2))

	fun edges(node: Node): List<Edge> {
		val edgeList = mutableListOf<Edge>()
		for ( rowOffset in -1..1) {
			for ( colOffset in -1..1)
			{
				if (rowOffset != 0 || colOffset != 0) {
					val endNode = nodes.getOrNull(node.row + rowOffset)?.getOrNull(node.col + colOffset)
					if (endNode != null) {
						edgeList.add(Edge(if (rowOffset == 0 || colOffset == 0) 1.0F else 1.5F, endNode))
					}
				}
			}
		}

		return edgeList
	}

	fun getOutput(): String {
		return nodes.joinToString(separator = "\n") { nodeList ->
			nodeList.joinToString(separator = "") { node ->
				when {
					node == EmptyNode -> "B"
					node.partOfPath -> "*"
					else -> "."
				}
			}
		}
	}
}
