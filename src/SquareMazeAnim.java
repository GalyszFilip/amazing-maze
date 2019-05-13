import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class SquareMazeAnim {
	private static Random rng = new Random();

	// should use java.util.Optional
	private Cell nextCellOrNull(Cell c, Set<Cell> cells) {
		Cell res = new Cell(c.r, c.c + 1);
		if (!cells.contains(res)) {
			res = new Cell(c.r + 1, 0);
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

	private static int connectCellsBothWays(Cell c0, Cell c1, int currentEdgeId, DiGraph<Cell, Integer> maze) {
		maze.addEdge(currentEdgeId++, c0, c1);
		maze.addEdge(currentEdgeId++, c1, c0);
		return currentEdgeId;
	}

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

	private static void draw(DiGraph<Cell, Integer> maze, Color cellColor, Color wallColor) {
		draw(maze, cellColor, wallColor, maze.getVertices());
	}

	private static void draw(DiGraph<Cell, Integer> maze, Color cellColor, Color wallColor, Collection<Cell> cells) {
		// StdDraw.setPenRadius(0.01);
		StdDraw.enableDoubleBuffering();
		for (Cell c : cells) {
			List<Cell> adj = maze.getAdjacentVertices(c);
			StdDraw.setPenColor(cellColor);
			StdDraw.filledSquare(c.c, -c.r, 1. / 2);

			StdDraw.setPenColor(wallColor);
			if (!adj.contains(new Cell(c.r, c.c - 1))) {
				StdDraw.line(c.c - 0.5, -c.r - 0.5, c.c - 0.5, -c.r + 0.5);
			}
			if (!adj.contains(new Cell(c.r, c.c + 1))) {
				StdDraw.line(c.c + 0.5, -c.r - 0.5, c.c + 0.5, -c.r + 0.5);
			}
			if (!adj.contains(new Cell(c.r - 1, c.c))) {
				StdDraw.line(c.c - 0.5, -c.r + 0.5, c.c + 0.5, -c.r + 0.5);
			}
			if (!adj.contains(new Cell(c.r + 1, c.c))) {
				StdDraw.line(c.c - 0.5, -c.r - 0.5, c.c + 0.5, -c.r - 0.5);
			}
		}
		StdDraw.show();
	}

	private static void drawNames(DiGraph<Cell, Integer> maze, Color namesColor) {
		StdDraw.setPenColor(namesColor);
		for (String name : maze.getNames()) {
			Cell c = maze.getVertexByName(name);
			StdDraw.text(c.c, -c.r, name);
		}
		StdDraw.show();
	}

	private static int[] maxRowMaxCol(Collection<Cell> cells) {
		int[] res = { 0, 0 };
		for (Cell c : cells) {
			if (c.r > res[0]) {
				res[0] = c.r;
			}
			if (c.c > res[1]) {
				res[1] = c.c;
			}
		}
		return res;
	}

	private static void setupCanvas(Collection<Cell> cells, int canvasWidth, int canvasHeight) {
		StdDraw.setCanvasSize(canvasWidth, canvasHeight);
		int[] nRowsNCols = maxRowMaxCol(cells);
		StdDraw.setXscale(-0.5, nRowsNCols[1] + 0.5);
		StdDraw.setYscale(-(nRowsNCols[0] + 0.5), 0.5);
	}

	private static DiGraph<Cell, Integer> makeUnconnectedMaze(int nRows, int nCols) {
		DiGraph<Cell, Integer> res = new AdjacencyDiGraph<Cell, Integer>();
		for (int r = 0; r != nRows; ++r) {
			for (int c = 0; c != nCols; ++c) {
				res.addVertex(new Cell(r, c));
			}
		}
		return res;
	}

	private static int connectToCoveringTree(DiGraph<Cell, Integer> maze) {
		List<Cell> cells = maze.getVertices();
		if (cells.isEmpty()) {
			return 0;
		}
		Set<Cell> existingCells = new HashSet<Cell>(cells);
		Set<Cell> visited = new HashSet<Cell>();
		Queue<Cell> toVisit = new LinkedList<Cell>();
		int edgeId = 0;
		Cell visiting = cells.get(0);// get one a the center ?
		toVisit.add(visiting);
		while (!toVisit.isEmpty()) {
			visited.add(visiting);
			List<Cell> candidates = new ArrayList<Cell>();
			for (int dr = -1; dr <= 1; ++dr) {
				for (int dc = -1; dc <= 1; ++dc) {
					if ((dr != 0) ^ (dc != 0)) {
						Cell c = new Cell(visiting.r + dr, visiting.c + dc);
						if (existingCells.contains(c) && !visited.contains(c)) {
							candidates.add(c);
						}
					}
				}

			}
			if (candidates.isEmpty()) {
				visiting = toVisit.remove();
			} else {
				toVisit.add(visiting);
				Cell next = candidates.get(rng.nextInt(candidates.size()));
				edgeId = connectCellsBothWays(visiting, next, edgeId, maze);
				visiting = next;
			}
		}
		return edgeId;
	}

	public static void main(String[] args) throws IOException {
		try (InputStream is = args.length == 0 ? System.in : new FileInputStream(new File(args[0]))) {
			DiGraph<Cell, Integer> maze = read(is);
			// : ((args.length == 2) ? makeUnconnectedMaze(Integer.parseInt(args[0]),
			// Integer.parseInt(args[1]))
			// : null);
//			int nbEdges = (args.length == 2) ? connectToCoveringTree(maze) : maze.getEdges().size();
//			int[] nRowsNCols = maxRowMaxCol(maze.getVertices());
//			if (args.length == 2) {
//				maze.nameVertex("A", new Cell(0, 0));
//				maze.nameVertex("B", new Cell(nRowsNCols[0], nRowsNCols[1]));
//			}
			System.out.print(mazeToString(maze));
			/*
			 * System.out.println("A and B are connected ? :" + maze.areConnected("A",
			 * "B")); System.out.println("Shortest path from A to B :" +
			 * maze.shortestPath("A", "B")); System.out.
			 * println("Shortest path from A to B with manhattan distance heuristic :" +
			 * maze.shortestPath("A", "B", new ManhattanDistance()));
			 */
			setupCanvas(maze.getVertices(), Math.max(1024, (int) maze.getEdges().size() * 8),
					Math.max(1024, (int) maze.getVertices().size() * 8));
			Cell[] srcAndDest = new Cell[] { maze.getVertexByName("A"), maze.getVertexByName("B") };
			boolean done = false;
			draw(maze, StdDraw.WHITE, StdDraw.BLACK);
			while (!done) {
//				Cell currentMouseCell = new Cell(-(int) (StdDraw.mouseY() - 0.5), (int) (StdDraw.mouseX() + 0.5));
//				int idx = StdDraw.mousePressed() ? 0 : 1;// mouse pressed for src, otherwise for dest
//				if (!srcAndDest[idx].equals(currentMouseCell)) {
//					srcAndDest[idx] = currentMouseCell;
				draw(maze, StdDraw.WHITE, StdDraw.BLACK);
				Collection<Cell> pathAndVisited = ((AdjacencyDiGraph<Cell, Integer>) maze).shortestPath("A", "B");
				if (!pathAndVisited.isEmpty()) {
					draw(maze, StdDraw.RED, StdDraw.BLACK, pathAndVisited);
					// draw(maze, StdDraw.GREEN, StdDraw.BLACK, pathAndVisited.get(0));
//					}
				}
			}
		}
	}
}