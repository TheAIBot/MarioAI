package tests.massTests;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import MarioAI.FastAndFurious;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import tests.TestTools;
import wox.serial.ShadowTest.Y;

public class testMassCrashSeeds {
	@Test
	public void testCrashSeeds() {
		
		int wins = 0;
		int losses = 0;
		int crashes = 0;
		ArrayList<Integer> crashSeeds = new ArrayList<Integer>(); 
		HashMap<String, Integer> crashStacktrace = new HashMap<String, Integer>();
		
		try (Stream<String> stream = Files.lines(Paths.get(testMassLevels.MASS_CRASHED_SEEDS_FILE_PATH))) {
	        stream.forEach(x -> crashSeeds.add(Integer.parseInt(x)));
		} catch (IOException e1) {
		}
		
		for (int i = 0; i < crashSeeds.size(); i++) {
			FastAndFurious agent = new FastAndFurious();
			agent.DEBUG = false;
			
			Environment observation = TestTools.loadLevelWithSeed(agent, crashSeeds.get(i));
			
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
				final StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				final String key = e.getMessage() + "\n" + sw.toString();
				final int crashCount = crashStacktrace.get(key) == null ? 0 : crashStacktrace.get(key);
				crashStacktrace.put(key, crashCount + 1);
				System.out.println(i);
				continue;
			} catch (Error e) {
				crashes++;
				final StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				final String key = e.getMessage() + "\n" + sw.toString();
				final int crashCount = crashStacktrace.get(key) == null ? 0 : crashStacktrace.get(key);
				crashStacktrace.put(key, crashCount + 1);
				System.out.println(i);
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
		
		if (losses != 0 || crashes != 0) {
			StringBuilder sBuilder = new StringBuilder();
			crashStacktrace.forEach((x, y) -> {
				sBuilder.append("Count: " + y);
				sBuilder.append("\n" + x);
				sBuilder.append("\n\n");
			});
			Assert.fail("\nWins: " + wins + 
						"\nLosses: " + losses + 
						"\nCrashes: " + crashes + 
						"\nWin rate: " + ((float)wins / crashSeeds.size()) * 100 + "%" + 
						"\n\n" + sBuilder.toString());
		}
	}
}
