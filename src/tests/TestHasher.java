package tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import MarioAI.graph.edges.RunningEdge;
import MarioAI.Hasher;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import MarioAI.marioMovement.MarioControls;

public class TestHasher {
	
	@Test
	public void testNoOverlapJumpHashingAndRunningHashing() {
		List<Integer> allJumpingEdgesList = getAllPossibleJumpingEdgeHashcodes();
		List<Integer> allRunningEdges = getAllPossibleRunningEdgeHashcodes();
		HashSet<Integer> allEdgesHashed = new HashSet<Integer>();
		allEdgesHashed.addAll(allJumpingEdgesList);
		allEdgesHashed.addAll(allRunningEdges);
		assertEquals(allJumpingEdgesList.size() + allRunningEdges.size(), allEdgesHashed.size());
		
	}

	@Test
	public void testProperJumpEdgeHashing() {
		final HashSet<Integer> allRuningEdgesHashed = new HashSet<Integer>();
		
		//adding the edges twice should give the same size unlees some hashes are the same
		for (int i = 0; i < 2; i++) {
			//Because of the small state space, brute force over the statespace will be used to check its correctness.
			final List<Integer> allJumpingEdgesHashcodes = getAllPossibleJumpingEdgeHashcodes();
			allRuningEdgesHashed.addAll(allJumpingEdgesHashcodes);
			assertEquals(allJumpingEdgesHashcodes.size(), allRuningEdgesHashed.size());
		}
	}
	
	private List<Integer> getAllPossibleJumpingEdgeHashcodes() {
		final int limitY = 15;
		final int limitX = 32;
		final int limitJumpHeight = 4;
		final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2) * (limitJumpHeight + 1);
		ArrayList<Integer> allRunningEdgesHashes = new ArrayList<Integer>(expectedHashes);
		
		for (short sourceY = 0; sourceY <= limitY; sourceY++) {
			for (short sourceX = 0; sourceX <= limitX; sourceX++) {			
				for (short targetY = 0; targetY <= limitY; targetY++) {
					for (short targetX = 0; targetX <= limitX; targetX++) {		
						for (int JumpHeight = 0; JumpHeight <= limitJumpHeight; JumpHeight++) {
							//Type does not matter
							final Node source = new Node(sourceX, sourceY, (byte)10);
							final Node target = new Node(targetX, targetY, (byte)10);
							final JumpingEdge newPolynomial = new JumpingEdge(source,target, sourceY + JumpHeight);
							allRunningEdgesHashes.add(newPolynomial.hashCode());
						}						
					}
				}			
			}
		}
		assertEquals(expectedHashes, allRunningEdgesHashes.size());
		return allRunningEdgesHashes;
	}

	@Test
	public void testProperRunningEdgeHashing() {
		final HashSet<Integer> allRuningEdgesHashed = new HashSet<Integer>();
		
		//adding the edges twice should give the same size unlees some hashes are the same
		for (int i = 0; i < 2; i++) {
			//Because of the small state space, brute force over the statespace will be used to check its correctness.
			final List<Integer> allRunningEdgesList = getAllPossibleRunningEdgeHashcodes();
			allRuningEdgesHashed.addAll(allRunningEdgesList);
			assertEquals(allRunningEdgesList.size(), allRuningEdgesHashed.size());
		}
	}

	private ArrayList<Integer> getAllPossibleRunningEdgeHashcodes() {
		final int limitY = 15;
		final int limitX = 32;
		final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2);
		ArrayList<Integer> allRunningEdgesHashcodes = new ArrayList<Integer>();
		
		for (short sourceY = 0; sourceY <= limitY; sourceY++) {
			for (short sourceX = 0; sourceX <= limitX; sourceX++) {						
				for (short targetY = 0; targetY <= limitY; targetY++) {
					for (short targetX = 0; targetX <= limitX; targetX++) {
						final RunningEdge run = new RunningEdge(new Node(sourceX, sourceY, (byte)10), 
		                        				  		new Node(targetX, targetY, (byte)10));
						allRunningEdgesHashcodes.add(run.hashCode());
						
					}
				}
			}
		}
		assertEquals(expectedHashes, allRunningEdgesHashcodes.size());
		return allRunningEdgesHashcodes;
	}
	
	@Test
	public void testSpeedNodeHashCodes() {
		
		for (int g = 1; g < 20; g += 3) {
			final HashSet<Integer> allSpeedNodesHashed = new HashSet<Integer>();
			
			//adding the edges twice should give the same size unlees some hashes are the same
			for (int i = 0; i < 2; i++) {
				//Because of the small state space, brute force over the statespace will be used to check its correctness.
				final List<Integer> allSpeedNodesList = getAllPossibleSpeedNodeHashcodes(g);
				allSpeedNodesHashed.addAll(allSpeedNodesList);
				assertEquals(allSpeedNodesList.size(), allSpeedNodesHashed.size());
			}
		}
	}
	
	private ArrayList<Integer> getAllPossibleSpeedNodeHashcodes(int speedGranularity) {
		final int limitY = 15;
		final int limitX = 32;
		final float speedLimit = MarioControls.MAX_X_VELOCITY;
		final int expectedHashes = limitY * limitX * (speedGranularity * 2 + 1);
		ArrayList<Integer> allSpeedNodeHashcodes = new ArrayList<Integer>();
		
		for (int y = 0; y < limitY; y++) {
			for (int x = 0; x < limitX; x++) {
				for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedLimit / speedGranularity) {
					//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
					allSpeedNodeHashcodes.add(Hasher.hashEndSpeedNode(x, y, v, speedGranularity));
				}
			}
		}
		assertEquals(expectedHashes, allSpeedNodeHashcodes.size());
		return allSpeedNodeHashcodes;
	}
	
	@Test
	public void testSpeedHashing() {
		
		for (int g = 1; g < 20; g += 3) {
			final HashSet<Integer> allSpeedHashed = new HashSet<Integer>();
			
			//adding the edges twice should give the same size unlees some hashes are the same
			for (int i = 0; i < 2; i++) {
				//Because of the small state space, brute force over the statespace will be used to check its correctness.
				final List<Integer> allSpeedList = getAllPossibleSpeedNodeHashcodes(g);
				allSpeedHashed.addAll(allSpeedList);
				assertEquals(allSpeedList.size(), allSpeedHashed.size());
			}
		}
	}
	
	private ArrayList<Integer> getAllPossibleSpeedHashcodes(int speedGranularity) {
		final int limitY = 15;
		final int limitX = 32;
		final float speedLimit = MarioControls.MAX_X_VELOCITY;
		final int expectedHashes = limitY * limitX * (speedGranularity * 2 + 1);
		ArrayList<Integer> allSpeedHashcodes = new ArrayList<Integer>();
		
		for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedLimit / speedGranularity) {
			//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
			allSpeedHashcodes.add(Hasher.hashSpeed(v, speedGranularity));
		}
		assertEquals(expectedHashes, allSpeedHashcodes.size());
		return allSpeedHashcodes;
	}
	
	
}
