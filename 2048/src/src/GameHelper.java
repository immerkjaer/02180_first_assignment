package src;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import game.Game;
import game.Location;
import game.Grid;

public class GameHelper {
	
	public Game game;
	
	public Grid grid;
	
	public GameHelper(Game game) {
		this.game = game;
		this.grid = game.getGrid();
	}
	
	public void actPretend(int direction)
	{
		//Grid lastBoard = this.board.clone();
		Grid boardClone = game.board.clone();

		List<Location> locations = boardClone.getLocationsInTraverseOrder(direction);


		for(Location loc : locations)
			movePretend(boardClone, loc, direction);

	}

	private void movePretend(Grid board, Location from, int direction)
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
					// If they have the same value, combine
					if(board.get(from) == board.get(to))
						game.add(from, to);

					return;
				}
			}
		}
	}
	
    public boolean rowIsFull(int rowNo) {

        for (int col = 0; col < grid.getNumCols(); col++) {
            if (grid.board[rowNo][col] == 0) {
                return false;
            }
        }

        return true;
    }

    public int rowAlignments() {
        int alignments = 0;
        for (int col = 0; col < grid.getNumCols(); col++) {
            for (int row = 1; row < grid.getNumRows(); row++) {
                if (grid.board[row-1][col] == grid.board[row][col]) {
                    alignments += grid.board[row][col];
                }
            }
        }
        return alignments;
    }

    public int columnAlignments() {
        int alignments = 0;
        for (int row = 0; row < grid.getNumRows(); row++) {
            for (int col = 1; col < grid.getNumCols(); col++) {
                if (grid.board[row][col - 1] == grid.board[row][col]) {
                    alignments += grid.board[row][col];
                }
            }
        }
        return alignments;
    }

    public boolean rowIsSorted(int rowNo, boolean fromRightToLeft) {
        if (fromRightToLeft) {
            for (int col = grid.getNumCols() - 2; col >= 0; col--) {
                if (grid.board[rowNo][col + 1] == 0) {
                    //
                } else if (grid.board[rowNo][col] > grid.board[rowNo][col + 1]) {
                    return false;
                }
            }
        } else {
            for (int col = 1; col < grid.getNumCols(); col++) {
                if (grid.board[rowNo][col - 1] == 0) {
                    //
                } else if (grid.board[rowNo][col] < grid.board[rowNo][col - 1]) {
                    return false;
                }
            }
        }
        return true;
    }



    public double eval() {
        List<Location> emptyLoc2 = getEmptyLocationsInRow(grid.getNumRows()-3);
        List<Location> emptyLoc3 = getEmptyLocationsInRow(grid.getNumRows()-2);
        List<Location> topRow1 = getEmptyLocationsInRow(0);
        List<Location> topRow2 = getEmptyLocationsInRow(1);
        if (rowIsFull(grid.getNumRows()-1) && rowIsFull(grid.getNumRows()-2)
                && emptyLoc2.size() == 1 && topRow1.size() == 4) {
            if (rowAlignments() == 0 && columnAlignments() == 0) {
                ArrayList<Integer> neighbours = getNeighbours(emptyLoc2.get(0));
                if (neighbours.contains(2) && neighbours.contains(4)) {
                    return 0.0;
                } else if (neighbours.contains(2)) {
                    return 1.0/5.0 * (1-Game.CHANCE_OF_2);
                } else if (neighbours.contains(4)) {
                    return  1.0/5.0 * Game.CHANCE_OF_2;
                } else {
                    return 1.0/5.0;
                }
            }
        } else if (rowIsFull(grid.getNumRows()-1)
                && emptyLoc3.size() == 1 && topRow1.size() == 4 && topRow2.size() == 4) {
            if (rowAlignments() == 0 && columnAlignments() == 0) {
                ArrayList<Integer> neighbours = getNeighbours(emptyLoc3.get(0));
                if (neighbours.contains(2) && neighbours.contains(4)) {
                    return 0.0;
                } else if (neighbours.contains(2)) {
                    return 1.0/9.0 * (1-Game.CHANCE_OF_2);
                } else if (neighbours.contains(4)) {
                    return  1.0/9.0 * Game.CHANCE_OF_2;
                } else {
                    return 1.0/9.0;
                }
            }
        }
        return 0.0;
    }

    public ArrayList<Integer> getNeighbours(Location loc) {
        ArrayList<Integer> neighbours = new ArrayList<Integer>();
        if (loc.getCol() == 0) {
            neighbours.add(grid.board[loc.getRow()][loc.getCol()+1]);
        } else if (loc.getCol() == grid.getNumCols()-1) {
            neighbours.add(grid.board[loc.getRow()][loc.getCol()-1]);
        } else {
            neighbours.add(grid.board[loc.getRow()][loc.getCol()+1]);
            neighbours.add(grid.board[loc.getRow()][loc.getCol()-1]);
        }
        neighbours.add(grid.board[loc.getRow()+1][loc.getCol()]);
        return neighbours;
    }

    public List<Location> getEmptyLocationsInRow(int rowNo) {
        LinkedList<Location> empty = new LinkedList<Location>();
            for (int col = 0; col < grid.getNumCols(); col++)
                if (grid.board[rowNo][col] == 0)
                    empty.add(new Location(rowNo, col));
        return empty;
    }

}
