package MarioAI.marioMovement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import MarioAI.MarioMethods;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.FallEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;
import ch.idsia.mario.environments.Environment;

/**
 * This class is responsible for creating movements and moving 
 * mario according to those movements
 * @author Andreas Gramstrup
 *
 */
public class MarioControls {
	//Due to floating point error a deviation is needed
	public static final float ACCEPTED_DEVIATION = 0.0002f;
	
	//If marios speed is below this value then it must be zero
	private static final double MIN_MARIO_SPEED = 0.03125f; // 0.5 / 16;
	public static final float MAX_X_VELOCITY = 0.6818161f;//0.351f;
	private static final float MARIO_START_X_POS = 2;
		
	private static final int NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS = 5;
	private static final int MAX_JUMP_HEIGHT = 4;
	private static final int LEVEL_HEIGHT = 15;
	private static final int NUMBER_OF_DIFFERENT_JUMPS = (LEVEL_HEIGHT + NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS) * NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS;
	//Stores all the possible YMoveMentInformation's so they only have to be created once
	private static final YMovementInformation[] yMovements = new YMovementInformation[NUMBER_OF_DIFFERENT_JUMPS];
	private static boolean hasCreatedYMovements = false; // Is true if above array has YMovementInforation's in it
	
	//How many ticks mario has moved on this edge
	private int ticksOnThisEdge = 0;
	private float oldX = MARIO_START_X_POS;
	private DirectedEdge prevEdge = null;
	private MovementInformation prevMoveInfo = null;
	//Marios current speed
	private float currentXSpeed = 0;
	//Array of buttons that mario should press
	//Because of the way the game works The same array should always
	//be used because otherwise marios movements in the game
	//will lag behind by 1 tick
	private final boolean[] actions = new boolean[Environment.numberOfButtons];
	
	//Is true if mario has gone over all the ticks on this edge
	public boolean canUpdatePath = false;
	
	public MarioControls() {
		//Create y movements if they haven't been created yet
		if (!hasCreatedYMovements) {
			setupYMovements();
			hasCreatedYMovements = true;
		}
	}
	
	/**
	 * Goes through all the possible combinations of jumps and adds them to the array yMovements
	 */
	public static void setupYMovements() {
		for (int jumpHeightDifference = -LEVEL_HEIGHT; jumpHeightDifference <= MAX_JUMP_HEIGHT; jumpHeightDifference++) {
			for (int jumpHeight = 0; jumpHeight < NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS; jumpHeight++) {
				final int index = getIndexForYMovement(jumpHeight, jumpHeightDifference);
				yMovements[index] = getJumpTime(jumpHeight, jumpHeightDifference, 0);
			}
		}
	}
	
	/**
	 * Reset information in this class
	 */
	public void reset() {
		ticksOnThisEdge = 0;
		oldX = MARIO_START_X_POS;
		prevEdge = null;
		prevMoveInfo = null;
		currentXSpeed = 0;
	}
	
