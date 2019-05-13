import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AdjacencyDiGraph<Vertex, Edge> implements DiGraph<Vertex, Edge> {
	protected Set<Vertex> vertices = new HashSet<Vertex>();
	protected Set<Edge> edges = new HashSet<Edge>();

	protected Map<Vertex, List<Edge>> vertexToEdges = new HashMap<Vertex, List<Edge>>();
	private Map<Edge, Vertex> edgeToSrc = new HashMap<Edge, Vertex>();
	private Map<Edge, Vertex> edgeToDest = new HashMap<Edge, Vertex>();
	private Map<String, Vertex> nameToVertex = new HashMap<String, Vertex>();
	protected List<Vertex> totalPath = new ArrayList<Vertex>();
	protected Set<Edge> WeightedEdges = new HashSet<Edge>();

	private int numberOfVerticies = 0;

	public int getNumberOfVerticies() {
		return numberOfVerticies;
	}

	public AdjacencyDiGraph() {
	}

	public void addVertex(Vertex v) {
		if (!vertices.contains(v)) {
			vertices.add(v);
			vertexToEdges.put(v, new ArrayList<Edge>());
			numberOfVerticies++;
		}
	}

	public List<Vertex> getVertices() {
		return new ArrayList<Vertex>(vertices);
	}

	public Set<Vertex> getTotalPath() {
		return new HashSet<Vertex>(totalPath);
	}

	public void addEdge(Edge e, Vertex src, Vertex dest) {

		addVertex(src);
		addVertex(dest);
		edges.add(e);
		edgeToSrc.put(e, src);
		edgeToDest.put(e, dest);
		vertexToEdges.get(src).add(e);
	}

	public List<Edge> getEdges() {
		return new ArrayList<Edge>(edges);
	}

	public List<Vertex> getAdjacentVertices(Vertex src) {
		List<Vertex> res = new ArrayList<Vertex>();
		for (Edge e : vertexToEdges.get(src)) {
			res.add(edgeToDest.get(e));
		}
		return res;
	}

	public void nameVertex(String name, Vertex v) {
		nameToVertex.put(name, v);
	}

	public Vertex getVertexByName(String name) {
		return nameToVertex.get(name);
	}

	public String getNameOrNullByVertex(Vertex v) {
		for (Map.Entry<String, Vertex> e : nameToVertex.entrySet()) {
			if (e.getValue().equals(v)) {
				return e.getKey();
			}
		}
		return null;
	}

	public List<String> getNames() {
		return new ArrayList<String>(nameToVertex.keySet());
	}

	public boolean areConnected(Vertex src, Vertex dest) {
		// Base case
		if (src.equals(dest)) {
			return true;
		}

		Queue<Vertex> queue = new LinkedList<Vertex>();
		ArrayList<Vertex> visited = new ArrayList<Vertex>();

		queue.add(src);
		visited.add(src);

		while (queue.size() != 0) {
			Vertex source = queue.poll();

			for (Vertex v : getAdjacentVertices(source)) {
				if (v.equals(dest))
					return true;

				if (!visited.contains(v)) {
					visited.add(v);
					queue.add(v);
				}

			}
		}
		return false;
	}

	public boolean areConnected(String src, String dest) {
		return areConnected(getVertexByName(src), getVertexByName(dest));
	}

	public class Pair<Vertex, Integer> {

		private Vertex v;
		private int dist;

		public void setDist(int dist) {
			this.dist = dist;
		}

		public Pair<Vertex, Integer> createPair(Vertex v, int dist) {
			return new Pair<Vertex, Integer>(v, dist);
		}

		public Pair(Vertex v, int dist) {
			this.v = v;
			this.dist = dist;
		}

		public Vertex getV() {
			return v;
		}

		public int getDist() {
			return dist;
		}

	}

	public List<Vertex> shortestPath(Vertex src, Vertex dest) {

		final int size = getVertices().size(); // used to size data structures appropriately
		final Set<Vertex> closedSet = new HashSet<Vertex>(size); // The set of nodes already evaluated.
		final List<Vertex> openSet = new ArrayList<Vertex>(size); // The set of tentative nodes to be evaluated,
																	// initially containing the src node
		openSet.add(src);
		final Map<Vertex, Vertex> cameFrom = new HashMap<Vertex, Vertex>(size); // The map of navigated nodes.

		final Map<Vertex, Integer> gScore = new HashMap<Vertex, Integer>(); // Cost from src along best known path.
		gScore.put(src, 0);

		// Estimated total cost from src to dest through y.
		final Map<Vertex, Integer> fScore = new HashMap<Vertex, Integer>();
		for (Vertex v : getVertices())
			fScore.put(v, Integer.MAX_VALUE);
		fScore.put(src, heuristicCostEstimate(src, dest));

		final Comparator<Vertex> comparator = new Comparator<Vertex>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public int compare(Vertex o1, Vertex o2) {
				if (fScore.get(o1) < fScore.get(o2))
					return -1;
				if (fScore.get(o2) < fScore.get(o1))
					return 1;
				return 0;
			}
		};

		while (!openSet.isEmpty()) {
			final Vertex current = openSet.get(0);
			if (current.equals(dest))
				return reconstructPath(cameFrom, dest);

			openSet.remove(0);
			closedSet.add(current);
			for (Vertex neighbor : getAdjacentVertices(current)) {
				if (closedSet.contains(neighbor))
					continue; // Ignore the neighbor which is already evaluated.

				final int tenativeGScore = gScore.get(current) + distanceBetween(current, neighbor); // length of this
																										// path.
				if (!openSet.contains(neighbor))
					openSet.add(neighbor); // Discover a new node
				else if (tenativeGScore >= gScore.get(neighbor))
					continue;

				// This path is the best until now.
				cameFrom.put(neighbor, current);
				gScore.put(neighbor, tenativeGScore);
				final int estimatedFScore = gScore.get(neighbor) + heuristicCostEstimate(neighbor, dest);
				fScore.put(neighbor, estimatedFScore);

				// fScore has changed, re-sort the list
				Collections.sort(openSet, comparator);
			}
		}

		return null;
	}

