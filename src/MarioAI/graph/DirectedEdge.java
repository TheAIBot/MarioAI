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
	public float getTraversedTime(float vx) {
		// formula: time = distance / velocity
		//return (target.x - source.x) / vx;
		return (float) (-8.58 * Math.log(-2.93 * vx + 1));
	}
	
	public float getSpeedAfterTraversal(float vx) {
		// v = a * t
		return (float) (0.34-0.34 * Math.exp(-0.12 * getTraversedTime(vx))); //a * getTraversedTime(vx, a); //TODO check
	}
	
}
