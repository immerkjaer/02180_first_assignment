package src;

import game.Grid;
import game.Location;

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

    public ArrayList<Integer> IgnoreMoves(Grid grid)
    {
        ArrayList<Integer> noGoMoves = new ArrayList<>();

        Grid upCopy = grid.clone();
        Grid DownCopy = grid.clone();
        Grid RightCopy = grid.clone();
        Grid LeftCopy = grid.clone();

        upCopy = movePiecesSomeDir(upCopy, 0);
        DownCopy = movePiecesSomeDir(DownCopy, 2);
        RightCopy = movePiecesSomeDir(RightCopy, 1);
        LeftCopy = movePiecesSomeDir(LeftCopy, 3);

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

    public int[][] transposeGridArray(Grid grid)
    {
        int[][] gridCopy = grid.clone().getArray();
        int rowsL = gridCopy[0].length;
        int colsL = gridCopy.length;
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

    public Grid movePiecesSomeDir(Grid grid, int direction)
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

    public static boolean isSortedAsc(int[] array)
    {
        if (Arrays.stream(array).sum() == 0)
        {
            return false;
        }

        boolean sorted = IntStream.range(0, array.length - 1).noneMatch(i -> array[i] < array[i + 1]);
        if (sorted && array[1] == 0)
        {
            return false;
        }

        return sorted;
    }
}
