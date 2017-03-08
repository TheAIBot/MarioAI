package MarioAI;

import MarioAI.graph.DirectedEdge;

public class Hasher {
	public static int hashShortPoint(short x, short y)
	{
		return x + Short.MAX_VALUE * y;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param vx
	 * @return
	 */
	public static int hashSpeedNode(short x, short y, float vx) {
		short factor = (short) Math.round(vx * 10); // arbitrarily chosen value
		//h = (a*P1 + b)*P2 + c
		//return (int) ((x*31 + y)*59 + vx * factor);
		return (int) (vx * factor) + x * Short.MAX_VALUE + y * Short.MAX_VALUE * Short.MAX_VALUE;
	}
	
	private static int ll = 0;
	public static int hashEdge(DirectedEdge edge)
	{
		//64 should be greater than the largest y value possible.
		//(*)Check it is unique
		/*if (edge.target == null) {
			return 0;
		} else if (edge.motion == null) {
			
		}
		else return edge.target.x + Short.MAX_VALUE * edge.target.y + Short.MAX_VALUE*64*edge.motion.motionTypeID();
		*/
		byte isJumpEdge = (byte) ((edge instanceof SecondOrderPolynomial) ? 0x80 : 0x0);
		if (edge.target == null) {
			return 0;
		}
		
		int b1 = (((int)edge.target.y & 0x000000ff) | isJumpEdge) << 0;
		int b2 = ((int)edge.target.x  & 0x000000ff) << 8;
		int b3 = ((int)edge.source.y  & 0x000000ff) << 16;
		int b4 = ((int)edge.source.x  & 0x000000ff) << 24;
		
		//return b1 | b2 | b3 | b4;
		return ll++;
	}
	
}
