package MarioAI;

import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;

public class Hasher {
	
	public static int hashIntPoint(int x, int y) {
		return x + Short.MAX_VALUE * y;
	}

	/**
	 * Hash speednode based on veloctiy and the edge leading to this speedNode
	 * @param vx
	 * @param edge
	 * @return
	 */
	public static long hashSpeedNode(float vx, DirectedEdge edge) {
		//the hash of the speed needs to be fixed
		//final long a = hashSpeed(vx) << 32;
		final int speedHash = hashSpeed(vx, FACTOR_NUMBER_OF_SPEED_NODES);
		final long speedSign = (speedHash >= 0) ? 0 : Long.MIN_VALUE;
		final long a = ((long)hashSpeed(vx, FACTOR_NUMBER_OF_SPEED_NODES) << 32) | speedSign;
		final long edgeHash = edge.hashCode();
		
		return a | edgeHash;
	}
	
	/**
	 * Hash speednode based on x, y coords and velocity.
	 * To be used for checking if we have seen this speedNode before.
	 * Used in closed set.
	 * @param sn
	 * @return
	 */
	public static int hashEndSpeedNode(SpeedNode sn) {
		return hashEndSpeedNode(sn.node.x, sn.node.y, sn.vx);
	}
	
	public static int hashEndSpeedNode(int x, int y, float vx) {
		return hashEndSpeedNode(x, y, vx, FACTOR_NUMBER_OF_SPEED_NODES);
	}
	
	public static int hashEndSpeedNode(int x, int y, float vx, int granularity) {
		final int a = x & 0xffff;
		final int b = (y & 0xff) << 16;
		final int c = ((byte)hashSpeed(vx, granularity)) << 24;
		final int d = hashSpeed(vx, granularity) & 0x80000000;
		
		return d | c | b | a;
	}
	
	public static final int FACTOR_NUMBER_OF_SPEED_NODES = 40;
	public static byte hashSpeed(float vx, int granularity) {
		final float ADD_FOR_ROUND = MarioControls.MAX_X_VELOCITY / (granularity * 2);
		if (vx >= 0) {
			return (byte) ((((vx + ADD_FOR_ROUND) / MarioControls.MAX_X_VELOCITY) * granularity));
		} else {
			int hashWithOutSign = (int) ((((vx - ADD_FOR_ROUND) / MarioControls.MAX_X_VELOCITY) * granularity));
			return (byte) (0x80 | hashWithOutSign);
		}
		/*
		final int speedHash = hashSpeed(vx);
		final long speedSign = (speedHash >= 0) ? 0 : Long.MIN_VALUE;
		final long a = ((long)hashSpeed(vx) << 32) | speedSign;
		*/
	}

	public static int hashEdge(DirectedEdge edge, int extraHash) {
		//TODO It does not handle wall jumps properly. Look at later.

		//TODO remember using isJumpEdge
		//General hash for all kings of edges:
		if (edge.target == null) { //setMovementEdges will delete it later:
			//This will mean a running edge from x,y=0,0 to 0,0, will not be unique: 
			//no worries, as they will be discarded.
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
		final int b5 = extraHash << position5; 
		return b1 | b2 | b3 | b4 | b5;
	}
}
