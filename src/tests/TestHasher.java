package tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import MarioAI.graph.edges.RunningEdge;
import MarioAI.Hasher;
import MarioAI.graph.edges.FallEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.nodes.Node;
import MarioAI.marioMovement.MarioControls;

public class TestHasher {
	
	private final int LIMIT_X = 32;
	private final int LIMIT_Y = 15;
	
	@Test
	public void testNoOverlapDifferentEdgeHashing() {
		List<Integer> allJumpingEdgesList = getAllPossibleJumpingEdgeHashcodes();
		List<Integer> allRunningEdges = getAllPossibleRunningEdgeHashcodes();
		List<Integer> allFallEdges = getAllPossibleFallEdgeHashcodes();
		HashSet<Integer> allEdgesHashed = new HashSet<Integer>();
		allEdgesHashed.addAll(allJumpingEdgesList);
		allEdgesHashed.addAll(allRunningEdges);
		allEdgesHashed.addAll(allFallEdges);
		assertEquals(allJumpingEdgesList.size() + allRunningEdges.size() + allFallEdges.size(), allEdgesHashed.size());
		
	}

	@Test
	public void testProperFallEdgeHashing() {
		final HashSet<Integer> allFallEdgesHashed = new HashSet<Integer>();
		
		//adding the edges twice should give the same size unlees some hashes are the same
		for (int i = 0; i < 2; i++) {
			//Because of the small state space, brute force over the statespace will be used to check its correctness.
			final List<Integer> allFallEdgesHashcodes = getAllPossibleFallEdgeHashcodes();
			allFallEdgesHashed.addAll(allFallEdgesHashcodes);
			assertEquals(allFallEdgesHashcodes.size(), allFallEdgesHashed.size());
		}
	}
	private List<Integer> getAllPossibleFallEdgeHashcodes() {
		final int expectedHashes =  (int) ((LIMIT_Y+1)*(LIMIT_X+1))*((LIMIT_X+1)*(LIMIT_Y+1)) * 2;
		ArrayList<Integer> allFallEdgesHashes = new ArrayList<Integer>(expectedHashes);
		
		for (short sourceY = 0; sourceY <= LIMIT_Y; sourceY++) {
			for (short sourceX = 0; sourceX <= LIMIT_X; sourceX++) {			
				for (short targetY = 0; targetY <= LIMIT_Y; targetY++) {
					for (short targetX = 0; targetX <= LIMIT_X; targetX++) {		
						//Type does not matter
						final Node source = new Node(sourceX, sourceY, (byte)10);
						final Node target = new Node(targetX, targetY, (byte)10);
						final FallEdge fall1 = new FallEdge(source, target, false);
						final FallEdge fall2 = new FallEdge(source, target, true);
						allFallEdgesHashes.add(fall1.hashCode());
						allFallEdgesHashes.add(fall2.hashCode());
					}
				}			
			}
		}
		assertEquals(expectedHashes, allFallEdgesHashes.size());
		return allFallEdgesHashes;
	}
	
	@Test
	public void testProperJumpEdgeHashing() {
		final HashSet<Integer> allJumpEdgesHashed = new HashSet<Integer>();
		
		//adding the edges twice should give the same size unlees some hashes are the same
		for (int i = 0; i < 2; i++) {
			//Because of the small state space, brute force over the statespace will be used to check its correctness.
			final List<Integer> allJumpingEdgesHashcodes = getAllPossibleJumpingEdgeHashcodes();
			allJumpEdgesHashed.addAll(allJumpingEdgesHashcodes);
			assertEquals(allJumpingEdgesHashcodes.size(), allJumpEdgesHashed.size());
		}
	}
	private List<Integer> getAllPossibleJumpingEdgeHashcodes() {
		final int limitY = 15;
		final int limitX = 32;
		final int limitJumpHeight = 4;
		final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2) * (limitJumpHeight + 1) * 2;
		ArrayList<Integer> allJumpingEdgesHashes = new ArrayList<Integer>(expectedHashes);
		
