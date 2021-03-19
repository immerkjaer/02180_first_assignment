package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

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

    public static class FinalScore
    {
        private int perfectStructureScore;
        private int monoScore;
        private int placementScore;
        private int mergeScore;
        private int spaceScore;
        private int skipped;
        private int totalScore;

        public FinalScore(int perfectStructureScore, int monoScore, int placementScore, int mergeScore, int spaceScore, int skipped, int totalScore)
        {
            this.perfectStructureScore = perfectStructureScore;
            this.monoScore = monoScore;
            this.placementScore = placementScore;
            this.mergeScore = mergeScore;
            this.spaceScore = spaceScore;
            this.totalScore = totalScore;
            this.skipped = skipped;
        }
    }

    public static class GameStats
    {
        private FinalScore finalScore;
        private int turn;
        private ArrayList<Integer> noGoMoves;
        private Grid grid;
        private int move;
        private int score;


        public GameStats(FinalScore finalScore, int turn, ArrayList<Integer> noGoMoves, Grid grid, int move, int score)
        {
            this.finalScore = finalScore;
            this.turn = turn;
            this.noGoMoves = noGoMoves;
            this.grid = grid;
            this.move = move;
            this.score = score;
        }
    }

    // virker som om, at vi løber tør for scoring, og så bliver bare lort til sisdt.
    // tjek med endnu flere "sidste" states, og se hvor går galt
    public static LinkedList<GameStats> expectimaxAi(Game game)
    {
        var hC = new HelperFunctions();
        LinkedList<GameStats> decisionStats = new LinkedList<>();

//        while (!(game.lost()) && game.getScore() < 100)
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
            FinalScore chosenMoveStats = null;

            for (var move : moves)
            {
                var gridMoveClone = gridClone.clone();
                var newGrid = hC.movePicesSomeDir(gridMoveClone, move);

                if (noGoMoves.contains(move))
                {
                    continue;
                }

                var moveScore = getMoveScore(newGrid, hC, move, depth);

                if (moveScore.totalScore > score)
                {
                    score = moveScore.totalScore;
                    bestMove = move;
                    chosenMoveStats = moveScore;
                }
            }

            var stats = new GameStats(chosenMoveStats, game.getTurns(), noGoMoves, game.getGrid(), bestMove, game.getScore());
            decisionStats.addLast(stats);
            if (decisionStats.size() >= 30)
            {
                decisionStats.removeFirst();
            }

            if(game.lost())
            {
                return decisionStats;
            }
            game.act(bestMove);
            System.out.println(game);
        }

        return decisionStats;

    }

    public static FinalScore getMoveScore(Grid grid, HelperFunctions hC, int moveDir, int depth)
    {
//        if (grid.getEmptyLocations().size() <= 1)
//        {
//            new FinalScore(0,0,0,0, 0,0);
//        }
        // if not enough small values??

        if (depth == 0)
        {
            return finaleScoring(grid, hC);
        }

        var score = 0;
        var perfectStructureScore = 0;
        var monoScore = 0;
        var placementScore = 0;
        var mergeScore = 0;
        var spaceScore = 0;
        var skipped = 0;

        var emptyLocations = grid.getEmptyLocations();
        for (var emptyLoc : emptyLocations)
        {
            var gT2 = grid.clone();
            var gT4 = grid.clone();

            gT2.set(emptyLoc, 2);
            gT4.set(emptyLoc, 4);

            var gT2Score = calculateMoveScore(gT2, hC, depth-1);
            var gT4Score = calculateMoveScore(gT4, hC, depth-1);

            score += (gT2Score.totalScore * 0.9);
            score += (gT4Score.totalScore * 0.1);

            perfectStructureScore += (int) ((gT2Score.perfectStructureScore * 0.9) + (gT4Score.perfectStructureScore * 0.1));
            monoScore += (int) ((gT2Score.monoScore * 0.9) + (gT4Score.monoScore * 0.1));
            placementScore += (int) ((gT2Score.placementScore * 0.9) + (gT4Score.placementScore * 0.1));
            mergeScore += (int) ((gT2Score.mergeScore * 0.9) + (gT4Score.mergeScore * 0.1));
            spaceScore += (int) ((gT2Score.spaceScore * 0.9) + (gT4Score.spaceScore * 0.1));
            skipped += (int) ((gT2Score.skipped * 0.9) + (gT4Score.skipped * 0.1));
        }

        var fScore = score;

        return new FinalScore(perfectStructureScore, monoScore, placementScore, mergeScore, spaceScore, skipped, fScore);
    }

    public static FinalScore calculateMoveScore(Grid grid, HelperFunctions hC, int depth)
    {
        var moves = hC.getMovesAsInts();
        var noGoMoves = hC.IgnoreMoves(grid);
        if (noGoMoves.size() >= 4 || grid.getEmptyLocations().size() <= 1)
        {
            return new FinalScore(0,0,0,0,0,1,0);
        }

        var score = Integer.MIN_VALUE;
        var perfectStructureScore = 0;
        var monoScore = 0;
        var placementScore = 0;
        var mergeScore = 0;
        var spaceScore = 0;

        for (var move : moves)
        {
            if (noGoMoves.contains(move))
            {
                continue;
            }

            var gridSomeMove = grid.clone();
            var newGrid = hC.movePicesSomeDir(gridSomeMove, move);

            var moveScore = getMoveScore(newGrid, hC, move, depth);
            if (moveScore.totalScore > score)
            {
                score = moveScore.totalScore;
                perfectStructureScore = moveScore.perfectStructureScore;
                monoScore = moveScore.monoScore;
                placementScore = moveScore.placementScore;
                mergeScore = moveScore.mergeScore;
                spaceScore = moveScore.spaceScore;

            }
        }
        return new FinalScore(perfectStructureScore, monoScore, placementScore, mergeScore, spaceScore, 0, score);
    }

    public static FinalScore finaleScoring(Grid grid, HelperFunctions hC)
    {
        if (grid.getEmptyLocations().size() <= 1)
        {
            return new FinalScore(0, 0, 0, 0, 0, 0, 0);
        }

        var gArr1 = grid.getArray();
        var gArr2 = hC.transposeGridArray(grid);

        var maxVal = hC.getHighestPiece(grid);

        var gArr1Score = scoreGridArray(gArr1, maxVal, hC);
        var gArr2Score = scoreGridArray(gArr2, maxVal, hC);

        ArrayList<Integer> scoreList = new ArrayList<>();

        var placementScore = scorePlacement(gArr1, maxVal);
        var monoScore = 50 * placementScore * scoreMonotonicity(gArr1, maxVal, hC);

        var mergeScore = 700 * (gArr1Score.possibleMerges + gArr2Score.possibleMerges);

        scoreList.add(placementScore);
        scoreList.add(monoScore);
        scoreList.add(mergeScore);

        Collections.sort(scoreList);

        var spaceC = 300;
        if (grid.getEmptyLocations().size() <= 2)
        {
            spaceC = 2000;
            monoScore = monoScore / 10;
            placementScore = placementScore / 10;
        }
        var spaceScore = spaceC * grid.getEmptyLocations().size();

        var perfectStructureScore = perfectStructure(gArr1, hC) * 2000;

        var totalScore = monoScore + mergeScore + spaceScore + perfectStructureScore;
        var res = new FinalScore(perfectStructureScore, monoScore, placementScore, mergeScore, spaceScore, 0, totalScore);

        if (grid.getEmptyLocations().size() <= 2 && detectClutter(grid.getArray()) && (gArr1Score.possibleMerges + gArr2Score.possibleMerges) <= 1)
        {
            return new FinalScore(0, 0, 0, 0, 0, 0, 0);
        }
        return res;
    }

    public static boolean detectClutter(int[][] arr)
    {
        var length = arr.length-1;
        var bestScore = (arr.length-1)*(arr.length);
        for (var i=0; i<=length-1; i++)
        {
            for (var j=0; j<=length-1; j++)
            {
                if (arr[i][j]<arr[i][j+1])
                {
                    bestScore--;
                }
            }
        }
        if (bestScore < 6)
        {
            return true;
        }
        return false;
    }

    public static int perfectStructure(int[][] arr, HelperFunctions hC)
    {
        var length = arr.length-1;
        var uniqueSortedAsc = hC.getTilesAsc(arr);
        var maxVal = uniqueSortedAsc[uniqueSortedAsc.length-1];
        if (maxVal < 60)
        {
            return 0;
        }

        var secondMaxVal = uniqueSortedAsc[uniqueSortedAsc.length-2];
        var thirdMaxVal = uniqueSortedAsc[uniqueSortedAsc.length-3];

        var score = 0;

        if (arr[0][0] == maxVal && arr[0][1] == secondMaxVal && arr[1][0] == secondMaxVal)
        {
            score = 10;
        }
        if (arr[length][0] == maxVal && arr[length][1] == secondMaxVal && arr[length-1][0] == secondMaxVal)
        {
            score = 10;
        }
        if (arr[0][0] == maxVal && (arr[0][1] == secondMaxVal || arr[1][0] == secondMaxVal))
        {
            score = 3;
        }
        if (arr[length][0] == maxVal && (arr[length][1] == secondMaxVal || arr[length-1][0] == secondMaxVal))
        {
            score = 3;
        }

        if (arr[0][0] == maxVal && (arr[0][1] == thirdMaxVal && arr[1][0] == secondMaxVal))
        {
            score = 8;
        }
        if (arr[length][0] == maxVal && (arr[length][1] == secondMaxVal && arr[length-1][0] == thirdMaxVal))
        {
            score = 8;
        }

        return score;
    }

    public static int scorePlacement(int[][] arr, int maxVal)
    {
        var placementSCore = 1;
        var length = arr.length-1;
        var lengthHalf = (arr.length / 2) - 1;
        for (var i=0; i <= length; i++)
        {
            for (var j=0; j <= lengthHalf; j++)
            {
                if (arr[i][j] == maxVal)
                {
                    placementSCore++;
                }
            }
        }
        if (arr[0][0] == maxVal || arr[length][0] == maxVal)
        {
            placementSCore+=2;
        }

        return placementSCore;
    }

    public static int scoreMonotonicity(int[][] arr, int maxVal, HelperFunctions hC)
    {
        var monotonicity = 0;

        var length = arr.length-1;
        var leftMostColDown = new int[length+1];
        var leftMostColUp= new int[length+1];

        for (var i=0; i <= length; i++)
        {
            leftMostColDown[i] = arr[i][0];
            leftMostColUp[i] = arr[length-i][0];
        }

        var leftMostUp = hC.isSortedAsc(leftMostColUp);
        var leftMostDown = hC.isSortedAsc(leftMostColDown);
        var bottomRight = hC.isSortedAsc(arr[length]);
        var topRight = hC.isSortedAsc(arr[0]);

        if ((leftMostUp && bottomRight) || (leftMostDown && topRight))
        {
            monotonicity=10;
        }
        if (leftMostUp || leftMostDown || bottomRight || topRight)
        {
            monotonicity=3;
        }

        return monotonicity;
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
            var variance = hC.getVariance(arr[i]);
            smoothness += variance;

            for (int j = 0; j < arr[i].length-1; j++)
            {
                if (arr[i][j] == arr[i][j+1] && arr[i][j] != 0)
                {
                    possibleMerges++;
//                    possibleMerges += arr[i][j];
                }
                if (arr[i][j] == arr[i][j+1] && arr[i][j] == maxVal && arr[i][j] != 0)
                {
                    possibleMerges+=10;
//                    possibleMerges += (arr[i][j]*10);
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

}
