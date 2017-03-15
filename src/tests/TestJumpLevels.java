package tests;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.FastAndFurious;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TestJumpLevels {
	
	@Test
	public void test1WidthJump() {
		testLevel("jumpLevels/1Width.lvl");
	}
	
	@Test
	public void test2WidthJump() {
		testLevel("jumpLevels/2Width.lvl");
	}
	
	@Test
	public void test3WidthJump() {
		testLevel("jumpLevels/3Width.lvl");
	}
	
	@Test
	public void test4WidthJump() {
		testLevel("jumpLevels/4Width.lvl");
	}
	
	private void testLevel(String levelPath) {
		final FastAndFurious agent = new FastAndFurious();		
		Environment observation = TestTools.loadLevel(levelPath, agent);
		
		/*
		for (int i = 0; i < 2000; i++) {
			TestTools.runOneTick(observation);
			
			if (((MarioComponent) observation).getMarioMode() != Mario.STATUS_RUNNING) {
				break;
			}
			
		}*/
		TestTools.runWholeLevel(observation);
		
		final int mode = ((MarioComponent) observation).getMarioStatus();
		if (mode != Mario.STATUS_WIN) {
			Assert.fail("Failed to complete " + levelPath);
		}
	}

}
