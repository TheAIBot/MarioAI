package MarioAI.graph.nodes;

import java.util.ArrayList;
import java.util.HashMap;

import MarioAI.graph.edges.DirectedEdge;

/**
 * Represents every possible type of node being valid input for the A* search algorithm
 */
public abstract class SuperNode {	
	public final HashMap<Integer, DirectedEdge> edgesMap = new HashMap<Integer,DirectedEdge>();
	public final ArrayList<DirectedEdge> edges = new ArrayList<DirectedEdge>();
	private boolean allEdgesMade = false;
	
	public abstract ArrayList<DirectedEdge> getEdges();
	
	public boolean isAllEdgesMade() {
		return allEdgesMade;
	}
	
	public void setIsAllEdgesMade(boolean value) {
		allEdgesMade = value;
	}
}