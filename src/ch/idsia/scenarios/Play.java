package ch.idsia.scenarios;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import MarioAI.CollisionDetection;
import MarioAI.FastAndFurious;
import MarioAI.enemySimuation.EnemyType;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.mario.engine.sprites.Enemy;
import ch.idsia.mario.environments.Environment;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import tests.TestTools;
import tests.UnitTestAgent;

public class Play {


	public static void main(String[] args) {
		boolean loadLevel = false;
		if (loadLevel) {
			FastAndFurious controller = new FastAndFurious();
			//Agent controller = new UnitTestAgent();
			//((UnitTestAgent) controller).action[0] = true;
			//Agent controller = new HumanKeyboardAgent();
			//Environment observation = TestTools.loadLevel("jumpLevels/jumpDownLevels/jumpDown1.lvl", controller, true);
			//Environment observation = TestTools.loadLevel("jumpLevels/randomWidthJump.lvl", controller, true);
			Environment observation = TestTools.loadLevel("plainbox.lvl", controller, true);
			TestTools.spawnEnemy(observation, 4, 6, 1, EnemyType.BULLET_BILL);
			//TestTools.setMariogetRunningReachableEdgesPosition(observation, 6, 8);
			//Environment observation = TestTools.loadLevel("flat.lvl", controller, true);
			//Environment observation = TestTools.loadLevel("straightTunnel.lvl", controller, true);
			//Environment observation = TestTools.loadLevel("jumpLevels/jumpStraightUp.lvl", controller, true);
			//TODO bug i collision detection for level = TheMazeError.
			//TODO bug i collision detection for level = thinStairs.
			//TestTools.setMarioPosition(observation, 15, 10);
			//Environment observation = TestTools.loadLevel("jumpLevels/only1Width.lvl", controller, true);
			//Environment observation = TestTools.loadLevel("deadend1.lvl", controller, true);
	        if (new File(FastAndFurious.saveStateFileName).exists()) {
	        	String fileContent = null;
				try {
					fileContent = Files.readAllLines(Paths.get(FastAndFurious.saveStateFileName)).get(0);
					final int tick = Integer.parseInt(fileContent.split(" ")[1]);
					controller.runToTick(tick);
				} catch (IOException e) {
					System.out.println("Failed to load level state");
				}
			}
			TestTools.runWholeLevel(observation);
		} else {
			FastAndFurious controller = new FastAndFurious();
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
	        if (new File(FastAndFurious.saveStateFileName).exists()) {
	        	String fileContent = null;
				try {
					fileContent = Files.readAllLines(Paths.get(FastAndFurious.saveStateFileName)).get(0);
					
					final long loadedSeed = Long.parseLong(fileContent.split(" ")[0]);
					final int tick = Integer.parseInt(fileContent.split(" ")[1]);
					
					controller.runToTick(tick);
					options.setLevelRandSeed((int)loadedSeed);
					options.setMaxFPS(true);
				} catch (IOException e) {
					System.out.println("Failed to load level state");
				}
			}
	        System.out.println("Seed = " + seed);
	        //options.setLevelRandSeed(136829693);
	        //options.setLevelRandSeed(238114835);
	        //options.setLevelRandSeed(898452612); //Difficulty 1
	        //options.setLevelRandSeed(632962519); //Difficulty 1, good seed
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
1671739449 // gets stuck because cannot find path due to enemy (RED GOOMBA) 1/5th in
916101382 // stuck in beginning because of drop down + gap
	         */
	        
	        //options.setLevelRandSeed(42243);(*) Includes a missing feature.
	        //options.setLevelDifficulty(2);
	        options.setLevelDifficulty(4);	 
	        task.setOptions(options);
	        
	        System.out.println ("Score: " + task.evaluate (controller)[0]);
		}
	}
}
