package MarioAI.graph;

import MarioAI.Hasher;
import MarioAI.MotionAction;

public class DirectedEdge implements motionType {
	public Node source; 
	public Node target;
	public MotionAction motion;
	private int hash;
	
	public DirectedEdge(Node source, Node sink, MotionAction motion) {
		this.source = source;
		this.target = sink;
		this.motion = motion;
		hash = Hasher.hashEdge(this);
	}
	
	public int hashCode() {
		return hash;
	}
}
