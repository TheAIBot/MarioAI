package tests;

import java.util.ArrayList;

import MarioAI.MarioControls;
import MarioAI.MarioMethods;
import ch.idsia.ai.agents.*;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestAgent implements Agent {
	private int tick = 0;
	//private float maxX = 0;
	//private float startX = 1000;
	
	//Fitted estimate of v(t) for Mario
	//v(t) := .340909068708614-.340909068708614*exp(-.116533823678965*t)
	
	private float prevX = 0;

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		boolean[] actions = new boolean[Environment.numberOfButtons];
		
		if (tick == TestTools.LEVEL_INIT_TICKS) {
			prevX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		}
		else if (tick > TestTools.LEVEL_INIT_TICKS) {
			actions[Mario.KEY_RIGHT] = true;
			float marioX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			System.out.println(marioX - prevX);
			prevX = marioX;
		}
		
		/*
		startX = Math.min(startX, MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()));
		maxX = Math.max(maxX, MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos()));

		int a1 = TestTools.LEVEL_INIT_TICKS +  200;
		int a3 =                         a1 +  8;
		
		if (tick >= TestTools.LEVEL_INIT_TICKS && tick < a1) {
			System.out.println((maxX - startX) - prevX);
			actions[Mario.KEY_RIGHT] = true;
		}
		else if (tick == a1) {
			System.out.println((maxX - startX) - prevX);
			System.out.println("reversing");
			actions[Mario.KEY_LEFT] = true;
		}
		else if (tick > a1 && tick < a3) {
			System.out.println((maxX - startX) - prevX);
			actions[Mario.KEY_LEFT] = true;
		}
		prevX = (maxX - startX);

		*/
		tick++;
		//System.out.println(MarioControls.getDeaccelerationDistanceMoved(tick));
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
