package MarioAI.graph;

import MarioAI.Hasher;
import MarioAI.MotionAction;

public class DirectedEdge {
	public Node source;
	public Node target;
	public MotionAction motion;
	private int hash;
	public float weight;
	
	public DirectedEdge(Node source, Node sink, MotionAction motion) {
		this.source = source;
		this.target = sink;
		this.motion = motion;
		hash = Hasher.hashEdge(this);
		if (source != null && sink != null) weight = GraphMath.distanceBetween(source, sink);
	}
	
	public DirectedEdge(Node source, Node sink, MotionAction motion, float weight) {
		this.source = source;
		this.target = sink;
		this.motion = motion;
		hash = Hasher.hashEdge(this);
		if (source != null && sink != null) this.weight = GraphMath.distanceBetween(source, sink) + weight;
	}

	public int hashCode() {
		return hash;
	}
	
}
