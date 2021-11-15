import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads and contains in memory the map of the game.
 *
 */
public class Map {

	/* Representation of the map */
	private char[][] map;

	/* Map name */
	private String mapName = null;

	/* Gold required for the human player to win */
	private int goldRequired;

	/* last character on a pos before a player was on it */
	private char lastCharAtPlayerPos;

	/* last character on a pos before a player was on it */
	private char lastCharAtBotPos;


	/* 2d list, rather than 2d array, easier to add the sub-lists to the list. Stores the
	positions of the gold on the map. Used to decide whether the player is standing on a gold pos.
	 */
	private final List<List<Integer>> goldPositions = new ArrayList<>();
	private final List<List<Integer>> exitPositions = new ArrayList<>();

	/**
	 * Default constructor, creates the default map "Very small Labyrinth of doom".
	 */
	public Map() {
		mapName = "Very small Labyrinth of Doom";
		goldRequired = 2;
		map = new char[][]{
		{'#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','G','.','.','.','.','.','.','.','.','.','E','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','E','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','G','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','#'},
		{'#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#'}
		};
		// get the positions of gold and exits
		addPositions('G', goldPositions);
		addPositions('E', exitPositions);

	}

	/**
	 * Constructor that accepts a map to read in from.
	 *
	 * @param fileName: The filename of the map file.
	 */
	public Map(String fileName) {
		readMap(fileName);
		if (mapName != null) {
			// only if a valid map get the positions of gold and exits
			addPositions('G', goldPositions);
			addPositions('E', exitPositions);
		}
	}

    /**
     * @return : Gold required to exit the current map.
     */
    protected int getGoldRequired() {
        return goldRequired;
    }

    /**
     * @return : The map as stored in memory.
     */
    protected char[][] getMap() {
        return map;
    }


    /**
     * @return : The name of the current map.
     */
    protected String getMapName() {
        return mapName;
    }

	/**
	 * @return : the position of last occurrence of a character
	 *
	 * @param object: the map char to look for
	 */
    protected int[] getPos(char object) {
    	int[] pos = new int[2];
    	// iterate through the 2d array
    	for (int y=0; y<map.length; y++){
    		for (int x=0; x<map[y].length; x++){
    			if (map[y][x] == object){
    				// when desired object is reached save it.
    				pos = new int[]{x,y};
				}
			}
		}
    	// return the last occurrence of the character
		return pos;
	}


	/**
	 * returns a character in a given position of the map. Doesn't need to be validated as no
	 * user input will be given.
	 *
	 * @param pos: A int array with a position in the form [column, row]
	 * @return : return the character at given pos.
	 */
	protected char getAtPos(int[] pos) {
		return map[pos[1]][pos[0]];
	}


	/**
	 * updates the a 2d array, linked to an object on the map, with the positions in the form of [column, row]
	 *
	 * @param object: the object to look for.
	 * @param posArray: the 2d array of positions to save the position to.
	 */
	private void addPositions(char object, List<List<Integer>> posArray) {
		// iterate through the 2d map array
		for(int y=0; y<map.length; y++){
			for (int x=0; x<map[y].length; x++){
				if (map[y][x] == object){
					posArray.add(Arrays.asList(x,y));
				}
			}
		}
	}


	/**
	 * moves a player on the map whilst remembering the character of the previous position of
	 * a given player
	 *
	 * @param pos: the position to move the player to in the form [column, row]
	 * @param player: the player or bot to move
	 */
	protected void movePlayer(int[] pos, char player) {
		char lastCharAtPos;
		if (player == 'P'){
			lastCharAtPos = lastCharAtPlayerPos;
		} else {
			lastCharAtPos = lastCharAtBotPos;
		}
		// iterate through the map
    	for (int y=0; y<map.length; y++){
    		for (int x=0; x< map[y].length; x++){
    			if (map[y][x] == player){
    				// the player is found so change it to the previous object that was in that position
    				map[y][x] = lastCharAtPos;
				}
			}
		}
    	// next position is saved as previous position for the next call of movePlayer
    	lastCharAtPos = map[pos[1]][pos[0]];
		if (player == 'P'){
			 lastCharAtPlayerPos = lastCharAtPos;
		} else {
			lastCharAtBotPos = lastCharAtPos;
		}
		// change the next position to the player.
    	map[pos[1]][pos[0]] = player;
	}


	/**
	 * getter for the goldPositions field.
	 * @return: goldPositions as a 2d list in the form of [column, row]
	 */
	protected List<List<Integer>> getGoldPositions() {
    	return goldPositions;
	}

	/**
	 * getter for the exitPositions field.
	 * @return: goldPositions as a 2d list in the form of [column, row]
	 */
	protected List<List<Integer>> getExitPositions() {
		return exitPositions;
	}


    /**
     * Reads the map from file.
     *
     * @param fileName: Name of the map's file.
     */
    protected void readMap(String fileName) {
		List<String> lines;
    	try {
    		// reads all the lines of the file as a list
    		lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
    		// set the fields from the file data
    		map = new char[lines.size()-2][lines.get(2).length()];
			mapName = lines.get(0).replace("name ", "");
			goldRequired = Integer.parseInt(lines.get(1).replace("win ", ""));

			// update the map array with the correct chars
			for (int row=2; row<lines.size(); row++) {
				for (int column = 0; column < lines.get(row).length(); column++) {
					map[row - 2][column] = lines.get(row).charAt(column);
				}
			}

		}
    	catch (IOException e) {
    		// catch the IOException created by an incorrect map name
    		System.out.println("Incorrect map name");
		}

    }


	/**
	 * Puts the player on the map array in a random valid position.
	 *
	 * @param player: Bot or player character.
	 */
    protected void placePlayerOnMap(char player) {
		// counts the empty positions in the map
    	int counter = 0;
		for (char[] value : map) {
			for (char c : value) {
				if (c == '.') {
					counter++;
				}
			}
		}
		// get a random int from in the range of the amount of empty spaces
		int randomInt = (int)(Math.random() * counter);

		// iterate through the map and place the player at the correct position.
		for (int i=0; i<map.length; i++){
			for (int x=0; x< map[i].length; x++){
				if (map[i][x] == '.') {
					if (randomInt == 0){
						// save the first previous character
						if (player == 'P'){
							lastCharAtPlayerPos = map[i][x];
						} else {
							lastCharAtBotPos = map[i][x];
						}
						map[i][x] = player;
					}
					// decrease the random int with every empty space, when its 0 place the player.
					randomInt--;
				}
			}

		}
	}


	/**
	 * updates the goldPositions field by removing positions, so that coins cannot be picked up
	 * twice.
	 *
	 * @param pos: The position of the gold piece to be removed.
	 */
	protected void removeGold(List<Integer> pos) {
		// if the player is on a gold position, update the gold positions and change the gold char to a empty space
		if (lastCharAtPlayerPos == 'G'){
			lastCharAtPlayerPos = '.';
			goldPositions.remove(pos);
		}
	}
}
