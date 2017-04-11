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
	
	public float getNextXSpeedAfterTick(int tick, float currentSpeed, MovementInformation movementInformation) {
		 //If Mario is in the deaccelerating period of the movement:
		if (tick  <= movementInformation.xMoveInfo.ticksDeaccelerating) {
			return MarioControls.getNextTickDeacceleratingSpeed(tick, currentSpeed);
		} //If he is in the accelerating period:
		else if( tick <= movementInformation.xMoveInfo.ticksDeaccelerating + 
						 movementInformation.xMoveInfo.ticksAccelerating) { 
			return MarioControls.getNextTickSpeed(currentSpeed);
		} //else it is simply a mater of "drifting"
		else {
			return MarioControls.getNextTickDriftSpeed(currentSpeed);
		}
	}
	
	public float getNextYSpeedAfterTick(int tick, float ySpeed, float currentJumpHeight, MovementInformation movementInformation) {
		if (this instanceof Running) { //Running gives no change in y position, nor speed, the later being 0.
			return 0;
		} else {
			final float fallTo = target.y;
			
			//Numbers are taken from mario class in the game
			//Used for simulating mario's movement.
			final float yJumpSpeed = 1.9f;
			float jumpTime = 8 - tick + 1; //The decrementation is moved to this part.
			
			if (tick <= movementInformation.getTicksHoldingJump() + 1) { //The "jumping" part.
				//Math derived from mario code
				return (yJumpSpeed * Math.min(jumpTime, 7)) / 16f;
			} else if (currentJumpHeight < fallTo){ //and afterwards
				//The if part is removed.				
				//Math derived from mario code.
				return (ySpeed * 0.85f) - (3f / 16f);			
			}
			return 0;
		}
	}
	
	public boolean hasReachedTarget() {
		return true; //TODO make.
	}
	
	protected abstract int getExtraEdgeHashcode();
	
}
