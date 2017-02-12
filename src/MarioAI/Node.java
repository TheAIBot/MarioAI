package MarioAI;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node> {
	public ArrayList<Node> edges = new ArrayList<Node>();
	public final short x;
	public final short y;
	public int gScore; // g(n) for current node
	public int fScore; // f(n) for current node
	private static int LARGE_NUMBER = 10000; // temp in place of infinity
	public Node parent; //cameFrom
	private List<Node> neighbors;

	public Node(short x, short y) {
		this.x = x;
		this.y = y;
		gScore = LARGE_NUMBER;
		gScore = LARGE_NUMBER;
		parent = null;
		neighbors = new ArrayList<Node>();
	}
	
	public List<Node> getNeighbors() {
		return neighbors;
	}

	@Override
	public int compareTo(Node o) {
		if (this.gScore < o.fScore) return -1;
		if (this.gScore > o.fScore) return 1;
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Node)) return false;
		Node node = (Node) o;
		return (this.x == node.x && this.y == node.y);
	}
}
