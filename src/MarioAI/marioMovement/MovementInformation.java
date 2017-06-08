package MarioAI.marioMovement;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import MarioAI.CollisionDetection;
import MarioAI.World;
import MarioAI.graph.nodes.StateNode;
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
	private final int originalTicksOfMovement;
	private int ticksOfMovement; //Necessary for stomp to work.
	//position information
	private final Point2D.Float[] positions;
	
	public MovementInformation(XMovementInformation xMoveInfo, YMovementInformation yMoveInfo) {
		this.xMovedDistance = xMoveInfo.xMovedDistance;
		this.endSpeed = xMoveInfo.endSpeed;
		this.totalTicksXMoved = xMoveInfo.totalTicksXMoved;
		this.useSuperSpeed = xMoveInfo.useSuperSpeed;
		this.totalTicksJumped = yMoveInfo.totalTicksJumped;
		this.originalTicksOfMovement = Math.max(totalTicksXMoved, totalTicksJumped); ;
		this.ticksOfMovement = originalTicksOfMovement;
		
		this.pressXButton = new boolean[getMoveTime()];
		for (int i = 0; i < xMoveInfo.pressXButton.size(); i++) {
			pressXButton[i] = xMoveInfo.pressXButton.get(i);
		}
		this.pressYButton = new boolean[getMoveTime()];
		for (int i = 0; i < yMoveInfo.pressYButton.size(); i++) {
			pressYButton[i] = yMoveInfo.pressYButton.get(i);
		}
		
		this.positions = getCombinedXYMovementPositions(xMoveInfo.xPositions, yMoveInfo.yPositions, getMoveTime());
	}
	
	private Point2D.Float[] getCombinedXYMovementPositions(ArrayList<Float> x, ArrayList<Float> y, int moveTime) {
		final Point2D.Float[] combinedPositions = new Point2D.Float[moveTime];
		
		for (int i = 0; i < Math.max(x.size(), y.size()); i++) {
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
		
		actions[Mario.KEY_JUMP] = pressYButton[tick];
		
		return actions;
	}
	
	public float getXMovementDistance() {
		if (ticksOfMovement == originalTicksOfMovement) {
			return xMovedDistance;
		} else return positions[ticksOfMovement-1].x;
	}
	
	public float getEndSpeed() {
		if (ticksOfMovement != originalTicksOfMovement) {
			if (ticksOfMovement == 1) {
				return positions[0].x;
			} else {
				return positions[ticksOfMovement-1].x - positions[ticksOfMovement-2].x;
			}
		} else return endSpeed;
	}
	
	public int getTotalTicksJumped() {
		if (ticksOfMovement != originalTicksOfMovement) {
			return Math.min(ticksOfMovement, totalTicksJumped);
		} else return totalTicksJumped;
	}
	
	public int getMoveTime() {
		return ticksOfMovement;
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
			else if (bb.ticksOfMovement != ticksOfMovement){
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

	public boolean hasCollisions(StateNode sourceNode, World world) { //The x position should however suffice, as edges only comes from the ground.
		Point2D.Float previousPosition = new Point2D.Float(0, 0);
		final float lastYValue = positions[ticksOfMovement - 1].y;
		
		for (int i = 0; i < ticksOfMovement; i++) { //Up to the end of the movement. 
			final Point2D.Float currentPosition = positions[i];
			if (world.isColliding(currentPosition, previousPosition, sourceNode, lastYValue)) {
				return true;
			}
			previousPosition = currentPosition;
		}	
		return false;
	}
	
	public boolean hasCollisions(float startX, float startY, World world) { //The x position should however suffice, as edges only comes from the ground.
		Point2D.Float previousPosition = new Point2D.Float(0, 0);
		final float lastYValue = positions[ticksOfMovement - 1].y;
		for (int i = 0; i < ticksOfMovement; i++) { 
			final Point2D.Float currentPosition = positions[i];
			if (world.isColliding(currentPosition, previousPosition, startX, startY, lastYValue)) {
				return true;
			}
			previousPosition = currentPosition;
		}	
		return false;
	}
	
	public int getTicksOfMovement(){
		return ticksOfMovement;
	}

	public void setStopTime(int tickForCollision) {
		ticksOfMovement = tickForCollision;
	}
}
