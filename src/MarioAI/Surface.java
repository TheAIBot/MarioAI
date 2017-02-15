package MarioAI;

import java.util.*;

/**
 * A surface is a collection of nodes that are horisontally next to eachother
 * By using surfaces instead of nodes any path finding algorithm will be much
 * faster as it only have to deal with one surface instead of multiple individual nodes.
 * 
 * To make a surface the same as a giant node it needs to keep track of all edges from all the
 * nodes in the surface because the surface can go to all of them. When a node is removed from 
 * the surface it has to remove its edges from the surface but only if no other node in the surface
 * has the same edges. By iterating over all nodes in the surface it would take O(nodes * edges) time
 * which is too much so instead the surface keeps count of how many surfaces has the same edge.
 * When a edge has 0 nodes supporting it it's  removed from the surface.
 * This makes adding nodes easy as it just has to check if the node exists in the surface and if it does
 * then add 1 to its count, if not then add the edge to the surface.
 * This makes adding and removing a node from the surface take O(edges) time where edges is the number 
 * of edges the node to add/remove has.
 */
public class Surface extends SuperNode {
	// keeps count on how many nodes have a connection to another node
	private HashMap<Integer, Integer> edgesCount = new HashMap<Integer, Integer>();
	// contains the nodes this surface is made out of
	private HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
	// a list of edges this surface contains for fast access to edges when doing
	// a* and other algorithms
	private ArrayList<Node> edges = new ArrayList<Node>();

	
	public void addNode(Node newNode) {
		nodes.put(newNode.hashCode(), newNode);

		for (Node edge : newNode.edges) {
			if (edgesCount.containsKey(edge.hashCode())) {
				edgesCount.put(edge.hashCode(), edge.hashCode() + 1);
			} else {
				edgesCount.put(edge.hashCode(), 1);
				// an edge is only added to a surfaces edges the first time
				// so it isn't duplicated in the edges list
				edges.add(edge);
			}
		}
	}

	public boolean containsNodeAt(short x, short y) {
		int hash = Hasher.hashShortPoint(x, y);
		return nodes.containsKey(hash);
	}

	public Node removeNodeAndGet(short x, short y) {
		Node toRemove = nodes.get(Hasher.hashShortPoint(x, y));
		nodes.remove(toRemove.hashCode());
		for (Node edge : toRemove.edges) {
			int edgeCount = edgesCount.get(edge.hashCode()) - 1;
			if (edgeCount > 0) {
				edgesCount.put(edge.hashCode(), edgeCount);
			} else {
				edgesCount.remove(edge.hashCode());
				edges.remove(edge);
			}
		}
		return toRemove;
	}

	@Override
	public ArrayList<Node> getNeighbors() {
		return edges;
	}
}
