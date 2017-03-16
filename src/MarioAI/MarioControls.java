package MarioAI;

import java.util.List;

import MarioAI.graph.DirectedEdge;
import MarioAI.graph.GraphMath;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MarioControls {

	private static final double MIN_MARIO_SPEED = 0.03;
	private static final int MAX_JUMP_TIME = 8;
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
	
	private static int jumpCounter = 0;
	private static int xAxisCounter = 0;
	private static int movementDirection = 0;
	private static boolean missionSet = false;
	
	private static float oldX = 0;
	private static boolean shouldBeJumping = false;
	private static boolean first = false;
	private static int fallTime = 0;
	
	public static boolean reachedNextNode(Environment observation, final List<DirectedEdge> path) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		DirectedEdge next = path.get(0);
		if (GraphMath.distanceBetween(marioXPos, marioYPos, next.target.x, next.target.y) <= 0.7) {
			path.remove(0);
			next = path.get(0);
			return true;
		}
		return false;
	}
	
	
	public static void getNextAction(Environment observation, final List<DirectedEdge> path, boolean[] action) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		final boolean canJump = observation.mayMarioJump();
		DirectedEdge next = path.get(0);
		
		if (!missionSet && canJump) {
			missionSet = true;
			first = true;
		}

		if (missionSet) {
			if (jumpCounter == 0) {
				float getJumpNodeHeight = next.getMaxY();
				//float jumpTarget = next.source.y - getJumpNodeHeight;
				float jumpHeight = Math.abs(Math.round(marioYPos) - Math.round(getJumpNodeHeight));
				if (getJumpNodeHeight < Math.round(marioYPos)) {
					jumpCounter = getJumpTime(jumpHeight);
					fallTime = getFallingTime(next.target.y - next.getMaxY());
				}
			}
			if (jumpCounter > 0) {
				if (first) {
					shouldBeJumping = true;
					action[Mario.KEY_JUMP] = true;
					jumpCounter--;
					//fallTime--;
					first = false;
				}
				if (shouldBeJumping) {
					if (!observation.isMarioOnGround()) {
						action[Mario.KEY_JUMP] = true;
						jumpCounter--;
						//fallTime--;
					}
					else {
						first = true;
					}
				}
			}
		}
		
		if (missionSet) {
			final float speed = marioXPos - oldX;
			movementDirection = (next.target.x - marioXPos > 0) ? Mario.KEY_RIGHT : Mario.KEY_LEFT;
			xAxisCounter = getXMovementTime(next.target.x - marioXPos, speed, jumpCounter + fallTime);
			if (xAxisCounter > 0) {
				action[movementDirection] = true;
			}
			//xAxisCounter--;
		}		
		oldX = marioXPos;
		missionSet = !(jumpCounter == 0 && xAxisCounter == 0);
	}

	private static int getJumpTime(float neededHeight) {
		for (int i = 0; i < heights.length; i++) {
			if (heights[i] >= neededHeight) {
				return i;
			}
		}
		return MAX_JUMP_TIME;
	}
	
	private static int getXMovementTime(float neededXDistance, float speed, int time) {
		float distanceMoved = 0;
		int steps = 0;
		if (neededXDistance < 0) {
			if (speed > 0) {
				steps = getDeaccelerationNeededSteps(speed);
				distanceMoved = -getDeaccelerationDistanceMoved(speed);
				
				//speed is now 0
				speed = 0;
			}
			while (distanceMoved + getDriftingDistance(speed, time - steps) < -neededXDistance) {
				steps++;
				speed = getNextTickSpeed(-speed);
				distanceMoved += speed;
			}
			return steps;
		}
		else if (neededXDistance > 0) {
			if (speed < 0) {
				steps = getDeaccelerationNeededSteps(-speed);
				distanceMoved = -getDeaccelerationDistanceMoved(-speed);
				
				//speed is now 0
				speed = 0;
			}
			while (distanceMoved + getDriftingDistance(speed, time - steps) < neededXDistance) {
				steps++;
				speed = getNextTickSpeed(speed);
				distanceMoved += speed;
			}
			return steps;
		}
		else {
			return 0;
		}
	}
	
	public static float getNextTickSpeed(float speed) {
		final float a = 0.8899999428459493f;
		final float b = 0.03750000378899981f;
		return a * speed + b;
	}
	
	public static float getSpeedFromSpeedInt(int speed) {
		final double a = -0.340909068708614;
		final double b = -0.116533823678965;
		final double c = 0.340909068708614;
		return (float)(a * Math.exp(b * (double)speed) + c);
	}
	
	public static float getDeaccelerationDistanceMoved(float speed) {
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
		while(speed > MIN_MARIO_SPEED) {
			speed = (float) (a * Math.exp(b * steps)  + c);
			steps++;
		}
		return steps;
	}
	
	public static float getDriftingDistance(float speed, int driftTime) {
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
	
	private static int getFallingTime(float fallingHeight) {
		final double a = 0.08333333333;
		final double b = 2400;
		final double c = -0.7500000000;
		return (int)Math.ceil(a * Math.sqrt(b * fallingHeight + 81) + c);
	}
}
