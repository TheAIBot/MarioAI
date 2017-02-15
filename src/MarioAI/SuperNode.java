package MarioAI;

import java.util.ArrayList;
import java.util.List;

<<<<<<< HEAD
/**
 * Represents every possible type of node being valid input for the A* search algorithm
 */
public abstract class SuperNode implements Comparable<Node> {
	public ArrayList<Node> neighbors = new ArrayList<Node>(); // previously called edges
	public int gScore; // g(n) for current node - cost of path from start node to this node
	public int fScore; // f(n) for current node - cost estimate of going through this node on cheapest path to the goal node
	protected static int LARGE_NUMBER = 10000; // temp in place of infinity
	public Node parent; //node we came from - used in A*
=======
public abstract class SuperNode implements Comparable<SuperNode> {
	public int gScore = LARGE_NUMBER; // g(n) for current node
	public int fScore = LARGE_NUMBER; // f(n) for current node
	protected static int LARGE_NUMBER = 10000; // temp in place of infinity
	public Node parent = null; //cameFrom
>>>>>>> refs/remotes/origin/graphoptimizer
	
	public abstract ArrayList<Node> getNeighbors();
	
	public int compareTo(SuperNode o) {
		//replace this with g - f;
		if (this.gScore < o.fScore) return -1;
		if (this.gScore > o.fScore) return 1;
		return 0;
	}
}