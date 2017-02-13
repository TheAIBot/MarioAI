package MarioAI;

import java.util.*;

/*
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
public class Surface {
	private HashMap<Integer, Integer> edges = new HashMap<Integer, Integer>();
	private HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();

	public void addNode(Node newNode) {
		nodes.put(newNode.hashCode(), newNode);

		for (Node edge : newNode.edges) {
			if (edges.containsKey(edge)) {
				edges.put(edge.hashCode(), edges.get(edge) + 1);
			} else {
				edges.put(edge.hashCode(), 1);
			}
		}
	}

	public boolean containsNodeAt(short x, short y) {
		int hash = Hasher.hashShortPoint(x, y);
		return nodes.containsKey(hash);
	}

	public Node removeNodeAndGet(short x, short y) {
		Node toRemove = nodes.get(Hasher.hashShortPoint(x, y));
		for (Node edge : toRemove.edges) {
			int edgeCount = edges.get(edge.hashCode()) - 1;
			if (edgeCount > 0) {
				edges.put(edge.hashCode(), edgeCount);
			} else {
				edges.remove(edge.hashCode());
			}
		}
		nodes.remove(toRemove.hashCode());
		return toRemove;
	}
}
