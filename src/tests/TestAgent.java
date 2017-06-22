package tests;

import java.util.ArrayList;

import MarioAI.MarioMethods;
import MarioAI.World;
import MarioAI.graph.edges.DirectedEdge;
import MarioAI.marioMovement.MarioControls;
import ch.idsia.ai.agents.*;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
/** 
 * @author Andreas
 *
 */
public class TestAgent implements Agent {
	private int tick = 0;	
	private float prevX = 0;
	private float prevY = 0;
	private float startX = 0;
	private float startY = 0;

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		final boolean[] actions = new boolean[Environment.numberOfButtons];
		final float currentXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		final float currentYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		
		actions[Mario.KEY_RIGHT] = true;
		actions[Mario.KEY_SPEED] = true;
		
		System.out.println(currentXPos - prevX);
		prevX = currentXPos;
		
		
		/*
		if (tick == TestTools.LEVEL_INIT_TICKS) {
			startX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			startY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			actions[Mario.KEY_RIGHT] = true;
		}		
		
		
		int a1 = 1 + TestTools.LEVEL_INIT_TICKS +  50;
		int a3 =                             a1 +  20;
		
		float xChange = currentXPos - prevX;
		
		if (tick > TestTools.LEVEL_INIT_TICKS && tick < a1) {
			//System.out.println(prevY - currentYPos);
			System.out.println(currentXPos - startX);
			//actions[Mario.KEY_RIGHT] = true;
			//actions[Mario.KEY_JUMP] = true;
			actions[Mario.KEY_RIGHT] = true;
		}
		else if (tick == a1) {
			//System.out.println("stop jumping");
			//System.out.println(prevY - currentYPos);
			System.out.println(currentXPos - startX);
			//actions[Mario.KEY_JUMP] = false;
			actions[Mario.KEY_RIGHT] = true;
			//actions[Mario.KEY_RIGHT] = false;
			//actions[Mario.KEY_LEFT] = true;
			//actions[Mario.KEY_JUMP] = false;
		}
		else if (tick > a1 && tick < a3) {
			//System.out.println(prevY - currentYPos);
			System.out.println(currentXPos - startX);
			//actions[Mario.KEY_JUMP] = false;
			actions[Mario.KEY_RIGHT] = true;
			//actions[Mario.KEY_LEFT] = true;
		}
		prevX = currentXPos;
		prevY = currentYPos;
		
		*/
		/*
		switch (tick) {
		case TestTools.LEVEL_INIT_TICKS:
			startX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			startY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			break;
		case TestTools.LEVEL_INIT_TICKS + 1:
		case TestTools.LEVEL_INIT_TICKS + 2:
		case TestTools.LEVEL_INIT_TICKS + 3:
		case TestTools.LEVEL_INIT_TICKS + 4:
		case TestTools.LEVEL_INIT_TICKS + 5:
		case TestTools.LEVEL_INIT_TICKS + 6:
		case TestTools.LEVEL_INIT_TICKS + 7:
		case TestTools.LEVEL_INIT_TICKS + 8:
		case TestTools.LEVEL_INIT_TICKS + 9:
		case TestTools.LEVEL_INIT_TICKS + 10:
		case TestTools.LEVEL_INIT_TICKS + 11:
		case TestTools.LEVEL_INIT_TICKS + 12:
		case TestTools.LEVEL_INIT_TICKS + 13:
		case TestTools.LEVEL_INIT_TICKS + 14:
		case TestTools.LEVEL_INIT_TICKS + 15:
		case TestTools.LEVEL_INIT_TICKS + 16:
		case TestTools.LEVEL_INIT_TICKS + 17:
		case TestTools.LEVEL_INIT_TICKS + 18:
			System.out.println(currentXPos - startX);
			//System.out.println(currentYPos - startY);
			prevX = currentXPos;
			startY = currentYPos;
			actions[Mario.KEY_RIGHT] = true;
			break;

		case TestTools.LEVEL_INIT_TICKS + 19:
		case TestTools.LEVEL_INIT_TICKS + 20:
		case TestTools.LEVEL_INIT_TICKS + 21:
		case TestTools.LEVEL_INIT_TICKS + 22:
		case TestTools.LEVEL_INIT_TICKS + 23:
		case TestTools.LEVEL_INIT_TICKS + 24:
		case TestTools.LEVEL_INIT_TICKS + 25:
		case TestTools.LEVEL_INIT_TICKS + 26:
		case TestTools.LEVEL_INIT_TICKS + 27:
		case TestTools.LEVEL_INIT_TICKS + 28:
		case TestTools.LEVEL_INIT_TICKS + 29:
		case TestTools.LEVEL_INIT_TICKS + 30:
		case TestTools.LEVEL_INIT_TICKS + 31:
		case TestTools.LEVEL_INIT_TICKS + 32:
		case TestTools.LEVEL_INIT_TICKS + 33:
		case TestTools.LEVEL_INIT_TICKS + 34:
		case TestTools.LEVEL_INIT_TICKS + 35:
		case TestTools.LEVEL_INIT_TICKS + 36:
		case TestTools.LEVEL_INIT_TICKS + 37:
		case TestTools.LEVEL_INIT_TICKS + 38:
		case TestTools.LEVEL_INIT_TICKS + 39:
			System.out.println(currentXPos - prevX);
			//System.out.println(currentYPos - startY);
			prevX = currentXPos;
			startY = currentYPos;
			actions[Mario.KEY_RIGHT] = false;
			actions[Mario.KEY_LEFT] = true;
			
		}
		*/
		tick++;
		return actions;
	}
	
    public static void main(String[] args) {
    	
    	World world = new World();
    	UnitTestAgent agent = new UnitTestAgent();
    	MarioControls marioControls = new MarioControls();
    	Environment observation = TestTools.loadLevel("flat.lvl", agent, true);
    	//float startXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
    	//float startYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
    	//agent.action[Mario.KEY_RIGHT] = true;
    	//agent.action[Mario.KEY_JUMP] = true;
    	//TestTools.setMarioXPosition(observation, 3);
    	ArrayList<DirectedEdge> path = PathHelper.createPath(1, 1, 1, 0, 0, 1, world, false);
    	
    	MarioAI.debugGraphics.DebugDraw.drawPathParts(observation, path);
    	TestTools.runOneTick(observation);
    	TestTools.renderLevel(observation);
    	System.out.println();
    	//float endXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
    	//float endYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
    	//System.out.println((endXPos - startXPos) + ", " + (endYPos - startYPos));
    	
    	/*
        Agent agent = new TestAgent();
        Environment observation = TestTools.loadLevel("flat.lvl", agent, true);
        //Environment observation = TestTools.loadLevel("jumpLevels/jumpDown.lvl", agent, true);
        TestTools.runWholeLevel(observation);
    	*/
    }

	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	public String getName() {
		return "";
	}

	public void setName(String name) {
	}

}
