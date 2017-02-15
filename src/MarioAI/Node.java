package MarioAI;

import java.util.ArrayList;

/**
 * Standard type of node
 */
public class Node extends SuperNode {
	public ArrayList<Node> edges = new ArrayList<Node>();
	public final short x;
	public final short y;
	
	private int hash;
	public byte type;
	public short coloumn;
	public short row;
	
	public Node(short x, short y, short coloumn, short row, byte type)
	{
		this.x = x;
		this.y = y;
		this.coloumn = coloumn;
		this.row = row;
		this.type = type;
	}
	
	public Node(short x, short y) {
		this.x = x;
		this.y = y;
		this.hash = Hasher.hashShortPoint(x, y);
	}
	
	@Override
	public ArrayList<Node> getNeighbors()
	{
		return edges;
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
