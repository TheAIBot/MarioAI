package tests;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.FastAndFurious;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class testMassLevels {
	
	@Test
	public void test1000RandomLevels() {
		
		int wins = 0;
		int losses = 0;
		int crashes = 0;
		
		for (int i = 0; i < 1000; i++) {
			Agent agent = new FastAndFurious();
			Environment observation = TestTools.loadRandomLevel(agent);
			
			try {
				for (int x = 0; x < 4000; x++) {
					TestTools.runOneTick(observation);

					final int status = ((MarioComponent) observation).getMarioStatus();
					if (status != Mario.STATUS_RUNNING) {
						break;
					}
				}
			} catch (Exception e) {
				crashes++;
				continue;
			}

			
			final int status = ((MarioComponent) observation).getMarioStatus();
			if (status == Mario.STATUS_WIN) {
				wins++;
			}
			else {
				losses++;
			}
			System.out.println(i);
		}
		
		if (losses != 0) {
			Assert.fail("\nWins: " + wins + 
						"\nLosses: " + losses + 
						"\nCrashes: " + crashes + 
						"\nWin rate: " + ((float)wins / 1000) * 100 + "%");
		}
	}

}
