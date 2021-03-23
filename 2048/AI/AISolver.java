package AI;

import AI.HelperFunctions;
import src.Game;
import src.Grid;

import java.util.*;
import java.util.concurrent.*;

public class AISolver
{
    public static class Pair
    {
        private double p1;
        private double p2;

        public Pair(double p1, double p2)
        {
            this.p1 = p1;
            this.p2 = p2;
        }

        double getFirst()
        {
            return this.p1;
        }

        double getSecond()
        {
            return this.p2;
        }

    }

    public static class TilesInformation
    {
        private int[] tilesAsc;
        private int[] tilesDesc;
        private int maxValX;
        private int maxValY;
        private int tilesSet;
        private HashMap<Integer, Integer> tileCounts;

        public TilesInformation(
                int[] tilesAsc,
                int[] tilesDesc,
                int maxValX,
                int maxValY,
                int tilesSet,
                HashMap<Integer, Integer> tileCounts)
        {
            this.tilesAsc = tilesAsc;
            this.tilesDesc = tilesDesc;
            this.maxValX = maxValX;
            this.maxValY = maxValY;
            this.tilesSet = tilesSet;
            this.tileCounts = tileCounts;
        }
    }

    public static class GridScore
    {
        private double varianceScore;
        private double monotonicityScore;
        private double placementScore;
        private double mergeScore;
        private double groupSpread;

        public GridScore(
                double varianceScore,
                double monotonicityScore,
                double placementScore,
                double mergeScore,
                double groupSpread)
        {
            this.varianceScore = varianceScore;
            this.monotonicityScore = monotonicityScore;
            this.placementScore = placementScore;
            this.mergeScore = mergeScore;
            this.groupSpread = groupSpread;
        }
    }

    public static class FinalScore
    {
        private int varianceScore;
        private int monoScore;
        private int placementScore;
        private int maxValScore;
        private int spaceScore;
        private int mergeScore;
        private int groupSpreadScore;
        private int statesDiscarded;
        private int statesConsidered;
        private int totalScore;

        public FinalScore(
                int varianceScore,
                int monoScore,
                int placementScore,
                int maxValScore,
                int spaceScore,
                int mergeScore,
                int groupSpreadScore,
                int statesDiscarded,
                int statesConsidered,
                int totalScore)
        {
            this.varianceScore = varianceScore;
            this.monoScore = monoScore;
            this.placementScore = placementScore;
            this.maxValScore = maxValScore;
            this.spaceScore = spaceScore;
            this.mergeScore = mergeScore;
            this.groupSpreadScore = groupSpreadScore;
            this.totalScore = totalScore;
            this.statesDiscarded = statesDiscarded;
            this.statesConsidered = statesConsidered;
        }
    }

