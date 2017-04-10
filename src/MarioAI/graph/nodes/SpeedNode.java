package MarioAI.graph.nodes;

import MarioAI.Hasher;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;

public class SpeedNode implements Comparable<SpeedNode> {
	public final float SCORE_MULTIPLIER = 1024;
	
	public final Node node;
	public final float vx;
	public final SpeedNode parent;
	public final int hash;
	public final DirectedEdge ancestorEdge;
	public final float correctXPos;
	
	public float gScore = 0;
	public float fScore = 0;
	
	public SpeedNode(Node node, float vx, SpeedNode parent, DirectedEdge ancestorEdge, float correctXPos) {
		this.node = node;
		this.vx = vx;
		this.parent = parent;
		this.ancestorEdge =ancestorEdge;
		this.correctXPos = correctXPos;
		this.hash = Hasher.hashSpeedNode(node.x, node.y, vx, ancestorEdge instanceof Running);
	}
	
	@Override
	public boolean equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof SpeedNode) {
			SpeedNode bb = (SpeedNode) b;
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
		return (int) ((this.fScore * SCORE_MULTIPLIER) - (o.fScore * SCORE_MULTIPLIER));
	}
	
	@Override
	public String toString() {
		return node.toString();
	}
}
