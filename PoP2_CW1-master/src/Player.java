import java.util.Arrays;
import java.util.List;


/**
 * Player Super class that bot and human player will inherit from
 *
 */
abstract class Player {
    final String[] moveList;
    String input;

    // constructor used to have a different move list for both players
    Player(String[] moveList) {
        this.moveList = moveList;
    }

    /**
     * Reads player's input from the console.
     * The abstract method is used as both players will use the same method but needs to have different contents.
     *
     * return : A string containing the input the player entered.
     */
    protected String getInput(){
        return input.toUpperCase();
    }

    protected void setInput(String Input){
        input = Input;
    }
    /**
     * Processes the command. It should return a reply in form of a String, as the protocol dictates.
     * Otherwise it should return the string "Invalid".
     *
     * @return : Processed output or Invalid if the @param command is wrong.
     */
    protected String getNextAction() {
        List<String> validMoves = Arrays.asList(moveList);
        String playerMoves = getInput();

        // if the input is in the move list accept it, if not reject it.
        if (validMoves.contains(playerMoves)){
            return playerMoves;
        }
        else {
            return "Invalid";
        }
    }

    /**
     * MoveList getter
     * @return : String of valid moves.
     */
    protected String[] getMoveList(){
        return moveList;
    }

}
