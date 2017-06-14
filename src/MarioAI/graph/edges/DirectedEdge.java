package MarioAI.graph.edges;

import MarioAI.graph.nodes.Node;
import MarioAI.marioMovement.MovementInformation;

/**
 * The super class of the actions the agent can do. An edge corresponds
 * to a possible movement transition from one node to another (in practise, one block to another) 
 */
public abstract class DirectedEdge {
	public final Node source; 
	public final Node target;
	public final boolean useSuperSpeed;
	protected int hash;
	private MovementInformation moveInfo;
	
	public DirectedEdge(Node source, Node target, boolean useSuperSpeed) {
		this.source = source;
		this.target = target;
		this.useSuperSpeed = useSuperSpeed;
	}
	
	public abstract float getMaxY();
	
	public abstract float getWeight();
	
	protected abstract byte getExtraEdgeHashcode();
		
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
		
	public void setMoveInfo(MovementInformation moveInfo) {
		this.moveInfo = moveInfo;
	}
	
	public MovementInformation getMoveInfo() {
		return moveInfo;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public String toString() {
		return "[" + source.x + " : " + source.y + "]" + 
				   " --> " + 
	          "[" + target.x + " : " + target.y + "]" +
				 " H: " + Math.round(getMaxY()) + "\n";
	}
}