	/**
	 * Updates marios speed
	 * @param observation
	 */
	public void update(Environment observation) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		currentXSpeed = marioXPos - oldX;
		oldX = marioXPos;
	}
	
	/**
	 * Determines if mario can use an edge by looking as his speed position and landing position
	 * @param edge
	 * @param currentXPos
	 * @param speed
	 * @param ticksJumping
	 * @param xMoved
	 * @param xPositions
	 * @return
	 */
	public static boolean canMarioUseEdge(DirectedEdge edge, float currentXPos, float speed, int ticksJumping, float xMoved, float[] xPositions) {
		final float xEndPos = currentXPos + xMoved;
		
		if (edge instanceof RunningEdge) {
			//Edge is only allowed if mario ends within half marios max speed
			//of the center of the target node.
			return xEndPos < edge.target.x + (MAX_X_VELOCITY / 2) &&
				   xEndPos > edge.target.x - (MAX_X_VELOCITY / 2);
		}
		//Edges are made for this but they are not
		//implemented yet
		if (edge instanceof FallEdge) {
			return false;
		}
		
		final float distanceToMove = edge.target.x - currentXPos;
		
		//If mario moves to the right, he can only jump to the right,
		//the other way around with to the left, and if he stands still,
		//he can go both ways.
		//Note, this works as a limitation in his movement pattern when jumping.
		if (!((distanceToMove < 0 && speed < 0) ||
			   (distanceToMove > 0 && speed > 0) ||
			   speed == 0)) {
			return false;
		}
		
		//Can't take the jump if he doesn't jump long enough while in the air
		final float jumpLength = xPositions[ticksJumping - 1];
		if (jumpLength + (MAX_X_VELOCITY / 2) < distanceToMove) {
			return false;
		}
		
		//Edge is only allowed if mario lands within half marios max speed
		//of the center of the target node.
		return xEndPos < edge.target.x + (MAX_X_VELOCITY / 2) &&
			    xEndPos > edge.target.x - (MAX_X_VELOCITY / 2);
	}
	
	/**
	 * Uses the path that mario should follow to chose which button to press
	 * @param observation
	 * @param path
	 * @return Returns a boolen array for the buttons to press
	 */
	public boolean[] getNextAction(Environment observation, final ArrayList<DirectedEdge> path) {
		//can't follow a path if there is not
		if (path != null && path.size() > 0) {			
			final DirectedEdge next = path.get(0);
			final int movementTime = next.getMoveInfo().getMoveTime();
			
			//Path can change between runs of this method so
			//only continue with the old edge if it's the same
			//as the current edge to follow
			if (prevEdge == null ||
				!next.equals(prevEdge) ||
				!next.getMoveInfo().equals(prevMoveInfo)) {
				//Restart with the new edge
				ticksOnThisEdge = 0;
	 			prevEdge = next;
	 			prevMoveInfo = next.getMoveInfo();
			}
			else {
				ticksOnThisEdge++;
			}
			
			//If this is the last movement on this edge then  signal that a new
			//path can be found
			canUpdatePath = movementTime == ticksOnThisEdge + 1;

			next.getMoveInfo().getActionsFromTick(ticksOnThisEdge, actions);			
			
			//If this is the last movement on this edge then remove it from the last
			//as the next time this method is run the first one will be a new one
			if (canUpdatePath) {
				path.remove(0);
			}
		}
		else {
			//If there is no path then don't press any buttons
			//and signal that a new path can be found
			canUpdatePath = true;
			Arrays.fill(actions, false);
		}
		return actions;
	}
	
	/**
	 * Retursn how many ticks it takes to move the specified distance with the specified start speed
	 * @param neededXDistance
	 * @param speed
	 * @return
	 */
	public static int getTicksToTarget(float neededXDistance, float speed) {
		float distanceMoved = 0;
		int ticks = 0;
		while (distanceMoved < neededXDistance) {
			speed = getNextTickSpeed(speed, true);
			distanceMoved += speed;
			ticks++;
		}
		return ticks;
	}
	
	/**
	 * If mario for some reason doesn't follow the path correctly then there is some cases
	 * where it's not possible for mario to follwo the path anymore. This return true if that's the case
	 * @param observation
	 * @param path
	 * @return
	 */
	public static boolean isPathInvalid(Environment observation, final List<DirectedEdge> path) {
		if (path != null && path.size() > 1) {
			final int marioXPos = (int)MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
			final int marioYPos = Math.round(MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()));
			final DirectedEdge nextEdge = path.get(0);
			
			//Mario can't jump straight up and down through a block.
			//If that's currently the case then return true
			return (nextEdge.target.x == marioXPos &&
					observation.isMarioOnGround() &&
					(nextEdge.target.y > marioYPos ||
					 nextEdge.target.y < marioYPos));
		}
		else {
			//Path is also invalid if there is no path or the size of the path is less than 2
			return true;
		}
	}
	
	/**
	 * Creates a MovementInformation from an edge, start speed and marios x position
	 * @param edge
	 * @param speed
	 * @param xPos
	 * @return
	 */
	public static MovementInformation getEdgeMovementInformation(DirectedEdge edge, float speed, float xPos) {
		return getMovementInformationFromEdge(xPos, edge.source.y, edge.target, edge, speed);
	}
	private static MovementInformation getMovementInformationFromEdge(float startX, float startY, Node endNode, DirectedEdge edge, float speed) {
		YMovementInformation jumpInfo;
		//If running then get a y movement where he doesn't jump
		if (edge instanceof RunningEdge) {
			jumpInfo = getYMovement(0, 0, 0);
		} else {
			jumpInfo = getYMovement((int)Math.round(edge.getMaxY()), edge.source.y, edge.target.y);
		}
		return getMovementInformationFromEdge(startX, startY, endNode.x, speed, jumpInfo, edge.useSuperSpeed);
	}
	private static MovementInformation getMovementInformationFromEdge(float startX, float startY, float endX, float speed, YMovementInformation jumpInfo, boolean useSuperSpeed) {
		final XMovementInformation xMovementInfo = getXMovementTime(endX - startX, speed, jumpInfo.totalTicksJumped, useSuperSpeed);
		return new MovementInformation(xMovementInfo, jumpInfo);
	}
	
	/**
	 * Returns an index for the array yMovements from a jumpheight and jump 
	 * height difference between source and target y position
	 * @param jumpHeight
	 * @param jumpHeightDifference
	 * @return
	 */
	private static int getIndexForYMovement(int jumpHeight, int jumpHeightDifference) {
		return (jumpHeightDifference + LEVEL_HEIGHT) * NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS + jumpHeight;
	}
	
	private static YMovementInformation getYMovement(int jumpHeight, int marioYPos, int targetYPos) {
		final int index = getIndexForYMovement(jumpHeight, marioYPos - targetYPos);
		return yMovements[index];
	}
	
	/**
	 * Creates a YMovementInformation from the arguments
	 * @param targetJumpHeight
	 * @param targetYPos
	 * @param marioYPos
	 * @return a YMovementInformation that adheres to the arguments specifications
	 */
	private static YMovementInformation getJumpTime(float targetJumpHeight, float targetYPos, float marioYPos) {
		if (targetJumpHeight > 0) {
			final ArrayList<Float> yPositions = new ArrayList<Float>(); 
			final ArrayList<Boolean> pressJumpButton = new ArrayList<Boolean>();
			//0.0625 is because marios y value isn't an integer but that number less
			//than expected so to jump high enough it has to be added
			final float jumpHeight = targetJumpHeight + 0.0625f;
			final float fallTo = Math.round(marioYPos) + targetYPos;
			
			float currentJumpHeight = 0;
			int totalTicksJumped = 0;
			float prevYDelta = 0;
			
			//Calculate ticks for jumping up to desired height
			for (int jumpTime = 8; jumpTime > 0; jumpTime--) {
				//Math derived from mario code
				prevYDelta = (1.9f * Math.min(jumpTime, 7)) / 16f;
				currentJumpHeight += prevYDelta;
				yPositions.add(currentJumpHeight);
				pressJumpButton.add(true);
				totalTicksJumped++;
				if (currentJumpHeight >= jumpHeight) {
					break;
				}
			}
			//Calculate ticks for falling down			
			while (currentJumpHeight > fallTo) {
				//Math derived from mario code
				prevYDelta = (prevYDelta * 0.85f) - (3f / 16f);
				currentJumpHeight += prevYDelta;
				totalTicksJumped++;
				//end jump height can't be lower than fallTo because that would mean mario is jumping through the target block
				currentJumpHeight = (currentJumpHeight <= fallTo) ? fallTo : currentJumpHeight;
				yPositions.add(currentJumpHeight);
			}
			
			//Convert the two arraylists to arrays
			final boolean[] pressJump = new boolean[pressJumpButton.size()];
			for (int i = 0; i < pressJump.length; i++) {
				pressJump[i] = pressJumpButton.get(i);
			}
			
			final float[] yPos = new float[yPositions.size()];
			for (int i = 0; i < yPos.length; i++) {
				yPos[i] = yPositions.get(i);
			}
			
			return new YMovementInformation(totalTicksJumped, yPos, pressJump);
		}
		return new YMovementInformation(0, new float[0], new boolean[0]);
	}
	
	/**
	 * Creates and returns an XMovementInformation
	 * @param originalNeededXDistance
	 * @param startSpeed
	 * @param airTime
	 * @param useSuperSpeed
	 * @return
	 */
	private static XMovementInformation getXMovementTime(final float originalNeededXDistance, final float startSpeed, final int airTime, final boolean useSuperSpeed) {
		//If there is no need to move on the x axis then return a movement that reflects that
		if (originalNeededXDistance == 0) {
			return new XMovementInformation(0, startSpeed, 0, new float[0], new boolean[0], useSuperSpeed);
		}
		
		//Need to preserve the start speed as it's used later in the method
		float speed = startSpeed;
		
		//The movement is created independent of the speed and distance to move
		//This means that the mivement is created by assuming that mario need to move to the right
		//If this isn't true then all the position will get the opposize value at the end of the function.
		//That way there is no difference between making a movement to the left and right
		
		//If mario needs to change direction then set his speed to the negaive value of his speed
		//as that will be seen as mario moving away from his desired position which is exaclty what should be simulated.
		//Else set to posetive value of the speed
		if ((originalNeededXDistance < 0 && startSpeed > 0) ||
			(originalNeededXDistance > 0 && startSpeed < 0)) {
			speed = -Math.abs(startSpeed);
		}
		else {
			speed = Math.abs(startSpeed);
		}
		
		final boolean distanceIsNegative = originalNeededXDistance < 0;
		float distanceMoved = 0;
		int ticksAccelerating = 0;
		
		//These calculations are independent of the direction.
		//The direction of the movement is added at the end of this method
		final float neededXDistance = Math.abs(originalNeededXDistance);
		boolean moveInLastMovemet = true;
		
		//This first loop is only used to figure out how long the movement is so an array with that length
		//can be created
		
		//Move mario until the distance between the neededXDistance
		//and distance moved is within an accepted deviation.
		float distanceToTarget = neededXDistance - (distanceMoved + getDriftingDistance(speed, airTime - ticksAccelerating));
		while (distanceToTarget > ACCEPTED_DEVIATION) {
			final float futureSpeed = getNextTickSpeed(speed, useSuperSpeed);
			final float futureDistanceMoved = distanceMoved + futureSpeed;
			
			float lastMovementDistance = 0;
			//If mario is jumping then subtract the last movement from the distanceToTarget
			if (airTime > 0) {
				//The math is magic
				float speedAfterDrifting = (float) Math.pow(0.89f, airTime - ticksAccelerating - 1) * futureSpeed;
				//If marios speed is too low the nset to zero like the game does
				speedAfterDrifting = Math.abs(speedAfterDrifting) < MIN_MARIO_SPEED ? 0 : speedAfterDrifting;
				lastMovementDistance = getNextTickSpeed(speedAfterDrifting, useSuperSpeed);
			}
			
			final float oldDistanceToTarget = distanceToTarget;
			//Update distance to target
			distanceToTarget = neededXDistance - (futureDistanceMoved + getDriftingDistance(futureSpeed, airTime - ticksAccelerating - 1));
			distanceToTarget -= lastMovementDistance;
			
			//If the new distance to target is worse than the previous one then stop here as it will only get worse from now on
			if (Math.abs(distanceToTarget) > Math.abs(oldDistanceToTarget) && Math.abs(oldDistanceToTarget) < MAX_X_VELOCITY / 2) {
				break;
			}
			
			speed = futureSpeed;
			distanceMoved = futureDistanceMoved;
			ticksAccelerating++;
			
			//If the movement completely overshot the node then drift for the last movement to try to save the movement
			if (distanceToTarget < -(MAX_X_VELOCITY / 2)) {
				moveInLastMovemet = false;
				break;
			}			
		}
		
		final int ticksDrifting = Math.max(0, airTime - ticksAccelerating);
		final int totalTicks = ticksAccelerating + ticksDrifting + ((airTime > 0) ? 1 : 0);
		final boolean[] pressButton = new boolean[totalTicks];
		final float[] xPositions = new float[totalTicks];
		
		//Reset speed to same as in the beginning
		if ((originalNeededXDistance < 0 && startSpeed > 0) ||
			(originalNeededXDistance > 0 && startSpeed < 0)) {
			speed = -Math.abs(startSpeed);
		}
		else {
			speed = Math.abs(startSpeed);
		}
		
		distanceMoved = 0;
		//As the length of the movement is known the data can now be inserted into the array
		for (int i = 0; i < ticksAccelerating; i++) {
			pressButton[i] = true;
			speed = getNextTickSpeed(speed, useSuperSpeed);
			distanceMoved += speed;
			xPositions[i] = distanceMoved;
		}
		
		//If jumping then add drifting and last movement
		if (airTime > 0) {
			
			speed = addOnDriftingPositionsAndReturnLastSpeed(speed, distanceMoved, ticksDrifting, ticksAccelerating, xPositions);
			//Distance moved is equal to the second last position because the last position in the array
			//is reserved to the last movement
			distanceMoved = xPositions[xPositions.length - 2];
			
			//Move the last tick
			//which should be on ground
			//this allows two jumping edges
			//after each other.
			if (moveInLastMovemet) {
				speed = getNextTickSpeed(speed, useSuperSpeed);
				distanceMoved += speed;
				xPositions[xPositions.length - 1] = distanceMoved;
				pressButton[pressButton.length - 1] = true;	
			}
			else {
				speed = getNextDriftingSpeed(speed);
				distanceMoved += speed;
				xPositions[xPositions.length - 1] = distanceMoved;
				pressButton[pressButton.length - 1] = false;
			}
		}
		
		//If distance is negative then put sign back on values as it was lost before and
		//turn all points around as the movement is in the wrong direction
		if (distanceIsNegative) {
			speed = -speed;
			distanceMoved = -distanceMoved;
			for (int i = 0; i < xPositions.length; i++) {
				xPositions[i] = -xPositions[i];
			}
		}
		
		return new XMovementInformation(distanceMoved, speed, totalTicks, xPositions, pressButton, useSuperSpeed);
	}
	
	/**
	 * Returns the next ticks speed
	 * @param speed
	 * @param useSuperSpees
	 * @return
	 */
	public static float getNextTickSpeed(final float speed, final boolean useSuperSpees) {
		final float c = useSuperSpees ? 0.075f : 0.0375f;
		final float newSpeed = speed * 0.89f + c;
		return Math.abs(newSpeed) < MIN_MARIO_SPEED ? 0f : newSpeed;
	}
	
	/**
	 * Returns the distance driften over a certain amount of ticks and start speed.
	 * @param speed
	 * @param driftTime
	 * @return
	 */
	private static float getDriftingDistance(float speed, final int driftTime) {
		float driftDistance = 0;
		for (int i = 0; i < driftTime; i++) {
			speed = getNextDriftingSpeed(speed);
			driftDistance += speed;
		}
		return driftDistance;
	}
	
	/**
	 * Adds drifting positions to the xPositons array at the starting index equal to startSize.
	 * Returns the speed after drifting.
	 * @param speed
	 * @param distanceMoved
	 * @param driftTime
	 * @param startSize
	 * @param xPositions
	 * @return
	 */
	private static float addOnDriftingPositionsAndReturnLastSpeed(final float speed, float distanceMoved, int driftTime, final int startSize, float[] xPositions) {
		getDriftingPositions(speed, driftTime, distanceMoved, startSize, xPositions);
		return getLastSpeedDrifting(speed, distanceMoved, xPositions,  startSize);
	}
	
	/**
	 * Adds drifting positions to the xPositons array at the starting index equal to startSize.
	 * @param speed
	 * @param driftTime
	 * @param startXPosition
	 * @param startSize
	 * @param xPositions
	 */
	private static void getDriftingPositions(float speed, final int driftTime, final float startXPosition, final int startSize, final float[] xPositions) {
		float xMoved = startXPosition;
		for (int i = startSize; i < startSize + driftTime; i++) {
			speed = getNextDriftingSpeed(speed);
			xMoved += speed;
			xPositions[i] = xMoved;
		}
	}
	
	/**
	 * Returns next drifting speed
	 * @param speed
	 * @return
	 */
	private static float getNextDriftingSpeed(float speed) {
		speed = 0.89f * speed;
		return (speed <= MIN_MARIO_SPEED)? 0 : speed;
	}
	
	/**
	 * Returns the speed after drifting
	 * @param speed
	 * @param distanceMoved
	 * @param driftPositions
	 * @param initialSize
	 * @return
	 */
	private static float getLastSpeedDrifting(float speed, float distanceMoved, final float[] driftPositions, int initialSize) {
		//If no drifting positions was added the return the start speed
		if (driftPositions.length - initialSize == 1) {
			return speed;
		}
		//if only one drifting position was added then the distance moved must be equal to the distance drifted
		else if (driftPositions.length - initialSize == 2) {
			return driftPositions[initialSize] - distanceMoved;
		}
		else {
			//If more than to drifting position was added then return the difference between 
			//the last two drifting position as that's the last speed
			//The last drifting position is equal to the second last index in the array as the last index is reserved
			final float last = driftPositions[driftPositions.length - 2];
			final float secondLast = driftPositions[driftPositions.length - 3];
			return last - secondLast;
		}
	}

	public float getXVelocity() {
		return currentXSpeed;
	}

	public boolean[] getActions() {
		return actions;
	}
}
