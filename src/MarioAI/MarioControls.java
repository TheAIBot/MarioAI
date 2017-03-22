package MarioAI;

import java.util.List;
import java.util.Map.Entry;

import MarioAI.graph.DirectedEdge;
import MarioAI.graph.GraphMath;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MarioControls {
	
	private static float vx;
	
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
	private static final int deaccelerationSteps[][] = {
		{Integer.MIN_VALUE, 0},
		{ 1, 2},
		{ 3, 4},
		{ 5, 6},
		{ 7, 11},
		{12, 26},
		{27, Integer.MAX_VALUE}
	};
	
	private static int jumpCounter = 0;
	private static int xAxisCounter = 0;
	private static int movementDirection = 0;
	private static boolean missionSet = false;
	
	private static int xSpeedIndex = 0;

	public static boolean reachedNextNode(Environment observation, final List<DirectedEdge> path) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		DirectedEdge next = path.get(0);
		if (GraphMath.distanceBetween(marioXPos, marioYPos, next.target.x, next.target.y) <= 0.5) {
			path.remove(0);
			next = path.get(0);
			vx = path.get(0).getSpeedAfterTraversal(vx);
			return true;
		}
		return false;
	}
	
	private static float oldX = 0;
	private static boolean shouldBeJumping = false;
	private static boolean first = false;
	
	// TODO Pending implementation of functionality for getting info about
	// movement between nodes in Graph.
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
			jumpCounter = getJumpTime(Math.round(marioYPos) - next.getMaxY());
			if (jumpCounter > 0) {
				if (first) {
					shouldBeJumping = true;
					action[Mario.KEY_JUMP] = true;
					first = false;
				}
				if (shouldBeJumping) {
					if (!observation.isMarioOnGround()) {
						action[Mario.KEY_JUMP] = true;
					}
					else {
						first = true;
					}
				}
				
				//jumpCounter--;	
			}
		}
		
		float xDiff = marioXPos - oldX;
		xSpeedIndex = (xDiff >= 0 ? 1 : -1) *  getSpeedIntFromDistance(Math.abs(xDiff));
		movementDirection = (next.target.x - marioXPos > 0) ? Mario.KEY_RIGHT : Mario.KEY_LEFT;
		
		if (missionSet) {
			int fallTime = getFallingTime(next.target.y - next.getMaxY());
			xAxisCounter = getXMovementTime(next.target.x - marioXPos, jumpCounter + fallTime);
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
	
	private static int getXMovementTime(float neededXDistance, int time) {
		if (neededXDistance < 0) {
			float distanceMoved = 0;
			int steps = 0;
			if (xSpeedIndex > 0) {
				steps = getDeaccelerationNeededSteps(xSpeedIndex);
				distanceMoved = -getDeaccelerationDistanceMoved(xSpeedIndex);
				
				//speed is now 0
				xSpeedIndex = 0;
			}
			while (distanceMoved - getDriftingDistance(xSpeedIndex, time - steps)[0] < -neededXDistance) {
				steps++;
				xSpeedIndex--;
				distanceMoved += getDistanceFromSpeedInt(-xSpeedIndex);
			}
			xSpeedIndex = -getSpeedIntFromDistance(getDriftingDistance(xSpeedIndex, time - steps)[1]);
			return steps;
		}
		else if (neededXDistance > 0) {
			float distanceMoved = 0;
			int steps = 0;
			if (xSpeedIndex < 0) {
				steps = getDeaccelerationNeededSteps(-xSpeedIndex);
				distanceMoved = -getDeaccelerationDistanceMoved(-xSpeedIndex);
				
				//speed is now 0
				xSpeedIndex = 0;
			}
			while (distanceMoved + getDriftingDistance(xSpeedIndex, time - steps)[0] < neededXDistance) {
				steps++;
				xSpeedIndex++;
				distanceMoved += getDistanceFromSpeedInt(xSpeedIndex);
			}
			xSpeedIndex = getSpeedIntFromDistance(getDriftingDistance(xSpeedIndex, time - steps)[1]);
			return steps;
		}
		else {
			return 0;
		}
	}
	
	public static float getDistanceFromSpeedInt(int speed) {
		final double a = -0.340909068708614;
		final double b = -0.116533823678965;
		final double c = 0.340909068708614;
		return (float)(a * Math.exp(b * (double)speed) + c);
	}
	
	public static int getSpeedIntFromDistance(float distance) {
		final double a = -8.581199590;
		final double b = -2.933333524;
		return (int)Math.round(a * Math.log(1 + b * distance));
	}
	
	public static float getDeaccelerationDistanceMoved(int speed) {
		final double a = 0.2606629227512888;
		final double b = 4.161597216697656;
		final double c = -0.342432087168023;
		final double actualSpeed = getDistanceFromSpeedInt(speed);
		//has an average error of 0.0072 in the speed range 5-50
		return (float)(a * Math.exp(b * actualSpeed) + c);
	}
	
	private static int getDeaccelerationNeededSteps(int speed) {
		for (int i = 0; i < deaccelerationSteps.length; i++) {
			if (deaccelerationSteps[i][0] <= speed && 
				deaccelerationSteps[i][1] >= speed) {
				return i;
			}			
		}
		//if it ever happens then it should be visible with this
		return Integer.MAX_VALUE;
	}
	
	public static float[] getDriftingDistance(int speed, int driftTime) {
		final double a = getDistanceFromSpeedInt(speed);
		final double b = -0.11653355831586142;
		final double c = -0.00000056420864292;
		double driftDistance = 0;
		double lastSpeed = a;
		for (int i = 0; i < driftTime; i++) {
			lastSpeed = a * Math.exp(b * (i + 1)) + c;
			//mario stops if his speed is less than 0.03
			final double MIN_MARIO_SPEED = 0.03;
			if (lastSpeed <= MIN_MARIO_SPEED) {
				lastSpeed = 0;
				break;
			}
			driftDistance += lastSpeed;
		}
		return new float[] {(float)driftDistance, (float)lastSpeed};
	}
	
	private static int getFallingTime(float fallingHeight) {
		final double a = 0.08333333333;
		final double b = 2400;
		final double c = -0.7500000000;
		return (int)Math.ceil(a * Math.sqrt(b * fallingHeight + 81) + c);
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
