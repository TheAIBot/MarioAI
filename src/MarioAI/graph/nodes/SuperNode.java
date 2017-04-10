package MarioAI.graph.nodes;

import java.util.ArrayList;
import java.util.HashMap;

import MarioAI.graph.edges.DirectedEdge;

/**
 * Represents every possible type of node being valid input for the A* search algorithm
 */
public abstract class SuperNode implements Comparable<SuperNode> {	
	public final HashMap<Integer, DirectedEdge> edgesMap = new HashMap<Integer,DirectedEdge>();
	public final ArrayList<DirectedEdge> edges = new ArrayList<DirectedEdge>();
	private boolean allEdgesMade = false;
	
	public float gScore; // g(n) for current node - cost of path from start node to this node
	public float fScore; // f(n) for current node - cost estimate of going through this node on cheapest path to the goal node
	public Node parent; //node we came from - used in A*
	
	public final float SCORE_MULTIPLIER = 1024;
	
	//public abstract ArrayList<Node> getNeighbors();
	
	public abstract ArrayList<DirectedEdge> getEdges();
	
	public int compareTo(SuperNode o) {
		return (int) (o.fScore * SCORE_MULTIPLIER - this.fScore * SCORE_MULTIPLIER);
	}
	
	public boolean isAllEdgesMade() {
		return allEdgesMade;
	}
	
	public void setIsAllEdgesMade(boolean value) {
		allEdgesMade = value;
	}
}