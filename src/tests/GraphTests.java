package tests;

import org.junit.Test;

import ch.idsia.mario.environments.Environment;

public class GraphTests {
	
	@Test
	public void TestLoading()
	{
		Environment observation = TestTools.loadLevel("src/tests/testLevels/flat.lvl");
		TestTools.runWholeLevel(observation);
	}

}
