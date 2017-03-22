package MarioAI.graph.nodes;

import MarioAI.Hasher;
import MarioAI.graph.edges.DirectedEdge;

public class SpeedNode implements Comparable<SpeedNode> {
	
	public Node node;
	public float vx;
	public float gScore, fScore;
	public SpeedNode parent;
	public int hash;
	public DirectedEdge ancestorEdge;
	
	public SpeedNode(Node node, float vx, SpeedNode parent, DirectedEdge ancestorEdge) {
		this.node = node;
		this.vx = vx;
		this.parent = parent;
		gScore = fScore = 0;
		this.ancestorEdge =ancestorEdge;
		this.hash = Hasher.hashSpeedNode(node.x, node.y, vx);
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
	
	public int compareTo(SpeedNode o) {
		return (int) (this.fScore - o.fScore);
	}

}
