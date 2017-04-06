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
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import tests.TestTools;

public class testMassLevels {
	
	public static final String MASS_LEVEL_RESULTS_FILE_PATH = "src/tests/massTests/MassLevelsResults.txt";
	public static final String MASS_LEVELS_LOSSED_FILE_PATH = "src/tests/massTests/MassLossedSeeds.txt";
	public static final String MASS_CRASHED_SEEDS_FILE_PATH = "src/tests/massTests/MassCrashedSeeds.txt";
	
	@Test
	public void test1000RandomLevels() {
		
		int wins = 0;
		int losses = 0;
		int crashes = 0;
		ArrayList<Integer> crashedSeeds = new ArrayList<Integer>();
		ArrayList<Integer> lossedSeeds = new ArrayList<Integer>();
		
		for (int i = 0; i < 1000; i++) {
			FastAndFurious agent = new FastAndFurious();
			agent.DEBUG = false;
			int seed = (int) (Math.random () * Integer.MAX_VALUE);
			Environment observation = TestTools.loadLevelWithSeed(agent, seed);
			
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
				crashedSeeds.add(seed);
				continue;
			} catch (Error e) {
				crashes++;
				crashedSeeds.add(seed);
				continue;
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
		
		appendStringToFile(MASS_LEVEL_RESULTS_FILE_PATH, wins + ", " + losses + ", " + crashes + "\n");
		
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
