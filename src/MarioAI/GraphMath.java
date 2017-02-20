package MarioAI;

public class GraphMath {
	
	/**
	 * Distance between two nodes.
	 * @param current
	 * @param neighbor
	 * @return distance
	 */
	public static float distanceBetween(Node node1, Node node2) {
		return (float) Math.sqrt(Math.pow((node2.x - node1.x), 2) + Math.pow((node2.y - node1.y), 2));
	}
	
	
}
