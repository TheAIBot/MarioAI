package tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import MarioAI.graph.edges.Running;
import MarioAI.graph.edges.SecondOrderPolynomial;
import MarioAI.graph.nodes.Node;

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
		//Because of the small state space, brute force over the statespace will be used to check its correctness.
		final List<Integer> allJumpingEdgesHashcodes = getAllPossibleJumpingEdgeHashcodes();
		final HashSet<Integer> allRuningEdgesHashed = new HashSet<Integer>();
		
		//adding the edges twice should give the same size unlees some hashes are the same
		for (int i = 0; i < 2; i++) {
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
							final SecondOrderPolynomial newPolynomial = new SecondOrderPolynomial(source,target, sourceY + JumpHeight);
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
		//Because of the small state space, brute force over the statespace will be used to check its correctness.
		final List<Integer> allRunningEdgesList = getAllPossibleRunningEdgeHashcodes();
		final HashSet<Integer> allRuningEdgesHashed = new HashSet<Integer>();
		
		//adding the edges twice should give the same size unlees some hashes are the same
		for (int i = 0; i < 2; i++) {
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
						final Running run = new Running(new Node(sourceX, sourceY, (byte)10), 
		                        				  		new Node(targetX, targetY, (byte)10));
						allRunningEdgesHashcodes.add(run.hashCode());
						
					}
				}
			}
		}
		assertEquals(expectedHashes, allRunningEdgesHashcodes.size());
		return allRunningEdgesHashcodes;
	}
}
