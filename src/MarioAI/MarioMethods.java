package MarioAI;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.engine.sprites.Mario.MODE;

/**
 * Class containing auxiliary getters and setters for info about Mario
 */
public class MarioMethods {

	private static final int BLOCK_PIXEL_SIZE = 16;
	private static final float BLOCK_CENTERING_OFFSET = 0.5f;

	public static int getMarioXPos(final float[] marioPos) {
		return (int) getPreciseMarioXPos(marioPos);
	}

	public static int getMarioYPos(final float[] marioPos) {
		return (int) getPreciseMarioYPos(marioPos);
	}

	public static float getPreciseMarioXPos(final float[] marioPos) {
		return marioPos[0] / BLOCK_PIXEL_SIZE;
	}

	public static float getPreciseMarioYPos(final float[] marioPos) {
		return marioPos[1] / BLOCK_PIXEL_SIZE;
	}

	public static float getPreciseCenteredMarioXPos(final float[] marioPos) {
		return getPreciseMarioXPos(marioPos) - BLOCK_CENTERING_OFFSET;
	}

	public static float getPreciseCenteredMarioYPos(final float[] marioPos) {
		return getPreciseMarioYPos(marioPos) - BLOCK_CENTERING_OFFSET;
	}
	
	public static int getMarioHeightFromMarioMode(int mode) {
		switch (mode) {
		case 0:
			return 1;
		case 1:
			return 2;
		case 2:
			return 2;
		}
		throw new Error("Invalid mario mode given: " + mode);
	}
}