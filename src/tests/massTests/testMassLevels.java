package tests.massTests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.FastAndFurious;
import MarioAI.MarioMethods;
import MarioAI.graph.edges.AStarHelperEdge;
import MarioAI.path.AStar;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import tests.TestTools;
/**
 * 
 * @author Andreas
 *
 */
public class testMassLevels {
	
	public static final String MASS_LEVEL_DIRETORY = "src/tests/massTests/";
	public static final String MASS_LEVEL_RESULTS_FILE_PATH = MASS_LEVEL_DIRETORY + "MassLevelsResults.txt";
	public static final String MASS_LEVELS_LOSSED_FILE_PATH = MASS_LEVEL_DIRETORY + "MassLossedSeeds.txt";
	public static final String MASS_CRASHED_SEEDS_FILE_PATH = MASS_LEVEL_DIRETORY + "MassCrashedSeeds.txt";
	
	@Test
	public void testWithoutEnemies() {
		test1000RandomLevels(-1);
	}
	
	@Test
	public void testWithDifficulty0() {
		test1000RandomLevels(0);
	}
	
	@Test
	public void testWithDifficulty1() {
		test1000RandomLevels(1);
	}
	
	@Test
	public void testWithDifficulty2() {
		test1000RandomLevels(2);
	}
	
	@Test
	public void testWithDifficulty4() {
		test1000RandomLevels(4);
	}
	
	@Test
	public void testWithDifficulty8() {
		test1000RandomLevels(8);
	}
	
	@Test
	public void testWithDifficulty20() {
		test1000RandomLevels(20);
	}
	
	
	private void test1000RandomLevels(int difficulty) {
		int wins = 0;
		int losses = 0;
		int crashes = 0;
		ArrayList<Integer> crashedSeeds = new ArrayList<Integer>();
		ArrayList<Integer> lossedSeeds = new ArrayList<Integer>();
		
		for (int i = 0; i < 100; i++) {
			FastAndFurious agent = new FastAndFurious();
			agent.DEBUG = false;
			int seed = (int) (Math.random () * Integer.MAX_VALUE);
			Environment observation = TestTools.loadLevelWithSeed(agent, seed, difficulty, false);
			
			int ticksRun = 0;
			
			try {
				final int MAX_TICKS_TO_WIN_LEVEL = 1700;
				for (ticksRun = 0; ticksRun < MAX_TICKS_TO_WIN_LEVEL; ticksRun++) {
					final int status = TestTools.runOneTick(observation);

					if (status != Mario.STATUS_RUNNING) {
						break;
					}
				}
			} catch (Exception e) {
				crashes++;
				crashedSeeds.add(seed);
				
			} catch (Error e) {
				crashes++;
				crashedSeeds.add(seed);
			}

			final int status = ((MarioComponent) observation).getMarioStatus();
			if (status == Mario.STATUS_WIN) {
				wins++;
			}
			else {
				losses++;
				lossedSeeds.add(seed);
			}	
			
			System.out.println(i);
		}
		
		appendStringToFile(MASS_LEVEL_RESULTS_FILE_PATH, wins + ", " + losses + ", " + crashes + ", " + difficulty + "\n");
		
		StringBuilder lossedSBuilder = new StringBuilder();
		lossedSeeds.forEach(x -> lossedSBuilder.append(x.intValue() + "\n"));
		appendStringToFile(MASS_LEVELS_LOSSED_FILE_PATH, lossedSBuilder.toString());
		
		StringBuilder crashesSBuilder = new StringBuilder();
		crashedSeeds.forEach(x -> crashesSBuilder.append(x.intValue() + "\n"));
		appendStringToFile(MASS_CRASHED_SEEDS_FILE_PATH, crashesSBuilder.toString());
		
		if (losses != 0 || crashes != 0) {
			Assert.fail("\nWins: " + wins + 
						"\nLosses: " + losses + 
						"\nCrashes: " + crashes + 
						"\nWin rate: " + ((float)wins / 1000) * 100 + "%");
		}
	}
	
	private void appendStringToFile(String filaPath, String toAppend) {
		try {
			final Path filePath = Paths.get(filaPath);
			
			if (!Files.exists(filePath)) {
				Files.createFile(filePath);
			}
			
		    Files.write(filePath, toAppend.getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
			
		}
	}
}
