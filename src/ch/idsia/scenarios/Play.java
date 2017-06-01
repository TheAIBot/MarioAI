package ch.idsia.scenarios;

import MarioAI.FastAndFurious;
import MarioAI.graph.CollisionDetection;
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
		CollisionDetection.loadTileBehaviors();
		boolean loadLevel = false;
		if (loadLevel) {
			Agent controller = new FastAndFurious();
			//Agent controller = new HumanKeyboardAgent();
			// Agent controller = new HumanKeyboardAgent();
			//Environment observation = TestTools.loadLevel("jumpLevels/jumpDownLevels/jumpDown1.lvl", controller, true);
			//Environment observation = TestTools.loadLevel("jumpLevels/randomWidthJump.lvl", controller, true);
			Environment observation = TestTools.loadLevel("TheMaze.lvl", controller, true);
			//TODO bug i collision detection for level = TheMazeError.
			//TODO bug i collision detection for level = thinStairs.
			//TestTools.setMarioPosition(observation, 15, 10);
			//Environment observation = TestTools.loadLevel("jumpLevels/only1Width.lvl", controller, true);
			//Environment observation = TestTools.loadLevel("deadend1.lvl", controller, true);
			TestTools.runWholeLevel(observation);
		} else {
	        Agent controller = new FastAndFurious();
	        //HumanKeyboardAgent controller = new HumanKeyboardAgent();
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
	        options.setLevelRandSeed(632962519); //Difficulty 1, good seed
	        //options.setLevelRandSeed(860788790);
	        //options.setLevelRandSeed(1145934057);
	        //options.setLevelRandSeed(1319952038); //Difficulty 2, error, especially with reusing speed nodes.
	        //options.setLevelRandSeed(1585046168);
	        //options.setLevelRandSeed(3261372);
	        //options.setLevelRandSeed(41);
	        //options.setLevelRandSeed(42);
	        //options.setLevelRandSeed(650);
	        //options.setLevelRandSeed(666);
	        //options.setLevelRandSeed(42243);
	        //options.setLevelRandSeed(1028660435);
	        //options.setLevelRandSeed(1905810938); //Bug here without running edges

	        /*
860788790 //Error at difficulty 2
1145934057 //Difficulty -1. Error with mario movement.
1145934057
1549733898
793284811
640346535
1772112418
232887628
500432374 //Difficulty 2, fejl i collision engine
	         */
	        
	        //options.setLevelRandSeed(42243);(*) Includes a missing feature.
	        options.setLevelDifficulty(1);
	        task.setOptions(options);
	        
	        System.out.println ("Score: " + task.evaluate (controller)[0]);
		}
	}
}
