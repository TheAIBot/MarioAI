package MarioAI;

import java.util.ArrayList;
import java.util.List;

import MarioAI.graph.GraphMath;
import MarioAI.graph.edges.DirectedEdge;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MarioControls {
	
<<<<<<< HEAD
	public static final float ACCEPTED_DEVIATION = 0.0002f;
	
	private static final double MIN_MARIO_SPEED = 0.03;
=======
	private static float vx;
	
>>>>>>> refs/remotes/origin/dev
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
	
	public static boolean reachedNextNode(Environment observation, final List<DirectedEdge> path) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		DirectedEdge next = path.get(0);
		if (GraphMath.distanceBetween(marioXPos, marioYPos, next.target.x, next.target.y) <= 0.2) {
			path.remove(0);
			next = path.get(0);
			vx = path.get(0).getSpeedAfterTraversal(vx);
			return true;
		}
		return false;
	}
	
	
	public static void getNextAction(Environment observation, final List<DirectedEdge> path, boolean[] action) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		final DirectedEdge next = path.get(0);

		if (jumpTime < 0) {
			jumpTime = getJumpTime(next, marioYPos);
		}
		if (jumpTime > 0) {
			action[Mario.KEY_JUMP] = true;
		}
		jumpTime--;
		
	
		final float speed = marioXPos - oldX;
		final int xAxisCounter = getXMovementTime(next.target.x - marioXPos, speed, jumpTime);
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
				nextEdge.target.y > marioYPos &&
				observation.isMarioOnGround());
	}
	
	public static int getJumpTime(DirectedEdge next, float marioYPos) {
		final float getJumpNodeHeight = next.getMaxY();
		if (getJumpNodeHeight < Math.round(marioYPos)) {
			final int jumpCounter = getJumpUpTime(getJumpNodeHeight);
			final int fallTime = getFallingTime(next.target.y - next.getMaxY());
			return jumpCounter + fallTime;
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
	
	public static int getXMovementTime(float neededXDistance, float speed, final int time) {
		float distanceMoved = 0;
		int steps = 0;
		if ((neededXDistance < 0 && speed > 0) ||
			(neededXDistance > 0 && speed < 0)) {
			speed = Math.abs(speed);
			
			if (speed > 0) {
				steps = getDeaccelerationNeededSteps(speed);
				distanceMoved = -getDeaccelerationDistanceMoved(speed);
				
				//speed is now 0
				speed = 0;
			}
		}
		else if (neededXDistance == 0) {
			return 0;
		}
		speed = Math.abs(speed);
		neededXDistance = Math.abs(neededXDistance);
		
		while (neededXDistance - (distanceMoved + getDriftingDistance(speed, time - steps)) > ACCEPTED_DEVIATION) {
			speed = getNextTickSpeed(speed);
			distanceMoved += speed;
			steps++;
		}
		return steps;
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
	
	public static float getDriftingDistance(final float speed, final int driftTime) {
		final double a = speed;
		final double b = -0.11653355831586142;
		final double c = -0.00000056420864292;
		double driftDistance = 0;
		for (int i = 0; i < driftTime; i++) {
			final double lastSpeed = a * Math.exp(b * (i + 1)) + c;
			
			if (lastSpeed <= MIN_MARIO_SPEED) {
				break;
			}
			driftDistance += lastSpeed;
		}
		return (float)driftDistance;
	}
	
	public static float getMaxV() {
		return MAX_X_VELOCITY;
	}

	public static float getXVelocity() {
		return vx;
	}

	public static void setVelocity(float velocity) {
		MarioControls.vx = velocity;
	}
	
}
