package MarioAI.marioMovement;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import MarioAI.MarioMethods;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.RunningEdge;
import MarioAI.graph.nodes.Node;
import ch.idsia.mario.environments.Environment;

public class MarioControls {
	
	public static final float ACCEPTED_DEVIATION = 0.0002f;
	
	private static final double MIN_MARIO_SPEED = 0.03125f; // 0.5 / 16;
	public static final float MAX_X_VELOCITY = 0.6818161f;//0.351f;
	private static final float MARIO_START_X_POS = 2;
		
	private static final int NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS = 5;
	private static final int MAX_JUMP_HEIGHT = 4;
	private static final int LEVEL_HEIGHT = 15;
	private static final int NUMBER_OF_DIFFERENT_JUMPS = (LEVEL_HEIGHT + NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS) * NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS;
	private static final YMovementInformation[] yMovements = new YMovementInformation[NUMBER_OF_DIFFERENT_JUMPS];
	private static boolean hasCreatedYMovements = false;
	
	private int ticksOnThisEdge = 0;
	private float oldX = MARIO_START_X_POS;
	private DirectedEdge prevEdge = null;
	private float currentXSpeed = 0;
	private final boolean[] actions = new boolean[Environment.numberOfButtons];
	
	public boolean canUpdatePath = false;
	
	public MarioControls() {
		if (!hasCreatedYMovements) {
			setupYMovements();
			hasCreatedYMovements = true;
		}
	}
	
	public static void setupYMovements() {
		for (int jumpHeightDifference = -LEVEL_HEIGHT; jumpHeightDifference <= MAX_JUMP_HEIGHT; jumpHeightDifference++) {
			for (int jumpHeight = 0; jumpHeight < NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS; jumpHeight++) {
				final int index = getIndexForYMovement(jumpHeight, jumpHeightDifference);
				yMovements[index] = getJumpTime(jumpHeight, jumpHeightDifference, 0);
			}
		}
	}
		
	public void reset() {
		ticksOnThisEdge = 0;
		oldX = MARIO_START_X_POS;
		prevEdge = null;
		currentXSpeed = 0;
	}
	
