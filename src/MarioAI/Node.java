package MarioAI;

public class Node extends SuperNode {
	public final short x;
	public final short y;

	public Node(short x, short y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Node)) return false;
		Node node = (Node) o;
		return (this.x == node.x && this.y == node.y);
	}
}
