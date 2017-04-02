package MarioAI.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import MarioAI.MarioMethods;
import MarioAI.Surface;
import MarioAI.astar.Action;
import MarioAI.astar.Problem;
import MarioAI.astar.SearchNode;
import MarioAI.astar.State;
import MarioAI.graph.nodes.Node;
import ch.idsia.mario.environments.Environment;

public class Graph extends Problem {
	
	public static final int LEVEL_HEIGHT = 15;
	public static final int LEVEL_WIDTH = 22;
	public static final int SIGHT_WIDTH = 22;
	public static final int SIGHT_HEIGHT = 22;
	private static final int LEVEL_START_SCOLLING = 160;
	private static final int BLOCK_PIXEL_SIZE = 16;
	private static final int MARIO_START_X_POS = 2;
	private static final int LEVEL_LEFT_X_POS = -(SIGHT_WIDTH / 2);
	private static final int LEVEL_RIGHT_X_POS = -LEVEL_LEFT_X_POS;

	@SuppressWarnings("unchecked") 
	private final ArrayList<Surface>[] surfaces = (ArrayList<Surface>[]) new ArrayList[LEVEL_HEIGHT]; // pending implementation 
	private final Node[][] levelMatrix = new Node[SIGHT_WIDTH][LEVEL_HEIGHT]; // main graph
	private final HashMap<Integer, Node[]> savedColumns = new HashMap<Integer, Node[]>();
	private int oldMarioXPos = MARIO_START_X_POS;
	private int maxMarioXPos = oldMarioXPos;
	private Node marioNode;
	//private int levelOffSet; // number of nodes moved to the right on the level

	public Node[][] getLevelMatrix(){
		return levelMatrix;
	}
	
	public void printMatrix(Environment observation)
	{
//		if (marioNode != null) {
//			//System.out.println(levelOffSet + "," + marioNode.x + "," + marioNode.y);
//		}

		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		for (int x = 0; x < LEVEL_HEIGHT; x++) {
			for (int y = 0; y < LEVEL_WIDTH; y++) {
				if (marioNode != null && 11 == y && x == marioYPos) {
					System.out.print("M");
					//continue;
				} else if (levelMatrix[y][x] == null) {
					System.out.print(" ");
				}
				else {
					System.out.print("X");
				}
			}
			System.out.println("");
		}
		System.out.println();
	}
	
	public void createStartGraph(final Environment observation) {
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());

