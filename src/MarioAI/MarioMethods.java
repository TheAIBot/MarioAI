package MarioAI;

public class MarioMethods {
	
	private static final int BLOCK_PIXEL_SIZE = 16;
	
	public static int getMarioXPos(final float[] marioPos)
	{
		return (int)(marioPos[0] / BLOCK_PIXEL_SIZE);
	}
	
	public static int getMarioYPos(final float[] marioPos)
	{
		return (int)(marioPos[1] / BLOCK_PIXEL_SIZE);
	}
	
	public static float getPreciseMarioXPos(final float[] marioPos)
	{
		return marioPos[0] / BLOCK_PIXEL_SIZE;
	}
	
	public static float getPreciseMarioYPos(final float[] marioPos)
	{
		return marioPos[1] / BLOCK_PIXEL_SIZE;
	}
}