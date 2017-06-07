package MarioAI;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map.Entry;

import org.omg.CORBA.COMM_FAILURE;

import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.StateNode;
import ch.idsia.mario.environments.Environment;


public class World {
	public static final int LEVEL_HEIGHT = 15;
	public static final int LEVEL_WIDTH = 22;
	public static final int SIGHT_WIDTH = 22;
	public static final int SIGHT_HEIGHT = 22;
	private static final int MARIO_START_X_POS = 2;

	private final CollisionDetection collisionDetection = new CollisionDetection();
	private final Node[][] levelMatrix = new Node[LEVEL_WIDTH][LEVEL_HEIGHT]; // main graph
	private final HashMap<Integer, Node[]> savedColumns = new HashMap<Integer, Node[]>();
	private int oldMarioXPos = MARIO_START_X_POS;
	private int oldMarioYPos;
	private int maxMarioXPos = oldMarioXPos;
	private Node marioNode;
	private boolean goalNodesChanged = false;
	private boolean hasWorldChanged = false; //Has the world been changed, with the new update.
	
	private final HashMap<Integer, Node> enemyCollisionNodes = new HashMap<Integer, Node>();
	
	public void printMatrix(final Environment observation)
	{
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		for (int x = 0; x < LEVEL_HEIGHT; x++) {
			for (int y = 0; y < LEVEL_WIDTH; y++) {
				if (marioNode != null && (SIGHT_HEIGHT / 2) == y && x == marioYPos) {
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
	
	public void initialize(final Environment observation) {
		//Creates the initial graph
		updateWholeMatrix(observation);
		setMarioNode(observation);
		oldMarioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		maxMarioXPos = (SIGHT_WIDTH / 2) + MARIO_START_X_POS - 1;
		goalNodesChanged = true;
	}
	
	private void updateWholeMatrix(final Environment observation) {
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());

		for (int i = 0; i < levelMatrix.length; i++) {
			final byte[] byteColumn = getByteColumnFromLevel(observation.getLevelSceneObservation(), marioYPos, i);
			final int columnIndex = i + marioXPos - (SIGHT_WIDTH / 2);
			final Node[] columnToInsert = convertByteColumnToNodeColumn(byteColumn, columnIndex);
			levelMatrix[i] = columnToInsert;
			
			saveColumn(columnIndex, columnToInsert);
		}
	}

	public void update(final Environment observation) {
		//Updates the world (Matrix)
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final int changeX = marioXPos - oldMarioXPos;
		final int changeY = marioYPos - oldMarioYPos;
		oldMarioXPos = marioXPos;
		oldMarioYPos = marioYPos;
		
		final int newMaxMarioXPos = Math.max(maxMarioXPos, marioXPos + (SIGHT_WIDTH / 2) - 1);
		goalNodesChanged = goalNodesChanged || (newMaxMarioXPos != maxMarioXPos);
		maxMarioXPos = newMaxMarioXPos;		
		
		if (changeX != 0 || changeY != 0) {
			updateWholeMatrix(observation);
			setMarioNode(observation);
			hasWorldChanged = true;
		}
		else {
			hasWorldChanged = false;
		}
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
	
	public Node[] getGoalNodes(int validColumnsToIgnore)
	{
		for (int x = maxMarioXPos; x >= 0; x--) {
			Node[] column = getColumn(x);
			for (int y = 0; y < column.length; y++) {
				if (column[y] != null) {
					if (validColumnsToIgnore == 0) {
						return column;
					}
					else {
						validColumnsToIgnore--;
						break;
					}
				}
			}
		}
		throw new Error("No blocks was found in the matrix");
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
		Node[] nodeColumn = getColumn(x);
		if (nodeColumn == null) {
			nodeColumn = new Node[byteColumn.length];
		}
		for (int y = 0; y < byteColumn.length; y++) {
			if (nodeColumn[y] == null &&
				byteColumn[y] != 0) {
				nodeColumn[y] = new Node(x, y, byteColumn[y]);
			}
		}
		return nodeColumn;
	}

	private void saveColumn(final int x, final Node[] column) {
		savedColumns.put(x, column);
	}

	public Node[] getColumn(final int x) {
		return savedColumns.get(x);
	}
	
	public void syncFrom(World world) {
		//copy levelMatrix
		for (int x = 0; x < levelMatrix.length; x++) {
			for (int y = 0; y < levelMatrix[x].length; y++) {
				levelMatrix[x][y] = world.levelMatrix[x][y];
			}
		}
		
		//copy savedColumn
		savedColumns.clear();
		for (Entry<Integer, Node[]> entry : world.savedColumns.entrySet()) {
			Node[] columnCopy = new Node[entry.getValue().length];
			for (int i = 0; i < columnCopy.length; i++) {
				columnCopy[i] = entry.getValue()[i];
			}
			savedColumns.put(entry.getKey().intValue(), columnCopy);
		}
	}
	
	public boolean hasEnemyCollisionNode(int hash){
		return enemyCollisionNodes.containsKey(hash);
	}
	
	public Node getEnemyCollisionNode(int hash){
		return enemyCollisionNodes.get(hash);
	}
	
	public Node getEnemyCollisionNode(int x, int y){
		return getEnemyCollisionNode(Hasher.hashNode(x, y));
	}
	
	public void addEnemyCollisionNode(Node node){
		enemyCollisionNodes.put(Hasher.hashNode(node.x, node.y), node);
	}
	
	public boolean isColliding(Point2D.Float futureOffset, Point2D.Float currentOffset, StateNode sourceNode, float lastYValue){
		return collisionDetection.isColliding(futureOffset, currentOffset, sourceNode.xPos, sourceNode.yPos, lastYValue, this);
	}
	
	public boolean isColliding(Point2D.Float futureOffset, Point2D.Float currentOffset, float startX, float startY, float lastYValue){
		return collisionDetection.isColliding(futureOffset, currentOffset, startX, startY, lastYValue, this);
	}
	
	public boolean hasGoalNodesChanged() {
		return goalNodesChanged;
	}
	
	public void resetGoalNodesChanged() {
		goalNodesChanged = false;
	}
	
	public Node[][] getLevelMatrix(){
		return levelMatrix;
	}

	public boolean hasWorldChanged() {
		return hasWorldChanged;
	}
	
	public void resetHasWorldChanged() {
		hasWorldChanged = false;
	}

	public Node[][] getLevelMatrixAt(int x) {
		int xCenter = x; //Center of the new level matrix
		
		//We don't want there to be any columns in the right part of the level matrix,
		//that we haven't seen. This will not work as intended with EdgeCreator.
		if (xCenter > maxMarioXPos - LEVEL_WIDTH/2 - 1) {
			xCenter = maxMarioXPos - LEVEL_WIDTH/2 - 1;
			//TODO check if it is set to the correct value.
		}
		Node[][] newLevelMatrix = new Node[LEVEL_WIDTH][LEVEL_HEIGHT];
		for (int i = 0; i < newLevelMatrix.length; i++) {
			int relativeXPosition = (xCenter - LEVEL_WIDTH/2 + i);
			Node[] column = getColumn(relativeXPosition);
			//Must not be to much to the left: out of the level. 
			//We want to express this as empty space. Luckily, this is already done.
			if (!(column == null)) { 
				newLevelMatrix[i] = column;
			}
		}
		
		return newLevelMatrix;		
	}
}
