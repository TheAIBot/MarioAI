package MarioAI;

import java.util.ArrayList;
import java.util.List;

public abstract class SuperNode implements Comparable<SuperNode> {
	public int gScore = LARGE_NUMBER; // g(n) for current node
	public int fScore = LARGE_NUMBER; // f(n) for current node
	protected static int LARGE_NUMBER = 10000; // temp in place of infinity
	public Node parent = null; //cameFrom
	
	public abstract ArrayList<Node> getNeighbors();
	
	public int compareTo(SuperNode o) {
		//replace this with g - f;
		if (this.gScore < o.fScore) return -1;
		if (this.gScore > o.fScore) return 1;
		return 0;
	}
}