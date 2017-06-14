package MarioAI.graph.nodes;

import java.util.ArrayList;
import java.util.HashMap;

import MarioAI.Hasher;
import MarioAI.graph.edges.DirectedEdge;

/**Represents a given block in level/mario world. Also sometimes Mario himself.
 * Used as a basis for pretty much everything about movements.
 * @author jesper
 */
public class Node {
	//Used to check for duplicate edges and the likes for the node in constant time.
	private final HashMap<Integer, DirectedEdge> edgesMap = new HashMap<Integer, DirectedEdge>();
	public final ArrayList<DirectedEdge> edges = new ArrayList<DirectedEdge>();
	private boolean allEdgesMade = false;
	private final int hash;
	//Depending on the type, it might be a block one can pass through upwards, or not.
	public final byte type; 
	public final int x;
	public final int y;
	
	/** Initializes a node with a given x,y coordinate placement, and with the given type.
	 * @param x The Nodes x position.
	 * @param y The Nodes y position.
	 * @param type The type of the node: includes things like being passable if going upwards, or not.
	 */
	public Node(int x, int y, byte type) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.hash = Hasher.hashIntPoint(x, y);
	}
	
	/**Adds a possible movement edge to the node.
	 * Will ignore duplicates, and will not verify that the source of the edge is equal to the node.
	 * @param edge The edge to add.
	 */
	public void addEdge(DirectedEdge edge) {
		if (!isConnectingEdge(edge)) {
			this.edgesMap.put(edge.hashCode(), edge);
			this.edges.add(edge);			
		}
	}
	/**Clears all edges for the node. Mostly for testing purposes.
	 */
	public void deleteAllEdges() {
		this.edges.clear();
		this.edgesMap.clear();
	}
	/** Removes a given edge representing a possible movement, 
	 *  from its list of possible movement edges.
	 * @param edge
	 */
	public void removeEdge(DirectedEdge edge) {
		if (isConnectingEdge(edge)) {
			this.edgesMap.remove(edge.hashCode(), edge);
			this.edges.remove(edge);			
		} 
	}
	/**Returns whether or not that the given edge is a possible movement edge for the node.
	 * @param edge Given edge of possible movement.
	 * @return
	 */
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
			//Can be done as the hash code is unique,
			//for a given x,y position of the node, and it is always made.
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
	/**Gets whether all movement edges for the node have been made.
	 * If it is so, then there is no need to add new edges!
	 * @return Whether all the edges have been made. 
	 */
	public boolean isAllEdgesMade() {
		return allEdgesMade;
	}
	
	/** Sets whether all edges for the node have been made.
	 * If it is so, then there is no need to add new edges!
	 * @param value Value to set the boolean to.
	 */
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