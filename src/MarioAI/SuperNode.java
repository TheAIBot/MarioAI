package MarioAI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Represents every possible type of node being valid input for the A* search algorithm
 */
public abstract class SuperNode implements Comparable<SuperNode> {
	//public HashMap<Integer, Node> neighborMap = new HashMap<Integer,Node>();
	//public ArrayList<Node> neighbors = new ArrayList<Node>(); // previously called edges
	
	public HashMap<Integer, DirectedEdge> edgesMap = new HashMap<Integer,DirectedEdge>();
	public ArrayList<DirectedEdge> edges = new ArrayList<DirectedEdge>();
	
	public float gScore; // g(n) for current node - cost of path from start node to this node
	public float fScore; // f(n) for current node - cost estimate of going through this node on cheapest path to the goal node
	protected static int LARGE_NUMBER = 10000; // temp in place of infinity
	public Node parent; //node we came from - used in A*

	//public abstract ArrayList<Node> getNeighbors();
	

	public abstract ArrayList<DirectedEdge> getEdges();
	
	public int compareTo(SuperNode o) {
		return (int) (this.fScore - o.fScore); //(this.gScore - o.fScore)
	}
	
}