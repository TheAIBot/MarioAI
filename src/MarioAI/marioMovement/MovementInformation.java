package MarioAI.marioMovement;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MovementInformation {
	//vertical information
	private final int ticksHoldingJump;
	private final int totalTicksJumped;
	
	//horizontal information
	private final float xMovedDistance;
	private final float endSpeed;
	private final int totalTicksXMoved;
	private final int ticksDeaccelerating;
	private final int ticksAccelerating;
	private final int ticksDrifting;
	
	//position information
	private final Point2D.Float[] positions;
	
	public MovementInformation(XMovementInformation xMoveInfo, YMovementInformation yMoveInfo) {
		this.xMovedDistance = xMoveInfo.xMovedDistance;
		this.endSpeed = xMoveInfo.endSpeed;
		this.totalTicksXMoved = xMoveInfo.totalTicksXMoved;
		this.ticksDeaccelerating = xMoveInfo.ticksDeaccelerating;
		this.ticksAccelerating = xMoveInfo.ticksAccelerating;
		this.ticksDrifting = xMoveInfo.ticksDrifting;
		
		this.ticksHoldingJump = yMoveInfo.ticksHoldingJump;
		this.totalTicksJumped = yMoveInfo.totalTicksJumped;
		
		this.positions = getCombinedXYMovementPositions(xMoveInfo.xPositions, yMoveInfo.yPositions, getMoveTime());
	}
	
	private Point2D.Float[] getCombinedXYMovementPositions(ArrayList<Float> x, ArrayList<Float> y, int moveTime) {
		final Point2D.Float[] combinedPositions = new Point2D.Float[moveTime];
		
		for (int i = 0; i < combinedPositions.length; i++) {
			float xPos;
			float yPos;
			
			if (x.size() == 0) {
				xPos = 0;
			}
			else if (x.size() <= i) {
				xPos = x.get(x.size() - 1);
			}
			else {
				xPos = x.get(i);
			}
			
			if (y.size() == 0) {
				yPos = 0;
			}
			else if (y.size() <= i) {
				yPos = y.get(y.size() - 1);
			}
			else {
				yPos = y.get(i);
			}
			
			combinedPositions[i] = new Point2D.Float(xPos, yPos);
		}
		
		return combinedPositions;
	}
	
	private int heldUp = 0;
	private int heldDirection = 0;
	
	public void reset() {
		heldUp = ticksHoldingJump;
		heldDirection = ticksDeaccelerating + ticksAccelerating;
	}
	
	public boolean[] getActionsFromTick(int tick) {
		final boolean[] actions = new boolean[Environment.numberOfButtons];
		
		if (totalTicksXMoved > 0) {
			final int buttonXMovement = xMovedDistance > 0 ? Mario.KEY_RIGHT : Mario.KEY_LEFT;
			if (heldDirection > 0) {
				actions[buttonXMovement] = true;
				heldDirection--;
			}	
		}
		if (totalTicksJumped > 0) {
			if (heldUp > 0) {
				actions[Mario.KEY_JUMP] = true;
				heldUp--;
			}	
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
	
	public int getTicksHoldingJump() {
		return ticksHoldingJump;
	}
	
	public int getTotalTicksJumped() {
		return totalTicksJumped;
	}
	
	public int getMoveTime() {
		return Math.max(totalTicksXMoved, totalTicksJumped);
	}
	
	public Point2D.Float[] getPositions() {
		return positions;
	}
}
