package MarioAI.marioMovement;

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
	public static final float MAX_X_VELOCITY = 0.351f;
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
	private boolean[] actions = new boolean[Environment.numberOfButtons];
	
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
		return Math.abs(edge.target.x - correctXPos) < MAX_X_VELOCITY;
	}
		
	public boolean[] getNextAction(Environment observation, final List<DirectedEdge> path) {
		if (path != null && path.size() > 0) {			
			DirectedEdge next = path.get(0);
			int movementTime = next.getMoveInfo().getMoveTime();
			if (prevEdge != null && 
				next.equals(prevEdge) &&
				next.getMoveInfo().equals(prevEdge.getMoveInfo()) &&
				movementTime == ticksOnThisEdge + 1) 
			{
				path.remove(0);
				if (path.size() == 0) {
					canUpdatePath = true;
					Arrays.fill(actions, false);
					return actions;
				}
				next = path.get(0);
				movementTime = next.getMoveInfo().getMoveTime();
			}
			
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
			
			return new YMovementInformation(totalTicksJumped, yPositions, pressJumpButton);
		}
		return new YMovementInformation(0, new ArrayList<Float>(), new ArrayList<Boolean>());
	}
	
	private static XMovementInformation getXMovementTime(float neededXDistance, float speed, final int airTime, boolean useSuperSpeed) {
		final ArrayList<Float> xPositions = new ArrayList<Float>();
		final ArrayList<Boolean> pressButton = new ArrayList<Boolean>(); 
		final boolean distanceIsNegative = neededXDistance < 0;
		float distanceMoved = 0;
		int totalTicks = 0;
		//If Mario currently moves the opposite way of the way he should go,
		//he first needs to deaccelerate		
		if ((neededXDistance < 0 && speed > 0) ||
			(neededXDistance > 0 && speed < 0)) {
			speed = Math.abs(speed);
			
			speed = addOnDeaccelerationPositions(speed, xPositions, pressButton, useSuperSpeed);
			
			totalTicks = xPositions.size();
			distanceMoved = xPositions.get(xPositions.size() - 1);		
			
			//because mario has now completely 
			//deaccelerated his speed is now 0
			//speed = 0;
		} else if (neededXDistance == 0) {
			return new XMovementInformation(0, speed, 0, xPositions, pressButton, useSuperSpeed);
		}
		
		//The calculations are independent of the direction:
		speed = Math.abs(speed);
		neededXDistance = Math.abs(neededXDistance) - (MAX_X_VELOCITY / 3);
		
		//move mario until the distance between the neededXDistnce
		//and distance moved is within an accepted deviation.
		float distanceToTarget = neededXDistance - (distanceMoved + getDriftingDistance(speed, airTime - totalTicks));
		while (distanceToTarget > ACCEPTED_DEVIATION) {
			////BLOCK 1////
			speed = getNextTickSpeed(speed, useSuperSpeed);
			distanceMoved += speed;
			xPositions.add(distanceMoved);
			pressButton.add(true);
			totalTicks++;
			////BLOCK 1////
			
			final float oldDistanceToTarget = distanceToTarget;
			distanceToTarget = neededXDistance - (distanceMoved + getDriftingDistance(speed, airTime - totalTicks));
			
			if (Math.abs(distanceToTarget) > Math.abs(oldDistanceToTarget)) {
				break;
			}
		}
		
		final int ticksDrifting = Math.max(0, airTime - totalTicks);
		totalTicks += ticksDrifting;
		
		speed = addOnDriftingPositionsAndReturnLastSpeed(speed, distanceMoved, ticksDrifting, xPositions, pressButton);
		distanceMoved = (xPositions.size() == 0)? 0 : xPositions.get(xPositions.size() - 1);
		
		//move the last tick
		//which should be on ground
		//this allows two jumping edges
		//after each other.
		////BLOCK 1 COPY////
		speed = getNextTickSpeed(speed, useSuperSpeed);
		distanceMoved += speed;
		xPositions.add(distanceMoved);
		pressButton.add(true);
		totalTicks++;
		////BLOCK 1 COPY////			
		
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
	
	private static float addOnDeaccelerationPositions(final float speed, final ArrayList<Float> xPositions, final ArrayList<Boolean> pressButton, final boolean useSuperSpees) {
		final ArrayList<Float> xDeaccelerationPositions = new ArrayList<Float>();
		final float endSpeed = getDeaccelerationPositions(speed, xDeaccelerationPositions, useSuperSpees);
		
		for (int i = 0; i < xDeaccelerationPositions.size(); i++) {
			xPositions.add(-xDeaccelerationPositions.get(i));
			pressButton.add(true);
		}	
		
		return endSpeed;
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
			xPositions.add(xMovement);
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
	
	private static float addOnDriftingPositionsAndReturnLastSpeed(final float speed, float distanceMoved, int driftTime, ArrayList<Float> xPositions, final ArrayList<Boolean> pressButton) {
		final ArrayList<Float> driftPositions = getDriftingPositions(speed, driftTime);
		final float startXPosition = distanceMoved;
		
		for (int i = 0; i < driftPositions.size(); i++) {
			xPositions.add(startXPosition + driftPositions.get(i));
			pressButton.add(false);
		}
		return getLastSpeedDrifting(speed, distanceMoved, driftPositions);
	}
	
	private static ArrayList<Float> getDriftingPositions(final float speed, final int driftTime) {
		final ArrayList<Float> driftPositions = new ArrayList<Float>(); 
		float xMoved = 0;
		for (int i = 0; i < driftTime; i++) {	
			xMoved += getNextDriftingDistance(speed, i);
			driftPositions.add(xMoved);
		}
		return driftPositions;
	}
	
	private static float getNextDriftingDistance(float speed, float ticksDrifting) {
		final float a = speed;
		final float b = -0.11653355831586142f;
		final float c = -0.00000056420864292f;
		final float currentSpeed = (float) (a * Math.exp(b * (ticksDrifting + 1)) + c);
		
		return (currentSpeed <= MIN_MARIO_SPEED) ? 0 : currentSpeed;
	}
	
	private static float getLastSpeedDrifting(float speed, float distanceMoved, ArrayList<Float> driftPositions) {
		if (driftPositions.size() == 0) {
			return speed;
		}
		else if (driftPositions.size() == 1) {
			return driftPositions.get(0);
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
}
