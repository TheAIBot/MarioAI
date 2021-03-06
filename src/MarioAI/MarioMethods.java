package MarioAI;

/**
 * Class containing auxiliary getters and setters for info about Mario
 */
public class MarioMethods {

	private static final int BLOCK_PIXEL_SIZE = 16;
	private static final float BLOCK_CENTERING_OFFSET = 0.5f;
	public static final float MARIO_WIDTH = 4f / BLOCK_PIXEL_SIZE;

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

	/**
	 * Returns marios x pos as a float moved half a block to the left
	 * @param marioPos
	 * @return
	 */
	public static float getPreciseCenteredMarioXPos(final float[] marioPos) {
		return getPreciseMarioXPos(marioPos) - BLOCK_CENTERING_OFFSET;
	}

	/**
	 * Returns marios y pos as a float moved half a block to the left
	 * @param marioPos
	 * @return
	 */
	public static float getPreciseCenteredMarioYPos(final float[] marioPos) {
		return getPreciseMarioYPos(marioPos) - BLOCK_CENTERING_OFFSET;
	}
	
	/**
	 * Returns marios height as a float from marios mode
	 * @param mode
	 * @return
	 */
	public static float getMarioHeightFromMarioMode(int mode) {
		//Numbers taken from the games source code.
		switch (mode) {
		case 0:
			return 0.75f;
		case 1:
			return 1.5f;
		case 2:
			return 1.5f;
		}
		throw new Error("Invalid mario mode given: " + mode);
	}
	
	public static int getMarioLives(int mode) {
		return mode + 1;
	}
}