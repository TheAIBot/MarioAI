package tests;

import java.util.ArrayList;

import MarioAI.MarioControls;
import MarioAI.MarioMethods;
import ch.idsia.ai.agents.*;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestAgent implements Agent {
	private int tick = 0;	
	private float prevX = 0;
	private float startX = 0;

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		boolean[] actions = new boolean[Environment.numberOfButtons];
/*
		int a1 = TestTools.LEVEL_INIT_TICKS +  40;
		int a3 =                         a1 +  40;
		
		float currentXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		float xChange = currentXPos - prevX;
		
		if (tick >= TestTools.LEVEL_INIT_TICKS && tick < a1) {
			System.out.println(xChange);
			actions[Mario.KEY_RIGHT] = true;
		}
		else if (tick == a1) {
			System.out.println(xChange);
			System.out.println("drifting");
			actions[Mario.KEY_LEFT] = false;
		}
		else if (tick > a1 && tick < a3) {
			System.out.println(xChange);
		}
		prevX = currentXPos;
*/
		
		switch (tick) {
		case TestTools.LEVEL_INIT_TICKS:
			startX = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
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
			actions[Mario.KEY_JUMP] = true;
			System.out.println(startX - MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()));
		}
		
		/*
height
4.1562505
4.069688
3.80861
3.3991938
2.86369
2.2210116
1.4872351
0.6760254
		 */
		
		tick++;
		return actions;
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
