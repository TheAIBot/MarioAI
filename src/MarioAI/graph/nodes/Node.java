package MarioAI.graph.nodes;

import java.util.ArrayList;
import java.util.HashMap;

import MarioAI.Hasher;
import MarioAI.graph.edges.DirectedEdge;

/**
 * Standard type of node
 */
public class Node {
	public final HashMap<Integer, DirectedEdge> edgesMap = new HashMap<Integer,DirectedEdge>();
	public final ArrayList<DirectedEdge> edges = new ArrayList<DirectedEdge>();
	private boolean allEdgesMade = false;
	public final short x;
	public final short y;
	public final byte type;
	private final int hash;
	
	public Node(short x, short y, byte type) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.hash = Hasher.hashShortPoint(x, y);
	}
	
	public void addEdge(DirectedEdge edge) {
		if (!isConnectingEdge(edge)) {
			this.edgesMap.put(edge.hashCode(), edge);
			this.edges.add(edge);			
		}
	}
	
	public void deleteAllEdges() {
		this.edges.clear();
		this.edgesMap.clear();
	}
	
	public void removeEdge(DirectedEdge edge) {
		if (isConnectingEdge(edge)) {
			this.edgesMap.remove(edge.hashCode(), edge);
			this.edges.remove(edge);			
		} 
	}
	
	public boolean isConnectingEdge(DirectedEdge edge) {
		return (edge != null && edgesMap.containsKey(edge.hashCode()));		
	}	

	@Override
	public boolean equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof Node) {
			final Node bb = (Node) b;
			return bb.hashCode() == hashCode();
		} else {
			return false;
		}
	}
	
	public boolean containsEdgeWithTargetAndType(int xCoordinate, int yCoordinate, DirectedEdge type) {
		for (DirectedEdge directedEdge : edges) {
			if (directedEdge.target.x == xCoordinate && directedEdge.target.y == yCoordinate) {
				if (directedEdge.getClass().equals(type.getClass())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public int getNumberOfEdgesOfType(DirectedEdge type) {
		int count = 0;
		for (DirectedEdge directedEdge : edges) {
			if (directedEdge.getClass().equals(type.getClass())) {
				count++;
			}
		}
		return count;
	}
	
	public ArrayList<DirectedEdge> getEdges() {
		return edges;
	}
	
	public boolean isAllEdgesMade() {
		return allEdgesMade;
	}
	
	public void setIsAllEdgesMade(boolean value) {
		allEdgesMade = value;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return "[" + x + " : " + y + "]";
	}

}
