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
			DirectedEdge bb = (DirectedEdge) b;
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
	
	/**
	 * TODO Change assumption of not accelerating during traversal
	 * @param initial velocity v0
	 * @return the time it takes to traverse the edge given an intial velocity
	 */
	public abstract float getTraversedTime(float v0);
	
	public abstract float getSpeedAfterTraversal(float v0);
	
	public Pair<Float, Float> getNextXPostionAndSpeedAfterTick(int tick, Pair<Float, Float> currentTickXPositionAndSpeed,
																				  MovementInformation movementInformation) {
		float newSpeed;//New speed next tick.
		float newXPositon;//New x position next tick.
		 //If Mario is in the deaccelerating period of the movement:
		if (tick  <= movementInformation.xMoveInfo.ticksDeaccelerating) {
			newSpeed = MarioControls.getNextTickDeacceleratingSpeed(tick, currentTickXPositionAndSpeed.value);
		} //If he is in the accelerating period:
		else if( tick <= (movementInformation.xMoveInfo.ticksAccelerating + 
				            movementInformation.xMoveInfo.ticksDeaccelerating)) { 
			newSpeed = MarioControls.getNextTickSpeed(currentTickXPositionAndSpeed.value);
		} //else it is simply a mater of "drifting"
		else newSpeed = MarioControls.getNextTickDriftSpeed(currentTickXPositionAndSpeed.value);
		newXPositon = currentTickXPositionAndSpeed.key + newSpeed;
		return new Pair<Float,Float>(newXPositon,newSpeed);
	}
	
	public Pair<Float, Float> getNextYPostionAndDeltaDistanceAfterTick(int tick, Pair<Float, Float> currentTickYPositionAndDeltaDistance,
			                                                   MovementInformation movementInformation) {
		if (this instanceof Running) { //Running gives no change in y position, nor speed, the later being 0.
			return new Pair<Float, Float>(currentTickYPositionAndDeltaDistance.key, (float) 0);
		} else {
			final float jumpHeight = this.getMaxY();
			final float fallTo = Math.round(source.y) - target.y;
			
			//Numbers are taken from mario class in the game
			//Used for simulating mario's movement.
			final float yJumpSpeed = 1.9f;
			float jumpTime = 8 - tick + 1; //The decrementation is moved to this part.
			float currentJumpHeight = 0;
			float prevYDelta = currentTickYPositionAndDeltaDistance.value;
			
			if (tick <= movementInformation.getTicksHoldingJump()) { //The "jumping" part.
				//Math derived from mario code
				prevYDelta = (yJumpSpeed * Math.min(jumpTime, 7)) / 16f;
				currentJumpHeight += prevYDelta;
			} else { //and afterwards
				//The if part is removed.				
				//Math derived from mario code.
				prevYDelta = (prevYDelta * 0.85f) - (3f / 16f);
				currentJumpHeight += prevYDelta;			
			}
			return new Pair<Float, Float>(currentJumpHeight, prevYDelta);
		}
	}
	
	public boolean hasReachedTarget() {
		return true; //TODO make.
	}
	
	protected abstract int getExtraEdgeHashcode();

	public float getNextXSpeedAfterTick() {
		return 0;
	}
	
}
