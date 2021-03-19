package src;

import java.util.ArrayList;

public class AISolver
{
    public static class ArrScore
    {
        private int smoothness;
        private int monotonicity;
        private int placement;
        private int bonus;
        private int possibleMerges;

        public ArrScore(int smoothness, int monotonicity, int placement, int bonus, int possibleMerges)
        {
            this.smoothness = smoothness;
            this.monotonicity = monotonicity;
            this.placement = placement;
            this.bonus = bonus;
            this.possibleMerges = possibleMerges;
        }
    }

    public static void expectimaxAi(Game game)
    {
        var hC = new HelperFunctions();

        while (!(game.lost()))
        {
            var score = Integer.MIN_VALUE;
            var bestMove = Integer.MIN_VALUE;
            var depth = 3;

            if (game.getGrid().getEmptyLocations().size() >= 5)
            {
                depth = 2;
            }

            var gridClone = game.getGrid().clone();
            var moves = hC.getMovesAsInts();
            var noGoMoves = hC.IgnoreMoves(gridClone);

            for (var move : moves)
            {
                var gridMoveClone = gridClone.clone();
                var newGrid = hC.movePicesSomeDir(gridMoveClone, move);

                if (noGoMoves.contains(move))
                {
                    continue;
                }

                var moveScore = getMoveScore(newGrid, hC, move, depth);

                if (moveScore > score)
                {
                    score = moveScore;
                    bestMove = move;
                }
            }

            game.act(bestMove);
            System.out.println(game);
        }

    }

    public static int getMoveScore(Grid grid, HelperFunctions hC, int moveDir, int depth)
    {
        if (grid.getEmptyLocations().size() <= 2) { return 0; }
        // if not enough small values??

        if (depth == 0)
        {
            return finaleScoring(grid, hC);
        }

        var score = 0;
        var emptyLocations = grid.getEmptyLocations();
        for (var emptyLoc : emptyLocations)
        {
            var gT2 = grid.clone();
            var gT4 = grid.clone();

            gT2.set(emptyLoc, 2);
            gT4.set(emptyLoc, 4);

            var gT2Score = calculateMoveScore(gT2, hC, depth-1) * 0.9;
            var gT4Score = calculateMoveScore(gT4, hC, depth-1) * 0.1;

            score += gT2Score;
            score += gT4Score;
        }

        return score / emptyLocations.size();
    }

    public static int finaleScoring(Grid grid, HelperFunctions hC)
    {
        var gArr1 = grid.getArray();
        var gArr2 = hC.transposeGridArray(grid);

        var maxVal = hC.getHighestPiece(grid);

        var gArr1Score = scoreGridArray(gArr1, maxVal, hC);
        var gArr2Score = scoreGridArray(gArr2, maxVal, hC);

        var placementScore = (maxVal*2) * (gArr1Score.placement + gArr2Score.placement);
        var monoScore = ((gArr1Score.placement + gArr2Score.placement)*100) * ((gArr1Score.monotonicity * gArr1Score.bonus) + (gArr2Score.monotonicity * gArr2Score.bonus));
//        var mergeScore = 2000 * (gArr1Score.possibleMerges + gArr2Score.possibleMerges);
        var mergeScore = 100 * (gArr1Score.possibleMerges + gArr2Score.possibleMerges); // var 10 før
        var smoothScore = 100 * (gArr1Score.smoothness + gArr2Score.smoothness);

        var c = 2;
        if (maxVal > 2000)
        {
            c = 4;
        }
        var spaceScore = (maxVal*c) * grid.getEmptyLocations().size();

        if (maxVal > 1000)
        {
            var hej = 123;
        }
        return monoScore + placementScore + mergeScore + smoothScore + spaceScore;
    }

    public static ArrScore scoreGridArray(int[][] arr, int maxVal, HelperFunctions hC)
    {
        var monotonicity = 0;
        var bonus = 0;
        var possibleMerges = 0;
        var placement = 0;
        var smoothness = 0;

        var length = arr.length-1;
        for (int i = 0; i <= length; i++)
        {
            if (hC.isSortedAsc(arr[i]))
            {
                monotonicity++;
            }

            for (int j = 0; j < arr[i].length-1; j++)
            {
                if (arr[i][j] == arr[i][j+1] && arr[i][j] != 0)
                {
//                    possibleMerges++;
                    possibleMerges += arr[i][j];
                }
                if (arr[i][j] == arr[i][j+1] && arr[i][j] == maxVal && arr[i][j] != 0)
                {
//                    possibleMerges+=5;
                    possibleMerges += (arr[i][j]*20);
                }
            }
        }
        if (arr[0][0] == maxVal || arr[length][0] == maxVal)
        {
            bonus = 2;
            placement = 10;
        }
        var arrScore = new ArrScore(smoothness, monotonicity, placement, bonus, possibleMerges);
        return arrScore;
    }

    public static int calculateMoveScore(Grid grid, HelperFunctions hC, int depth)
    {
        var score = Integer.MIN_VALUE;

        var moves = hC.getMovesAsInts();
        var noGoMoves = hC.IgnoreMoves(grid);
        for (var move : moves)
        {
            if (noGoMoves.contains(move))
            {
                continue;
            }

            var gridSomeMove = grid.clone();
            var newGrid = hC.movePicesSomeDir(gridSomeMove, move);

            var moveScore = getMoveScore(newGrid, hC, move, depth);
            if (moveScore > score)
            {
                score = moveScore;
            }
        }
        return score;
    }

}
