package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.Pair;
import MarioAI.graph.nodes.Node;
import MarioAI.marioMovement.MarioControls;
import MarioAI.marioMovement.MovementInformation;

public abstract class DirectedEdge {
	public final Node source; 
	public final Node target;
	protected int hash;
	private MovementInformation moveInfo;
	
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
	
	public void setMoveInfo(MovementInformation moveInfo) {
		this.moveInfo = moveInfo;
	}
	
	public MovementInformation getMoveInfo() {
		return moveInfo;
	}
	
	@Override
	public String toString() {
		return "[" + source.x + " : " + source.y + "]" + " --> " + "[" + target.x + " : " + target.y + "]" + " H: " + getMaxY() + " Type: " + this.getClass().toString() + "\n";
	}
		
	public boolean hasReachedTarget() {
		return true; //TODO make.
	}
	
	protected abstract int getExtraEdgeHashcode();
	
}
