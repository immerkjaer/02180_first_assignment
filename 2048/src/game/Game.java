package src.game;

import java.util.*;
public class Game implements java.io.Serializable
{
	private static final long serialVersionUID = 3356339029021499348L;

	// The main board the game is played on
	public Grid board;

	// The chance of a 2 appearing
	public static final double CHANCE_OF_2 = .90;
	
	private int score = 0;
	private int turnNumber = 0;

	// Used to start the time limit on the first move
	// instead of when the game is created
	private boolean newGame = true;

	/**
	 * Creates a default game with the size 4x4
	 */
	public Game()
	{
		this(4,4);
	}
	
	/**
	 * @param rows The number of rows in the game
	 * @param cols The number of columns in the game
	 */
	public Game(int rows, int cols)
	{
		// The main board the game is played on
		board = new Grid(rows,cols);
		
		// Keeps track of the turn number
		turnNumber = 1;

		// Adds 2 pieces to the board
		addRandomPiece();
		addRandomPiece();
	}
	
	/**
	 * Creates a new game as a clone.
	 * Only used by the clone method
	 * @param toClone The game to clone
	 */
	private Game(Game toClone)
	{
		board = toClone.board.clone();
		turnNumber = toClone.turnNumber;
		score = toClone.score;

		newGame = toClone.newGame;
	}

	
	/**
	 * Moves the entire board in the given direction
	 * @param direction Called using a final variable in the location class
	*/
	public void act(int direction)
	{
		// Used to determine if any pieces moved
		Grid lastBoard = board.clone();
				
		// If moving up or left start at location 0,0 and move right and down
		// If moving right or down start at the bottom right and move left and up
		List<Location> locations = board.getLocationsInTraverseOrder(direction);
		
		// Move each piece in the direction
		for(Location loc : locations)
			move(loc, direction);
		
		// If no pieces moved then it was not a valid move
		if(! board.equals(lastBoard))
		{
			turnNumber++;
			addRandomPiece();
		}
	}
	
	/** 
	 * Move a single piece all of the way in a given direction
	 * Will combine with a piece of the same value
	 * @param from The location of he piece to move
	 * @param direction Called using a final variable in the location class
	 */
	private void move(Location from, int direction)
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
						add(from, to);
					
					return;
				}
			}
		}
	}

	/**
	 * Adds piece "from" into piece "to", 4 4 -> 0 8
	 * Precondition: from and to are valid locations with equal values
	 * @param from The piece to move
	 * @param to The destination of the piece
	*/
	public void add(Location from, Location to)
	{
		score += board.get(to) + board.get(from);
		board.set(to, board.get(to) + board.get(from));
		board.set(from, 0);
	}

	/**
	 * Randomly adds a new piece to an empty space
	 * 90% add 2, 10% add 4
	 * CHANCE_OF_2 is a final variable declared at the top
	 * 
	 * If dynamicTileSpawing is true,
	 * any tile less than the max tile can spawn
	 * Ex. If the highest piece is 32 then a 2,4,8, or 16 can appear
	 * All possible tiles have an equal chance of appearing
	 */
	public void addRandomPiece()
	{
		if(Math.random() < CHANCE_OF_2)
			addRandomPiece(2);
		else
			addRandomPiece(4);
	}
	
	/**
	 * Adds a specified tile to the board in a random location
	 * @param tile The number tile to add
	 */
	private void addRandomPiece(int tile)
	{
		// A list of the empty spaces on the board
		List<Location> empty = board.getEmptyLocations();

		// If there are no empty pieces on the board don't do anything
		if(! empty.isEmpty())
		{
			int randomLoc = (int) (Math.random() * empty.size());
			board.set(empty.get(randomLoc), tile);
		}
	}
	
	/**
	 * @return Whether or not the game is won
	 * A game is won if there is a 2048 tile or greater
	 */
	public boolean won()
	{
		return won(2048);
	}
	
	/**
	 * @param winningTile The target tile
	 * @return If a tile is >= winningTile
	 */
	public boolean won(int winningTile)
	{
		Location loc;
		for(int col = 0; col < board.getNumCols(); col++)
		{
			for(int row = 0; row < board.getNumRows(); row++)
			{
				loc = new Location(row, col);
				if(board.get(loc) >= winningTile)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * @return If the game is lost
	 */
	public boolean lost()
	{
		// If the board is not filled then the game is lost
		if(!board.getEmptyLocations().isEmpty())
			return false;
		
		int current = -1;
		int next;
		
		// Check if two of the same number are next to each
		// other in a row.
		for(int row = 0; row < board.getNumRows(); row++)
		{
			for(int col = 0; col < board.getNumCols(); col++)
			{
				next = current;
				current = board.get(new Location(row,col));
				
				if(current == next)
					return false;
			}
			current = -1;
		}
		
		// Check if two of the same number are next to each
		// other in a column.
		for(int col = 0; col < board.getNumCols(); col++)
		{
			for(int row = 0; row < board.getNumRows(); row++)
			{
				next = current;
				current = board.get(new Location(row,col));
				
				if(current == next)
					return false;
			}
			current = -1;
		}
		return true;
	}

	/**
	 * @return The highest piece on the board
	 */
	public int highestPiece()
	{
		int highest = 0;
		for(int col = 0; col < board.getNumCols(); col++)
			for(int row = 0; row < board.getNumRows(); row++)
			{
				if(board.get(new Location(row, col)) > highest)
					highest = board.get(new Location(row, col));
			}
		
		return highest;
	}

	/**
	 * @param otherGame The other game to check
	 * @return If the games are equal
	 * Games are equal if they have the same board and score, 
	 * even if their history is different.
	 */
	public boolean equals(Game otherGame)
	{
		return board.equals(otherGame.getGrid()) && score == otherGame.getScore();
	}
	
	/**
	 * Used to avoid creating aliases 
	 * @return A clone of the game
	 */
	public Game clone()
	{
		Game game = new Game(this);
		return game;
	}

	/**
	 * @return The score of the game
	 */
	public int getScore()
	{
		return score;
	}
	
	/**
	 * @return The current turn number of the game
	 */
	public int getTurns()
	{
		return turnNumber;
	}
	
	/**
	 * @return The grid of the game
	 */
	public Grid getGrid()
	{
		return board;
	}
	
	/**
	 * Only used in the hideTileValues and speedMode methods to print the game
	 */
	public void printGame()
	{
		System.out.println(toString());
	}
	
	/** @return a string of the game in the form:
	---------------------------------------------
	||  Turn #8  Score: 20  Moves Left: 3
	---------------------------------------------
	| 8  |    | 2  |    |
	| 4  |    |    |    |
	| 2  |    |    | 2  |
	|    |    |    |    |		*/
	public String toString()
	{
		String output = "---------------------------------------------\n";
		output += "||  Turn #" + turnNumber + "  Score: " + score + "\n";
		output += "---------------------------------------------\n";
		output += board.toString();
		
		return output;
	}
}