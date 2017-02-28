package MarioAI;

import java.util.List;

import MarioAI.graph.Node;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class MarioControls {

	private static final float[] heights = new float[] { 0, 1.632164f, 2.4634132f, 2.9610314f, 3.3680468f, 3.6599998f, 3.9153123f, 4.051875f, 4.15625f };

	private static int jumpCounter = 0;
	// private static float xStartJump = 0;
	// private static boolean isJumping = false;
	// private static int marioOnGroundCounter = 0;

	// TODO Pending implementation of functionality for getting info about
	// movement between nodes in Graph.
	public static boolean[] getNextAction(Environment observation, final List<Node> path) {
		final float marioXPos = MarioMethods.getPreciseCenteredMarioXPos(observation.getMarioFloatPos());
		final float marioYPos = MarioMethods.getPreciseCenteredMarioYPos(observation.getMarioFloatPos());
		final boolean canJump = observation.mayMarioJump();

		final boolean[] action = new boolean[Environment.numberOfButtons];
		final Node next = path.get(0);
		if ((float) next.x > marioXPos) {
			action[Mario.KEY_RIGHT] = true;
		}
		if ((float) next.x < marioXPos) {
			action[Mario.KEY_LEFT] = true;
		}
		if ((Math.abs((float) next.x - marioXPos) > 1 || Math.abs((float) next.y - marioYPos) > 1) && canJump && jumpCounter == 0) {
			// jumpCounter = getJumpTime(marioYPos - next.y);
			jumpCounter = 7;
		}
		if (jumpCounter > 0) {
			jumpCounter--;
			action[Mario.KEY_JUMP] = true;
		}

		return action;
	}

	private static int getJumpTime(float neededHeight) {
		for (int i = 0; i < heights.length; i++) {
			if (heights[i] > neededHeight) {
				return i;
			}
		}
		return heights.length;
	}
}
