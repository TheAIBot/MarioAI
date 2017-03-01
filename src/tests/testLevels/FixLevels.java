package tests.testLevels;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import ch.idsia.mario.engine.level.Level;

public class FixLevels {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		final String filepath = "src/tests/testLevels/flat.lvl";
		
		Level level = null;
		try {
			level = Level.load(new DataInputStream(new FileInputStream(filepath)));
			
			
			level.xExit = 230;//default end
			level.save(new DataOutputStream(new FileOutputStream(filepath)));
		} catch (Exception e) {
		}
	}
}