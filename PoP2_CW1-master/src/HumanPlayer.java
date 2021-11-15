/**
 * Runs the game with a human player and contains code needed to read inputs.
 *
 * *updated DoD code*
 */
public class HumanPlayer extends Player {
    // player gold counter
    private int gold = 0;

    HumanPlayer() {
        // move array for the player
        super(new String[]{"HELLO", "GOLD", "MOVE N", "MOVE E", "MOVE S", "MOVE W", "PICKUP", "LOOK", "QUIT"});
    }

    /**
     * gold getter
     *
     * @return : amount of gold
     */
    protected int getGold() {
        return gold;
    }

    /**
     * Increments the gold value
     */
    protected void addGold() {
        gold++;
    }

}