	public void update(Environment observation) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		currentXSpeed = marioXPos - oldX;
		oldX = marioXPos;
	}
	
	public static boolean canMarioUseEdge(DirectedEdge edge, float currentXPos, float speed, int ticksJumping) {
		if (edge instanceof RunningEdge) {
			return true;
		}
		final float distanceToMove = edge.target.x - currentXPos;
		
		//If mario moves to the right, he can only jump to the right,
		//the other way around with to the left, and if he stands still,
		//he can go both ways.
		//Note, this works as a limitation in his movement pattern.
		if (!( (distanceToMove < 0 && speed < 0) ||
			   (distanceToMove > 0 && speed > 0) ||
			    speed == 0)) {
			return false;
		}
		float distanceMoved = 0;
		speed = Math.abs(speed);
		for (int i = 0; i < ticksJumping; i++) {
			speed = getNextTickSpeed(speed, edge.useSuperSpeed);
			distanceMoved += speed;
		}
		
		return (distanceMoved >= Math.abs(distanceToMove));
	}
	
	public static boolean canMarioUseJumpEdge(DirectedEdge edge, float correctXPos) {
		return Math.abs(edge.target.x - correctXPos) < MAX_X_VELOCITY / 2;
	}
		
	public boolean[] getNextAction(Environment observation, final List<DirectedEdge> path) {
		if (path != null && path.size() > 0) {			
			final DirectedEdge next = path.get(0);
			final int movementTime = next.getMoveInfo().getMoveTime();
			
			if (prevEdge == null ||
				!next.equals(prevEdge) ||
				!next.getMoveInfo().equals(prevEdge.getMoveInfo())) {
				ticksOnThisEdge = 0;
	 			prevEdge = next;
			}
			else {
				ticksOnThisEdge++;
			}
			
			canUpdatePath = movementTime == ticksOnThisEdge + 1;

			next.getMoveInfo().getActionsFromTick(ticksOnThisEdge, actions);			
			
			if (canUpdatePath) {
				path.remove(0);
			}
		}
		else {
			canUpdatePath = true;
			Arrays.fill(actions, false);
		}
		return actions;
	}
	
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
	
	public static boolean isPathInvalid(Environment observation, final List<DirectedEdge> path) {
		if (path != null && path.size() > 1) {
			final int marioXPos = (int)MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
			final int marioYPos = Math.round(MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()));
			final DirectedEdge nextEdge = path.get(0);
			
			return (nextEdge.target.x == marioXPos &&
					observation.isMarioOnGround() &&
					(nextEdge.target.y > marioYPos ||
					 nextEdge.target.y < marioYPos));
		}
		else {
			return true;
		}
	}
	
	public static MovementInformation getEdgeMovementInformation(DirectedEdge edge, float speed, float xPos) {
		MovementInformation movementInformation = getMovementInformationFromEdge(xPos, edge.source.y, edge.target, edge, speed);
		//if (movementInformation.hasCollisions(edge)) {
		//	//Curses!
		//	return movementInformation;
		//}
		return movementInformation;
	}
	
	private static MovementInformation getMovementInformationFromEdge(float startX, float startY, Node endNode, DirectedEdge edge, float speed) {
		YMovementInformation jumpInfo;
		if (edge instanceof RunningEdge) {
			jumpInfo = getYMovement(0, 0, 0);
		} else {
			jumpInfo = getYMovement((int)Math.round(edge.getMaxY()), edge.source.y, edge.target.y);
		}
		return getMovementInformationFromEdge(startX, startY, endNode.x, speed, jumpInfo, edge.useSuperSpeed);
	}
	
	private static MovementInformation getMovementInformationFromEdge(float startX, float startY, float endX, float speed, YMovementInformation jumpInfo, boolean useSuperSpeed) {
		final XMovementInformation xMovementInfo = getXMovementTime(endX - startX, speed, jumpInfo.totalTicksJumped, useSuperSpeed);
		MovementInformation movementInformation = new MovementInformation(xMovementInfo, jumpInfo);
		return movementInformation;
	}
	
	private static int getIndexForYMovement(int jumpHeight, int jumpHeightDifference) {
		return (jumpHeightDifference + LEVEL_HEIGHT) * NUMBER_OF_DIFFERENT_Y_JUMP_POSITIONS + jumpHeight;
	}
	
	private static YMovementInformation getYMovement(int jumpHeight, int marioYPos, int targetYPos) {
		final int index = getIndexForYMovement(jumpHeight, marioYPos - targetYPos);
		return yMovements[index];
	}
	
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
	
	private static XMovementInformation getXMovementTime(float neededXDistance, float speed, final int airTime, boolean useSuperSpeed) {
		final ArrayList<Float> xPositions = new ArrayList<Float>(8);
		final boolean distanceIsNegative = neededXDistance < 0;
		float distanceMoved = 0;
		int totalTicks = 0;
		//If Mario currently moves the opposite way of the way he should go,
		//he first needs to deaccelerate		
		if ((neededXDistance < 0 && speed > 0) ||
			(neededXDistance > 0 && speed < 0)) {
			speed = Math.abs(speed);
			
			speed = addOnDeaccelerationPositions(speed, xPositions, useSuperSpeed);
			
			totalTicks = xPositions.size();
			distanceMoved = xPositions.get(xPositions.size() - 1);		
			
			//because mario has now completely 
			//deaccelerated his speed is now 0
			//speed = 0;
		} else if (neededXDistance == 0) {
			return new XMovementInformation(0, speed, 0, xPositions, new boolean[0], useSuperSpeed);
		}
		
		//The calculations are independent of the direction:
		speed = Math.abs(speed);
		neededXDistance = Math.abs(neededXDistance);
		
		//move mario until the distance between the neededXDistnce
		//and distance moved is within an accepted deviation.
		float distanceToTarget = neededXDistance - (distanceMoved + getDriftingDistance(speed, airTime - totalTicks));
		while (distanceToTarget > ACCEPTED_DEVIATION) {
			////BLOCK 1////
			final float futureSpeed = getNextTickSpeed(speed, useSuperSpeed);
			final float futureDistanceMoved = distanceMoved + futureSpeed;
			
			final float oldDistanceToTarget = distanceToTarget;
			distanceToTarget = neededXDistance - (futureDistanceMoved + getDriftingDistance(futureSpeed, airTime - totalTicks));
			
			if (Math.abs(distanceToTarget) > Math.abs(oldDistanceToTarget)) {
				break;
			}
			
			speed = futureSpeed;
			distanceMoved = futureDistanceMoved;
			xPositions.add(distanceMoved);
			totalTicks++;
			////BLOCK 1////
			
		}
		final int ticksDrifting = Math.max(0, airTime - totalTicks);
		final int ticksAccelerating = totalTicks;
		totalTicks += ticksDrifting;
		final boolean[] pressButton = new boolean[totalTicks + ((airTime > 0) ? 1 : 0)];
		for (int i = 0; i < ticksAccelerating; i++) {
			pressButton[i] = true;
		}
		
		if (airTime > 0) {
			
			speed = addOnDriftingPositionsAndReturnLastSpeed(speed, distanceMoved, ticksDrifting, xPositions);
			distanceMoved = (xPositions.size() == 0)? 0 : xPositions.get(xPositions.size() - 1);
			
			//move the last tick
			//which should be on ground
			//this allows two jumping edges
			//after each other.
			////BLOCK 1 COPY////
			speed = getNextTickSpeed(speed, useSuperSpeed);
			distanceMoved += speed;
			xPositions.add(distanceMoved);
			pressButton[pressButton.length - 1] = true;
			totalTicks++;
			////BLOCK 1 COPY////		
		}		
		
		//if distance is negative then put sign back on values as it was lost before and
		//turn all points around as the movement is in the wrong direction
		if (distanceIsNegative) {
			speed = -speed;
			distanceMoved = -distanceMoved;
			for (int i = 0; i < xPositions.size(); i++) {
				xPositions.set(i, -xPositions.get(i));
			}
		}

		return new XMovementInformation(distanceMoved, speed, totalTicks, xPositions, pressButton, useSuperSpeed);
	}
	
	private static float addOnDeaccelerationPositions(final float speed, final ArrayList<Float> xPositions, final boolean useSuperSpees) {
		return getDeaccelerationPositions(speed, xPositions, useSuperSpees);		
	}
	
	private static float getDeaccelerationPositions(float speed, final ArrayList<Float> xPositions, final boolean useSuperSpees) {
		float xMovement = 0;
		do {
			if (useSuperSpees) {
				speed = speed * 0.89f - 0.075f;
			}
			else {
				speed = speed * 0.89f - 0.0375f;	
			}
			if (Math.abs(speed) < MIN_MARIO_SPEED) {
				speed = 0;
			}
			xMovement += speed;
			xPositions.add(-xMovement);
		} while (speed >= MIN_MARIO_SPEED);
		return speed;
	}
	
	public static float getNextTickSpeed(final float speed, final boolean useSuperSpees) {
		if (useSuperSpees) {
			return speed * 0.89f + 0.075f;
		}
		else {
			return speed * 0.89f + 0.0375f;	
		}
	}
	
	private static float getDriftingDistance(final float speed, final int driftTime) {
		float driftDistance = 0;
		for (int i = 0; i < driftTime; i++) {				
			driftDistance += getNextDriftingDistance(speed, i);
		}
		return driftDistance;
	}
	
	private static float addOnDriftingPositionsAndReturnLastSpeed(final float speed, float distanceMoved, int driftTime, ArrayList<Float> xPositions) {
		final int startSize = xPositions.size();
		getDriftingPositions(speed, driftTime, distanceMoved, xPositions);
		
		return getLastSpeedDrifting(speed, distanceMoved, xPositions,  startSize);
	}
	
	private static void getDriftingPositions(final float speed, final int driftTime, final float startXPosition, final ArrayList<Float>xPositions) {
		float xMoved = 0;
		for (int i = 0; i < driftTime; i++) {	
			xMoved += getNextDriftingDistance(speed, i);
			xPositions.add(startXPosition + xMoved);
		}
	}
	
	private static float getNextDriftingDistance(float speed, float ticksDrifting) {
		final float a = speed;
		final float b = -0.11653355831586142f;
		final float c = -0.00000056420864292f;
		final float currentSpeed = (float) (a * Math.exp(b * (ticksDrifting + 1)) + c);
		
		return (currentSpeed <= MIN_MARIO_SPEED) ? 0 : currentSpeed;
	}
	
	private static float getLastSpeedDrifting(float speed, float distanceMoved, ArrayList<Float> driftPositions, int initialSize) {
		if (driftPositions.size() - initialSize == 0) {
			return speed;
		}
		else if (driftPositions.size() - initialSize == 1) {
			return driftPositions.get(initialSize) - distanceMoved;
		}
		else {
			final float last = driftPositions.get(driftPositions.size() - 1);
			final float secondLast = driftPositions.get(driftPositions.size() - 2);
			return last - secondLast;
		}
	}

	public float getXVelocity() {
		return currentXSpeed;
	}

	public static boolean canMarioUseFallEdge(DirectedEdge ancestorEdge, float xPos) {
		// Not currently
		//TODO make method
		return false;
	}
	
	public boolean[] getActions() {
		return actions;
	}
}
