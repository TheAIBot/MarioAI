package MarioAI;

import java.util.ArrayList;
import java.util.HashMap;

import ch.idsia.mario.environments.Environment;

public class Graph {
	public static final int LEVEL_HEIGHT = 15;
	public static final int SIGHT_WIDTH = 22;
	private static final int MARIO_START_X_POS = 32;
	private static final int LEVEL_START_SCOLLING = 160;
	
	private final ArrayList<Surface>[] surfaces = (ArrayList<Surface>[])new Object[LEVEL_HEIGHT];
	private final Node[][] levelMatrix = new Node[SIGHT_WIDTH][LEVEL_HEIGHT];
	private final HashMap<Integer, Node[]> savedColumns = new HashMap<Integer, Node[]>();
	private int oldMarioXPos = MARIO_START_X_POS;
	
	public Graph()
	{
		for (short x = 0; x < levelMatrix.length; x++) {
			for (short y = 0; y < levelMatrix[0].length; y++) {
				levelMatrix[x][y] = new Node(x, y);	
			}
		}
	}
	
	public void updateMatrix(Environment observer)
	{
		//observer.
		int marioXPos = (int)observer.getMarioFloatPos()[0];
		int change = marioXPos - oldMarioXPos;
	}
	/*
	private void moveMatrixOneLeft(Environment observer, int leftAmount)
	{
		//save column that will be overwritten
		saveColumn(levelMatrix[0]);
		//move columns left
		for (int x = 1; x < levelMatrix.length; x++) {
			levelMatrix[x - 1] = levelMatrix[x];
		}
		
		if (containsColumn(levelMatrix.length)) {
			
		}
		
		for (short x = levelMatrix.length - leftAmount; x < levelMatrix.length; x++) {
			
		}
	}
	
	private void saveColumn(Node[] column)
	{
		
	}
	
	private boolean containsColumn(short x)
	{
		
	}*/
	
	private Node[] getColumn(short x)
	{
		return null;
	}
}
