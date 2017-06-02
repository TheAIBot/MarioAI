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
import MarioAI.graph.nodes.SpeedNode;
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

	private List<Integer> getAllPossibleFallEdgeHashcodes() {
		final int expectedHashes =  (int) ((LIMIT_Y+1)*(LIMIT_X+1))*((LIMIT_X+1)*(LIMIT_Y+1));
		ArrayList<Integer> allFallEdgesHashes = new ArrayList<Integer>(expectedHashes);
		
		for (short sourceY = 0; sourceY <= LIMIT_Y; sourceY++) {
			for (short sourceX = 0; sourceX <= LIMIT_X; sourceX++) {			
				for (short targetY = 0; targetY <= LIMIT_Y; targetY++) {
					for (short targetX = 0; targetX <= LIMIT_X; targetX++) {		
						//Type does not matter
						final Node source = new Node(sourceX, sourceY, (byte)10);
						final Node target = new Node(targetX, targetY, (byte)10);
						final FallEdge fall = new FallEdge(source, target);
						allFallEdgesHashes.add(fall.hashCode());						
					}
				}			
			}
		}
		assertEquals(expectedHashes, allFallEdgesHashes.size());
		return allFallEdgesHashes;
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
		final int limitJumpHeight = 4;
		final int expectedHashes = (int)Math.pow((LIMIT_Y + 1), 2) * (int)Math.pow((LIMIT_X + 1), 2) * (limitJumpHeight + 1);
		ArrayList<Integer> allJumpingEdgesHashes = new ArrayList<Integer>(expectedHashes);
		
		for (short sourceY = 0; sourceY <= LIMIT_Y; sourceY++) {
			for (short sourceX = 0; sourceX <= LIMIT_X; sourceX++) {			
				for (short targetY = 0; targetY <= LIMIT_Y; targetY++) {
					for (short targetX = 0; targetX <= LIMIT_X; targetX++) {		
						for (int JumpHeight = 0; JumpHeight <= limitJumpHeight; JumpHeight++) {
							//Type does not matter
							final Node source = new Node(sourceX, sourceY, (byte)10);
							final Node target = new Node(targetX, targetY, (byte)10);
							final JumpingEdge newPolynomial = new JumpingEdge(source, target, sourceY + JumpHeight);
							allJumpingEdgesHashes.add(newPolynomial.hashCode());
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
		final int expectedHashes = (int)Math.pow((LIMIT_Y + 1), 2) * (int)Math.pow((LIMIT_X + 1), 2);
		ArrayList<Integer> allRunningEdgesHashcodes = new ArrayList<Integer>();
		
		for (short sourceY = 0; sourceY <= LIMIT_Y; sourceY++) {
			for (short sourceX = 0; sourceX <= LIMIT_X; sourceX++) {						
				for (short targetY = 0; targetY <= LIMIT_Y; targetY++) {
					for (short targetX = 0; targetX <= LIMIT_X; targetX++) {
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
		for (int g = 1; g < 20; g += 3) {
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
		final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2) * (speedGranularity * 2 + 1) * 3;
		ArrayList<Long> allRunningEdgesHashcodes = new ArrayList<Long>();
		
		final float speedIncrements = speedLimit / speedGranularity;
		
		for (short sourceY = 0; sourceY <= limitY; sourceY++) {
			for (short sourceX = 0; sourceX <= limitX; sourceX++) {						
				for (short targetY = 0; targetY <= limitY; targetY++) {
					for (short targetX = 0; targetX <= limitX; targetX++) {
						final RunningEdge edge = new RunningEdge(new Node(sourceX, sourceY, (byte)10), 
								new Node(targetX, targetY, (byte)10));
						for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
							//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
							allRunningEdgesHashcodes.add(Hasher.hashSpeedNode(v, edge, speedGranularity));
							allRunningEdgesHashcodes.add(Hasher.hashSpeedNode(v + speedIncrements / 3, edge, speedGranularity));
							allRunningEdgesHashcodes.add(Hasher.hashSpeedNode(v - speedIncrements / 3, edge, speedGranularity));
						}
					}
				}
			}
		}
		assertEquals(expectedHashes, allRunningEdgesHashcodes.size());
		return allRunningEdgesHashcodes;
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
				final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2) * (speedGranularity * 2 + 1) * heightLimit;
				ArrayList<Long> allRunningEdgesHashcodes1 = new ArrayList<Long>();
				ArrayList<Long> allRunningEdgesHashcodes2 = new ArrayList<Long>();
				ArrayList<Long> allRunningEdgesHashcodes3 = new ArrayList<Long>();
				
				final float speedIncrements = speedLimit / speedGranularity;
				
				for (short sourceY = 0; sourceY <= limitY; sourceY++) {
					for (short sourceX = 0; sourceX <= limitX; sourceX++) {						
						for (short targetY = 0; targetY <= limitY; targetY++) {
							for (short targetX = 0; targetX <= limitX; targetX++) {
								for (int h = 1; h <= heightLimit; h++) {
									final JumpingEdge edge = new JumpingEdge(new Node(sourceX, sourceY, (byte)10), 
											new Node(targetX, targetY, (byte)10),
											h);
									for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
										//final SpeedNode sn = new SpeedNode(new Node(x, y, (byte) 0), v, Hasher.hashEndSpeedNode(x, y, v));
										allRunningEdgesHashcodes1.add(Hasher.hashSpeedNode(v, edge, speedGranularity));
										allRunningEdgesHashcodes2.add(Hasher.hashSpeedNode(v + speedIncrements / 3, edge, speedGranularity));
										allRunningEdgesHashcodes3.add(Hasher.hashSpeedNode(v - speedIncrements / 3, edge, speedGranularity));
									}
								}
							}
						}
					}
				}
				assertEquals(expectedHashes, allRunningEdgesHashcodes1.size());
				assertEquals(expectedHashes, allRunningEdgesHashcodes2.size());
				assertEquals(expectedHashes, allRunningEdgesHashcodes3.size());

				allSpeedNodesHashed.addAll(allRunningEdgesHashcodes1);
				allSpeedNodesHashed.addAll(allRunningEdgesHashcodes2);
				allSpeedNodesHashed.addAll(allRunningEdgesHashcodes3);
				assertEquals(allRunningEdgesHashcodes1.size(), allSpeedNodesHashed.size());
			}
		}
	}
	
}
