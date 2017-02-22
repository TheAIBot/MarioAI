package MarioAI;

import java.util.List;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MarioControls {
	
	private static float xStartJump = 0;
	private static boolean isJumping = false;
	private static int marioOnGroundCounter = 0;

	// TODO Pending implementation of functionality for getting info about
	// movement between nodes in Graph.
	public static boolean[] getNextAction(Environment observation, final List<Node> path) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		final boolean canJump = observation.mayMarioJump();
		
		final boolean[] action = new boolean[Environment.numberOfButtons];
		final Node next = path.get(0);
		if ((float)next.x > marioXPos) {
			action[Mario.KEY_RIGHT] = true;
		}
		if ((float)next.x < marioXPos) {
			action[Mario.KEY_LEFT] = true;
		}
		if ((float)next.x + 1 != marioXPos && canJump) {
			action[Mario.KEY_JUMP] = true;
			xStartJump = marioXPos;
			isJumping = true;
			marioOnGroundCounter = 0;
		}
		if (isJumping) {
			marioOnGroundCounter = observation.isMarioOnGround() ? ++marioOnGroundCounter : 0;
			final float distanceJumped = marioXPos - xStartJump;
			final float totalJumpDistance = ((float)(next.x - xStartJump)) * 0.6f;
			if (isJumping && distanceJumped <= totalJumpDistance) {
				action[Mario.KEY_JUMP] = true;
			}
			else {
				isJumping = false;
			}
		}
		if (isJumping && marioOnGroundCounter > 10) {
			isJumping = false;
		}
		
		return action;
	}

}
