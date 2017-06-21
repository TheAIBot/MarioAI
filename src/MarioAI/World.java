package MarioAI;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import MarioAI.graph.nodes.Node;
import MarioAI.graph.nodes.SpeedNode;
import ch.idsia.mario.environments.Environment;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;


public class World {
	public static final int LEVEL_HEIGHT = 15;
	public static final int LEVEL_WIDTH = 22;
	public static final int SIGHT_WIDTH = 22;
	public static final int SIGHT_HEIGHT = 22;
	public static final int PIXELS_PER_BLOCK = 16;
	private static final int MARIO_START_X_POS = 2;
	private static final int TOWER_TYPE = 20;
	
	private final CollisionDetection collisionDetection = new CollisionDetection();
	//A 2D array of the nodes that mario can see
	//this is stored for easy access to EdgeCreator
	private final Node[][] levelMatrix = new Node[SIGHT_WIDTH][LEVEL_HEIGHT]; 
	//Contains all the seen columns of the world
	private final Int2ObjectOpenHashMap<Node[]> savedColumns = new Int2ObjectOpenHashMap<Node[]>();	
	 //Will not handle negative x values. Level must not be longer than 400.
	private final Int2ObjectOpenHashMap<ArrayList<Node>> bulletBillTowers = new Int2ObjectOpenHashMap<ArrayList<Node>>();
	private int oldMarioXPos = MARIO_START_X_POS;
	private int oldMarioYPos;
	private int maxMarioXPos = oldMarioXPos; // The maximum x position that mario has seen
	private Node marioNode; // A node that is always positioned where mario is
	private boolean goalNodesChanged = false;
	private boolean hasWorldChanged = false;
	
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
		//Set max seen x pos which is a contant at the start of each level
		maxMarioXPos = (SIGHT_WIDTH / 2) + MARIO_START_X_POS - 1;
		//Tell the rest of the agent that the world changed
		goalNodesChanged = true;
	}
	
	/**
	 * Updates the seen world with the data from the game. Created nodes
	 * for each new block seen.
	 * @param observation
	 */
	private void updateWholeMatrix(final Environment observation) {
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		byte[][] scene = observation.getLevelSceneObservation();
		for (int i = 0; i < levelMatrix.length; i++) {
			//Get relevant piece of column from the game
			final byte[] byteColumn = getByteColumnFromLevel(scene, marioYPos, i);
			//The actual position of the column in the game 
			final int columnIndex = i + marioXPos - (SIGHT_WIDTH / 2);
			//Create nodes if the column is new or get an old on if it's already seen
			final Node[] columnToInsert = convertByteColumnToNodeColumn(byteColumn, columnIndex);
			levelMatrix[i] = columnToInsert;
			//Save column in case it's a new column
			saveColumn(columnIndex, columnToInsert);
			//Also updates the list of towers:
			if (0 < i && i < levelMatrix.length - 1) {
				updateTowers(levelMatrix[i-1], columnToInsert, levelMatrix[i+1]);				
			}
		}
	}

	/**
	 * Updates the seen world with the data from the game. Created nodes
	 * for each new block seen. Also sets the hasWorldChanged and 
	 * goalNodesChanged variable and updates the marioNode
	 * @param observation
	 */
	public void update(final Environment observation) {
		//Updates the world (Matrix)
		final int marioXPos = MarioMethods.getMarioXPos(observation.getMarioFloatPos());
		final int marioYPos = MarioMethods.getMarioYPos(observation.getMarioFloatPos());
		
		final int changeX = marioXPos - oldMarioXPos;
		final int changeY = marioYPos - oldMarioYPos;
		oldMarioXPos = marioXPos;
		oldMarioYPos = marioYPos;
		
		final int newMaxMarioXPos = Math.max(maxMarioXPos, marioXPos + (SIGHT_WIDTH / 2) - 1);
		//goal nodes is true when it's true or if the new max mario x pos is bigger than the old one
		goalNodesChanged = goalNodesChanged || (newMaxMarioXPos != maxMarioXPos);
		maxMarioXPos = newMaxMarioXPos;		
		
		setMarioNode(observation);
		//Only actually update the world if mario actually moved a block
		if (changeX != 0 || changeY != 0) {
			updateWholeMatrix(observation);
		}
		else {
			hasWorldChanged = false;
		}
	}
	
	/**
	 * Updates marios node to his current position.
	 * @param observation
	 */
	private void setMarioNode(final Environment observation) {
		final float marioXPos = MarioMethods.getPreciseMarioXPos(observation.getMarioFloatPos());
		float marioYPos = MarioMethods.getPreciseMarioYPos(observation.getMarioFloatPos());
		//limit mario y pos to a position inside the matrix
		marioYPos = Math.min(Math.max(marioYPos, 0), LEVEL_HEIGHT - 1);
		
		marioNode = new Node(Math.round(marioXPos), Math.round(marioYPos), (byte)0);
	}

	public Node getMarioNode(final Environment observation)
	{
		return marioNode;
	}
	
	/**
	 * Returns the n'th column that isn't full of null
	 * where n is equal to validColumnsToIgnore
	 * @param validColumnsToIgnore
	 * @return
	 */
	public Node[] getGoalNodes(int validColumnsToIgnore)
	{
		for (int x = maxMarioXPos; x >= 0; x--) {
			Node[] column = getColumn(x);
			for (int y = 0; y < column.length; y++) {
				//column isonly valid if there is atleast one node in it
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

	/**
	 * Returns the relevalt part ofthe column where the relevant part is the part that can be
	 * seen on the screen.
	 * @param level 2d array of block types from the game
	 * @param marioYPos marios current y position
	 * @param sightColumnIndex what column to take from in the level 2d array
	 * @return
	 */
	private byte[] getByteColumnFromLevel(final byte[][] level, final int marioYPos, final int sightColumnIndex) {
		//Basically the levels dimension is 22x22 and what can be seen on the screen is a 22x15 matrix of blocks
		//the level 2d array is centered on mario so depending on his y position he relevant part of the level will
		//move up and down. This code here takes care of copying the correct values and handling that mario can se above
		//and below the actual level
		final byte[] byteColumn = new byte[LEVEL_HEIGHT];		
		final int topObservationYPos = marioYPos - (SIGHT_HEIGHT / 2);
		final int startIndex = Math.max(topObservationYPos, 0);
		final int endIndex = Math.min(Math.min(startIndex + LEVEL_HEIGHT, SIGHT_HEIGHT + topObservationYPos), LEVEL_HEIGHT);
		for (int i = startIndex; i < endIndex; i++) {
			byteColumn[i] = level[i - topObservationYPos][sightColumnIndex];
		}
		return byteColumn;
	}

	/**
	 * Either updates an existing column with new nodes or creates a new one and adds nodes to it
	 * @param byteColumn
	 * @param x
	 * @return
	 */
	private Node[] convertByteColumnToNodeColumn(final byte[] byteColumn, final int x) {
		//If column already exists then check only for new nodes.
		//That way nodes are only created once
		Node[] nodeColumn = getColumn(x);
		if (nodeColumn == null) {
			nodeColumn = new Node[byteColumn.length];
		}
		for (int y = 0; y < byteColumn.length; y++) {
			//if a node was added that wasn't found before then the world obviously changed
			if (nodeColumn[y] == null &&
				byteColumn[y] != 0) {
				nodeColumn[y] = new Node(x, y, byteColumn[y]);
				hasWorldChanged = true;
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
	
	/**
	 * Updates this World with the data from the other world.
	 * @param world The world to copy from
	 */
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
	
	// There is no direct access to the CollisionDtector object so these methods makes them accessible to the public
	public boolean isColliding(float futureOffsetX, float futureOffsetY, float currentOffsetX, float currentOffsetY, SpeedNode sourceNode, float lastY){
		return collisionDetection.isColliding(futureOffsetX, futureOffsetY, currentOffsetX, currentOffsetY, sourceNode.currentXPos, sourceNode.yPos, lastY, this);
	}
	public boolean isColliding(float futureOffsetX, float futureOffsetY, float currentOffsetX, float currentOffsetY, float startX, float startY, float lastY){
		return collisionDetection.isColliding(futureOffsetX, futureOffsetY, currentOffsetX, currentOffsetY, startX, startY, lastY, this);
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
	
	private void updateTowers(Node[] formerColumn, Node[] columnToInsert, Node[] nextColumn) {
		for (int i = 0; i < columnToInsert.length; i++) {
			Node currentNode = columnToInsert[i];
			//If the one currently is part of a bullet bill tower, and the node above isn't part of the tower:
			if (currentNode != null && currentNode.type == TOWER_TYPE && 
				(i == 0 || (columnToInsert[i-1] == null || columnToInsert[i-1].type != TOWER_TYPE))) {		
				
				//neighbors must not be "towers", as pipes have the same type as towers.
				if ((formerColumn[i] == null || formerColumn[i].type != TOWER_TYPE) &&
					 (nextColumn[i] == null || nextColumn[i].type != TOWER_TYPE)) {
					ArrayList<Node> bulletColumn = bulletBillTowers.get(currentNode.x);
					if (bulletColumn == null) {
						bulletColumn = new ArrayList<Node>();
						bulletColumn.add(currentNode);
						bulletBillTowers.put(currentNode.x, bulletColumn);
					} else if (!bulletColumn.contains(currentNode)) {
						bulletColumn.add(currentNode);
					}					
				}
			}
		}
	}
	
	public ArrayList<Point> getTowersOnLevel(){
		ArrayList<Point> relevantTowers = new ArrayList<Point>();
		//Testing it out in reality, and looking at the code, this seems about right.
		for (int i = -LEVEL_WIDTH/2 - 1 + marioNode.x; i < LEVEL_WIDTH/2 + 1 + marioNode.x; i++) {
			ArrayList<Node> currentTowers = bulletBillTowers.get(i);
			if (currentTowers != null) {
				for (Node node : currentTowers) {
					relevantTowers.add(new Point(node.x, node.y));
				}
			}
		}		
		//System.out.println(relevantTowers.toString());
		return relevantTowers;
	}
	
	public void resetHasWorldChanged() {
		hasWorldChanged = false;
	}
}