    public static class MoveToConsider
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
    }

    public static class GameStats
    {
        private FinalScore finalScore;
        private int turn;
        private ArrayList<Integer> noGoMoves;
        private Grid grid;
        private int move;
        private int score;
        private ArrayList<MoveToConsider> otherOptions;
        private int maxValue;


        public GameStats(
                FinalScore finalScore,
                int turn,
                ArrayList<Integer> noGoMoves,
                Grid grid,
                int move,
                int score,
                ArrayList<MoveToConsider> otherOptions,
                int maxValue)
        {
            this.finalScore = finalScore;
            this.turn = turn;
            this.noGoMoves = noGoMoves;
            this.grid = grid;
            this.move = move;
            this.score = score;
            this.otherOptions = otherOptions;
            this.maxValue = maxValue;
        }
    }

    public static class runMove implements Callable<MoveToConsider> {

        private Grid oldGrid;
        private Grid newGrid;
        private HelperFunctions hC;
        private int depth;
        private int moveDir;
        private int emptyLocsForMove;

        public runMove(
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
            var moveScore = getMoveScore(newGrid, oldGrid, hC, Integer.MAX_VALUE, depth);
            return new MoveToConsider(moveScore, moveDir, emptyLocsForMove);
        }
    }

    public static LinkedList<GameStats> expectimaxAi(Game game) throws InterruptedException, ExecutionException {
        var hC = new HelperFunctions();
        LinkedList<GameStats> decisionStats = new LinkedList<>();

        ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(10);

        while (!(game.lost()))
        {
            long score = Integer.MIN_VALUE;
            var bestMove = Integer.MIN_VALUE;
            var depth = 3;

            ArrayList<MoveToConsider> finalScores = new ArrayList<>();

            if (game.getGrid().getEmptyLocations().size() >= 6)
            {
                depth = 2;
            }

            var gridClone = game.getGrid().clone();
            var moves = hC.getMovesAsInts();
            var noGoMoves = hC.IgnoreMoves(gridClone);
            FinalScore chosenMoveStats = null;

            ArrayList<Callable<MoveToConsider>> callables = new ArrayList<>();

            for (var move : moves)
            {
                if (noGoMoves.contains(move))
                {
                    continue;
                }

                var oldGrid = gridClone.clone();
                var newGrid = hC.movePiecesSomeDir(oldGrid, move);
                var emptyLocsForMove = newGrid.getEmptyLocations();

                callables.add( new runMove(newGrid, oldGrid, hC, depth, move, emptyLocsForMove.size()));


//                var moveScore = getMoveScore(newGrid, gridClone, hC, Integer.MAX_VALUE, depth);
//                statesConsidered.add(moveScore.statesConsidered);
//                statesDiscarded.add(moveScore.statesDiscarded);
//
//                if (moveScore.statesConsidered == 0 || moveScore.statesDiscarded == 0)
//                {
//                    skipScoreScaling = true;
//                }
//
//                finalScores.add( new MoveToConsider(moveScore, move, emptyLocsForMove.size()) );

            }

            List<Future<MoveToConsider>> futures = WORKER_THREAD_POOL.invokeAll(callables);
            for (var res : futures)
            {
                var moveRes = res.get();
                finalScores.add( moveRes );
            }

            for (var move : finalScores)
            {
                long scoreForMove = move.finalScore.totalScore;
                scoreForMove = scoreForMove / move.initialEmpty;

                if (scoreForMove > score)
                {
                    score = scoreForMove;
                    bestMove = move.moveDir;
                    chosenMoveStats = move.finalScore;
                }
            }

            var stats = new GameStats(chosenMoveStats, game.getTurns(), noGoMoves, game.getGrid(), bestMove, game.getScore(), finalScores, game.highestPiece());
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
            game.printGame();
        }

        return decisionStats;

    }

    public static FinalScore getMoveScore(
            Grid grid,
            Grid previousGrid,
            HelperFunctions hC,
            int moveDir,
            int depth)
    {
        if (depth == 0)
        {
            return finaleScoring(grid, previousGrid, moveDir, hC);
        }

        var score = 0;
        var varianceScore = 0;
        var monoScore = 0;
        var placementScore = 0;
        var maxValScore = 0;
        var spaceScore = 0;
        var mergeScore = 0;
        var groupSpreadScore = 0;
        var statesDiscarded = 0;
        var statesConsidered = 0;

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

            varianceScore += (int) ((gT2Score.varianceScore * 0.9) + (gT4Score.varianceScore * 0.1));
            monoScore += (int) ((gT2Score.monoScore * 0.9) + (gT4Score.monoScore * 0.1));
            placementScore += (int) ((gT2Score.placementScore * 0.9) + (gT4Score.placementScore * 0.1));
            maxValScore += (int) ((gT2Score.maxValScore * 0.9) + (gT4Score.maxValScore * 0.1));
            spaceScore += (int) ((gT2Score.spaceScore * 0.9) + (gT4Score.spaceScore * 0.1));
            statesDiscarded += ((gT2Score.statesDiscarded * 0.9) + (gT4Score.statesDiscarded * 0.1));
            statesConsidered += ((gT2Score.statesConsidered * 0.9) + (gT4Score.statesConsidered * 0.1));
            groupSpreadScore += ((gT2Score.groupSpreadScore * 0.9) + (gT4Score.groupSpreadScore * 0.1));
            mergeScore += ((gT2Score.mergeScore * 0.9) + (gT4Score.mergeScore * 0.1));
        }

        return new FinalScore(varianceScore, monoScore, placementScore, maxValScore, spaceScore, mergeScore, groupSpreadScore, statesDiscarded, statesConsidered, score);
    }

    public static FinalScore calculateMoveScore(
            Grid grid,
            HelperFunctions hC,
            int depth)
    {
        var moves = hC.getMovesAsInts();
        var score = Integer.MIN_VALUE;
        var varianceScore = 0;
        var monoScore = 0;
        var placementScore = 0;
        var maxValScore = 0;
        var spaceScore = 0;
        var mergeScore = 0;
        var groupSpreadScore = 0;
        var statesDiscarded = 0;
        var statesConsidered = 0;

        for (var move : moves)
        {
            var gridSomeMove = grid.clone();
            var newGrid = hC.movePiecesSomeDir(gridSomeMove, move);

            var moveScore = getMoveScore(newGrid, grid, hC, move, depth);
            if (moveScore.totalScore > score)
            {
                score = moveScore.totalScore;
                varianceScore = moveScore.varianceScore;
                monoScore = moveScore.monoScore;
                placementScore = moveScore.placementScore;
                maxValScore = moveScore.maxValScore;
                spaceScore = moveScore.spaceScore;
                mergeScore = moveScore.mergeScore;
                groupSpreadScore = moveScore.groupSpreadScore;
            }
            statesDiscarded += moveScore.statesDiscarded;
            statesConsidered += moveScore.statesConsidered;
        }
        return new FinalScore(
                varianceScore,
                monoScore,
                placementScore,
                maxValScore,
                spaceScore,
                mergeScore,
                groupSpreadScore,
                statesDiscarded,
                statesConsidered,
                score);
    }

    public static FinalScore finaleScoring(
            Grid grid,
            Grid previousGrid,
            int moveDir,
            HelperFunctions hC)
    {
        var tilesInformation = getTilesInformation(grid.getArray());
        var maxVal = tilesInformation.tilesDesc[0];

        var scoreGrid = scoreBoard(grid, tilesInformation, hC);
        var scoreVariance = scoreGrid.varianceScore;
        var scorePlacement = scoreGrid.placementScore;
        var scoreMerge = scoreGrid.mergeScore;
        var scoreGroupSpread = scoreGrid.groupSpread;
        var scoreMonotonicty = scoreGrid.monotonicityScore; // not actually scored
        var emptySpaces = grid.getEmptyLocations().size();

//        var sVari = 0.03 * (Math.log(maxVal) * scoreVariance);
//        var sEmpty = 280 * Math.log(emptySpaces);
//        var sPlace = 0.25 * (Math.log(maxVal) * scorePlacement);
//        var sMerges = 15 * (Math.log(maxVal) * scoreMerge); // 10
//        var sGroupSpread = 0.3 * (Math.log(maxVal) * scoreGroupSpread);
//        var sMax = maxVal;
        var sVari = 0.05 * scoreVariance;
        var sEmpty = 30 * Math.log(emptySpaces);
        var sPlace = 1.45 * scorePlacement; // * 1.5
        var sMerges = 20 * scoreMerge; // 30
        var sGroupSpread = 0.80 * scoreGroupSpread; // * 0.65
        var sMax = Math.log(maxVal) / Math.log(2); // 0
        var totalScore = sVari + sEmpty + sPlace + sMax + sMerges + sGroupSpread;

        sVari = Math.round(sVari);
        sEmpty = Math.round(sEmpty);
        sPlace = Math.round(sPlace);
        sMax = Math.round(sMax);
        sMerges = Math.round(sMerges);
        sGroupSpread = Math.round(sGroupSpread);
        totalScore = Math.round(totalScore);

        return new FinalScore(
                (int) sVari,
                0,
                (int) sPlace,
                (int) sMax,
                (int) sEmpty,
                (int) sMerges,
                (int) sGroupSpread,
                0,
                1,
                (int) totalScore);
    }

    public static GridScore scoreBoard(
            Grid grid,
            TilesInformation tilesInformation,
            HelperFunctions hC)
    {
        var gridArr = grid.getArray();
        var gridArrTransposed = hC.transposeGridArray(grid.clone());

        var length = gridArr.length-1;
        var uniqueTilesAsc = tilesInformation.tilesAsc;
        var uniqueTilesDesc = tilesInformation.tilesDesc;
        var maxValX = tilesInformation.maxValX;
        var maxValY = tilesInformation.maxValY;
        var tilesSet = tilesInformation.tilesSet;
        var tileCounts = tilesInformation.tileCounts;
        var perfectStructureScaler = perfectStructure(gridArr, uniqueTilesDesc, length);
        var varianceCutOf = uniqueTilesDesc[0];

        double mergeScore = 0.0;
        double placementScore = 0.0;
        double varianceScore = 0.0;
        double groupSpread = 0.0;

        var rowLen = gridArr.length-1;
        var colLen = gridArr[0].length-1;
        for (int i = 0; i <= rowLen; i++)
        {
            for (int j = 0; j < colLen; j++)
            {
                var currentVal = gridArr[i][j];
                var currentValTransposed = gridArrTransposed[i][j];
                if (j < rowLen)
                {
                    var nextVal = gridArr[i][j+1];
                    var nextValTransposed = gridArrTransposed[i][j+1];

                    mergeScore += scoreMerging(currentVal, nextVal);
                    mergeScore += scoreMerging(currentValTransposed, nextValTransposed);
                }

                placementScore += scorePlacement(currentVal, i, j, uniqueTilesDesc, uniqueTilesAsc, maxValX, maxValY, length, tilesSet, varianceCutOf);
                var varianceAndGroupSpread = scoreVarianceAndGrouping(currentVal, i, j, gridArr, varianceCutOf, length, tileCounts);
                varianceScore += varianceAndGroupSpread.getFirst();
                groupSpread += varianceAndGroupSpread.getSecond();

            }
        }

        placementScore = placementScore * perfectStructureScaler;
        return new GridScore(varianceScore, 0.0, placementScore, mergeScore, groupSpread);
    }

    public static Pair scoreVarianceAndGrouping(
            int currentValue,
            int i,
            int j,
            int[][] arr,
            int varianceCutOf,
            int length,
            HashMap<Integer, Integer> tileCounts)
    {
        double variance = 0.0;
        double groupSpread = 0.0;

        var c1 = (Math.log(currentValue) / Math.log(2));

        for (var i2=0; i2<=length; i2++)
        {
            for (var j2=0; j2<=length; j2++)
            {
                if (i == i2 && j == j2)
                {
                    continue;
                }

                // grouping
                if (currentValue != 0 && currentValue == arr[i2][j2])
                {
                    var dist = manDist(i, j, i2, j2);
                    if (dist != 1) // next to each other
                    {
                        groupSpread -= ((dist * tileCounts.get(currentValue)) / c1);
                    }
                }

                // variance
                if (currentValue == 0 || arr[i2][j2] == 0 || arr[i2][j2] > varianceCutOf || currentValue > varianceCutOf)
                {
                    continue;
                }
                var c2 = (Math.log(arr[i2][j2]) / Math.log(2));
                if (c1 > c2)
                {
                    variance += (c2 - c1);
                }
                else if (c2 > c1)
                {
                    variance += (c1 - c2);
                }
            }
        }

        return new Pair(variance, groupSpread);
    }

    public static double scorePlacement(
            int value,
            int i,
            int j,
            int[] tilesDesc,
            int[] tilesAsc,
            int maxValX,
            int maxValY,
            int length,
            int tilesSet,
            int minValCutOf)
    {
        var maxVal = tilesDesc[0];
        if (maxVal < 128 || tilesDesc.length < 3)
        {
            return 0.0;
        }

        var secondMaxVal = tilesDesc[1];
        var thirdMaxVal = tilesDesc[2];

        double placementSCore = 0.0;
        double minPlacementScore = 0.0;

        // minimize distance to corners for max value
        if (value == maxVal)
        {
            var leftCorner = (Math.min(manDist(0,0,i,j), manDist(length,0,i,j)));
            var rightCorner = (Math.min(manDist(0,length,i,j), manDist(length,length,i,j)));

            var c = (Math.log(maxVal) / Math.log(2));
            var bestCorner = Math.min(leftCorner, rightCorner);

            placementSCore -= (bestCorner * c);

            return placementSCore;
        }

        // minimize distance to corners for second max value (+ more)
        if (value == secondMaxVal && secondMaxVal >= 64)
        {
            var leftTopCorner = (Math.min(manDist(0,1,i,j), manDist(1,0,i,j)));
            var leftBottomCorner = (Math.min(manDist(length,1,i,j), manDist(length-1,0,i,j)));
            var rightTopCorner = (Math.min(manDist(0,length-1,i,j), manDist(1,length,i,j)));
            var rightBottomCorner = (Math.min(manDist(length,length-1,i,j), manDist(length-1,length,i,j)));

            var minLeft = Math.min(leftTopCorner, leftBottomCorner);
            var minRight = Math.min(rightTopCorner, rightBottomCorner);

            var c = (Math.log(secondMaxVal) / Math.log(2));
            placementSCore -= ((Math.min(minLeft, minRight)) * c);

            var distToMax = manDist(maxValX, maxValY, i, j);
            placementSCore -= (distToMax * c);

            return placementSCore;
        }

        // minimize distance to corners for third max value (+ more)
        if (value == thirdMaxVal && thirdMaxVal >= 32)
        {
            var leftTopCorner = (Math.min(manDist(0,1,i,j), manDist(1,0,i,j)));
            var leftBottomCorner = (Math.min(manDist(length,1,i,j), manDist(length-1,0,i,j)));
            var rightTopCorner = (Math.min(manDist(0,length-1,i,j), manDist(1,length,i,j)));
            var rightBottomCorner = (Math.min(manDist(length,length-1,i,j), manDist(length-1,length,i,j)));

            var minLeft = Math.min(leftTopCorner, leftBottomCorner);
            var minRight = Math.min(rightTopCorner, rightBottomCorner);

            var c = (Math.log(thirdMaxVal) / Math.log(2));
            placementSCore -= ((Math.min(minLeft, minRight)) * c);

            var distToMax = manDist(maxValX, maxValY, i, j);
            placementSCore -= (distToMax * c);

            return placementSCore;
        }

        // move small values away from max value
        for (var minVal : tilesAsc)
        {
            var distToMax = 0;

            if (value == minVal && minVal < minValCutOf)
            {
                var c = (Math.log(minValCutOf - minVal) / Math.log(2));
                distToMax = manDist(maxValX, maxValY, i, j);
                minPlacementScore += (distToMax * c);

                return minPlacementScore / tilesSet;
            }
        }

        return placementSCore;
    }

    public static double scoreMerging(
            int v1,
            int v2)
    {
        if (v1 == v2 && v1 != 0)
        {
            return (Math.log(v1) / Math.log(2));
        }
        return 0.0;
    }

    // perfect structure is when the biggest values are placed in corners
    public static double perfectStructure(
            int[][] arr,
            int[] uniqueTilesDesc,
            int length)
    {
        ArrayList<Integer> secondAndThird = new ArrayList<>();
        var maxVal = uniqueTilesDesc[0];

        if (maxVal < 128)
        {
            return 1.0;
        }

        secondAndThird.add(uniqueTilesDesc[1]);
        secondAndThird.add(uniqueTilesDesc[0]);

        double score = 1.0;

        if (arr[0][0] == maxVal && secondAndThird.contains(arr[0][1]) && secondAndThird.contains(arr[1][0]))
        {
            score = 0.6;
        }
        if (arr[length][0] == maxVal && secondAndThird.contains(arr[length][1]) && secondAndThird.contains(arr[length-1][0]))
        {
            score = 0.6;
        }
        if (arr[0][length] == maxVal && secondAndThird.contains(arr[0][length-1]) && secondAndThird.contains(arr[1][length]))
        {
            score = 0.6;
        }
        if (arr[length][length] == maxVal && secondAndThird.contains(arr[length][length-1]) && secondAndThird.contains(arr[length-1][length]))
        {
            score = 0.6;
        }

        return score;
    }

    // manhatten distance
    public static int manDist(int x1, int y1, int x2, int y2)
    {
        return Math.abs(x1-x2) + Math.abs(y1-y2);
    }

    // useful information about current grid
    public static TilesInformation getTilesInformation(int[][] arr)
    {
        ArrayList<Integer> unique = new ArrayList<>();
        HashMap<Integer, Integer> tileCounts = new HashMap<>();
        int currentMaxVal = Integer.MIN_VALUE;
        int maxValueX = Integer.MIN_VALUE;
        int maxValueY = Integer.MIN_VALUE;
        int tilesSet = 0;

        for (var i=0; i<= arr.length-1; i++)
        {
            for (var j=0; j<= arr.length-1; j++)
            {
                var currentValue = arr[i][j];
                if (!unique.contains(currentValue) && currentValue != 0)
                {
                    unique.add(currentValue);
                }
                if (currentValue > currentMaxVal)
                {
                    currentMaxVal = currentValue;
                    maxValueX = i;
                    maxValueY = j;
                }
                if (currentValue != 0)
                {
                    tilesSet++;
                }

                if (tileCounts.containsKey(currentValue))
                {
                    tileCounts.put(currentValue, tileCounts.get(currentValue) + 1);
                }
                else
                {
                    tileCounts.put(currentValue, 1);
                }
            }
        }

        var uniqueTilesAsc = unique.stream().sorted().mapToInt(x -> x).toArray();
        var uniqueTilesDesc = unique.stream().sorted(Collections.reverseOrder()).mapToInt(Integer::intValue).toArray();

        return new TilesInformation(uniqueTilesAsc, uniqueTilesDesc, maxValueX, maxValueY, tilesSet, tileCounts);
    }

}


//
//    public static int scoreMonotonicity(int[][] arr)
//    {
//        var len = arr.length-1;
//
//        var firstDir = 0;
//        var secondDir = 0;
//
//        for (var i=0; i<=len; i++)
//            for (var j=0; j<=len-1; j++)
//            {
//                if (arr[i][j] == 0 || arr[i][j+1] == 0)
//                {
//                    continue;
//                }
//                var c1 = (Math.log(arr[i][j]) / Math.log(2));
//                var c2 = (Math.log(arr[i][j+1]) / Math.log(2));
//
//                if (c1 >= c2)
//                {
//                    secondDir += (c1 - c2);
//                }
//                else if (c2 >= c1)
//                {
//                    firstDir += (c2 - c1);
//                }
//            }
//
//        return Math.max(secondDir, firstDir);
//    }