//		// Initialization.
//		Map<Vertex, Vertex> nextNodeMap = new HashMap<Vertex, Vertex>();
//		Vertex currentNode = src;
//
//		// Queue
//		Queue<Vertex> queue = new LinkedList<Vertex>();
//		queue.add(currentNode);
//
//		/*
//		 * The set of visited nodes doesn't have to be a Map, and, since order is not
//		 * important, an ordered collection is not needed. HashSet is fast for add and
//		 * lookup, if configured properly.
//		 */
//		Set<Vertex> visitedNodes = new HashSet<Vertex>();
//		visitedNodes.add(currentNode);
//
//		// Search.
//		while (!queue.isEmpty()) {
//			currentNode = queue.remove();
//			if (currentNode.equals(dest)) {
//				break;
//			} else {
//				for (Vertex nextNode : getAdjacentVertices(currentNode)) {
//					if (!visitedNodes.contains(nextNode)) {
//						queue.add(nextNode);
//						visitedNodes.add(nextNode);
//
//						// Look up of next node instead of previous.
//						nextNodeMap.put(currentNode, nextNode);
//					}
//				}
//			}
//		}
//
//		// If all nodes are explored and the destination node hasn't been found.
//		if (!currentNode.equals(dest)) {
//			throw new RuntimeException("No feasible path.");
//		}
//
//		Map<Vertex, Vertex> swapped = nextNodeMap.entrySet().stream()
//				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
//
//		List<Vertex> totalPath = new ArrayList<Vertex>();
//		Vertex current = dest;
//		while (current != null) {
//			final Vertex previous = current;
//			current = swapped.get(current);
//			totalPath.add(previous);
//
//		}
//		Collections.reverse(totalPath);
//		return totalPath;

	// Reconstruct path. No need to reverse.
//		List<Vertex> directions = new ArrayList<Vertex>();
//		for (Vertex node = src; node != null; node = nextNodeMap.get(node)) {
//			directions.add(node);
//		}
//
//		return directions;

	/*
	 * //Map<Vertex,Integer> distance = new HashMap<Vertex,Integer>();
	 * //array of distances
	 * 
	 * Map<Vertex, Boolean> visited = new HashMap<Vertex, Boolean>();
	 * 
	 * for(Vertex v: getVertices()) distance.put(v, 1000); //
	 * 
	 * 
	 * List<Vertex> result = new ArrayList<Vertex>(); PriorityQueue<Pair<Vertex,
	 * Integer>> pq = new PriorityQueue<Pair<Vertex, Integer>>();
	 * 
	 * 
	 * pq.add(new Pair(src,0)); //aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
	 * 
	 * while (!toVisit.isEmpty()) { Node min = toVisit.remove(); if (min == to) {
	 * return min.getDistanceFromSource(); } if (min.isVisited()) { continue; }
	 * min.setVisited(true); for (Map.Entry<Node, Integer> neighborEntry :
	 * min.getNeighborList().entrySet()) { int adjacentDistance =
	 * min.getDistanceFromSource() + neighborEntry.getValue(); Node neighbor =
	 * neighborEntry.getKey(); if (neighbor.getDistanceFromSource > adjacentDistance
	 * && !neighbor.isVisited()) { neighbor.setDistanceFromSource(adjacentDistance);
	 * toVisit.add(neighbor); } } }
	 * 
	 * //aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
	 * 
	 * 
	 * while (pq.size() != 0) {
	 * 
	 * Vertex u=pq.poll().getV();
	 * 
	 * for(Vertex v : getAdjacentVertices(u)) {
	 * 
	 * if(u==dest) { return (List<Vertex>) u; }
	 * 
	 * 
	 * 
	 * 
	 * // If there is a shorter path to v // through u. If dist[v] > dist[u] +
	 * weight(u, v)
	 * 
	 * }
	 * 
	 * 
	 * List<Edge> edg = vertexToEdges.get(ver);
	 * 
	 * for(Edge e : edg) { Vertex destination = edgeToDest.get(e); newDistance =
	 * 
	 * }
	 * 
	 * 
	 * 
	 * }
	 * 
	 * return new ArrayList<Vertex>();
	 */

	protected int distanceBetween(Vertex src, Vertex next) {
		for (Edge e : vertexToEdges.get(src)) {
			if (edgeToDest.get(e).equals(next))
				return 1;
		}
		return Integer.MAX_VALUE;
	}

	/**
	 * Default heuristic: cost to each vertex is 1.
	 */
	@SuppressWarnings("unused")
	protected int heuristicCostEstimate(Vertex src, Vertex dest) {
		return 1;
	}

	public List<Vertex> reconstructPath(Map<Vertex, Vertex> cameFrom, Vertex current) {

		while (current != null) {
			totalPath.add(current);
			current = cameFrom.get(current);

		}
		Collections.reverse(totalPath);
		return totalPath;
	}

	public List<Vertex> shortestPath(String src, String dest) {
		return shortestPath(getVertexByName(src), getVertexByName(dest));
	}
}