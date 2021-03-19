package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class HelperFunctions {

    public int[] getMovesAsInts()
    {
        //    UP = 0;
        //    RIGHT = 1;
        //    DOWN = 2;
        //    LEFT = 3;
        int[] moves = {0, 1, 2, 3};
        return moves;
    }

    public static boolean isSortedAsc(int[] array) {
        if (Arrays.stream(array).sum() == 0)
        {
            return false;
        }

        var sorted = IntStream.range(0, array.length - 1).noneMatch(i -> array[i] < array[i + 1]);
        if (sorted && array[1] == 0)
        {
            return false;
        }

        return sorted;
    }

    public int[] getTilesAsc(int[][] arr)
    {
        ArrayList<Integer> unique = new ArrayList<>();
        for (var row : arr)
        {
            for (var i=0; i <= arr.length-1; i++)
            {
                if (!unique.contains(row[i]))
                {
                    unique.add(row[i]);
                }
            }
        }

        return unique.stream().sorted().mapToInt(x -> x).toArray();
    }

    public ArrayList<Integer> IgnoreMoves(Grid grid)
    {
        ArrayList<Integer> noGoMoves = new ArrayList<>();

        var upCopy = grid.clone();
        var DownCopy = grid.clone();
        var RightCopy = grid.clone();
        var LeftCopy = grid.clone();

        upCopy = movePicesSomeDir(upCopy, 0);
        DownCopy = movePicesSomeDir(DownCopy, 2);
        RightCopy = movePicesSomeDir(RightCopy, 1);
        LeftCopy = movePicesSomeDir(LeftCopy, 3);

        if (grid.equals(upCopy))
        {
            noGoMoves.add(0);
        }

        if (grid.equals(DownCopy))
        {
            noGoMoves.add(2);
        }

        if (grid.equals(RightCopy))
        {
            noGoMoves.add(1);
        }

        if (grid.equals(LeftCopy))
        {
            noGoMoves.add(3);
        }

        return noGoMoves;
    }

    public int getHighestPiece(Grid grid)
    {
        int highest = 0;
        for(int col = 0; col < grid.getNumCols(); col++)
            for(int row = 0; row < grid.getNumRows(); row++)
            {
                if(grid.get(new Location(row, col)) > highest)
                    highest = grid.get(new Location(row, col));
            }

        return highest;
    }

    public int[][] transposeGridArray(Grid grid)
    {
        var gridCopy = grid.clone().getArray();
        var rowsL = gridCopy[0].length;
        var colsL = gridCopy.length;
        int[][] transposed = new int[colsL][rowsL];

        for (int i = 0; i < rowsL; i++)
        {
            for (int j = 0; j < colsL; j++)
            {
                transposed[j][i] = gridCopy[i][j];
            }
        }
        return transposed;

    }

    public Grid movePicesSomeDir(Grid grid, int direction)
    {
        List<Location> locations = grid.getLocationsInTraverseOrder(direction);

        for(Location from : locations)
        {
//            moveStuff(grid, from, direction);
            if(grid.get(from) != -1 && grid.get(from) != 0)
            {
                Location to = from.getAdjacent(direction);
                while(grid.isValid(to))
                {
                    // If the new position is empty, move
                    if(grid.isEmpty(to))
                    {
                        grid.move(from, to);
                        from = to.clone();
                        to = to.getAdjacent(direction);
                    }

                    // If the new position has a piece
                    else
                    {
                        // If they have the same value or if zenMode is enabled, combine
                        if(grid.get(from) == grid.get(to))
                        {
                            grid.set(to, grid.get(to) + grid.get(from));
                            grid.set(from, 0);
                        }
                        break;

                    }
                }
            }
        }

        return grid;
    }

    private void moveStuff(Grid board, Location from, int direction)
    {
        // Do not move X spaces or 0 spaces
        if(board.get(from) != -1 && board.get(from) != 0)
        {
            Location to = from.getAdjacent(direction);
            while(board.isValid(to))
            {
                // If the new position is empty, move
                if(board.isEmpty(to))
                {
                    board.move(from, to);
                    from = to.clone();
                    to = to.getAdjacent(direction);
                }

                // If the new position has a piece
                else
                {
                    // If they have the same value or if zenMode is enabled, combine
                    if(board.get(from) == board.get(to))
                        board.set(to, board.get(to) + board.get(from));
                        board.set(from, 0);

                    return;
                }
            }
        }
    }

    public double getVariance(int[] data)
    {
        double mean = getMean(data);
        double temp = 0;
        int size = data.length;

        for(double a :data)
            temp += (a-mean)*(a-mean);
        return temp/(size-1);
    }

    private double getMean(int[] data)
    {
        double sum = 0.0;
        int size = data.length;
        for(double a : data)
            sum += a;
        return sum/size;
    }
}
