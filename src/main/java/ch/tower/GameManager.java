package ch.tower;

public class GameManager {

    enum GameState
    {
        WAIT, GAME, END;
    }

    private GameState state;

    public GameManager()
    {
        this.state = GameState.WAIT;
    }

    public GameState getState()
    {
        return state;
    }

    public void setState(GameState state)
    {
        this.state = state;
        //maybe register WaitEvents if state == GameState.WAIT, etc...
    }

}
