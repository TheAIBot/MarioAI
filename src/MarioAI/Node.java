package MarioAI;

import java.util.ArrayList;

public class Node {
	public ArrayList<Node> edges = new ArrayList<Node>();
	public final short x;
	public final short y;
	private final int hash;

	public Node(short x, short y) {
		this.x = x;
		this.y = y;
		this.hash = Hasher.hashShortPoint(x, y);
	}

	@Override
	public boolean equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof Node) {
			Node bb = (Node) b;
			return bb.hashCode() == hashCode();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return hash;
	}
}
