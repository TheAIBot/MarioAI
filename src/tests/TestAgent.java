package tests;

import java.util.ArrayList;

import MarioAI.MarioMethods;
import ch.idsia.ai.agents.*;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestAgent implements Agent {
	private int tick = 0;
	private float maxX = 0;
	private float startX = 1000;
	
	//Fitted estimate of v(t) for Mario
	//v(t) := .340909068708614-.340909068708614*exp(-.116533823678965*t)
	
	private float prevX = 0;

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		boolean[] actions = new boolean[Environment.numberOfButtons];
		startX = Math.min(startX, MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()));
		maxX = Math.max(maxX, MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()));

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
		case TestTools.LEVEL_INIT_TICKS + 11:
		case TestTools.LEVEL_INIT_TICKS + 12:
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
		case TestTools.LEVEL_INIT_TICKS + 40:
		case TestTools.LEVEL_INIT_TICKS + 41:
		case TestTools.LEVEL_INIT_TICKS + 42:
			System.out.print("<" + (tick - (TestTools.LEVEL_INIT_TICKS + 1)) + ", " + ((maxX - startX) - prevX) + ">|");
			prevX = (maxX - startX);
			actions[Mario.KEY_RIGHT] = true;
			break;
			//0   0.0
			//1   0.037499905
			//2   0.108374834
			//3   0.20895362
			//4   0.33596873
			//5   0.48651218
			//6   0.6579957
			//7   0.84811616
			//8   1.0548234
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
