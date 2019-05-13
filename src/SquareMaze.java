import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SquareMaze {
	private Cell cellNextCol(Cell c) {
		return new Cell(c.r, c.c + 1);
	}

	private Cell cellNextRow(Cell c) {
		return new Cell(c.r + 1, 0);
	}

	// should use java.util.Optional
	private Cell nextCellOrNull(Cell c, Set<Cell> cells) {
		Cell res = cellNextCol(c);
		if (!cells.contains(res)) {
			res = cellNextRow(c);
			if (!cells.contains(res)) {
				res = null;
			}
		}
		return res;
	}

	public static String mazeToString(DiGraph<Cell, Integer> maze) {
		StringBuilder res = new StringBuilder();
		Set<Cell> cells = new HashSet<Cell>(maze.getVertices());
		for (Cell cr = new Cell(0, 0); cells.contains(cr); cr = new Cell(cr.r + 1, 0)) {
			StringBuilder horiz = new StringBuilder();
			for (Cell current = cr; cells.contains(current); current = new Cell(current.r, current.c + 1)) {
				String name = maze.getNameOrNullByVertex(current);
				res.append((name != null) ? name : " ");
				horiz.append(
						maze.getAdjacentVertices(current).contains(new Cell(current.r + 1, current.c)) ? " " : "-");
				res.append(maze.getAdjacentVertices(current).contains(new Cell(current.r, current.c + 1)) ? " " : "|");
				horiz.append("+");
			}
			res.append("\n");
			res.append(horiz);
			res.append("\n");
		}
		return res.toString();
	}

	public static String solvedMazeToString(DiGraph<Cell, Integer> maze) {
		StringBuilder res = new StringBuilder();
		Set<Cell> cells = new HashSet<Cell>(maze.getVertices());
		for (Cell cr = new Cell(0, 0); cells.contains(cr); cr = new Cell(cr.r + 1, 0)) {
			StringBuilder horiz = new StringBuilder();
			for (Cell current = cr; cells.contains(current); current = new Cell(current.r, current.c + 1)) {
				String name = maze.getNameOrNullByVertex(current);

				if (maze.getTotalPath().contains(new Cell(current.r, current.c))) {
					res.append("X");
				} else {
					res.append((name != null) ? name : " ");
				}
				horiz.append(
						maze.getAdjacentVertices(current).contains(new Cell(current.r + 1, current.c)) ? " " : "-");
				res.append(maze.getAdjacentVertices(current).contains(new Cell(current.r, current.c + 1)) ? " " : "|");
				horiz.append("+");
			}
			res.append("\n");
			res.append(horiz);
			res.append("\n");
		}

		return res.toString();
	}

	private static int connectCellsBothWays(Cell c0, Cell c1, int currentEdgeId, DiGraph<Cell, Integer> maze) {
		maze.addEdge(currentEdgeId++, c0, c1);
		maze.addEdge(currentEdgeId++, c1, c0);
		return currentEdgeId;
	}

	/*
	 * private static int connectWeightedCellsBothWays(Cell c0, Cell c1, int
	 * currentEdgeId, DiGraph<Cell, Edge> maze, int weight) {
	 * maze.addWeightedEdge(new Edge(currentEdgeId++, weight), c0, c1);
	 * maze.addWeightedEdge(new Edge(currentEdgeId++, weight), c1, c0); return
	 * currentEdgeId; }
	 */
	private static DiGraph<Cell, Integer> read(InputStream is) throws IOException {
		DiGraph<Cell, Integer> res = new AdjacencyDiGraph<Cell, Integer>();
		int currentEdgeId = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		for (int line = 0; br.ready(); ++line) {
			String currentLine = br.readLine();
			for (int nCh = 0; nCh != currentLine.length(); ++nCh) {
				char ch = currentLine.charAt(nCh);
				if (line % 2 == 0) { // cells and vertical walls
					if (nCh % 2 == 0) {// cell
						Cell currentCell = new Cell(line / 2, nCh / 2);
						res.addVertex(currentCell);
						if (ch != ' ') {// named cell
							res.nameVertex("" + ch, currentCell);
						}
					} else {// vertical wall or not
						if ((ch == ' ') && ((nCh != currentLine.length() - 1))) {// no wall, not end of the line

							currentEdgeId = connectCellsBothWays(new Cell(line / 2, (nCh + 1) / 2),
									new Cell(line / 2, (nCh - 1) / 2), currentEdgeId, res);
						}
						/*
						 * if ((Character.isDigit(ch)) && ((nCh != currentLine.length() - 1))) {// no
						 * wall, not end of the // line
						 * 
						 * currentEdgeId = connectWeightedCellsBothWays(new Cell(line / 2, (nCh + 1) /
						 * 2), new Cell(line / 2, (nCh - 1) / 2), currentEdgeId, res, ch); }
						 */
					}
				} else {// horizontal walls or unused
					if (nCh % 2 == 0) { // horizontal wall or not
						if (ch == ' ' && br.ready()) { // no wall, not end of the stream
							currentEdgeId = connectCellsBothWays(new Cell((line + 1) / 2, nCh / 2),
									new Cell((line - 1) / 2, nCh / 2), currentEdgeId, res);
						}
					}
				}
			}
		}
		return res;
	}

	private static String pathToString(Collection<Cell> path, String sep) {
		StringBuilder res = new StringBuilder();
		for (Cell c : path) {
			res.append(c);
			res.append(sep);
		}
		return res.toString();
	}

	public static void main(String[] args) throws IOException {
		try (InputStream is = args.length == 0 ? System.in : new FileInputStream(new File(args[0]))) {
			DiGraph<Cell, Integer> maze = read(is);
			System.out.println("working with maze:\n" + mazeToString(maze));
			System.out.println(maze.getAdjacentVertices(maze.getVertexByName("B")));
			System.out.println(maze.getVertexByName("B"));
			System.out.println(maze.getVertexByName("A"));
			System.out.println("A and B are connected ? :" + maze.areConnected("A", "B"));
			System.out.println("Shortest path from A to B :" + pathToString(maze.shortestPath("A", "B"), ""));
			System.out.println("working with maze:\n" + solvedMazeToString(maze));
			System.out.println(maze.getVertexByName("A"));
		}
	}
}
