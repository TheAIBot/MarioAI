package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.nodes.Node;

public abstract class DirectedEdge {
	public Node source; 
	public Node target;
	private int hash;
	
	public DirectedEdge(Node source, Node target) {
		this.source = source;
		this.target = target;
		hash = Hasher.hashEdge(this);
	}
	
	public void reHash() {
		hash = Hasher.hashEdge(this);		
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof DirectedEdge) {
			DirectedEdge bb = (DirectedEdge) b;
			return bb.hashCode() == hashCode();
		} else {
			return false;
		}
	}
	
	//public abstract int motionTypeID();
	//public abstract int getXAccelleration();
	//public abstract int getYAccelleration();
	//public abstract int getTimespan();
	

	//public abstract void getActionPatern();
	
	public abstract float getMaxY();
	
	public abstract float getWeight();
	
	@Override
	public String toString() {
		return "[" + source.x + " : " + source.y + "]" + " --> " + "[" + target.x + " : " + target.y + "]" + "\n";
	}
	
	/**
	 * TODO Change assumption of not accelerating during traversal
	 * @param initial velocity v0
	 * @return the time it takes to traverse the edge given an intial velocity
	 */
	public abstract float getTraversedTime(float v0);
	
	public abstract float getSpeedAfterTraversal(float v0);

	public abstract int getExtraEdgeHashcode();
	
	
}
