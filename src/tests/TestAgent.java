package tests;

import java.util.ArrayList;

import MarioAI.MarioControls;
import MarioAI.MarioMethods;
import ch.idsia.ai.agents.*;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestAgent implements Agent {
	private int tick = 0;
	private float oldXPos = 0;
	
	private float prevX = 0;

	public void reset() {
	}

	public boolean[] getAction(Environment observation) {
		boolean[] actions = new boolean[Environment.numberOfButtons];

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
		
		/*
speed
0.15054345

dirfting
0.13398361
0.11924553
0.106128454
0.09445429
0.084064245
0.07481718
0.06658745
0.059262753
0.05274391
0.046941996
0.041778326
0.037182808
0.033092737
0.0


speed
0.28154993

drifting
0.25057936
0.22301579
0.19848394
0.17665052
0.15721893
0.139925
0.12453318
0.1108346
0.098642826
0.08779192
0.07813501
0.069540024
0.061890602
0.055082798
0.04902363
0.043631077
0.03883171
0.034560204
0.0



//speed
0.33768654

drifting
0.30054092
0.26748085
0.23805809
0.2118721
0.18856621
0.16782379
0.14936352
0.13293266
0.11831093
0.105296135
0.09371376
0.083405495
0.074230194
0.066064835
0.058797836
0.052330017
0.04657364
0.0414505
0.036890984
0.0328331
0.0
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
