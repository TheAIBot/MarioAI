package MarioAI;

import java.util.ArrayList;

public class Node {
	public ArrayList<Node> edges = new ArrayList<Node>();
	public final short x;
	public final short y;

	public Node(short x, short y) {
		this.x = x;
		this.y = y;
	}

	public boolean Equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof Node) {
			Node bb = (Node) b;
			return Hasher.hashShortPoint(bb.x, bb.y) == Hasher.hashShortPoint(
					x, y);
		} else {
			return false;
		}
	}
}
