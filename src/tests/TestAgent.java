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
	private float startY = 0;

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
			startX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			startY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		case TestTools.LEVEL_INIT_TICKS + 1:
		case TestTools.LEVEL_INIT_TICKS + 2:
		case TestTools.LEVEL_INIT_TICKS + 3:
		case TestTools.LEVEL_INIT_TICKS + 4:
		case TestTools.LEVEL_INIT_TICKS + 5:
		case TestTools.LEVEL_INIT_TICKS + 6:
		case TestTools.LEVEL_INIT_TICKS + 7:
		case TestTools.LEVEL_INIT_TICKS + 8:
		case TestTools.LEVEL_INIT_TICKS + 9:
			actions[Mario.KEY_RIGHT] = true;
		startX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		startY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			break;
		case TestTools.LEVEL_INIT_TICKS + 10:
		case TestTools.LEVEL_INIT_TICKS + 11:
		case TestTools.LEVEL_INIT_TICKS + 12:
		case TestTools.LEVEL_INIT_TICKS + 13:
		case TestTools.LEVEL_INIT_TICKS + 14:
		case TestTools.LEVEL_INIT_TICKS + 15:
		case TestTools.LEVEL_INIT_TICKS + 16:
			actions[Mario.KEY_JUMP] = true;
			actions[Mario.KEY_RIGHT] = true;	
			System.out.print("<" + (startX - MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos())) + "|");
			System.out.println(startY - MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()) + ">");
			startX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			startY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			break;
		case TestTools.LEVEL_INIT_TICKS + 17:
			actions[Mario.KEY_JUMP] = false;
			actions[Mario.KEY_RIGHT] = false;
			System.out.print("<" + (startX - MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos())) + "|");
			System.out.println(startY - MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()) + ">");
			startX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			startY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
			break;
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
			System.out.print("<" + (startX - MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos())) + "|");
			System.out.println(startY - MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos()) + ">");
			startX = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
			startY = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		}
		
		/*
<-0.2346077|0.0>
<-0.24630094|0.8312502>
<-0.2567079|0.8312502>
<-0.26596975|0.7125001>
<-0.2742133|0.59375>
<-0.28154993|0.4749999>
<-0.28807926|0.3562498>
<-0.29389048|0.23750019>
<-0.26156235|0.01437521>
<-0.23279047|-0.17528105>
<-0.20718384|-0.3364892>
<-0.1843934|-0.4735155>
<-0.16411018|-0.58998823>
<-0.14605808|-0.6889901>
<-0.12999153|-0.7731414>
<-0.115692616|-0.8446703>
<-0.10296631|-0.1697998>
<-0.091639996|0.0>

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
