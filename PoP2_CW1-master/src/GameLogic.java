import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
/**
 * Contains the main logic part of the game, as it processes.
 *
 */
public class GameLogic extends Client{

	private Map map;
	private final HumanPlayer player;
	private final HumanPlayer chaser;


	// bool vars needed to check if the game should end and who's turn it is.
	private boolean gameRunning = true;
	private boolean playersTurn = true;

	// needed to only run a greeting then the client joins the server.
    private boolean greeted = false;

	/**
	 * Default constructor
     *
     * *original DoD code*
	 */
	public GameLogic() {
        player = new HumanPlayer();
        chaser = new HumanPlayer();

	    String userInput = getStartInput();
	    // The user selected the default map
	    if (userInput.equals("Default")) {
	        map = new Map();
        }
	    else {
	        // user typed a new map
            map = new Map(userInput + ".txt");
            // validate the map name
            while (map.getMapName() == null){
                userInput = getStartInput();
                // allow user to select default map again
                if (userInput.equals("Default")) {
                    map = new Map();
                }
                else {
                    // tries the map name, if not valid the name will remain null and stay in the loop
                    map = new Map(userInput + ".txt");
                }
            }
        }

	    // place both players randomly on the map
	    map.placePlayerOnMap('P');
	    map.placePlayerOnMap('B');

	}

    /**
     * main loop of the game. Gets input from player and bot and gives a resulting state after.
     * Deals with validating user input so only certain inputs are allowed.
     *
     * *updated DoD code*
     */
	private String loop() {
	    // player move list, used to validate the correct move directions.
        String message = null;
        String action;

        // get the action of either the player or bot depending who's turn it is
        if (playersTurn){
            action = player.getNextAction();
        } else {
            action = chaser.getNextAction();
        }

        // check what action to do and get a message response that is printed after.
        if (action.equals("HELLO")) {
            message = hello();
        } else if (action.equals("GOLD")) {
            message = gold();
        } else if (action.startsWith("MOVE")) {
            // move the player whose turn it is.
            if (playersTurn) {
                message = move(action.charAt(action.length() - 1), 'P');
            } else {
                message = move(action.charAt(action.length() - 1), 'B');
            }
        } else if (action.equals("LOOK")) {
            // display a map depending on what player is making the command
            if (playersTurn) {
                message = look('P');
            } else {
                message = look('B');
            }
        } else if (action.equals("PICKUP")) {
            message = pickup();
        } else if (action.equals("QUIT")) {
            // break the loop and leave the game
            quitGame();
        } else {
            // invalid command
            message = "Invalid";
        }
        // alternate turns
        playersTurn = !playersTurn;

        // when the player can't be found on the map end the game as the bot caught the player
        if (Arrays.equals(map.getPos('P'), new int[]{0,0})){
            message = quitGame();
        }

        return message;
    }

    /**
     * *original DoD code*
     *
	 * Checks if the game is running
	 *
     * @return if the game is running.
     */
    protected boolean gameRunning() {
        return gameRunning;
    }

    /**
     * *original DoD code*
     *
	 * Returns the gold required to win.
	 *
     * @return : Gold required to win.
     */
    protected String hello() {
        return "Gold to win: " + map.getGoldRequired();
    }
	
	/**
     * *original DoD code*
     *
	 * Returns the gold currently owned by the player.
	 *
     * @return : Gold currently owned.
     */
    protected String gold() {
        return "Gold owned: " + player.getGold();
    }

    /**
     * *original DoD code*
     *
     * Checks if movement is legal and updates player's location on the map.
     *
     * @param direction : The direction of the movement.
     * @return : Protocol if success or not.
     */
    protected String move(char direction, char player) {
        // the position of the player
        int[] pos = map.getPos(player);

        // validate that the position to move to is a valid position
        if (direction == 'N'){
            pos[1] -= 1;
            if (map.getAtPos(pos) != '#') {
                map.movePlayer(pos, player);
                return "Success";
            }
        }
        else if (direction == 'S'){
            pos[1] += 1;
            if (map.getAtPos(pos) != '#') {
                map.movePlayer(pos, player);
                return "Success";
            }
        }
        else if (direction == 'E'){
            pos[0] += 1;
            if (map.getAtPos(pos) != '#') {
                map.movePlayer(pos, player);
                return "Success";
            }
        }
        else if (direction == 'W'){
            pos[0] -= 1;
            if (map.getAtPos(pos) != '#') {
                map.movePlayer(pos, player);
                return "Success";
            }
        }
        // not a successful move
        return "Fail";
    }

