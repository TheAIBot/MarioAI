package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.enemySimuation.EnemyPredictor;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.JumpingEdge;
import MarioAI.graph.edges.edgeCreation.EdgeCreator;
import MarioAI.graph.edges.edgeCreation.JumpDirection;
import MarioAI.graph.nodes.*;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.mario.environments.Environment;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
/**
 * 
 * @author Andreas
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestEnemyCollisionDetection {
	boolean[] action;
	UnitTestAgent agent;
	Environment observation;
	MarioControls marioController;
	Node[][] level;
	Node marioNode;
	World graph = new World();
	EdgeCreator grapher = new EdgeCreator();
	
	
	public void startup(){
		action = new boolean[Environment.numberOfButtons];	
		agent = new UnitTestAgent();		
		observation = TestTools.loadLevel("flat.lvl", agent, false);	
		graph.initialize(observation);
		level = graph.getLevelMatrix();
		marioNode = graph.getMarioNode(observation);
		marioController = new MarioControls();
	}
		
	@Test
	public void testCorrectMarioBlockPlacementDetection(){
		EnemyPredictor enemyPredictor = new EnemyPredictor();
		startup();
		ArrayList<DirectedEdge> Path = new ArrayList<DirectedEdge>(); //Lenght 1 path. Testing one edge.
		JumpingEdge polynomial =  new JumpingEdge(marioNode, null);
		List<DirectedEdge> listOfEdges = new ArrayList<DirectedEdge>();
		polynomial.setToJumpPolynomial(marioNode, 11, 4, 4);
		grapher.setMovementEdges(graph, marioNode);
		grapher.jumpAlongPolynomial(marioNode, 11, polynomial, JumpDirection.RIGHT_UPWARDS, listOfEdges);
		
		assertEquals(1, listOfEdges.size());//One edge should exist.
		Path.add(listOfEdges.get(0));
		
		final float marioStartXPos =  MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float marioStartYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		

		action = marioController.getNextAction(observation, Path);
		agent.action = action;
		TestTools.runOneTick(observation);
		
		//System.out.println("Tick " + 0 + " -> (" + marioXPos + "," + marioYPos + ")");
		
		//Checking the path that mario would make with this:	
		for (int i = 1; i <= 100; i++) {
			action = marioController.getNextAction(observation, Path);
			agent.action = action;
			TestTools.runOneTick(observation);
			
			marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			
			System.out.println("Tick " + i + " -> (" + marioXPos + "," + marioYPos + ")");
			if (observation.isMarioOnGround()) {
				break;
			}
		}
		
		System.out.println("\nCompare with:\n");
		
		//final MovementInformation movementInformation = MarioControls.getMovementInformationFromEdge(marioStartXPos, marioStartYPos, Path.get(0).target, Path.get(0), 0);
		
		//assertFalse(marioController.doesMovementCollideWithEnemy(1, Path.get(0), marioStartXPos, marioStartYPos, 0, movementInformation));
		
	}
	
}
