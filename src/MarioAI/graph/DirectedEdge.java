package MarioAI.graph;

import MarioAI.Hasher;

public abstract class DirectedEdge {
	public Node source; 
	public Node target;
	private int hash;
	public float weight;
	
	public DirectedEdge(Node source, Node target) {
		this.source = source;
		this.target = target;
		hash = Hasher.hashEdge(this);
		if (source != null && sink != null) this.weight = GraphMath.distanceBetween(source, sink);
	}
	
	public DirectedEdge(Node source, Node sink, MotionAction motion, float weightAddon) {
		this.source = source;
		this.target = sink;
		this.motion = motion;
		hash = Hasher.hashEdge(this);
		if (source != null && sink != null) this.weight = GraphMath.distanceBetween(source, sink) + weightAddon;
	}

	public int hashCode() {
		return hash;
	}
	
<<<<<<< HEAD
	//public abstract int motionTypeID();
	//public abstract int getXAccelleration();
	//public abstract int getYAccelleration();
	//public abstract int getTimespan();
	

	//public abstract void getActionPatern();
	
	public abstract float getMaxY();
	
	@Override
	public String toString() {
		return "[" + target.x + " : " + target.y + "]";
	}
}
=======
}
>>>>>>> dev