    /**
     * *original DoD code*
     *
     * Converts the map from a 2D char array to a single string.
     *
     * @return : A String representation of the game map.
     */
    protected String look(char player) {
        int[] playerPos = map.getPos(player);
        char[][] mapArr = map.getMap();
        // used to add characters to a string iteratively
        StringBuilder result = new StringBuilder();

        // iterate through the map by starting 2 indices before the player position on the map
        for (int y=playerPos[1]-2; y<playerPos[1]+3; y++){
            // same thing but for the columns now
            for (int x=playerPos[0]-2; x<playerPos[0]+3; x++){
                try{
                    result.append(mapArr[y][x]);
                }
                catch (IndexOutOfBoundsException e) {
                    // the sides of the map only have 1 # so it will create a indexOutOfBoundsException
                    result.append('#');
                }

            }
            //add a line break after every row.
            result.append('\n');
        }
        // return the result as a string
        return result.toString();
    }

    /**
     * *original DoD code*
     *
     * Processes the player's pickup command, updating the map and the player's gold amount.
     *
     * @return If the player successfully picked-up gold or not.
     */
    protected String pickup() {
        int[] posArr = map.getPos('P');
        // if the player position is the same as a gold position remove the gold position and increment player gold
        if (map.getGoldPositions().contains(Arrays.asList(posArr[0],posArr[1]))){
            map.removeGold(Arrays.asList(posArr[0],posArr[1]));
            player.addGold();
            return "Success. " + gold();
        }
        // if not its a failed move
        return "Fail. " + gold();
    }

    /**
     * Quits the game, shutting down the application.
     *
     * *updated DoD code*
     */
    protected String quitGame() {
        gameRunning = false;
        int[] playerPos = map.getPos('P');
        // check if win condition ias been met.
        if (map.getExitPositions().contains(Arrays.asList(playerPos[0], playerPos[1])) && player.getGold() >= map.getGoldRequired()) {
            return "Player Won!, DoD client is Leaving Server";
        }
        else {
            return "Chaser Won!, DoD client is Leaving Server";
        }
    }

    /**
     * *original DoD code*
     *
     * Validates if the player wants to play on a default map or not.
     *
     * Validating if the map name is valid does not happen here, but the method gets called again
     * if the map name is not valid
     *
     * @return : A string with the map name, Default if user wants to use the default map.
     */
    protected String getStartInput() {
        // used scanner object to read inputs
        Scanner scan = new Scanner(System.in);
        System.out.println("Would you like to play on a Default map? (Y/N): ");
        String answer;
        // validate input to only allow Y/N
        while (true) {
            try {
                answer = scan.nextLine();
                if (!answer.equals("Y") && !answer.equals("N")) {
                    // correct input type but not correct input
                    System.out.println("Y/N");
                    continue;
                }
                // correct input so exit the code
                break;
            } catch (InputMismatchException e) {
                // deal with the exception
                System.out.println("Y/N");
            }
        }
        if (answer.equals("N")) {
            System.out.println("Enter Map name: ");
            answer = scan.nextLine();
        }
        else {
            return "Default";
        }
        return answer;
    }

    /**
     * sends who's turn it is and the result of a player making an action to the server.
     * If the game is not running it closes exits the server.
     *
     */
    @Override
    void write() {
        try {
            PrintWriter send = new PrintWriter(serverSocket.getOutputStream(), true);
            if (gameRunning()) {
                send.println(loop());
                // game could have ended in the loop method.
                if (gameRunning) {
                    if (playersTurn) {
                        send.println("Player's turn:");
                    } else {
                        send.println("Chaser's turn:");
                    }
                }
                // get response
                read();
            } else {
                // send exit command to server so that it knows the DoDClient is leaving
                send.println("/Exit");
            }
        } catch (SocketException e){
            // if the disconnected from server.
            System.out.println("Lost connection to server.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * sends greeting when joins the server.
     * on all other method calls it will filter out input from itself, and set the player input based on which player's
     * turn is next.
     *
     */
    @Override
    void read() {
        if (!greeted){
            // let the users know the DoD client has connected, done in read() as this called before write in the client class
            greeting();
        }
        try {
            BufferedReader receive = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            String message = receive.readLine();
            // ignore messages from itself
            while (message.startsWith("[DoD]")) {
                message = receive.readLine();
            }
            // removes the name tag
            message = message.replaceAll("\\[(.*?)] ", "");
            // updates relevant players input
            if (playersTurn){
                player.setInput(message);
            } else {
                chaser.setInput(message);
            }
            // send to server
            write();
        } catch (SocketException e){
            // exception is thrown if the server doesn't close properly or messageList is empty caused by server quitting
            // before bot got input.
            System.out.println("Lost Connection To Server.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // if connection stops close the server socket.
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends greeting to the server
     */
    public void greeting(){
        try {
            PrintWriter send = new PrintWriter(serverSocket.getOutputStream(), true);
            send.println("Connected.\nPlayer's Turn:");
        } catch (SocketException e){
            // if the disconnected from server.
            System.out.println("Lost connection to server.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // to only run this method once
            greeted = true;
        }
    }

    public static void main(String[] args) {
        GameLogic game = new GameLogic();
        game.setName("DoD");
        game.start(args);
    }
}