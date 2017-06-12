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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class TestHasher {
	
	private final int LIMIT_X = 25;
	private final int LIMIT_Y = 15;
	
	@Test
	public void testNoOverlapDifferentEdgeHashing() {
		IntArrayList allJumpingEdgesList = getAllPossibleJumpingEdgeHashcodes();
		IntArrayList allRunningEdges = getAllPossibleRunningEdgeHashcodes();
		IntArrayList allFallEdges = getAllPossibleFallEdgeHashcodes();
		IntOpenHashSet allEdgesHashed = new IntOpenHashSet();
		allEdgesHashed.addAll(allJumpingEdgesList);
		allEdgesHashed.addAll(allRunningEdges);
		allEdgesHashed.addAll(allFallEdges);
		assertEquals(allJumpingEdgesList.size() + allRunningEdges.size() + allFallEdges.size(), allEdgesHashed.size());
	}

	@Test
	public void testProperFallEdgeHashing() {
		final IntOpenHashSet allFallEdgesHashed = new IntOpenHashSet();
		
		//adding the edges twice should give the same size unlees some hashes are the same
		for (int i = 0; i < 2; i++) {
			//Because of the small state space, brute force over the statespace will be used to check its correctness.
			final IntArrayList allFallEdgesHashcodes = getAllPossibleFallEdgeHashcodes();
			allFallEdgesHashed.addAll(allFallEdgesHashcodes);
			assertEquals(allFallEdgesHashcodes.size(), allFallEdgesHashed.size());
		}
	}
	private IntArrayList getAllPossibleFallEdgeHashcodes() {
		final int expectedHashes =  (int) ((LIMIT_Y+1)*(LIMIT_X+1))*((LIMIT_X+1)*(LIMIT_Y+1)) * 2;
		IntArrayList allFallEdgesHashes = new IntArrayList(expectedHashes);
		
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
		final IntOpenHashSet allJumpEdgesHashed = new IntOpenHashSet();
		
		//adding the edges twice should give the same size unlees some hashes are the same
		for (int i = 0; i < 2; i++) {
			//Because of the small state space, brute force over the statespace will be used to check its correctness.
			final IntArrayList allJumpingEdgesHashcodes = getAllPossibleJumpingEdgeHashcodes();
			allJumpEdgesHashed.addAll(allJumpingEdgesHashcodes);
			assertEquals(allJumpingEdgesHashcodes.size(), allJumpEdgesHashed.size());
		}
	}
	private IntArrayList getAllPossibleJumpingEdgeHashcodes() {
		final int limitJumpHeight = 4;
		final int expectedHashes = (int)Math.pow((LIMIT_Y + 1), 2) * (int)Math.pow((LIMIT_X + 1), 2) * (limitJumpHeight + 1) * 2;
		IntArrayList allJumpingEdgesHashes = new IntArrayList(expectedHashes);
		
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
		final IntOpenHashSet allRunningEdgesHashed = new IntOpenHashSet();
		
		//adding the edges twice should give the same size, unless some hashes are the same
		for (int i = 0; i < 2; i++) {
			//Because of the small state space, brute force over the statespace will be used to check its correctness.
			final IntArrayList allRunningEdgesList = getAllPossibleRunningEdgeHashcodes();
			allRunningEdgesHashed.addAll(allRunningEdgesList);
			assertEquals(allRunningEdgesList.size(), allRunningEdgesHashed.size());
		}
	}
	private IntArrayList getAllPossibleRunningEdgeHashcodes() {
		final int expectedHashes = (int)Math.pow((LIMIT_Y + 1), 2) * (int)Math.pow((LIMIT_X + 1), 2) * 2;
		IntArrayList allRunningEdgesHashcodes = new IntArrayList();
		
		for (short sourceY = 0; sourceY <= LIMIT_Y; sourceY++) {
			for (short sourceX = 0; sourceX <= LIMIT_X; sourceX++) {						
				for (short targetY = 0; targetY <= LIMIT_Y; targetY++) {
					for (short targetX = 0; targetX <= LIMIT_X; targetX++) {
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
	public void testStateNodeHashCodes() {
		for (int g = 1; g < 20; g += 3) {
			final LongOpenHashSet allStateNodesHashed = new LongOpenHashSet();
			
			//adding the edges twice should give the same size unlees some hashes are the same
			for (int i = 0; i < 2; i++) {
				//Because of the small state space, brute force over the statespace will be used to check its correctness.
				final LongArrayList allStateNodesList = getAllPossibleStateNodeHashcodes(g);
				allStateNodesHashed.addAll(allStateNodesList);
				assertEquals(allStateNodesList.size() / 3, allStateNodesHashed.size());
			}
		}
	}
	private LongArrayList getAllPossibleStateNodeHashcodes(int speedGranularity) {
		final int limitY = 15;
		final int limitX = 32;
		final float speedLimit = MarioControls.MAX_X_VELOCITY;
		final int expectedHashes = limitY * limitX * (speedGranularity * 2 + 1) * 3;
		LongArrayList allStateNodeHashcodes = new LongArrayList();
		
		final float speedIncrements = speedLimit / speedGranularity;
		
		for (int y = 0; y < limitY; y++) {
			for (int x = 0; x < limitX; x++) {
				for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
					//final StateNode sn = new StateNode(new Node(x, y, (byte) 0), v, Hasher.hashEndStateNode(x, y, v));
					allStateNodeHashcodes.add(Hasher.hashEndStateNode(x, y, v, speedGranularity));
					allStateNodeHashcodes.add(Hasher.hashEndStateNode(x, y, v + speedIncrements / 3, speedGranularity));
					allStateNodeHashcodes.add(Hasher.hashEndStateNode(x, y, v - speedIncrements / 3, speedGranularity));
				}
			}
		}
		assertEquals(expectedHashes, allStateNodeHashcodes.size());
		return allStateNodeHashcodes;
	}
	
	@Test
	public void testSpeedHashing() {
		for (int g = 1; g < 64; g += 3) {
			final HashSet<Short> allSpeedHashed = new HashSet<Short>();
			
			//adding the edges twice should give the same size unlees some hashes are the same
			for (int i = 0; i < 2; i++) {
				//Because of the small state space, brute force over the statespace will be used to check its correctness.
				final ArrayList<Short> allSpeedList = getAllPossibleSpeedHashcodes(g);
				allSpeedHashed.addAll(allSpeedList);
				assertEquals(allSpeedList.size() / 3, allSpeedHashed.size());
			}
		}
	}
	private ArrayList<Short> getAllPossibleSpeedHashcodes(int speedGranularity) {
		final float speedLimit = MarioControls.MAX_X_VELOCITY;
		final int expectedHashes = (speedGranularity * 2 + 1) * 3;
		ArrayList<Short> allSpeedHashcodes = new ArrayList<Short>();
		
		final float speedIncrements = speedLimit / speedGranularity;
		
		for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
			//final StateNode sn = new StateNode(new Node(x, y, (byte) 0), v, Hasher.hashEndStateNode(x, y, v));
			allSpeedHashcodes.add(Hasher.hashSpeed(v, speedGranularity));
			allSpeedHashcodes.add(Hasher.hashSpeed(v + speedIncrements / 3, speedGranularity));
			allSpeedHashcodes.add(Hasher.hashSpeed(v - speedIncrements / 3, speedGranularity));
		}
		assertEquals("granularity: " + speedGranularity, expectedHashes, allSpeedHashcodes.size());
		return allSpeedHashcodes;
	}
	
	@Test
	public void testStateNodeWithEdgesHashing() {
		for (int g = 1; g < 20; g += 7) {
			final LongOpenHashSet allStateNodesHashed = new LongOpenHashSet();
			
			//adding the edges twice should give the same size unlees some hashes are the same
			for (int i = 0; i < 2; i++) {
				//Because of the small state space, brute force over the statespace will be used to check its correctness.
				final LongArrayList allStateNodesList = getAllPossibleStateNodesWithEdgesHashcodes(g);
				allStateNodesHashed.addAll(allStateNodesList);
				assertEquals(allStateNodesList.size() / 3, allStateNodesHashed.size());
			}
		}
	}
	private LongArrayList getAllPossibleStateNodesWithEdgesHashcodes(int speedGranularity) {
		final int limitY = LIMIT_Y;
		final int limitX = LIMIT_X;
		final float speedLimit = MarioControls.MAX_X_VELOCITY;
		final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2) * (speedGranularity * 2 + 1) * 3 * 2;
		LongArrayList allStateNodeHashcodes = new LongArrayList();
		
		final float speedIncrements = speedLimit / speedGranularity;
		
		for (short sourceY = 0; sourceY <= limitY; sourceY++) {
			for (short sourceX = 0; sourceX <= limitX; sourceX++) {	
				final Node source = new Node(sourceX, sourceY, (byte)10);
				for (short targetY = 0; targetY <= limitY; targetY++) {
					for (short targetX = 0; targetX <= limitX; targetX++) {
						final Node target = new Node(targetX, targetY, (byte)10);
						final RunningEdge edge1 = new RunningEdge(source, target, false);
						for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
							//final StateNode sn = new StateNode(new Node(x, y, (byte) 0), v, Hasher.hashEndStateNode(x, y, v));
							allStateNodeHashcodes.add(Hasher.hashStateNode(v, edge1, speedGranularity));
							allStateNodeHashcodes.add(Hasher.hashStateNode(v + speedIncrements / 3, edge1, speedGranularity));
							allStateNodeHashcodes.add(Hasher.hashStateNode(v - speedIncrements / 3, edge1, speedGranularity));
						}
						
						final RunningEdge edge2 = new RunningEdge(source, target, true);
						for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
							//final StateNode sn = new StateNode(new Node(x, y, (byte) 0), v, Hasher.hashEndStateNode(x, y, v));
							allStateNodeHashcodes.add(Hasher.hashStateNode(v, edge2, speedGranularity));
							allStateNodeHashcodes.add(Hasher.hashStateNode(v + speedIncrements / 3, edge2, speedGranularity));
							allStateNodeHashcodes.add(Hasher.hashStateNode(v - speedIncrements / 3, edge2, speedGranularity));
						}
					}
				}
			}
		}
		assertEquals(expectedHashes, allStateNodeHashcodes.size());
		return allStateNodeHashcodes;
	}
	
	@Test
	public void testStateNodeWithEdgesHashing2() {
		for (int speedGranularity = 1; speedGranularity < 20; speedGranularity += 7) {
			final LongOpenHashSet allStateNodesHashed = new LongOpenHashSet();
			
			//adding the edges twice should give the same size unlees some hashes are the same
			for (int i = 0; i < 2; i++) {
				//Because of the small state space, brute force over the statespace will be used to check its correctness.
				final int limitY = LIMIT_Y;
				final int limitX = LIMIT_X;
				final int heightLimit = 4;
				final float speedLimit = MarioControls.MAX_X_VELOCITY;
				final int expectedHashes = (int)Math.pow((limitY + 1), 2) * (int)Math.pow((limitX + 1), 2) * (speedGranularity * 2 + 1) * heightLimit * 2;
				LongArrayList allStateNodeHashcodes1 = new LongArrayList();
				LongArrayList allStateNodeHashcodes2 = new LongArrayList();
				LongArrayList allStateNodeHashcodes3 = new LongArrayList();
				
				final float speedIncrements = speedLimit / speedGranularity;
				
				for (short sourceY = 0; sourceY <= limitY; sourceY++) {
					for (short sourceX = 0; sourceX <= limitX; sourceX++) {						
						for (short targetY = 0; targetY <= limitY; targetY++) {
							for (short targetX = 0; targetX <= limitX; targetX++) {
								for (int h = 1; h <= heightLimit; h++) {
									final JumpingEdge edge1 = new JumpingEdge(new Node(sourceX, sourceY, (byte)10), new Node(targetX, targetY, (byte)10), false);
									for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
										//final StateNode sn = new StateNode(new Node(x, y, (byte) 0), v, Hasher.hashEndStateNode(x, y, v));
										allStateNodeHashcodes1.add(Hasher.hashStateNode(v, edge1, speedGranularity));
										allStateNodeHashcodes2.add(Hasher.hashStateNode(v + speedIncrements / 3, edge1, speedGranularity));
										allStateNodeHashcodes3.add(Hasher.hashStateNode(v - speedIncrements / 3, edge1, speedGranularity));
									}
									
									final JumpingEdge edge2 = new JumpingEdge(new Node(sourceX, sourceY, (byte)10), new Node(targetX, targetY, (byte)10), true);
									for (float v = -speedLimit; v <= speedLimit + 0.0001f; v += speedIncrements) {
										//final StateNode sn = new StateNode(new Node(x, y, (byte) 0), v, Hasher.hashEndStateNode(x, y, v));
										allStateNodeHashcodes1.add(Hasher.hashStateNode(v, edge2, speedGranularity));
										allStateNodeHashcodes2.add(Hasher.hashStateNode(v + speedIncrements / 3, edge2, speedGranularity));
										allStateNodeHashcodes3.add(Hasher.hashStateNode(v - speedIncrements / 3, edge2, speedGranularity));
									}
								}
							}
						}
					}
				}
				assertEquals(expectedHashes, allStateNodeHashcodes1.size());
				assertEquals(expectedHashes, allStateNodeHashcodes2.size());
				assertEquals(expectedHashes, allStateNodeHashcodes3.size());

				allStateNodesHashed.addAll(allStateNodeHashcodes1);
				allStateNodesHashed.addAll(allStateNodeHashcodes2);
				allStateNodesHashed.addAll(allStateNodeHashcodes3);
				assertEquals(allStateNodeHashcodes1.size(), allStateNodesHashed.size());
			}
		}
	}
	
}
