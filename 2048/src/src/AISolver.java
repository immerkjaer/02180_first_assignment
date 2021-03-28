package src;

import game.Game;
import game.Grid;
import game.Location;

import java.util.*;
import java.util.concurrent.*;

public class AISolver
{
    private static double variC;
    private static double emptyC;
    private static double placeC;
    private static double mergeC;
    private static double groupspreadC;
    private static boolean isCrossValidation;

    public AISolver(
            double variC,
            double placeC,
            double groupspreadC,
            double emptyC,
            double mergeC,
            boolean isCrossValidation)
    {
        AISolver.variC = variC;
        AISolver.emptyC = emptyC;
        AISolver.placeC = placeC;
        AISolver.mergeC = mergeC;
        AISolver.groupspreadC = groupspreadC;
        AISolver.isCrossValidation = isCrossValidation;
    }

    public static LinkedList<GameStats> expectimaxAI(Game game) throws InterruptedException, ExecutionException {
        HelperFunctions hC = new HelperFunctions();
        LinkedList<GameStats> decisionStats = new LinkedList<>();

        ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(10);


        while (!(game.lost()))
        {
            long score = Integer.MIN_VALUE;
            int bestMove = Integer.MIN_VALUE;
            int depth = 3;

            ArrayList<MoveToConsider> finalScores = new ArrayList<>();

            if (game.getGrid().getEmptyLocations().size() >= 6)
            {
                depth = 2;
            }
            else if (game.getGrid().getEmptyLocations().size() <= 2)
            {
                depth = 4;
            }

            Grid gridClone = game.getGrid().clone();
            int[] moves = hC.getMovesAsInts();
            ArrayList<Integer> noGoMoves = hC.IgnoreMoves(gridClone);
            FinalScore chosenMoveStats = null;

            ArrayList<Callable<MoveToConsider>> callables = new ArrayList<>();

            for (int move : moves)
            {
                if (noGoMoves.contains(move))
                {
                    continue;
                }

                Grid oldGrid = gridClone.clone();
                Grid newGrid = hC.movePiecesSomeDir(oldGrid, move);
                List<Location> emptyLocsForMove = newGrid.getEmptyLocations();

                callables.add(new RunMove(newGrid, oldGrid, hC, depth, move, emptyLocsForMove.size()));


            }

            List<Future<MoveToConsider>> futures = WORKER_THREAD_POOL.invokeAll(callables);
            for (Future<MoveToConsider> res : futures)
            {
                MoveToConsider moveRes = res.get();
                finalScores.add(moveRes);
            }

            for (MoveToConsider move : finalScores)
            {
                long scoreForMove = move.getFinalScore().getTotalScore();
//                scoreForMove = scoreForMove / move.getInitialEmpty();

                if (scoreForMove > score)
                {
                    score = scoreForMove;
                    bestMove = move.getMoveDir();
                    chosenMoveStats = move.getFinalScore();
                }
            }

            GameStats stats = new GameStats(chosenMoveStats, game.getTurns(), noGoMoves, game.getGrid(), bestMove, game.getScore(), finalScores, game.highestPiece());
            decisionStats.addLast(stats);
            if (decisionStats.size() >= 30)
            {
                decisionStats.removeFirst();
            }

            game.act(bestMove);
            if (!isCrossValidation)
                game.printGame();
        }

        if(isCrossValidation)
        {
            printTuningConstants();
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

        int score = 0;
        int varianceScore = 0;
        int placementScore = 0;
        int maxValScore = 0;
        int spaceScore = 0;
        int mergeScore = 0;
        int groupSpreadScore = 0;
        int statesDiscarded = 0;
        int statesConsidered = 0;

        List<Location> emptyLocations = grid.getEmptyLocations();
        for (Location emptyLoc : emptyLocations)
        {
            Grid gT2 = grid.clone();
            Grid gT4 = grid.clone();

            gT2.set(emptyLoc, 2);
            gT4.set(emptyLoc, 4);

            FinalScore gT2Score = calculateMoveScore(gT2, hC, depth-1);
            FinalScore gT4Score = calculateMoveScore(gT4, hC, depth-1);

            score += (gT2Score.getTotalScore() * 0.9);
            score += (gT4Score.getTotalScore() * 0.1);

            varianceScore += (int) ((gT2Score.getVarianceScore() * 0.9) + (gT4Score.getVarianceScore() * 0.1));
            placementScore += (int) ((gT2Score.getPlacementScore() * 0.9) + (gT4Score.getPlacementScore() * 0.1));
            maxValScore += (int) ((gT2Score.getMaxValScore() * 0.9) + (gT4Score.getMaxValScore() * 0.1));
            spaceScore += (int) ((gT2Score.getSpaceScore() * 0.9) + (gT4Score.getSpaceScore() * 0.1));
            statesDiscarded += ((gT2Score.getStatesDiscarded() * 0.9) + (gT4Score.getStatesDiscarded() * 0.1));
            statesConsidered += ((gT2Score.getStatesConsidered() * 0.9) + (gT4Score.getStatesConsidered() * 0.1));
            groupSpreadScore += ((gT2Score.getGroupSpreadScore() * 0.9) + (gT4Score.getGroupSpreadScore() * 0.1));
            mergeScore += ((gT2Score.getMergeScore() * 0.9) + (gT4Score.getMergeScore() * 0.1));
        }

        return new FinalScore(varianceScore, placementScore, maxValScore, spaceScore, mergeScore, groupSpreadScore, statesDiscarded, statesConsidered, score);
    }

    public static FinalScore calculateMoveScore(
            Grid grid,
            HelperFunctions hC,
            int depth)
    {
        int[] moves = hC.getMovesAsInts();
        int score = Integer.MIN_VALUE;
        int varianceScore = 0;
        int monoScore = 0;
        int placementScore = 0;
        int maxValScore = 0;
        int spaceScore = 0;
        int mergeScore = 0;
        int groupSpreadScore = 0;
        int statesDiscarded = 0;
        int statesConsidered = 0;

        for (int move : moves)
        {
            Grid gridSomeMove = grid.clone();
            Grid newGrid = hC.movePiecesSomeDir(gridSomeMove, move);

            FinalScore moveScore = getMoveScore(newGrid, grid, hC, move, depth);
            if (moveScore.getTotalScore() > score)
            {
                score = moveScore.getTotalScore();
                varianceScore = moveScore.getVarianceScore();
                placementScore = moveScore.getPlacementScore();
                maxValScore = moveScore.getMaxValScore();
                spaceScore = moveScore.getSpaceScore();
                mergeScore = moveScore.getMergeScore();
                groupSpreadScore = moveScore.getGroupSpreadScore();
            }
            statesDiscarded += moveScore.getStatesDiscarded();
            statesConsidered += moveScore.getStatesConsidered();
        }
        return new FinalScore(
                varianceScore,
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
        TilesInformation tilesInformation = getTilesInformation(grid.getArray());
        int maxVal = tilesInformation.getTilesDesc()[0];

        GridScore scoreGrid = scoreBoard(grid, tilesInformation, hC);
        double scoreVariance = scoreGrid.getVarianceScore();
        double scorePlacement = scoreGrid.getPlacementScore();
        double scoreMerge = scoreGrid.getMergeScore();
        double scoreGroupSpread = scoreGrid.getGroupSpread();
        int emptySpaces = grid.getEmptyLocations().size();

        double sVari = variC * scoreVariance;
        double sEmpty = emptyC * Math.log(emptySpaces);
        double sPlace = placeC * scorePlacement;
        double sMerges = mergeC * scoreMerge;
        double sGroupSpread = groupspreadC * scoreGroupSpread;
        double sMax = Math.log(maxVal) / Math.log(2);
        double totalScore = sVari + sEmpty + sPlace + sMax + sMerges + sGroupSpread;

        sVari = Math.round(sVari);
        sEmpty = Math.round(sEmpty);
        sPlace = Math.round(sPlace);
        sMax = Math.round(sMax);
        sMerges = Math.round(sMerges);
        sGroupSpread = Math.round(sGroupSpread);
        totalScore = Math.round(totalScore);

        return new FinalScore(
                (int) sVari,
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
        int[][] gridArr = grid.getArray();
        int[][] gridArrTransposed = hC.transposeGridArray(grid.clone());

        int length = gridArr.length-1;
        int[] uniqueTilesAsc = tilesInformation.getTilesAsc();
        int[] uniqueTilesDesc = tilesInformation.getTilesDesc();
        int maxValX = tilesInformation.getMaxValX();
        int maxValY = tilesInformation.getMaxValY();
        int tilesSet = tilesInformation.getTilesSet();
        HashMap<Integer,Integer> tileCounts = tilesInformation.getTileCounts();
        double perfectStructureScaler = perfectStructure(gridArr, uniqueTilesDesc, length);
        int varianceCutOf = uniqueTilesDesc[0];

        double mergeScore = 0.0;
        double placementScore = 0.0;
        double varianceScore = 0.0;
        double groupSpread = 0.0;

        int rowLen = gridArr.length-1;
        int colLen = gridArr[0].length-1;
        for (int i = 0; i <= rowLen; i++)
        {
            for (int j = 0; j < colLen; j++)
            {
                int currentVal = gridArr[i][j];
                int currentValTransposed = gridArrTransposed[i][j];
                if (j < rowLen)
                {
                    int nextVal = gridArr[i][j+1];
                    int nextValTransposed = gridArrTransposed[i][j+1];

                    mergeScore += scoreMerging(currentVal, nextVal);
                    mergeScore += scoreMerging(currentValTransposed, nextValTransposed);
                }

                placementScore += scorePlacement(currentVal, i, j, uniqueTilesDesc, uniqueTilesAsc, maxValX, maxValY, length, tilesSet, varianceCutOf, tileCounts);
                Pair varianceAndGroupSpread = scoreVarianceAndGrouping(currentVal, i, j, gridArr, varianceCutOf, length, tileCounts);
                varianceScore += varianceAndGroupSpread.getFirst();
                groupSpread += varianceAndGroupSpread.getSecond();

            }
        }

        placementScore = placementScore * perfectStructureScaler;
        return new GridScore(varianceScore, placementScore, mergeScore, groupSpread);
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

        double c1 = (Math.log(currentValue) / Math.log(2));

        for (int i2=0; i2<=length; i2++)
        {
            for (int j2=0; j2<=length; j2++)
            {
                if (i == i2 && j == j2)
                {
                    continue;
                }

                // grouping
                if (currentValue != 0 && currentValue == arr[i2][j2])
                {
                    int dist = manDist(i, j, i2, j2);
                    if (dist != 1) // next to each other
                    {
                        groupSpread -= (dist / c1);
                    }
                }

                // variance
                if (currentValue == 0 || arr[i2][j2] == 0 || arr[i2][j2] > varianceCutOf || currentValue > varianceCutOf)
                {
                    continue;
                }
                double c2 = (Math.log(arr[i2][j2]) / Math.log(2));
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
            int minValCutOf,
            HashMap<Integer, Integer> tileCounts)
    {
        int maxVal = tilesDesc[0];
        if (maxVal < 128 || tilesDesc.length < 3)
        {
            return 0.0;
        }

        int secondMaxVal = tilesDesc[1];
        int thirdMaxVal = tilesDesc[2];

        double placementSCore = 0.0;
        double minPlacementScore = 0.0;

        // minimize distance to corners for max value
        if (value == maxVal)
        {
            int leftCorner = (Math.min(manDist(0,0,i,j), manDist(length,0,i,j)));
            int rightCorner = (Math.min(manDist(0,length,i,j), manDist(length,length,i,j)));

            int c = 8;
            int bestCorner = Math.min(leftCorner, rightCorner);

            placementSCore -= (bestCorner * c);

            return placementSCore;
        }

        // minimize distance to corners for second max value (+ more)
        if (value == secondMaxVal && secondMaxVal >= 64)
        {
            int leftTopCorner = (Math.min(manDist(0,1,i,j), manDist(1,0,i,j)));
            int leftBottomCorner = (Math.min(manDist(length,1,i,j), manDist(length-1,0,i,j)));
            int rightTopCorner = (Math.min(manDist(0,length-1,i,j), manDist(1,length,i,j)));
            int rightBottomCorner = (Math.min(manDist(length,length-1,i,j), manDist(length-1,length,i,j)));

            int minLeft = Math.min(leftTopCorner, leftBottomCorner);
            int minRight = Math.min(rightTopCorner, rightBottomCorner);

            int c = 6;
            placementSCore -= ((Math.min(minLeft, minRight)) * c);

            int distToMax = manDist(maxValX, maxValY, i, j);
            placementSCore -= (distToMax * c);

            return placementSCore;
        }

        // minimize distance to corners for third max value (+ more)
        if (value == thirdMaxVal && thirdMaxVal >= 32)
        {
            int leftTopCorner = (Math.min(manDist(0,1,i,j), manDist(1,0,i,j)));
            int leftBottomCorner = (Math.min(manDist(length,1,i,j), manDist(length-1,0,i,j)));
            int rightTopCorner = (Math.min(manDist(0,length-1,i,j), manDist(1,length,i,j)));
            int rightBottomCorner = (Math.min(manDist(length,length-1,i,j), manDist(length-1,length,i,j)));

            int minLeft = Math.min(leftTopCorner, leftBottomCorner);
            int minRight = Math.min(rightTopCorner, rightBottomCorner);

            int c = 4;
            placementSCore -= ((Math.min(minLeft, minRight)) * c);

            int distToMax = manDist(maxValX, maxValY, i, j);
            placementSCore -= (distToMax * c);

            return placementSCore;
        }

        // move small values away from max value
        for (int minVal : tilesAsc)
        {
            int distToMax = 0;

            if (value == minVal && minVal < thirdMaxVal)
            {
                int c = 1;
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

    // TODO: remove - doesn't currently make any difference
    // perfect structure is when the biggest values are placed in corners
    public static double perfectStructure(
            int[][] arr,
            int[] uniqueTilesDesc,
            int length)
    {
        ArrayList<Integer> secondAndThird = new ArrayList<>();
        int maxVal = uniqueTilesDesc[0];

        if (maxVal < 128)
        {
            return 1.0;
        }

        secondAndThird.add(uniqueTilesDesc[1]);
        secondAndThird.add(uniqueTilesDesc[0]);

        double score = 1.0;

        if (arr[0][0] == maxVal && secondAndThird.contains(arr[0][1]) && secondAndThird.contains(arr[1][0]))
        {
            score = 1.0;
        }
        if (arr[length][0] == maxVal && secondAndThird.contains(arr[length][1]) && secondAndThird.contains(arr[length-1][0]))
        {
            score = 1.0;
        }
        if (arr[0][length] == maxVal && secondAndThird.contains(arr[0][length-1]) && secondAndThird.contains(arr[1][length]))
        {
            score = 1.0;
        }
        if (arr[length][length] == maxVal && secondAndThird.contains(arr[length][length-1]) && secondAndThird.contains(arr[length-1][length]))
        {
            score = 1.0;
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

        for (int i=0; i<= arr.length-1; i++)
        {
            for (int j=0; j<= arr.length-1; j++)
            {
                int currentValue = arr[i][j];
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

        int[] uniqueTilesAsc = unique.stream().sorted().mapToInt(x -> x).toArray();
        int[] uniqueTilesDesc = unique.stream().sorted(Collections.reverseOrder()).mapToInt(Integer::intValue).toArray();

        return new TilesInformation(uniqueTilesAsc, uniqueTilesDesc, maxValueX, maxValueY, tilesSet, tileCounts);
    }

    private static void printTuningConstants()
    {
        System.out.println("\n");
        System.out.println("#############################################");
        System.out.println("Variance - " + variC);
        System.out.println("Empty space - " + emptyC);
        System.out.println("Placement - " + placeC);
        System.out.println("Merge - " + mergeC);
        System.out.println("Group spread - " + groupspreadC);
    }

}