package MarioAI;

import java.util.*;

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
	
	public boolean is

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
