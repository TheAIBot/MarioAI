package tests.massTests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Test;


import MarioAI.FastAndFurious;
import MarioAI.MarioMethods;
import MarioAI.path.AStar;
import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import tests.TestTools;
/**
 * 
 * @author Andreas
 *
 */
public class MassCreateStatistics {
	private static final int LEVEL_CRASHED = -1;
	private static final int LEVEL_LOSSED = 0;
	private static final int LEVEL_WON = 1;
	private static final int START_LIVES = 3;
	private static final int NO_ENEMY = -1;
	
	@Test
	public void createStatisticsWithNoEnemies() {
		createStatistics(-1);
	}
	
	@Test
	public void createStatisticsWithDifficulty0() {
		createStatistics(0);
	}
	
	@Test
	public void createStatisticsWithDifficulty1() {
		createStatistics(1);
	}
	
	@Test
	public void createStatisticsWithDifficulty2() {
		createStatistics(2);
	}
	
	@Test
	public void createStatisticsWithDifficulty4() {
		createStatistics(4);
	}
	
	@Test
	public void createStatisticsWithDifficulty8() {
		createStatistics(8);
	}
	
	@Test
	public void createStatisticsWithDifficulty20() {
		createStatistics(20);
	}
	
	private void createStatistics(int difficulty) {
		final ArrayList<LevelInfo> levelInfos = new ArrayList<LevelInfo>();
		
		for (int i = 0; i < 200; i++) {
			FastAndFurious agent = new FastAndFurious();
			agent.DEBUG = false;
			int seed = (int) (Math.random () * Integer.MAX_VALUE);
			Environment observation = TestTools.loadLevelWithSeed(agent, seed, difficulty, false);
			
			int ticksRun = 0;
			int howLevelWasEnded = 0;
			int livesLost = 0;
			
			try {
				final int MAX_TICKS_TO_WIN_LEVEL = 1700;
				for (ticksRun = 0; ticksRun < MAX_TICKS_TO_WIN_LEVEL; ticksRun++) {
					final int status = TestTools.runOneTick(observation);

					if (status != Mario.STATUS_RUNNING) {
						break;
					}
				}
			} catch (Exception e) {
				howLevelWasEnded = LEVEL_CRASHED;
				livesLost = START_LIVES - MarioMethods.getMarioLives(observation.getMarioMode());
				
			} catch (Error e) {
				howLevelWasEnded = LEVEL_CRASHED;
				livesLost = START_LIVES - MarioMethods.getMarioLives(observation.getMarioMode());
			}

			if (howLevelWasEnded != LEVEL_CRASHED) {
				final int status = ((MarioComponent) observation).getMarioStatus();
				if (status == Mario.STATUS_WIN) {
					howLevelWasEnded = LEVEL_WON;
					livesLost = START_LIVES - MarioMethods.getMarioLives(observation.getMarioMode());
				}
				else {
					howLevelWasEnded = LEVEL_LOSSED;
					livesLost = START_LIVES;
				}	
			}
			
			levelInfos.add(new LevelInfo(difficulty, ticksRun, howLevelWasEnded, 
										 livesLost, AStar.timesAStarHasRun, AStar.timesAStarDidNotFinish, 
										 AStar.totalTimeUsedByAStar, AStar.neighborsAsChildsCount, AStar.neighborsAsParentsCount));
		}
		
		saveStatistics(difficulty, levelInfos);
	}
	
	private void saveStatistics(int difficulty, ArrayList<LevelInfo> levelInfos) {
		String path = testMassLevels.MASS_LEVEL_DIRETORY + "LevelStaticsticsDifficulty-" + difficulty + ".txt";
		try {
			final Path filePath = Paths.get(path);
			
			if (Files.exists(filePath)) {
				Files.delete(filePath);
			}
			
			StringBuilder sBuilder = new StringBuilder();
			levelInfos.stream().forEach(x -> sBuilder.append(x.toString() + "\n"));
		    Files.write(filePath, sBuilder.toString().getBytes(), StandardOpenOption.CREATE);
		}catch (IOException e) {
			Assert.fail("Couldn't save statistics to: " + path);
		}
	}
	
}
