package MarioAI.graph;

public class SpeedNode {
	
	public Node node;
	public float vx;
	public float gScore, fScore;
	public Node parent;
	
	public SpeedNode(Node node, float vx, Node parent) {
		this.node = node;
		this.vx = vx;
		this.parent = parent;
	}

}
