package AI;

public class MoveToConsider
{
    private FinalScore finalScore;
    private int moveDir;
    private int initialEmpty;

    public MoveToConsider(
            FinalScore finalScore,
            int moveDir,
            int initialEmpty)
    {
        this.finalScore = finalScore;
        this.moveDir = moveDir;
        this.initialEmpty = initialEmpty;
    }

    public FinalScore getFinalScore() {
        return finalScore;
    }

    public int getMoveDir() {
        return moveDir;
    }

    public int getInitialEmpty() {
        return initialEmpty;
    }
}
