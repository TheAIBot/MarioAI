package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.FastAndFurious;
import MarioAI.MarioMethods;
import MarioAI.graph.Graph;
import MarioAI.graph.Grapher;
import MarioAI.graph.JumpDirection;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.graph.edges.Running;
import MarioAI.graph.edges.SecondOrderPolynomial;
import MarioAI.graph.nodes.*;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import tests.*;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestEnemyCollisionDetection{
	boolean[] action;
	UnitTestAgent agent;
	Environment observation;
	MarioControls marioController;
	Node[][] level;
	Node marioNode;
	Graph graph = new Graph();
	Grapher grapher = new Grapher();
	
	
	public void startup(){
		boolean[] action = new boolean[Environment.numberOfButtons];	
		agent = new UnitTestAgent();		
		Environment observation = TestTools.loadLevel("flat.lvl", agent, false);	
		graph.createStartGraph(observation);
		level = graph.getLevelMatrix();
		marioNode = graph.getMarioNode(observation);
		MarioControls marioController = new MarioControls();
	}
	
	
	@Test
	public void testCorrectMarioBlockPlacementDetection(){//TODO maek version with initial speed.
		startup();
		List<DirectedEdge> Path = new ArrayList<DirectedEdge>(); //Lenght 1 path. Testing one edge.
		SecondOrderPolynomial polynomial =  new SecondOrderPolynomial(marioNode, null);
		List<DirectedEdge> listOfEdges = new ArrayList<DirectedEdge>();
		polynomial.setToJumpPolynomial(marioNode, 11, 4, 4);
		grapher.jumpAlongPolynomial(marioNode, 11, polynomial, JumpDirection.RIGHT_UPWARDS, listOfEdges);
		
		assertEquals(1, listOfEdges.size());//One edge should exist.
		Path.add(listOfEdges.get(0));
		
		//Checking the path that mario would make with this:	
		for (int i = 0; i < 20; i++) {
			marioController.getNextAction(observation, Path, action);
			TestTools.runOneTick(observation);
			
			final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			final float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			
			System.out.println("Tick " + i + " -> (" + marioXPos + "," + marioYPos + ")");
			
			if (observation.isMarioOnGround()) {
				break;
			}
		}
		
	}
	
}
