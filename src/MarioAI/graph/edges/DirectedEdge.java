package MarioAI.graph.edges;

import MarioAI.graph.nodes.Node;

public abstract class DirectedEdge {
	public final Node source; 
	public final Node target;
	protected int hash;
	
	public DirectedEdge(Node source, Node target) {
		this.source = source;
		this.target = target;
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
			final DirectedEdge bb = (DirectedEdge) b;
			return bb.hashCode() == hashCode();
		} else {
			return false;
		}
	}
	
	public abstract float getMaxY();
	
	public abstract float getWeight();
	
	@Override
	public String toString() {
		return "[" + source.x + " : " + source.y + "]" + " --> " + "[" + target.x + " : " + target.y + "]" + " H: " + getMaxY();
	}

	protected abstract int getExtraEdgeHashcode();
	
	
}