		for (short sourceY = 0; sourceY <= LIMIT_Y; sourceY++) {
			for (short sourceX = 0; sourceX <= LIMIT_X; sourceX++) {	
				final Node source = new Node(sourceX, sourceY, (byte)10);
				for (short targetY = 0; targetY <= LIMIT_Y; targetY++) {
					for (short targetX = 0; targetX <= LIMIT_X; targetX++) {
						final Node target = new Node(targetX, targetY, (byte)10);
						for (int JumpHeight = 0; JumpHeight <= limitJumpHeight; JumpHeight++) {
							//Type does not matter
							final JumpingEdge newPolynomial1 = new JumpingEdge(source,target, sourceY + JumpHeight, false);
							final JumpingEdge newPolynomial2 = new JumpingEdge(source,target, sourceY + JumpHeight, true);
							allJumpingEdgesHashes.add(newPolynomial1.hashCode());
							allJumpingEdgesHashes.add(newPolynomial2.hashCode());
						}						
					}
				}			
			}
		}
		assertEquals(expectedHashes, allJumpingEdgesHashes.size());
		return allJumpingEdgesHashes;
	}

	@Test
	public void testProperRunningEdgeHashing() {
		final HashSet<Integer> allRunningEdgesHashed = new HashSet<Integer>();
		
		//adding the edges twice should give the same size, unless some hashes are the same
		for (int i = 0; i < 2; i++) {
			//Because of the small state space, brute force over the statespace will be used to check its correctness.
			final List<Integer> allRunningEdgesList = getAllPossibleRunningEdgeHashcodes();
			allRunningEdgesHashed.addAll(allRunningEdgesList);
			assertEquals(allRunningEdgesList.size(), allRunningEdgesHashed.size());
		}
	}
	private ArrayList<Integer> getAllPossibleRunningEdgeHashcodes() {
		final int limitY = 15;
		final int limitX = 32;
		final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2) * 2;
		ArrayList<Integer> allRunningEdgesHashcodes = new ArrayList<Integer>();
		
		for (short sourceY = 0; sourceY <= limitY; sourceY++) {
			for (short sourceX = 0; sourceX <= limitX; sourceX++) {						
				for (short targetY = 0; targetY <= limitY; targetY++) {
					for (short targetX = 0; targetX <= limitX; targetX++) {
						final RunningEdge run1 = new RunningEdge(new Node(sourceX, sourceY, (byte)10), new Node(targetX, targetY, (byte)10), false);
						final RunningEdge run2 = new RunningEdge(new Node(sourceX, sourceY, (byte)10), new Node(targetX, targetY, (byte)10), true);
						allRunningEdgesHashcodes.add(run1.hashCode());
						allRunningEdgesHashcodes.add(run2.hashCode());
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
				assertEquals(allSpeedNodesList.size() / 3, allSpeedNodesHashed.size());
			}
		}
	}
	private ArrayList<Integer> getAllPossibleSpeedNodeHashcodes(int speedGranularity) {
		final int limitY = 15;
		final int limitX = 32;
		final float speedLimit = MarioControls.MAX_X_VELOCITY;
		final int expectedHashes = limitY * limitX * (speedGranularity * 2 + 1) * 3;
		ArrayList<Integer> allSpeedNodeHashcodes = new ArrayList<Integer>();
		
		final float speedIncrements = speedLimit / speedGranularity;
		
		for (int y = 0; y < limitY; y++) {
			for (int x = 0; x < limitX; x++) {
				for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
					//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
					allSpeedNodeHashcodes.add(Hasher.hashEndSpeedNode(x, y, v, speedGranularity));
					allSpeedNodeHashcodes.add(Hasher.hashEndSpeedNode(x, y, v + speedIncrements / 3, speedGranularity));
					allSpeedNodeHashcodes.add(Hasher.hashEndSpeedNode(x, y, v - speedIncrements / 3, speedGranularity));
				}
			}
		}
		assertEquals(expectedHashes, allSpeedNodeHashcodes.size());
		return allSpeedNodeHashcodes;
	}
	
	@Test
	public void testSpeedHashing() {
		for (int g = 1; g < 64; g += 3) {
			final HashSet<Byte> allSpeedHashed = new HashSet<Byte>();
			
			//adding the edges twice should give the same size unlees some hashes are the same
			for (int i = 0; i < 2; i++) {
				//Because of the small state space, brute force over the statespace will be used to check its correctness.
				final ArrayList<Byte> allSpeedList = getAllPossibleSpeedHashcodes(g);
				allSpeedHashed.addAll(allSpeedList);
				assertEquals(allSpeedList.size() / 3, allSpeedHashed.size());
			}
		}
	}
	private ArrayList<Byte> getAllPossibleSpeedHashcodes(int speedGranularity) {
		final float speedLimit = MarioControls.MAX_X_VELOCITY;
		final int expectedHashes = (speedGranularity * 2 + 1) * 3;
		ArrayList<Byte> allSpeedHashcodes = new ArrayList<Byte>();
		
		final float speedIncrements = speedLimit / speedGranularity;
		
		for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
			//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
			allSpeedHashcodes.add(Hasher.hashSpeed(v, speedGranularity));
			allSpeedHashcodes.add(Hasher.hashSpeed(v + speedIncrements / 3, speedGranularity));
			allSpeedHashcodes.add(Hasher.hashSpeed(v - speedIncrements / 3, speedGranularity));
		}
		assertEquals("granularity: " + speedGranularity, expectedHashes, allSpeedHashcodes.size());
		return allSpeedHashcodes;
	}
	
	@Test
	public void testSpeedNodeWithEdgesHashing() {
		for (int g = 1; g < 20; g += 7) {
			final HashSet<Long> allSpeedNodesHashed = new HashSet<Long>();
			
			//adding the edges twice should give the same size unlees some hashes are the same
			for (int i = 0; i < 2; i++) {
				//Because of the small state space, brute force over the statespace will be used to check its correctness.
				final List<Long> allSpeedNodesList = getAllPossibleSpeedNodesWithEdgesHashcodes(g);
				allSpeedNodesHashed.addAll(allSpeedNodesList);
				assertEquals(allSpeedNodesList.size() / 3, allSpeedNodesHashed.size());
			}
		}
	}
	private ArrayList<Long> getAllPossibleSpeedNodesWithEdgesHashcodes(int speedGranularity) {
		final int limitY = 10;
		final int limitX = 20;
		final float speedLimit = MarioControls.MAX_X_VELOCITY;
		final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2) * (speedGranularity * 2 + 1) * 3 * 2;
		ArrayList<Long> allSpeedNodeHashcodes = new ArrayList<Long>();
		
		final float speedIncrements = speedLimit / speedGranularity;
		
		for (short sourceY = 0; sourceY <= limitY; sourceY++) {
			for (short sourceX = 0; sourceX <= limitX; sourceX++) {	
				final Node source = new Node(sourceX, sourceY, (byte)10);
				for (short targetY = 0; targetY <= limitY; targetY++) {
					for (short targetX = 0; targetX <= limitX; targetX++) {
						final Node target = new Node(targetX, targetY, (byte)10);
						final RunningEdge edge1 = new RunningEdge(source, target, false);
						for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
							//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
							allSpeedNodeHashcodes.add(Hasher.hashSpeedNode(v, edge1, speedGranularity));
							allSpeedNodeHashcodes.add(Hasher.hashSpeedNode(v + speedIncrements / 3, edge1, speedGranularity));
							allSpeedNodeHashcodes.add(Hasher.hashSpeedNode(v - speedIncrements / 3, edge1, speedGranularity));
						}
						
						final RunningEdge edge2 = new RunningEdge(source, target, true);
						for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
							//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
							allSpeedNodeHashcodes.add(Hasher.hashSpeedNode(v, edge2, speedGranularity));
							allSpeedNodeHashcodes.add(Hasher.hashSpeedNode(v + speedIncrements / 3, edge2, speedGranularity));
							allSpeedNodeHashcodes.add(Hasher.hashSpeedNode(v - speedIncrements / 3, edge2, speedGranularity));
						}
					}
				}
			}
		}
		assertEquals(expectedHashes, allSpeedNodeHashcodes.size());
		return allSpeedNodeHashcodes;
	}
	
	@Test
	public void testSpeedNodeWithEdgesHashing2() {
		for (int speedGranularity = 1; speedGranularity < 20; speedGranularity += 7) {
			final HashSet<Long> allSpeedNodesHashed = new HashSet<Long>();
			
			//adding the edges twice should give the same size unlees some hashes are the same
			for (int i = 0; i < 2; i++) {
				//Because of the small state space, brute force over the statespace will be used to check its correctness.
				final int limitY = 10;
				final int limitX = 20;
				final int heightLimit = 4;
				final float speedLimit = MarioControls.MAX_X_VELOCITY;
				final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2) * (speedGranularity * 2 + 1) * heightLimit * 2;
				ArrayList<Long> allSpeedNodeHashcodes1 = new ArrayList<Long>();
				ArrayList<Long> allSpeedNodeHashcodes2 = new ArrayList<Long>();
				ArrayList<Long> allSpeedNodeHashcodes3 = new ArrayList<Long>();
				
				final float speedIncrements = speedLimit / speedGranularity;
				
				for (short sourceY = 0; sourceY <= limitY; sourceY++) {
					for (short sourceX = 0; sourceX <= limitX; sourceX++) {						
						for (short targetY = 0; targetY <= limitY; targetY++) {
							for (short targetX = 0; targetX <= limitX; targetX++) {
								for (int h = 1; h <= heightLimit; h++) {
									final JumpingEdge edge1 = new JumpingEdge(new Node(sourceX, sourceY, (byte)10), new Node(targetX, targetY, (byte)10), false);
									for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
										//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
										allSpeedNodeHashcodes1.add(Hasher.hashSpeedNode(v, edge1, speedGranularity));
										allSpeedNodeHashcodes2.add(Hasher.hashSpeedNode(v + speedIncrements / 3, edge1, speedGranularity));
										allSpeedNodeHashcodes3.add(Hasher.hashSpeedNode(v - speedIncrements / 3, edge1, speedGranularity));
									}
									
									final JumpingEdge edge2 = new JumpingEdge(new Node(sourceX, sourceY, (byte)10), new Node(targetX, targetY, (byte)10), true);
									for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
										//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
										allSpeedNodeHashcodes1.add(Hasher.hashSpeedNode(v, edge2, speedGranularity));
										allSpeedNodeHashcodes2.add(Hasher.hashSpeedNode(v + speedIncrements / 3, edge2, speedGranularity));
										allSpeedNodeHashcodes3.add(Hasher.hashSpeedNode(v - speedIncrements / 3, edge2, speedGranularity));
									}
								}
							}
						}
					}
				}
				assertEquals(expectedHashes, allSpeedNodeHashcodes1.size());
				assertEquals(expectedHashes, allSpeedNodeHashcodes2.size());
				assertEquals(expectedHashes, allSpeedNodeHashcodes3.size());

				allSpeedNodesHashed.addAll(allSpeedNodeHashcodes1);
				allSpeedNodesHashed.addAll(allSpeedNodeHashcodes2);
				allSpeedNodesHashed.addAll(allSpeedNodeHashcodes3);
				assertEquals(allSpeedNodeHashcodes1.size(), allSpeedNodesHashed.size());
			}
		}
	}
	
}
