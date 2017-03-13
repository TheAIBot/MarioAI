package MarioAI;

import java.util.List;

import MarioAI.graph.DirectedEdge;
import MarioAI.graph.GraphMath;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MarioControls {

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
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		DirectedEdge next = path.get(0);
		if (GraphMath.distanceBetween(marioXPos, marioYPos, next.target.x, next.target.y) <= 0.5) {
			path.remove(0);
			next = path.get(0);
			return true;
		}
		return false;
	}
	
	// TODO Pending implementation of functionality for getting info about
	// movement between nodes in Graph.
	public static void getNextAction(Environment observation, final List<DirectedEdge> path, boolean[] action) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		final boolean canJump = observation.mayMarioJump();
		DirectedEdge next = path.get(0);
		
		if (!missionSet && canJump) {
			jumpCounter = getJumpTime(Math.round(marioYPos) - (next.getMaxY()));
			xAxisCounter = getXMovementTime(next.target.x - marioXPos);
			movementDirection = (next.target.x - marioXPos > 0) ? Mario.KEY_RIGHT : Mario.KEY_LEFT;
			missionSet = true;
		}

		if (jumpCounter > 0) {
			action[Mario.KEY_JUMP] = true;
			jumpCounter--;
		}
		if (xAxisCounter > 0) {
			action[movementDirection] = true;
			xAxisCounter--;
		}
		
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
	
	private static int getXMovementTime(float neededXDistance) {
		if (neededXDistance < 0) {
			float distanceMoved = 0;
			int steps = 0;
			if (xSpeedIndex > 0) {
				steps = getDeaccelerationNeededSteps(xSpeedIndex);
				distanceMoved = -getDeaccelerationDistanceMoved(xSpeedIndex);
				
				//speed is now 0
				xSpeedIndex = 0;
			}
			while (distanceMoved < -neededXDistance) {
				steps++;
				xSpeedIndex--;
				distanceMoved += getDistanceFromSpeedInt(-xSpeedIndex);
			}
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
			while (distanceMoved < neededXDistance) {
				steps++;
				xSpeedIndex++;
				distanceMoved += getDistanceFromSpeedInt(xSpeedIndex);
			}
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
			if (deaccelerationSteps[i][0] < speed && 
				deaccelerationSteps[i][1] > speed) {
				return i;
			}			
		}
		//if it ever happens then it should be visible with this
		return Integer.MAX_VALUE;
	}
	
	private static float getDriftingDistance(int speed, int driftTime) {
		final double a = getDistanceFromSpeedInt(speed);
		final double b = -0.116533779064398;
		double driftDistance = 0;
		for (int i = 0; i < driftTime; i++) {
			driftDistance += a * Math.exp(b * i);
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
