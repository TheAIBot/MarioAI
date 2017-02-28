package MarioAI;

import MarioAI.graph.DirectedEdge;

public class Hasher {
	public static int hashShortPoint(short x, short y)
	{
		return x + Short.MAX_VALUE * y;
	}
	
	public static int hashEdge(DirectedEdge edge)
	{
		//64 should be greater than the largest y value possible.
		//(*)Check it is unique
		if (edge.target == null) {
			return 0;
		} else if (edge.motion == null) {
			return edge.target.x + Short.MAX_VALUE * edge.target.y;
		}
		else return edge.target.x + Short.MAX_VALUE * edge.target.y + Short.MAX_VALUE*64*edge.motion.motionTypeID();
	}
}
