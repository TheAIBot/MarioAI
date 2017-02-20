package MarioAI;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents every possible type of node being valid input for the A* search algorithm
 */
public abstract class SuperNode implements Comparable<SuperNode> {
	public ArrayList<Node> neighbors = new ArrayList<Node>(); // previously called edges
	public int gScore; // g(n) for current node - cost of path from start node to this node
	public int fScore; // f(n) for current node - cost estimate of going through this node on cheapest path to the goal node
	protected static int LARGE_NUMBER = 10000; // temp in place of infinity
	public Node parent; //node we came from - used in A*

	public abstract ArrayList<Node> getNeighbors();
	
	public int compareTo(SuperNode o) {
		return this.gScore - o.fScore;
	}
}