		for (int i = 0; i < levelMatrix.length; i++) {
			final byte[] byteColumn = getByteColumnFromLevel(observation.getCompleteObservation(), marioYPos, i);
			final Node[] columnToInsert = convertByteColumnToNodeColumn(byteColumn, (i - (SIGHT_WIDTH / 2)) + marioXPos);
			levelMatrix[i] = columnToInsert;
			//saveColumn((i - (SIGHT_WIDTH / 2)) + marioXPos - 1, columnToInsert);
			final int columnIndex = i + marioXPos - (SIGHT_WIDTH / 2);
			saveColumn(columnIndex, columnToInsert);
		}
		setMarioNode(observation);
		maxMarioXPos = SIGHT_WIDTH / 2;
	}

	public boolean updateMatrix(final Environment observation) {
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		final int change = marioXPos - oldMarioXPos;
		oldMarioXPos = marioXPos;
		maxMarioXPos = Math.max(maxMarioXPos, marioXPos + 10);
		if (change < 0) {
			moveMatrixOneLeft(marioXPos);
			setMarioNode(observation);
			return true;
		} else if (change > 0) {
			moveMatrixOneRight(observation, marioXPos, marioYPos);
			setMarioNode(observation);
			return true;
		}
		if ((short)(marioYPos + 1) != marioNode.y && 
			Grapher.canMarioStandThere((short)11, (short)(marioYPos + 1))) {
			marioNode = new Node((short)marioXPos, (short)(marioYPos + 1), (byte)0);
			return true;
		}
		return false;
	}
	
	private void setMarioNode(final Environment observation) {
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		//limit mario y pos to a position inside the matrix
		marioYPos = Math.min(Math.max(marioYPos, 0), LEVEL_HEIGHT - 1);
		
		marioNode = new Node((short)marioXPos, (short)(marioYPos + 1), (byte)0);
	}

	public Node getMarioNode(final Environment observation)
	{
		return marioNode;
	}
	
	public Node[] getGoalNodes()
	{
		for (int x = levelMatrix.length - 1; x >= 0; x--) {
			for (int y = 0; y < levelMatrix[x].length; y++) {
				if (levelMatrix[x][y] != null) {
					return levelMatrix[x];
				}
			}
		}
		throw new Error("No blocks was found in the matrix");
	}
	
	
	private void moveMatrixOneLeft(final int marioXPos) {
		// move columns right
		for (int x = levelMatrix.length - 1; x > 1; x--) {
			levelMatrix[x] = levelMatrix[x - 1];
		}
		final int columnToInsertXPos = marioXPos - 10;
		final Node[] columnToInsert = getColumn(columnToInsertXPos);
		levelMatrix[0] = columnToInsert;
	}

	private void moveMatrixOneRight(final Environment observation, final int marioXPos, final int marioYPos) {
		// no need to save the column that was overwritten as it's still saved
		// in savedColumns

		// move columns left
		for (int x = 1; x < levelMatrix.length; x++) {
			levelMatrix[x - 1] = levelMatrix[x];
		}
		Node[] columnToInsert;
		final int columnToInsertXPos = marioXPos + 10;
		// get column and insert it into the matrix
		if (containsColumn(columnToInsertXPos)) {
			columnToInsert = getColumn(columnToInsertXPos);
		} else {
			final byte[] byteColumn = getByteColumnFromLevel(observation.getCompleteObservation(), marioYPos);
			columnToInsert = convertByteColumnToNodeColumn(byteColumn, columnToInsertXPos);
			saveColumn(columnToInsertXPos, columnToInsert);
		}

		levelMatrix[levelMatrix.length - 1] = columnToInsert;
		
		//levelOffSet++;
	}

	private byte[] getByteColumnFromLevel(final byte[][] level, final int marioYPos) {
		return getByteColumnFromLevel(level, marioYPos, SIGHT_WIDTH - 1);
	}
	
	private byte[] getByteColumnFromLevel(final byte[][] level, final int marioYPos, final int sightColumnIndex) {
		final byte[] byteColumn = new byte[LEVEL_HEIGHT];		
		final int topObservationYPos = marioYPos - (SIGHT_HEIGHT / 2);
		final int startIndex = Math.max(topObservationYPos, 0);
		final int endIndex = Math.min(Math.min(startIndex + LEVEL_HEIGHT, SIGHT_HEIGHT + topObservationYPos), LEVEL_HEIGHT);
		for (int i = startIndex; i < endIndex; i++) {
			byteColumn[i] = level[i - topObservationYPos][sightColumnIndex];
		}
		return byteColumn;
		
	}

	private Node[] convertByteColumnToNodeColumn(final byte[] byteColumn, final int x) {
		final Node[] nodeColumn = new Node[byteColumn.length];
		for (int y = 0; y < byteColumn.length; y++) {
			if (byteColumn[y] != 0) {
				nodeColumn[y] = new Node((short) x, (short) y, byteColumn[y]);
			}
		}
		return nodeColumn;
	}

	private void saveColumn(final int x, final Node[] column) {
		savedColumns.put(x, column);
	}

	private boolean containsColumn(final int x) {
		return savedColumns.containsKey(x);
	}

	private Node[] getColumn(final int x) {
		return savedColumns.get(x);
	}
	
	public int getMaxMarioXPos() {
		return maxMarioXPos;
	}
	
	// --- NEW METHODS ---

	@Override
	public List<Action> actions(State state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchNode childNode(SearchNode node, Action action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double pathCost(SearchNode n1, SearchNode n2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double heuristicFunction(SearchNode node, SearchNode goal) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean goalTest(State goal) {
		// TODO Auto-generated method stub
		return false;
	}
}
