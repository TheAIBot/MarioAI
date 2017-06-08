package MarioAI.marioMovement;

import java.awt.geom.Point2D;
import java.util.ArrayList;
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
	private final Point2D.Float[] positions;
	
	public MovementInformation(XMovementInformation xMoveInfo, YMovementInformation yMoveInfo) {
		this.xMovedDistance = xMoveInfo.xMovedDistance;
		this.endSpeed = xMoveInfo.endSpeed;
		this.totalTicksXMoved = xMoveInfo.totalTicksXMoved;
		this.useSuperSpeed = xMoveInfo.useSuperSpeed;
		
		this.totalTicksJumped = yMoveInfo.totalTicksJumped;
		
		this.pressXButton = xMoveInfo.pressXButton;
		this.pressYButton = yMoveInfo.pressYButton;
		
		this.positions = getCombinedXYMovementPositions(xMoveInfo.xPositions, yMoveInfo.yPositions, getMoveTime());
		
		if (positions.length == 0) {
			throw new Error("movementinformation with length 0");
		}
	}
	
	private Point2D.Float[] getCombinedXYMovementPositions(ArrayList<Float> x, float[] y, int moveTime) {
		final Point2D.Float[] combinedPositions = new Point2D.Float[moveTime];
		
		for (int i = 0; i < Math.max(x.size(), y.length); i++) {
			float xPos;
			float yPos;
			
			if (x.size() == 0) {
				xPos = 0;
			}
			else if (x.size() <= i) {
				throw new Error("not enough x positions for the movement");
				//xPos = x.get(x.size() - 1);
			}
			else {
				xPos = x.get(i);
			}
			
			if (y.length == 0) {
				yPos = 0;
			}
			else if (y.length <= i) {
				yPos = y[y.length - 1];
			}
			else {
				yPos = y[i];
			}
			
			combinedPositions[i] = new Point2D.Float(xPos, yPos);
		}
		
		return combinedPositions;
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
	
	public Point2D.Float[] getPositions() {
		return positions;
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
		Point2D.Float previousPosition = new Point2D.Float(0, 0);
		final float lastY = positions[positions.length - 1].y;
		for (int i = 0; i < positions.length; i++) { 
			final Point2D.Float currentPosition = positions[i];
			if (world.isColliding(currentPosition, previousPosition, sourceNode, lastY)) {
				return true;
			}
			previousPosition = currentPosition;
		}	
		return false;
	}
	
	public boolean hasCollisions(float startX, float startY, World world) { //The x position should however suffice, as edges only comes from the ground.
		Point2D.Float previousPosition = new Point2D.Float(0, 0);
		final float lastY = positions[positions.length - 1].y;
		for (int i = 0; i < positions.length; i++) { 
			final Point2D.Float currentPosition = positions[i];
			if (world.isColliding(currentPosition, previousPosition, startX, startY, lastY)) {
				return true;
			}
			previousPosition = currentPosition;
		}	
		return false;
	}
}
