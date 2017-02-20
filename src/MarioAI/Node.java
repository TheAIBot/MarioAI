package MarioAI;

import java.util.ArrayList;

/**
 * Standard type of node
 */
public class Node extends SuperNode {
	public final short x;
	public final short y;
	
	private int hash;
	public byte type;
	public short coloumn;
	public short row;

	public Node(short x, short y, byte type) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.hash = Hasher.hashShortPoint(x, y);
	}

	@Override
	public ArrayList<Node> getNeighbors() {
		return neighbors;
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
	public String toString() {
		return "Node";
	}

}
