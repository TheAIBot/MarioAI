package MarioAI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.idsia.mario.environments.Environment;

public class Graph {
	public static final int LEVEL_HEIGHT = 15;
	public static final int SIGHT_WIDTH = 22;
	private static final int LEVEL_START_SCOLLING = 160;
	private static final int BLOCK_PIXEL_SIZE = 16;
	private static final int MARIO_START_X_POS = LEVEL_START_SCOLLING / BLOCK_PIXEL_SIZE;
	private static final int LEVEL_LEFT_X_POS = -(SIGHT_WIDTH / 2);
	private static final int LEVEL_RIGHT_X_POS = -LEVEL_LEFT_X_POS;

	private ArrayList<Surface>[] surfaces;
	private final Node[][] levelMatrix = new Node[SIGHT_WIDTH][LEVEL_HEIGHT];
	private final HashMap<Integer, Node[]> savedColumns = new HashMap<Integer, Node[]>();
	private int oldMarioXPos = MARIO_START_X_POS;
	private int maxMarioXPos = oldMarioXPos;

	public Graph() {
		surfaces = (ArrayList<Surface>[]) new ArrayList[LEVEL_HEIGHT];
	}

	private int getMarioXPos(float marioXPos) {
		return (int) Math.max(LEVEL_START_SCOLLING / BLOCK_PIXEL_SIZE, Math.round(marioXPos));
	}

	public void createStartGraph(Environment observation) {
		int marioYPos = Math.round(observation.getMarioFloatPos()[1]);

		for (int i = 0; i < levelMatrix.length; i++) {
			byte[] byteColumn = getByteColumnFromLevel(observation.getCompleteObservation(), marioYPos);
			Node[] columnToInsert = convertByteColumnToNodeColumn(byteColumn, i);
			saveColumn(i, columnToInsert);
		}
	}

	public boolean updateMatrix(Environment observation) {
		// observer.
		int marioXPos = getMarioXPos(observation.getMarioFloatPos()[0]);
		int marioYPos = Math.round(observation.getMarioFloatPos()[1]);
		int change = marioXPos - oldMarioXPos;
		oldMarioXPos = marioXPos;
		maxMarioXPos = Math.max(maxMarioXPos, marioXPos);
		if (change > 0) {
			moveMatrixOneLeft(observation, marioXPos, marioYPos);
			return true;
		} else if (change < 0) {
			moveMatrixOneRight(marioXPos);
			return true;
		}
		return false;
	}

	public Node getMarioNode(Environment observation)
	{
		int marioXPos = Math.round(observation.getMarioFloatPos()[0]);
		int marioYPos = Math.round(observation.getMarioFloatPos()[1]);
		return getColumn(marioXPos)[marioYPos];
	}
	
	public Node[] getGoalNodes()
	{
		return getColumn(maxMarioXPos);
	}
	
	
	private void moveMatrixOneRight(int marioXPos) {
		// move columns right
		for (int x = levelMatrix.length - 1; x > 1; x--) {
			levelMatrix[x] = levelMatrix[x - 1];
		}
		int columnToInsertXPos = marioXPos + LEVEL_LEFT_X_POS;
		Node[] columnToInsert = getColumn(columnToInsertXPos);
		levelMatrix[0] = columnToInsert;
	}

	private void moveMatrixOneLeft(Environment observation, int marioXPos, int marioYPos) {
		// no need to save the column that was overwritten as it's still saved
		// in savedColumns

		// move columns left
		for (int x = 1; x < levelMatrix.length; x++) {
			levelMatrix[x - 1] = levelMatrix[x];
		}
		Node[] columnToInsert;
		int columnToInsertXPos = marioXPos + LEVEL_RIGHT_X_POS;
		// get column and insert it into the matrix
		if (containsColumn(columnToInsertXPos)) {
			columnToInsert = getColumn(columnToInsertXPos);
		} else {
			byte[] byteColumn = getByteColumnFromLevel(observation.getCompleteObservation(), marioYPos);
			columnToInsert = convertByteColumnToNodeColumn(byteColumn, columnToInsertXPos);
			saveColumn(columnToInsertXPos, columnToInsert);
		}

		levelMatrix[levelMatrix.length - 1] = columnToInsert;
	}

	private byte[] getByteColumnFromLevel(byte[][] level, int marioYPos) {
		byte[] byteColumn = new byte[LEVEL_HEIGHT];
		if (marioYPos >= LEVEL_HEIGHT / 2) {
			int viewDiff = marioYPos - (LEVEL_HEIGHT / 2);

			for (int y = 0 + viewDiff; y < LEVEL_HEIGHT; y++) {
				byteColumn[y] = level[21][y - viewDiff];
			}

		} else {
			int viewDiff = (LEVEL_HEIGHT / 2) - marioYPos;

			for (int y = 0; y < 22 - viewDiff; y++) {
				byteColumn[y] = level[21][y + viewDiff];
			}
		}
		return byteColumn;
	}

	private Node[] convertByteColumnToNodeColumn(byte[] byteColumn, int x) {
		Node[] nodeColumn = new Node[byteColumn.length];
		for (int y = 0; y < byteColumn.length; y++) {
			if (byteColumn[y] > 0) {
				nodeColumn[y] = new Node((short) x, (short) y, (short)x, (short)y, byteColumn[y]);
			}
		}
		return nodeColumn;
	}

	private void saveColumn(int x, Node[] column) {
		savedColumns.put(x, column);
	}

	private boolean containsColumn(int x) {
		return savedColumns.containsKey(x);
	}

	private Node[] getColumn(int x) {
		return savedColumns.get(x);
	}
}
