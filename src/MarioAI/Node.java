package MarioAI;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Standard type of node
 */
public class Node extends SuperNode {
	public final short x;
	public final short y;
	
	private int hash;
	public byte type;

	public Node(short x, short y, byte type) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.hash = Hasher.hashShortPoint(x, y);
	}
	
	public void addNeighbor(Node neighbor) {
		if (!this.isNeighbor(neighbor)) {
			this.neighborMap.put(neighbor.hash, neighbor);
			this.neighbors.add(neighbor);			
		}
	}
	
	public boolean isNeighbor(Node node) {
		return (node != null && neighborMap.containsKey(node.hash));
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
