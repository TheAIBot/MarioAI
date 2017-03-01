package MarioAI;

import java.util.List;

import MarioAI.graph.DirectedEdge;
import MarioAI.graph.GraphMath;
import MarioAI.graph.Node;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MarioControls {

	private static final float[] heights = new float[] { 
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
	private static final float MAX_X_ACCELERATION = 0.3408022f;
	private static final float[] lengthXAcc = new float[] {
		0.07087493f,
		0.35730147f,
		MAX_X_ACCELERATION
	};
	
	private static int jumpCounter = 0;
	private static int xAxisCounter = 0;
	private static int movementDirection = 0;
	private static boolean missionSet = false;
	
	//max be lengthXAcc.length
	//min be -lengthXAcc.length
	private static int xSpeedIndex = 0;

	// TODO Pending implementation of functionality for getting info about
	// movement between nodes in Graph.
	public static boolean getNextAction(Environment observation, final List<DirectedEdge> path, boolean[] action) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseCenteredMarioYPos(observation.getMarioFloatPos());
		final boolean canJump = observation.mayMarioJump();
		boolean finishedPathSection = false;
		
		DirectedEdge next = path.get(0);
		if (GraphMath.distanceBetween(marioXPos, marioYPos, next.target.x, next.target.y) <= 0.5) {
			path.remove(0);
			next = path.get(0);
			finishedPathSection = true;
		}
<<<<<<< HEAD
		
		if (!missionSet && canJump) {
			jumpCounter = getJumpTime(marioYPos - next.getMaxY());
			xAxisCounter = getXMovementTime(next.target.x - marioXPos);
			movementDirection = (next.target.x - marioXPos > 0) ? Mario.KEY_RIGHT : Mario.KEY_LEFT;
			missionSet = false;
=======
		if ((Math.abs((float) next.x - marioXPos) > 1.5 || Math.abs((float) next.y - marioYPos) > 1.5) && canJump && jumpCounter == 0) {
			// jumpCounter = getJumpTime(marioYPos - next.y);
			jumpCounter = 7;
>>>>>>> dev
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
		
		return finishedPathSection;
	}

	private static int getJumpTime(float neededHeight) {
		for (int i = 0; i < heights.length; i++) {
			if (heights[i] > neededHeight) {
				return i;
			}
		}
		return heights.length;
	}
	
	private static int getXMovementTime(float neededXDistance) {
		if (neededXDistance < 0) {
			float distanceMoved = 0;
			int steps = 0;
			if (xSpeedIndex > 0) {
				for (int i = 0; i < xSpeedIndex; i++) {
					distanceMoved += lengthXAcc[i];
					steps++;
				}
			}
			//speed is now 0
			xSpeedIndex = 0;
			
			for (int i = 0; i < lengthXAcc.length; i++) {
				distanceMoved += lengthXAcc[i];
				steps++;
				if (distanceMoved >= neededXDistance) {
					xSpeedIndex = Math.max(-lengthXAcc.length, -steps);
					return steps;
				}
			}
			while(true) {
				distanceMoved += MAX_X_ACCELERATION;
				steps++;
				if (distanceMoved >= neededXDistance) {
					xSpeedIndex = -lengthXAcc.length;
					return steps;
				}
			}
		}
		else if (neededXDistance > 0) {
			float distanceMoved = 0;
			int steps = 0;
			if (xSpeedIndex < 0) {
				for (int i = 0; i < -xSpeedIndex; i++) {
					distanceMoved += lengthXAcc[i];
					steps++;
				}
			}
			//speed is now 0
			xSpeedIndex = 0;
			
			for (int i = 0; i < lengthXAcc.length; i++) {
				distanceMoved += lengthXAcc[i];
				steps++;
				if (distanceMoved >= neededXDistance) {
					xSpeedIndex = Math.min(lengthXAcc.length, steps);
					return steps;
				}
			}
			while(true) {
				distanceMoved += MAX_X_ACCELERATION;
				steps++;
				if (distanceMoved >= neededXDistance) {
					xSpeedIndex = lengthXAcc.length;
					return steps;
				}
			}
		}
		else {
			return 0;
		}
	}
}
