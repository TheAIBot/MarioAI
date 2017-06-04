package MarioAI;

import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;
import sun.launcher.resources.launcher;

public class Hasher {
	
	public static int hashIntPoint(int x, int y) {
		return x + Short.MAX_VALUE * y;
	}

	public static long hashSpeedNode(float vx, DirectedEdge edge, int hashGranularity) {
		final long a = ((long)hashSpeed(vx, hashGranularity) << 32);
		final long edgeHash = edge.hashCode();
		
		return a | edgeHash;
	}
	
	public static int hashEndSpeedNode(SpeedNode sn, int hashGranularity) {
		return hashEndSpeedNode(sn.node.x, sn.node.y, sn.vx, hashGranularity);
	}
	
	public static int hashEndSpeedNode(int x, int y, float vx, int speedGranularity) {
		final int a = x & 0xffff;
		final int b = (y & 0xff) << 16;
		final int c = ((byte)hashSpeed(vx, speedGranularity)) << 24;
		final int d = hashSpeed(vx, speedGranularity) & 0x80000000;
		
		return d | c | b | a;
	}
	
	public static byte hashSpeed(float vx, int hashGranularity) {
		final float ADD_FOR_ROUND = MarioControls.MAX_X_VELOCITY / (hashGranularity * 2);
		if (vx >= 0) {
			return (byte) ((((vx + ADD_FOR_ROUND) / MarioControls.MAX_X_VELOCITY) * hashGranularity));
		} else {
			final int hashWithOutSign = (int) ((((vx - ADD_FOR_ROUND) / MarioControls.MAX_X_VELOCITY) * hashGranularity));
			if (hashWithOutSign == 0) {
				return (byte) hashWithOutSign;
			}
			else {
				return (byte) (0x80 | hashWithOutSign);	
			}
		}
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
		
		final int b5 = edge.useSuperSpeed ? 0x8000 : 0x0000;
		
		//Unique edge information to add to the hash.
		//Includes things like if it is an jump edge or running edge, and things like that.
		final int b6 = extraHash << position5; 
		return b1 | b2 | b3 | b4 | b5 | b6;
	}
}
