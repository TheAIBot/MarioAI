package MarioAI.graph;

import MarioAI.Hasher;

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
	
	public abstract float getWeight();
	
	@Override
	public String toString() {
		return "[" + target.x + " : " + target.y + "]";
	}
	
	/**
	 * TODO Change assumption of not accelerating during traversal
	 * @param initial velocity v0
	 * @return the time it takes to traverse the edge given an intial velocity
	 */
	public abstract float getTraversedTime(float v0);
	
	public abstract float getSpeedAfterTraversal(float v0);
	
}
