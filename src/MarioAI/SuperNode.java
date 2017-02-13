package MarioAI;

import java.util.ArrayList;
import java.util.List;

public abstract class SuperNode implements Comparable<Node> {
	public ArrayList<Node> neighbors = new ArrayList<Node>(); //priviously called edges
	public int gScore; // g(n) for current node
	public int fScore; // f(n) for current node
	protected static int LARGE_NUMBER = 10000; // temp in place of infinity
	public Node parent; //cameFrom
	
	public SuperNode() {
		gScore = LARGE_NUMBER;
		gScore = LARGE_NUMBER;
		parent = null;
		neighbors = new ArrayList<Node>();
	}
	
	@Override
	public int compareTo(Node o) {
		if (this.gScore < o.fScore) return -1;
		if (this.gScore > o.fScore) return 1;
		return 0;
	}
	
	public List<Node> getNeighbors() {
		return neighbors;
	}
}
