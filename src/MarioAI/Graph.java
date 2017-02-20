package MarioAI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.idsia.mario.environments.Environment;

public class Graph {
	public static final int LEVEL_HEIGHT = 15;
	public static final int SIGHT_WIDTH = 22;
	public static final int SIGHT_HEIGHT = 22;
	private static final int LEVEL_START_SCOLLING = 160;
	private static final int BLOCK_PIXEL_SIZE = 16;
	private static final int MARIO_START_X_POS = LEVEL_START_SCOLLING / BLOCK_PIXEL_SIZE;
	private static final int LEVEL_LEFT_X_POS = -(SIGHT_WIDTH / 2);
	private static final int LEVEL_RIGHT_X_POS = -LEVEL_LEFT_X_POS;

	@SuppressWarnings("unchecked") // because java IS FUCKING STUPID
	private final ArrayList<Surface>[] surfaces = (ArrayList<Surface>[]) new ArrayList[LEVEL_HEIGHT];
	private final Node[][] levelMatrix = new Node[SIGHT_WIDTH][LEVEL_HEIGHT];
	private final HashMap<Integer, Node[]> savedColumns = new HashMap<Integer, Node[]>();
	private int oldMarioXPos = MARIO_START_X_POS;
	private int maxMarioXPos = oldMarioXPos;

	private int getCorrectedMarioXPos(final float[] marioXPos) {
		return (int) Math.max(LEVEL_START_SCOLLING / BLOCK_PIXEL_SIZE, getMarioXPos(marioXPos));
	}
	
	public static int getMarioXPos(final float[] marioXPos)
	{
		return (int)Math.round(marioXPos[0]) / BLOCK_PIXEL_SIZE;
	}
	
	public static int getMarioYPos(final float[] marioYPos)
	{
		return (int)Math.round(marioYPos[1]) / BLOCK_PIXEL_SIZE;
	}

	public Node[][] getLevelMatrix(){
		return levelMatrix;
	}
	
	public void createStartGraph(final Environment observation) {
		final int marioXPos = getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = getMarioYPos(observation.getMarioFloatPos());

		for (int i = 0; i < levelMatrix.length; i++) {
			final byte[] byteColumn = getByteColumnFromLevel(observation.getCompleteObservation(), marioYPos, i);
			final Node[] columnToInsert = convertByteColumnToNodeColumn(byteColumn, (i - SIGHT_WIDTH/2) + marioXPos);
			levelMatrix[i] = columnToInsert;
			saveColumn(i - (SIGHT_WIDTH / 2) - 1, columnToInsert);
		}
	}

	public boolean updateMatrix(final Environment observation) {
		final int marioXPos = getCorrectedMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = getMarioYPos(observation.getMarioFloatPos());
		final int change = marioXPos - oldMarioXPos;
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

	public Node getMarioNode(final Environment observation)
	{
		final int marioXPos = getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = getMarioYPos(observation.getMarioFloatPos());
		return new Node((short) marioXPos, (short) (marioYPos + 1),(byte) 0);
	}
	
	public Node[] getGoalNodes()
	{
		return getColumn(maxMarioXPos);
	}
	
	
	private void moveMatrixOneRight(final int marioXPos) {
		// move columns right
		for (int x = levelMatrix.length - 1; x > 1; x--) {
			levelMatrix[x] = levelMatrix[x - 1];
		}
		final int columnToInsertXPos = marioXPos + LEVEL_LEFT_X_POS;
		final Node[] columnToInsert = getColumn(columnToInsertXPos);
		levelMatrix[0] = columnToInsert;
	}

	private void moveMatrixOneLeft(final Environment observation, final int marioXPos, final int marioYPos) {
		// no need to save the column that was overwritten as it's still saved
		// in savedColumns

		// move columns left
		for (int x = 1; x < levelMatrix.length; x++) {
			levelMatrix[x - 1] = levelMatrix[x];
		}
		Node[] columnToInsert;
		final int columnToInsertXPos = marioXPos + LEVEL_RIGHT_X_POS;
		// get column and insert it into the matrix
		if (containsColumn(columnToInsertXPos)) {
			columnToInsert = getColumn(columnToInsertXPos);
		} else {
			final byte[] byteColumn = getByteColumnFromLevel(observation.getCompleteObservation(), marioYPos);
			columnToInsert = convertByteColumnToNodeColumn(byteColumn, columnToInsertXPos);
			saveColumn(columnToInsertXPos, columnToInsert);
		}

		levelMatrix[levelMatrix.length - 1] = columnToInsert;
	}

	private byte[] getByteColumnFromLevel(final byte[][] level, final int marioYPos) {
		return getByteColumnFromLevel(level, marioYPos, SIGHT_WIDTH - 1);
	}
	
	private byte[] getByteColumnFromLevel(final byte[][] level, final int marioYPos, final int sightColumnIndex) {
		final byte[] byteColumn = new byte[LEVEL_HEIGHT];
		if (marioYPos >= LEVEL_HEIGHT / 2) {
			final int viewDiff = marioYPos - (LEVEL_HEIGHT / 2);

			for (int y = 0 + viewDiff; y < LEVEL_HEIGHT; y++) {
				byteColumn[y] = level[y - viewDiff + 4][sightColumnIndex];
			}

		} else {
			final int viewDiff = (LEVEL_HEIGHT / 2) - marioYPos;

 			for (int y = 0; y < LEVEL_HEIGHT - viewDiff; y++) {
				byteColumn[y] = level[y + viewDiff - 4][sightColumnIndex];
			}
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
}
