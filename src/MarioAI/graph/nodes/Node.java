package MarioAI.graph.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import MarioAI.Hasher;
import MarioAI.astar.Action;
import MarioAI.graph.edges.DirectedEdge;

/**
 * Standard type of node
 */
public class Node extends SuperNode {
	public final short x;
	public final short y;
	public final byte type;
	private final int hash;
	public DirectedEdge ancestorEdge = null;
	
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
		this.edges = new ArrayList<DirectedEdge>();
		this.edgesMap = new HashMap<Integer, DirectedEdge>();
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
	public ArrayList<DirectedEdge> getEdges() {
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

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return "[" + x + " : " + y + "]";
	}

	public List<Action> getPossibleActions() {
		// TODO Auto-generated method stub
		return null;
	}

}
