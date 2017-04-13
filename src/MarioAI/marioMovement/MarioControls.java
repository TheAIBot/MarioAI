package MarioAI.marioMovement;

import java.util.List;

import MarioAI.MarioMethods;
import MarioAI.Pair;
import MarioAI.graph.GraphMath;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.nodes.Node;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MarioControls {
	
	public static final float ACCEPTED_DEVIATION = 0.0002f;
	
	private static final double MIN_MARIO_SPEED = 0.03;
	private static final int MAX_JUMP_TIME = 8;
	private static final float MAX_X_VELOCITY = 0.351f;
	private static final float MARIO_START_X_POS = 2f;
		
	private float oldX = MARIO_START_X_POS;
	private int jumpTime = 0;
	private int holdJumpTime = 0;
	private float currentXSpeed = 0;
	
	public void reset() {
		oldX = MARIO_START_X_POS;
		jumpTime = 0;
		holdJumpTime = 0;
		currentXSpeed = 0;
	}
	
	public static boolean reachedNextNode(Environment observation, final List<DirectedEdge> path) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		final DirectedEdge next = path.get(0);
		if (GraphMath.distanceBetween(marioXPos, marioYPos, next.target.x, next.target.y) <= MAX_X_VELOCITY / 2) {
			path.remove(0);
			return true;
		}
		return false;
	}
	
	public static boolean canMarioUseEdge(DirectedEdge edge, float currentXPos, float speed) {
		if (edge instanceof Running) {
			return true;
		} //Else:
		final float distanceToMove = edge.target.x - currentXPos;
		
		//If mario moves to the right, he can only jump to the right,
		//the other way around with to the left, and if he stands still,
		//he can go both ways.
		//Note, this works as a limitation in his movement pattern.
		//TODO maybe do so this isn't neccesary.
		if (!( (distanceToMove < 0 && speed < 0) ||
			   (distanceToMove > 0 && speed > 0) ||
			    speed == 0)) {
			return false;
		}
		final int ticksJumping = getJumpTime(edge, edge.source.y).value;
		float distanceMoved = 0;
		speed = Math.abs(speed);
		for (int i = 0; i < ticksJumping; i++) {
			speed = getNextTickSpeed(speed);
			distanceMoved += speed;
		}
		//+ 0.5f to make it the center of the block instead of the edge
		return (distanceMoved >= Math.abs(distanceToMove));
	}
	
	public static boolean canMarioUseJumpEdge(DirectedEdge edge, float correctXPos) {
		return Math.abs(edge.target.x - correctXPos) < MAX_X_VELOCITY;
	}
		
	public void getNextAction(Environment observation, final List<DirectedEdge> path, boolean[] action) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		final DirectedEdge next = path.get(0);
		
		currentXSpeed = marioXPos - oldX;
		MovementInformation moveInfo;
		
		if (jumpTime <= 0 && next.getMaxY() > 0) {
			moveInfo = getMovementInformationFromEdge(marioXPos, marioYPos, next.target, next, currentXSpeed);
		}
		else {
			moveInfo = getMovementInformationFromEdge(marioXPos, marioYPos, next.target.x, currentXSpeed, holdJumpTime, jumpTime);
		}		
		
		if (jumpTime < 0 && observation.isMarioOnGround()) {
			jumpTime = moveInfo.getTotalTicksJumped();
			holdJumpTime = moveInfo.getTicksHoldingJump();
		}
		if (holdJumpTime > 0) {
			action[Mario.KEY_JUMP] = true;
			holdJumpTime--;
		}
		jumpTime--;	
	
		if (moveInfo.getXMovementTime() > 0) {
			final int movementDirection = (next.target.x - marioXPos > 0) ? Mario.KEY_RIGHT : Mario.KEY_LEFT;
			action[movementDirection] = true;
		}
		oldX = marioXPos;
	}
	
	public static boolean isPathInvalid(Environment observation, final List<DirectedEdge> path) {
		final int marioXPos = (int)MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		final DirectedEdge nextEdge = path.get(0);
		
		return (nextEdge.target.x == marioXPos &&
				observation.isMarioOnGround() &&
				(nextEdge.target.y > marioYPos ||
				 nextEdge.target.y < marioYPos));
	}
	
	public static Pair<Integer, Integer> getJumpTime(DirectedEdge next, float marioYPos) {
		return getJumpTime(next.getMaxY(), next.target.y, marioYPos);
	}
	
	public static Pair<Integer, Integer> getJumpTime(float targetJumpHeight, float targetYPos, float marioYPos) {
		if (targetJumpHeight > 0) {
			final float jumpHeight = targetJumpHeight;
			final float fallTo = Math.round(marioYPos) - targetYPos;
			
			//Numbers are taken from mario class in the game
			//Used for simulating mario's movement.
			final float yJumpSpeed = 1.9f;
			float jumpTime = 8;
			float currentJumpHeight = 0;
			int totalTicksJumped = 0;
			int ticksHoldingJump = 0;
			float prevYDelta = 0;
			
			//Calculate ticks for jumping up to desired height
			for (int i = 0; i < MAX_JUMP_TIME; i++) {
				//Math derived from mario code
				prevYDelta = (yJumpSpeed * Math.min(jumpTime, 7)) / 16f;
				currentJumpHeight += prevYDelta;
				jumpTime--;
				totalTicksJumped++;
				ticksHoldingJump++;
				if (currentJumpHeight >= jumpHeight) {
					break;
				}
			}
			//Calculate ticks for falling down
			if (currentJumpHeight > fallTo) {				
				while (currentJumpHeight > fallTo) {
					//Math derived from mario code
					prevYDelta = (prevYDelta * 0.85f) - (3f / 16f);
					currentJumpHeight += prevYDelta;
					if (currentJumpHeight <= fallTo) {
						break;
					}
					totalTicksJumped++;
				}	
			}
			totalTicksJumped++;
			return new Pair<Integer, Integer>(ticksHoldingJump, totalTicksJumped);
		}
		return new Pair<Integer, Integer>(0, 0);
	}

	public static MovementInformation getStepsAndSpeedAfterJump(DirectedEdge edge, float speed) {
		return getMovementInformationFromEdge(edge.source.x, edge.source.y, edge.target, edge, speed);
	}
	
	public static MovementInformation getMovementInformationFromEdge(float startX, float startY, Node endNode, DirectedEdge edge, float speed) {
		final Pair<Integer, Integer> jumpInfo = getJumpTime(edge, startY);
		final int ticksHoldingUp = jumpInfo.key;
		final int totalTicksJumped = jumpInfo.value;
		return getMovementInformationFromEdge(startX, startY, endNode.x, speed, ticksHoldingUp, totalTicksJumped);
	}
	
	public static MovementInformation getMovementInformationFromEdge(float startX, float startY, float endX, float speed, int ticksHoldingUp, int totalTicksJumped) {
		final XMovementInformation xMovementInfo = getXMovementTime(endX - startX, speed, totalTicksJumped);
		return new MovementInformation(xMovementInfo, ticksHoldingUp, totalTicksJumped);
	}
	
	public static XMovementInformation getXMovementTime(float neededXDistance, float speed, final int airTime) {
		float distanceMoved = 0;
		int steps = 0;
		//If Mario currently moves the opposite way of the way he should go,
		//he first needs to deaccelerate:
		boolean distanceIsNegative = neededXDistance < 0;
		if ((neededXDistance < 0 && speed > 0) ||
			(neededXDistance > 0 && speed < 0)) {
			speed = Math.abs(speed);
			
			if (speed > 0) {
				steps = getDeaccelerationNeededSteps(speed);
				distanceMoved = -getDeaccelerationDistanceMoved(speed);				
				//speed is now 0
				speed = 0;
			}
		} else if (neededXDistance == 0) {
			return new XMovementInformation(0, speed, 0, 0, 0);
		}
		final int ticksDeaccelerating = steps;
		//The calculations are independent of the direction:
		speed = Math.abs(speed);
		neededXDistance = Math.abs(neededXDistance);
		
		//The movement:
		while (neededXDistance - (distanceMoved + getDriftingDistance(speed, airTime - steps).key) > ACCEPTED_DEVIATION) {
			speed = getNextTickSpeed(speed);
			distanceMoved += speed;
			steps++;
		}
		
		//Get end speed
		Pair<Float, Float> driftInfo = getDriftingDistance(speed, airTime - steps);
		speed = driftInfo.value;
		
		//Add drifting (in the air!) distance to distance moved
		distanceMoved += driftInfo.key;
		
		//Put sign back on values as it was lost before
		distanceMoved = (distanceIsNegative)? -1 * distanceMoved : distanceMoved;
		speed         = (distanceIsNegative)? -1 * speed         : speed;
		return new XMovementInformation(distanceMoved, speed, steps, ticksDeaccelerating, steps - ticksDeaccelerating);
	}
			
	public static float getNextTickSpeed(final float speed) {
		final float a = 0.8899999428459493f;
		final float b = 0.03750000378899981f;
		return a * speed + b;
	}
	
	public static float getNextTickDriftSpeed(final float speed) {
		final float a = speed;
		final float b = -0.11653355831586142f;
		final float c = -0.00000056420864292f;		
		final double currentSpeed = a * Math.exp(b) + c;		
		if (currentSpeed <= MIN_MARIO_SPEED) {
			return 0;
		} else return (float) currentSpeed;
	}
	
	public static float getNextTickDeacceleratingSpeed(int tick, final float speed) {
		final float a = 0.6714839288108793f;
		final float b = -0.11653327286299346f;
		final float c = -0.3409108568136135f;
		//Stepwise implementation of the getDeaccelerationNeededSteps method.
		float newSpeed = (float) (a * Math.exp(b * tick)  + c);
		if (newSpeed >= MIN_MARIO_SPEED) {
			return 0;
		} else return newSpeed;			
	}
	
	public static float getDeaccelerationDistanceMoved(final float speed) {
		final float a = 0.2606629227512888f;
		final float b = 4.161597216697656f;
		final float c = -0.342432087168023f;
		//has an average error of 0.0072 in the speed range 5-50
		return (float) (a * Math.exp(b * speed) + c);
	}
	
	private static int getDeaccelerationNeededSteps(float speed) {
		final float a = 0.6714839288108793f;
		final float b = -0.11653327286299346f;
		final float c = -0.3409108568136135f;
		int steps = 0;
		//TODO Can be made into exponential search, reducing it to a log n time complexity, instead of linear.
		while(speed >= MIN_MARIO_SPEED) { 
			speed = (float) (a * Math.exp(b * steps)  + c);
			steps++;
		}
		return steps;
	}
	
	public static Pair<Float, Float> getDriftingDistance(final float speed, final int driftTime) {
		final float a = speed;
		final float b = -0.11653355831586142f;
		final float c = -0.00000056420864292f;
		double driftDistance = 0;
		double lastSpeed = a;
		for (int i = 0; i < driftTime; i++) {
			final double currentSpeed = a * Math.exp(b * (i + 1)) + c;
			
			if (currentSpeed <= MIN_MARIO_SPEED) {
				lastSpeed = 0;
				break;
			}
			lastSpeed = currentSpeed;
			driftDistance += lastSpeed;
		}
		return new Pair<Float, Float>((float)driftDistance, (float)lastSpeed);
	}

	public float getXVelocity() {
		return currentXSpeed;
	}	
}
