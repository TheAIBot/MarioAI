package MarioAI.graph;

import MarioAI.graph.nodes.Node;

/**
 * Class containing auxiliary methods for calculations on the generated route graph
 */
public class GraphMath {
	
	/**
	 * Distance between two nodes.
	 * @param current
	 * @param neighbor
	 * @return distance
	 */
	public static float distanceBetween(Node node1, Node node2) {
		return distanceBetween(node1.x, node1.y, node2.x, node2.y);
	}
	
	public static float distanceBetween(int x1, int y1, int x2, int y2) {
		return (float) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}
	
	public static float distanceBetween(float x1, float y1, int x2, int y2) {
		return (float) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}
	
	
}
