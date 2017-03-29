package tests;

import MarioAI.MarioMethods;
import ch.idsia.ai.agents.*;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestAgent implements Agent {
	private int tick = 0;	
	private float prevX = 0;
	private float prevY = 0;
	private float startX = 0;
	private float startY = 0;

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		boolean[] actions = new boolean[Environment.numberOfButtons];
		
		if (tick == TestTools.LEVEL_INIT_TICKS) {
			startX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			startY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			actions[Mario.KEY_RIGHT] = true;
		}		
		/*
		int a1 = 1 + TestTools.LEVEL_INIT_TICKS +   8;
		int a3 =                             a1 +  20;
		
		float currentXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		float currentYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		float xChange = currentXPos - prevX;
		
		if (tick > TestTools.LEVEL_INIT_TICKS && tick < a1) {
			System.out.println(prevY - currentYPos);
			//actions[Mario.KEY_RIGHT] = true;
			actions[Mario.KEY_JUMP] = true;
			actions[Mario.KEY_RIGHT] = true;
		}
		else if (tick == a1) {
			//System.out.println("stop jumping");
			System.out.println(prevY - currentYPos);
			actions[Mario.KEY_JUMP] = false;
			actions[Mario.KEY_RIGHT] = true;
			//actions[Mario.KEY_RIGHT] = false;
			//actions[Mario.KEY_LEFT] = true;
			//actions[Mario.KEY_JUMP] = false;
		}
		else if (tick > a1 && tick < a3) {
			System.out.println(prevY - currentYPos);
			actions[Mario.KEY_JUMP] = false;
			actions[Mario.KEY_RIGHT] = true;
			//actions[Mario.KEY_LEFT] = true;
		}
		prevX = currentXPos;
		prevY = currentYPos;
		*/
		
		
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
			System.out.println(MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()) - startY);
			actions[Mario.KEY_RIGHT] = true;
			break;
		case TestTools.LEVEL_INIT_TICKS + 13:
		case TestTools.LEVEL_INIT_TICKS + 14:
		case TestTools.LEVEL_INIT_TICKS + 15:
		case TestTools.LEVEL_INIT_TICKS + 16:
		case TestTools.LEVEL_INIT_TICKS + 17:
		case TestTools.LEVEL_INIT_TICKS + 18:
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
			actions[Mario.KEY_RIGHT] = true;
			actions[Mario.KEY_JUMP] = true;
			System.out.println(MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()) - startY);
		}
		
		
		
		tick++;
		return actions;
	}
	
    public static void main(String[] args) {
        Agent controller = new TestAgent();
        Environment observation = TestTools.loadLevel("flat.lvl", controller, true);
        //Environment observation = TestTools.loadLevel("jumpLevels/jumpDown.lvl", controller, true);
        TestTools.runWholeLevel(observation);
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
