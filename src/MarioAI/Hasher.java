package MarioAI;

public class Hasher {
	public static int hashShortPoint(short x, short y)
	{
		return x + Short.MAX_VALUE * y;
	}
}
