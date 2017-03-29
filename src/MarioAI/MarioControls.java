package MarioAI;

import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.INTERNAL;

import MarioAI.graph.GraphMath;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.Node;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MarioControls {
	
	public static final float ACCEPTED_DEVIATION = 0.0002f;
	
	private static final double MIN_MARIO_SPEED = 0.03;
	private static final int MAX_JUMP_TIME = 8;
	private static final float MAX_X_VELOCITY = 0.35f;
	
	private static final float[] heights = new float[] { 
		0,
		0,
		1.632164f, 
		2.4634132f, 
		2.9610314f, 
		3.3680468f, 
		3.6599998f, 
		3.9153123f, 
		4.051875f, 
		4.15625f 
	};
		
	private static float oldX = 0;
	private static int jumpTime = 0;
	private static float currentXSpeed = 0;
	
	public static boolean reachedNextNode(Environment observation, final List<DirectedEdge> path) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		DirectedEdge next = path.get(0);
		if (GraphMath.distanceBetween(marioXPos, marioYPos, next.target.x, next.target.y) <= 0.2) {
			path.remove(0);
			next = path.get(0);
			return true;
		}
		return false;
	}
	
	
	public static void getNextAction(Environment observation, final List<DirectedEdge> path, boolean[] action) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		final DirectedEdge next = path.get(0);

		if (jumpTime < 0 && observation.isMarioOnGround()) {
			jumpTime = getJumpTime(next, marioYPos);
		}
		if (jumpTime > 0) {
			action[Mario.KEY_JUMP] = true;
		}
		jumpTime--;
		System.out.println(jumpTime);
		
	
		currentXSpeed = marioXPos - oldX;
		final int xAxisCounter = getStepsAndSpeedAfterJump(marioXPos, marioYPos, next.target, next, currentXSpeed, jumpTime).getXMovementTime();
		if (xAxisCounter > 0) {
			final int movementDirection = (next.target.x - marioXPos > 0) ? Mario.KEY_RIGHT : Mario.KEY_LEFT;
			action[movementDirection] = true;
		}
		oldX = marioXPos;
	}
	
	public static boolean isPathInvalid(Environment observation, final List<DirectedEdge> path) {
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		final DirectedEdge nextEdge = path.get(0);
		
		return (nextEdge.target.x == marioXPos &&
				observation.isMarioOnGround() &&
				(nextEdge.target.y > marioYPos ||
				 nextEdge.target.y < marioYPos));
	}
	
	public static int getJumpTime(DirectedEdge next, float marioYPos) {
		return getJumpTime(next.getMaxY(), next.target.y, marioYPos);
	}
	
	public static int getJumpTime(float targetJumpHeight, float targetYPos, float marioYPos) {
		if (targetJumpHeight > 0) {
			final float jumpHeight = targetJumpHeight;
			final float fallTo = targetYPos - marioYPos;
			
			//numbers are taken from mario class in the game
			final float yJumpSpeed = 1.9f;
			float jumpTime = 8;
			float currentJumpHeight = 0;
			int ticksJumped = 0;
			
			float prevYDelta = 0;
			
			//calculate ticks for jumping up to desired height
			for (int i = 0; i < MAX_JUMP_TIME; i++) {
				prevYDelta = (yJumpSpeed * Math.min(jumpTime, 7)) / 16f;
				currentJumpHeight += prevYDelta;
				jumpTime--;
				ticksJumped++;
				if (currentJumpHeight >= jumpHeight) {
					break;
				}
			}
			//calculate ticks for falling down
			if (currentJumpHeight > fallTo) {
				
				while (currentJumpHeight > fallTo) {
					prevYDelta = (prevYDelta * 0.85f) - (3f / 16f);
					currentJumpHeight += prevYDelta;
					if (currentJumpHeight <= fallTo) {
						break;
					}
					ticksJumped++;
				}	
			}
			
			return ticksJumped;
		}
		return 0;
	}

	private static int getJumpUpTime(float neededHeight) {
		for (int i = 0; i < heights.length; i++) {
			if (heights[i] >= neededHeight) {
				return i;
			}
		}
		return MAX_JUMP_TIME;
	}
	
	private static int getFallingTime(final float fallingHeight) {
		final double a = 0.08333333333;
		final double b = 2400;
		final double c = -0.7500000000;
		return (int)Math.ceil(a * Math.sqrt(b * fallingHeight + 81) + c);
	}
	
	public static MovementInformation getStepsAndSpeedAfterJump(DirectedEdge edge, float speed) {
		return getStepsAndSpeedAfterJump(edge.source.x, edge.source.y, edge.source, edge, speed);
	}
	
	public static MovementInformation getStepsAndSpeedAfterJump(float startX, float startY, Node endNode, DirectedEdge edge, float speed) {
		final int jumpTimeInTicks = getJumpTime(edge, startY);
		return getStepsAndSpeedAfterJump(startX, startY, endNode, edge, speed, jumpTimeInTicks);
	}
	
	public static MovementInformation getStepsAndSpeedAfterJump(float startX, float startY, Node endNode, DirectedEdge edge, float speed, int jumpTimeInTicks) {
		Pair<Integer, Float> xMovementInformation = getXMovementTime((float)endNode.x - startX, speed, jumpTimeInTicks);
		return new MovementInformation(xMovementInformation.key, jumpTimeInTicks, xMovementInformation.value);
	}
	
	public static Pair<Integer, Float> getXMovementTime(float neededXDistance, float speed, final int time) {
		float distanceMoved = 0;
		int steps = 0;
		boolean speedIsNegative = speed < 0;
		if ((neededXDistance < 0 && speed > 0) ||
			(neededXDistance > 0 && speed < 0)) {
			speed = Math.abs(speed);
			
			if (speed > 0) {
				steps = getDeaccelerationNeededSteps(speed);
				distanceMoved = -getDeaccelerationDistanceMoved(speed);
				
				//speed is now 0
				speed = 0;
				//speed in future should now be reversed
				speedIsNegative = !speedIsNegative;
			}
		}
		else if (neededXDistance == 0) {
			return new Pair<Integer, Float>(0, speed);
		}
		speed = Math.abs(speed);
		neededXDistance = Math.abs(neededXDistance);
		
		while (neededXDistance - (distanceMoved + getDriftingDistance(speed, time - steps).key.floatValue()) > ACCEPTED_DEVIATION) {
			speed = getNextTickSpeed(speed);
			distanceMoved += speed;
			steps++;
		}
		//get end speed
		speed = getDriftingDistance(speed, time - steps).value.floatValue();
		return new Pair<Integer, Float>(steps, (speedIsNegative)? -1 * speed : speed);
	}
	
	public static float getNextTickSpeed(final float speed) {
		final float a = 0.8899999428459493f;
		final float b = 0.03750000378899981f;
		return a * speed + b;
	}
	
	public static float getDeaccelerationDistanceMoved(final float speed) {
		final double a = 0.2606629227512888;
		final double b = 4.161597216697656;
		final double c = -0.342432087168023;
		//has an average error of 0.0072 in the speed range 5-50
		return (float)(a * Math.exp(b * speed) + c);
	}
	
	private static int getDeaccelerationNeededSteps(float speed) {
		final float a = 0.6714839288108793f;
		final float b = -0.11653327286299346f;
		final float c = -0.3409108568136135f;
		int steps = 0;
		while(speed >= MIN_MARIO_SPEED) {
			speed = (float) (a * Math.exp(b * steps)  + c);
			steps++;
		}
		return steps;
	}
	
	public static Pair<Float, Float> getDriftingDistance(final float speed, final int driftTime) {
		final double a = speed;
		final double b = -0.11653355831586142;
		final double c = -0.00000056420864292;
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
	
	public static float getMaxV() {
		return MAX_X_VELOCITY;
	}

	public static float getXVelocity() {
		return currentXSpeed;
	}	
}
