package MarioAI;

public class Node extends SuperNode {
	public final short x;
	public final short y;
<<<<<<< HEAD
	private final int hash;

	public Node(short x, short y) {
=======

	public Node(short x, short y) {
		super();
>>>>>>> refs/remotes/origin/AStar
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

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Node)) return false;
		Node node = (Node) o;
		return (this.x == node.x && this.y == node.y);
	}
}
