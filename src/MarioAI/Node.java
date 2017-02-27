package MarioAI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Standard type of node
 */
public class Node extends SuperNode {
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
	
	/*
	public void addNeighbor(Node neighbor) {
		if (!isNeighbor(neighbor)) {
			this.neighborMap.put(neighbor.hash, neighbor);
			this.neighbors.add(neighbor);			
		}
	}
	
	public boolean isNeighbor(Node node) {
		return (node != null && neighborMap.containsKey(node.hash));
	}

	@Override
	public ArrayList<Node> getNeighbors() {
		return neighbors;
	}
	*/	
	
	public void addEdge(DirectedEdge edge) {
		if (!this.isConnectingEdge(edge)) {
			this.edgesMap.put(edge.hashCode(), edge);
			this.edges.add(edge);			
		}
	}
	
	public void deleteAllEdges() {
		this.edges = new ArrayList<DirectedEdge>();
		this.edgesMap = new HashMap<Integer, DirectedEdge>();
	}
	
	public void removeEdge(DirectedEdge edge) {
		if (this.isConnectingEdge(edge)) {
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

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return "Node";
	}

}
