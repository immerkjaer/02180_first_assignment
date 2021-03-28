package src.solver;

import src.game.Grid;

import java.util.concurrent.Callable;

public class RunMove implements Callable<MoveToConsider> {

    private Grid oldGrid;
    private Grid newGrid;
    private HelperFunctions hC;
    private int depth;
    private int moveDir;
    private int emptyLocsForMove;

    public RunMove(
            Grid newGrid,
            Grid oldGrid,
            HelperFunctions hC,
            int depth,
            int moveDir,
            int emptyLocsForMove)
    {
        this.oldGrid = oldGrid;
        this.newGrid = newGrid;
        this.hC = hC;
        this.depth = depth;
        this.moveDir = moveDir;
        this.emptyLocsForMove = emptyLocsForMove;
    }

    public MoveToConsider call()
    {
        FinalScore moveScore = AISolver.getMoveScore(newGrid, oldGrid, hC, Integer.MAX_VALUE, depth);
        return new MoveToConsider(moveScore, moveDir, emptyLocsForMove);
    }


}
