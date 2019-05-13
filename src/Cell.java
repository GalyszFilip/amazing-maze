class Cell {
	public final int c;
	public final int r;

	public Cell(int row, int column) {
		r = row;
		c = column;
	}

	@Override
	public int hashCode() {
		return c ^ r;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof Cell))
			return false;
		Cell otherCell = (Cell) other;
		return r == otherCell.r && c == otherCell.c;
	}

	@Override
	public String toString() {
		return "[" + r + "," + c + "]";
	}
}