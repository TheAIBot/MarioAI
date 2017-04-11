package ch.idsia.scenarios;

import MarioAI.FastAndFurious;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.mario.environments.Environment;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import tests.TestTools;

public class Play {

	public static void main(String[] args) {
		boolean loadLevel = false;
		if (loadLevel) {
			Agent controller = new FastAndFurious();
			// Agent controller = new HumanKeyboardAgent();
			Environment observation = TestTools.loadLevel("flat.lvl", controller, true);
			//Environment observation = TestTools.loadLevel("jumpLevels/jumpDownLevels/jumpDown1.lvl", controller, true);
			//Environment observation = TestTools.loadLevel("TestAStarJump.lvl", controller, true);
			// controller, true);
			TestTools.runWholeLevel(observation);
		} else {
	        Agent controller = new FastAndFurious();	        
	        EvaluationOptions options = new CmdLineOptions(new String[0]);
	        options.setAgent(controller);
	        Task task = new ProgressTask(options);
	        options.setMaxFPS(false);
	        options.setVisualization(true);
	        options.setNumberOfTrials(1);
	        options.setMatlabFileName("");
	        int seed = (int) (Math.random() * Integer.MAX_VALUE);
	        options.setLevelRandSeed(seed);
	        System.out.println("Seed = " + seed);
	        //options.setLevelRandSeed(2);
	        //options.setLevelRandSeed(41);
	        //options.setLevelRandSeed(42);
	        //options.setLevelRandSeed(650);
	        //options.setLevelRandSeed(666);
	        //options.setLevelRandSeed(42243);
	        //options.setLevelRandSeed(1905810938); //Bug here without running edges
	        /*
	         * 
1549733898
793284811
640346535
1772112418
232887628
	         */
	        
	        
	        //options.setLevelRandSeed(42243);(*) Includes a missing feature.
	        options.setLevelDifficulty(-1);
	        task.setOptions(options);
	        
	        System.out.println ("Score: " + task.evaluate (controller)[0]);
		}
	}
}
