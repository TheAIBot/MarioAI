package MarioAI;

import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.SecondOrderPolynomial;

public class Hasher {

	public final float speedFactor = 46; // Se asociated math calculation.
											// Approxcematly 16/0.35, meaning 8
											// levels of speed.

	public static int hashShortPoint(short x, short y) {
		return x + Short.MAX_VALUE * y;
	}

	/**
	 * @param x
	 * @param y
	 * @param vx
	 * @return
	 */
	public static int hashSpeedNode(short x, short y, float vx, boolean isRunningEdge) {
		final short factor = (short) 10; // arbitrarily chosen value
		// h = (a*P1 + b)*P2 + c
		// return (int) ((x*31 + y)*59 + vx * factor);
		int ff = (isRunningEdge) ? 1 << 31 : 0;
		int a = (byte) (vx * factor) & 0x800f;
		int b = ((x & 511) << 4);
		int c = ((y & 15) << 13);
		int hashCode = a | b | c | ff;
		return hashCode;
	}

	private static int ll = 0;

	public static int hashEdge(DirectedEdge edge) {
		//TODO It does not handle wall jumps properly. Look at later.

		//TODO remember using isJumpEdge
		//General hash for all kings of edges:
		if (edge.target == null) { //setMovementEdges will delete it later:
			//This will mean a running edge from x,y=0,0 to 0,0, will not be unique: 
			//no worries, as it is not possible to have such an edge.
			return 0; 
		}
		
		//The y positions:
		final int position1 = 0;		
		final int b1 =  ((int) edge.target.y &  0xf) << position1; //Max height = 15, including 0, giving 4 bits.
		final int position2 = position1+4;
		final int b2 = 	((int) edge.source.y &  0xf) << position2;//Same as above.
		final int position3 = position2+4;
		//The x positions:
		final int b3 = 	((int) edge.target.x & 0x1ff) << position3; //X position is max 300 ish and min 0, requiring 9 bits.
		final int position4 = position3+9;
		final int b4 = 	((int) edge.source.x & 0x1ff) << position4; //Same as above.
		final int position5 = position4+9;
		
		//Unique edge information to add to the hash.
		//Includes things like if it is an jump edge or running edge, and things like that.
		final int b5 = edge.getExtraEdgeHashcode() << position5; 
		return b1 | b2 | b3 | b4 | b5;
		//return ll++;
	}
}
