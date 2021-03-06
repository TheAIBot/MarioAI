package MarioAI;

import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;

/** @author Emil
 * Auxiliary class used for assigning unique hash codes to different types of objects.
 * The overall strategy is to construct a value in the form a long, int or short, based
 * on the given parameters. Each parameter will have its own seperate place in the constructed value
 * to avoid overlaps and to achieve a unique hash code, which will only be the same for another object
 * if the same input parameters are given.
 * 
 * To do this, a mask for each parameter is made, with a number of bits and placements based on knowledge of
 * high values the input can take. Using bit-shifting and logical-and, each segment of the value to be returned is created and they are combiend
 * with logical-or in the end giving the resulting hash code to be returned.
 */
public class Hasher {
	
	public static int hashIntPoint(int x, int y) {
		return x + Short.MAX_VALUE * y;
	}

	public static long hashSpeedNode(float vx, DirectedEdge edge, int hashGranularity) {	
		final long edgeHash = edge.hashCode();
		final long speedHash = hashSpeed(vx, hashGranularity);
				
		final long b1Mask = 0b0000_0000_0000_0000_0000_0000_0000_0000_1111_1111_1111_1111_1111_1111_1111_1111L;
		final long b2Mask = 0b0000_0000_0000_0000_1111_1111_1111_1111_0000_0000_0000_0000_0000_0000_0000_0000L;
		
		final long b1Place = 0;
		final long b2Place = 32;
		
		final long b1 = (edgeHash  << b1Place) & b1Mask;
		final long b2 = (speedHash << b2Place) & b2Mask;
		
		return b1 | b2;
	}
	
	public static long hashEndSpeedNode(SpeedNode sn, int hashGranularity) {
		return hashEndSpeedNode(sn.node.x, sn.node.y, sn.vx, hashGranularity);
	}
	
	public static long hashEndSpeedNode(int x, int y, float vx, int hashGranularity) {
		final long speedHash = hashSpeed(vx, hashGranularity);
		
		final long b1Mask = 0b0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_1111_1111_1111_1111L;
		final long b2Mask = 0b0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_1111_1111_0000_0000_0000_0000L;
		final long b3Mask = 0b0000_0000_0000_0000_0000_0000_1111_1111_1111_1111_0000_0000_0000_0000_0000_0000L;
		
		final long b1Place = 0;
		final long b2Place = 16;
		final long b3Place = 24;
		
		final long b1 = (x         << b1Place) & b1Mask;
		final long b2 = (y         << b2Place) & b2Mask;
		final long b3 = (speedHash << b3Place) & b3Mask;
		
		return b1 | b2 | b3;
	}
	
	public static short hashSpeed(float vx, int hashGranularity) {
		final float ADD_FOR_ROUND = MarioControls.MAX_X_VELOCITY / (hashGranularity * 2);
		
		final int hashWithOutSign = (short) ((((Math.abs(vx) + ADD_FOR_ROUND) / MarioControls.MAX_X_VELOCITY) * hashGranularity));
		int hashSign = ((vx >= 0) ? 0 : 1);
		hashSign = (hashWithOutSign == 0) ? 0 : hashSign;
		
		final int b1Mask = 0b0001_1111_1111_1111;
		final int b2Mask = 0b0010_0000_0000_0000;
		
		final int b1Place = 0;
		final int b2Place = 13;
		
		final int b1 = (hashWithOutSign << b1Place) & b1Mask;
		final int b2 = (hashSign        << b2Place) & b2Mask;
		
		return (short) (b1 | b2);
	}

	public static int hashEdge(DirectedEdge edge, int extraHash) {
		if (edge.target == null) {
			//This will mean a running edge from x,y=0,0 to 0,0, will not be unique: 
			//no worries, as they will be discarded.
			return 0; 
		}
		
		final int b1Mask = 0b0000_0000_0000_0000_0000_0000_0000_1111;
		final int b2Mask = 0b0000_0000_0000_0000_0000_0000_1111_0000;
		final int b3Mask = 0b0000_0000_0000_0001_1111_1111_0000_0000;
		final int b4Mask = 0b0000_0011_1111_1110_0000_0000_0000_0000;
		final int b5Mask = 0b0000_0100_0000_0000_0000_0000_0000_0000;
		final int b6Mask = 0b1111_1000_0000_0000_0000_0000_0000_0000;
		
		final int b1Place = 0;
		final int b2Place = 4;
		final int b3Place = 8;
		final int b4Place = 17;
		final int b5Place = 26;
		final int b6Place = 27;
		
		final int b1 = ( edge.target.y               << b1Place) & b1Mask;
		final int b2 = ( edge.source.y               << b2Place) & b2Mask;
		final int b3 = ( edge.target.x               << b3Place) & b3Mask;
		final int b4 = ( edge.source.x               << b4Place) & b4Mask;
		final int b5 = ((edge.useSuperSpeed ? 1 : 0) << b5Place) & b5Mask;
		final int b6 = ( extraHash                   << b6Place) & b6Mask;
		
		return b1 | b2 | b3 | b4 | b5 | b6;
	}
}
