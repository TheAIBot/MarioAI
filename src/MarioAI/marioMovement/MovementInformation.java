package MarioAI.marioMovement;

import java.awt.geom.Point2D;
import java.util.Arrays;

import MarioAI.World;
import MarioAI.graph.nodes.SpeedNode;
import ch.idsia.mario.engine.sprites.Mario;

public class MovementInformation{
	//vertical information
	private final int totalTicksJumped;
	
	//horizontal information
	private final float xMovedDistance;
	private final float endSpeed;
	private final int totalTicksXMoved;
	private final boolean[] pressXButton;
	private final boolean[] pressYButton;
	private final boolean useSuperSpeed;
	
	//position information
	//private final Point2D.Float[] positions;
        private final float[] positionsX;
        private final float[] positionsY;
	
	public MovementInformation(XMovementInformation xMoveInfo, YMovementInformation yMoveInfo) {
		this.xMovedDistance = xMoveInfo.xMovedDistance;
		this.endSpeed = xMoveInfo.endSpeed;
		this.totalTicksXMoved = xMoveInfo.totalTicksXMoved;
		this.useSuperSpeed = xMoveInfo.useSuperSpeed;
		
		this.totalTicksJumped = yMoveInfo.totalTicksJumped;
		
		this.pressXButton = xMoveInfo.pressXButton;
		this.pressYButton = yMoveInfo.pressYButton;
                
		if (xMoveInfo.xPositions.length > yMoveInfo.yPositions.length) {
	        this.positionsX = xMoveInfo.xPositions;
	        this.positionsY = new float[this.positionsX.length];
	        
	        if (yMoveInfo.yPositions.length > 0) {
	            for (int i = 0; i < yMoveInfo.yPositions.length; i++) {
	                this.positionsY[i] = yMoveInfo.yPositions[i];
	            }
	            for (int i = yMoveInfo.yPositions.length; i < this.positionsX.length; i++) {
	                this.positionsY[i] = yMoveInfo.yPositions[yMoveInfo.yPositions.length - 1];
	            }
	        }
		}
		else {
			this.positionsX = new float[yMoveInfo.yPositions.length];
			this.positionsY = yMoveInfo.yPositions;
		}
	}
	
	public boolean[] getActionsFromTick(int tick, boolean[] actions) {
		if (tick < 0 || tick >= getMoveTime()) {
			throw new Error("Invalid tick given: " + tick);
		}
		
		if (xMovedDistance > 0) {
			actions[Mario.KEY_RIGHT] = pressXButton[tick];
			actions[Mario.KEY_LEFT] = false;
			actions[Mario.KEY_SPEED] = useSuperSpeed;//pressXButton[tick];
		}
		else if (xMovedDistance < 0) {
			actions[Mario.KEY_RIGHT] = false;
			actions[Mario.KEY_LEFT] = pressXButton[tick];
			actions[Mario.KEY_SPEED] = useSuperSpeed;//pressXButton[tick];
		}
		else {
			actions[Mario.KEY_RIGHT] = false;
			actions[Mario.KEY_LEFT] = false;
			actions[Mario.KEY_SPEED] = false;
		}
		
		if (pressYButton.length > tick) {
			actions[Mario.KEY_JUMP] = pressYButton[tick];	
		}
		else {
			actions[Mario.KEY_JUMP] = false;
		}
		
		return actions;
	}
	
	public int getXMovementTime() {
		return totalTicksXMoved;
	}
	
	public float getXMovementDistance() {
		return xMovedDistance;
	}
	
	public float getEndSpeed() {
		return endSpeed;
	}
	
	public int getTotalTicksJumped() {
		return totalTicksJumped;
	}
	
	public int getMoveTime() {
		return Math.max(totalTicksXMoved, totalTicksJumped);
	}
	
	public float[] getXPositions() {
		return positionsX;
	}
        
        public float[] getYPositions() {
		return positionsY;
	}

	public boolean[] getPressXButton() {
		return pressXButton;
	}
	
	public boolean[] getPressYButton() {
		return pressYButton;
	}
	
	@Override
	public boolean equals(Object b) {
		if (b == null) {
			return false;
		}
		if (b instanceof MovementInformation) {
			final MovementInformation bb = (MovementInformation) b;
			if (bb.endSpeed != endSpeed) {
				return false;
			}
			else if (bb.xMovedDistance != xMovedDistance) {
				return false;
			}
			else if (bb.totalTicksJumped != totalTicksJumped) {
				return false;
			}
			else if (bb.totalTicksXMoved != totalTicksXMoved) {
				return false;
			}
			else if (bb.useSuperSpeed != useSuperSpeed) {
				return false;
			}
			else if (!Arrays.equals(bb.pressXButton, pressXButton)) {
				return false;
			}
			else if (!Arrays.equals(bb.pressYButton, pressYButton)) {
				return false;
			}
			else {
				return true;
			}
		} else {
			return false;
		}
	}

	public boolean hasCollisions(SpeedNode sourceNode, World world) { //The x position should however suffice, as edges only comes from the ground.		
		float previousPositionX = 0;
		float previousPositionY = 0;
		final float lastY = positionsY[positionsY.length - 1];
		for (int i = 0; i < positionsX.length; i++) { 
			if (world.isColliding(positionsX[i], positionsY[i], previousPositionX, previousPositionY, sourceNode, lastY)) {
				return true;
			}
			previousPositionX = positionsX[i];
                        previousPositionY = positionsY[i];
		}	
		return false;
	}
	
	public boolean hasCollisions(float startX, float startY, World world) { //The x position should however suffice, as edges only comes from the ground.
		float previousPositionX = 0;
                float previousPositionY = 0;
		final float lastY = positionsY[positionsY.length - 1];
		for (int i = 0; i < positionsX.length; i++) { 
			if (world.isColliding(positionsX[i], positionsY[i], previousPositionX, previousPositionY, startX, startY, lastY)) {
				return true;
			}
			previousPositionX = positionsX[i];
                        previousPositionY = positionsY[i];
		}	
		return false;
	}
}
