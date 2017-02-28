package MarioAI.graph;

import MarioAI.Hasher;
import MarioAI.MotionAction;

public abstract class DirectedEdge {
	public Node source; 
	public Node target;
	private int hash;
	
	public DirectedEdge(Node source, Node target) {
		this.source = source;
		this.target = target;
		hash = Hasher.hashEdge(this);
	}
	
	public int hashCode() {
		return hash;
	}
	
	//public abstract int motionTypeID();
	//public abstract int getXAccelleration();
	//public abstract int getYAccelleration();
	//public abstract int getTimespan();
	

	//public abstract void getActionPatern();
	
	public abstract float getMaxY();
}