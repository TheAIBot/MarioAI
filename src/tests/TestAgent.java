package tests;

import java.util.ArrayList;

import MarioAI.MarioMethods;
import ch.idsia.ai.agents.*;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestAgent implements Agent {
	private int tick = 0;
	ArrayList<Float> heights = new ArrayList<Float>();

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		boolean[] actions = new boolean[Environment.numberOfButtons];
		heights.add(MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()));

		switch (tick) {
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
		//case TestTools.LEVEL_INIT_TICKS + 11:
			actions[Mario.KEY_JUMP] = true;
			break;
		}
		
		if (tick == TestTools.LEVEL_INIT_TICKS + 50) {
			float lowest = 0;
			float highest = 100;
			for (int i = TestTools.LEVEL_INIT_TICKS; i < heights.size(); i++) {
				lowest = Math.max(lowest, heights.get(i));
				highest = Math.min(highest, heights.get(i));
			}
			System.out.println("jump height: " + (lowest - highest));
		}

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
