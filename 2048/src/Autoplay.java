package src;

import java.util.ArrayList;

public class Autoplay
{
	// Used for the recursive autoplay method to determine
	// the total number of moves
	private static int autoMoveCount = 0;


	// Moves randomly and returns the final score
		public static int expectimax(Game game)
		{
			int move = Location.DOWN;
			ArrayList<Integer> invalidMoves = new ArrayList<>();

			while(!(game.lost()))
			{
				Grid tmp = game.getGrid().clone();
				invalidMoves.clear();
				boolean firstMove = true;
				while (game.getGrid().equals(tmp)) {
					if (!firstMove) {
						invalidMoves.add(move);
					}

					int bestAlignmentDirection = bestAlignment(game, invalidMoves);
					if (bestAlignmentDirection >= 0 && !invalidMoves.contains(bestAlignmentDirection)) {
						move = bestAlignmentDirection;
					} else if (move == Location.DOWN && !invalidMoves.contains(Location.DOWN)) {
						// direction of move doesn't change
					} else if (move == Location.RIGHT && !invalidMoves.contains(Location.RIGHT)) {
						// direction of move doesn't change
					} else if (!invalidMoves.contains(Location.DOWN)) { //down
						move = Location.DOWN;
					} else if (!invalidMoves.contains(Location.RIGHT) && invalidMoves.contains(Location.LEFT)) { //go right if you can't go left
						move = Location.RIGHT;
					} else if (!invalidMoves.contains(Location.LEFT) && invalidMoves.contains(Location.RIGHT)) { //go left if you can't go right
						move = Location.LEFT;
					} else if (invalidMoves.contains(Location.RIGHT) && invalidMoves.contains(Location.LEFT)) { //up
						move = Location.UP;
					} else {
						move = leftOrRight(game);
					}
					System.out.println("Acting " + Location.getLocationString(move));
					System.out.println("Invalid moves: " + invalidMoves.toString());
					game.act(move);
					System.out.println(game);
					firstMove = false;
				}
			}
			System.out.println("GAME LOST");
			return game.getScore();
		}
		
	public static int leftOrRight(Game game) {
		Game rightMove = game.clone();
		Game leftMove = game.clone();
		int bottomRow = game.getGrid().getNumRows()-1;

		// If bottom is not full, always move right
		if (!game.getGrid().rowIsFull(bottomRow)) {
			return Location.RIGHT;
		}

		rightMove.act2(Location.RIGHT);
		leftMove.act2(Location.LEFT);

		// Hvis noget kan slås sammen i bunden, så gør vi det til højre
		if (!rightMove.getGrid().rowIsFull(bottomRow) && game.getGrid().rowIsFull(bottomRow)) {
			return Location.RIGHT;
		} else if (leftMove.getGrid().rowIsFull(bottomRow) && leftMove.getGrid().rowAlignments() > 0) {
			return Location.LEFT;
		} else if (rightMove.getGrid().getEmptyLocations().size() >= leftMove.getGrid().getEmptyLocations().size()) {
			return Location.RIGHT;
		} else {
			return Location.LEFT;
		}
	}

	public static int bestAlignment(Game game, ArrayList<Integer> invalidMoves) {
		int direction = Location.DOWN;
		Game rightMove = game.clone();
		Game leftMove = game.clone();
		Game downMove = game.clone();

		rightMove.act(Location.RIGHT);
		leftMove.act(Location.LEFT);
		downMove.act(Location.DOWN);

		int[] alignments = new int[5];

		// before moving
		alignments[0] = game.getGrid().rowAlignments();
		alignments[1] = game.getGrid().columnAlignments();
		// after moving
		alignments[2] = invalidMoves.contains(Location.RIGHT) ? 0 : Math.max(rightMove.getGrid().rowAlignments(), rightMove.getGrid().columnAlignments());
		alignments[3] = invalidMoves.contains(Location.LEFT) ? 0 : Math.max(leftMove.getGrid().rowAlignments(), leftMove.getGrid().columnAlignments());
		alignments[4] = invalidMoves.contains(Location.DOWN) ? 0 : Math.max(downMove.getGrid().rowAlignments(), downMove.getGrid().columnAlignments());

		int max = 0;
		int index = 0;
		for (int i = 0; i < alignments.length; i++) {
			if (alignments[i] > max) {
				max = alignments[i];
				index = i;
			}
		}
		if (max == 0) {
			index = -1;
		}
		switch (index) {
			case 0:
				direction = Location.DOWN;
				break;
			case 1:
				if (alignments[2] >= alignments[3]) {
					direction = Location.RIGHT;
				} else {
					direction = Location.LEFT;
				}
				break;
			case 2:
				direction = Location.RIGHT;
				break;
			case 3:
				direction = Location.LEFT;
				break;
			case 4:
				direction = Location.DOWN;
				break;
			default:
				direction = -1;
		}

		return direction;
	}

	// Moves randomly and returns the final score
	public static int randomPlay(Game game)
	{
		double num;
		while(!(game.lost()))
		{
			num = Math.random();
			if(num > .5)
				if(num > .75)
				{
					//System.out.println("Acting up");
					game.act(Location.UP);
				}
				else
				{
					//System.out.println("Acting left");
					game.act(Location.LEFT);
				}
			else
				if(num > .25)
				{
					//System.out.println("Acting down");
					game.act(Location.DOWN);
				}
				else
				{
					//System.out.println("Acting right");
					game.act(Location.RIGHT);
				}
			//System.out.println(game);
		}
		//System.out.println("GAME LOST");
		return game.getScore();
	}
	

	
	
	

	// I ran the game over 100 times with a 10,000 move limit
	// The number of moves it took to reach 2048:
	// Min:932   |  Q1: 1707   |  Median: 2759
	// Q3: 5822  |  Max: 10000 |  Average: 4165
	// When the move gets above Q3 (6000 moves) the game resets to the original
	// The game should now take less than 12,000 moves, 94% of games

	public static boolean recursivePlay(Game game, Game original, int tile, boolean upFirst)
	{
		System.out.println(game);

		if(game.won(tile))
			return true;

		Game lastTurn = game.clone();
		autoMoveCount += 1;

		// Undos the the entire game every 6000 moves
		if(tile <= 2048 && autoMoveCount % 6000 == 0)
		{
			System.out.println("Reseting the game");
			game = original.clone();
			System.out.println(game);
		}

		// Stops automatically after 150000 moves because
		// most games take only 2000-3000
		if(autoMoveCount >= 15000)
		{
			System.out.println("***** Time Limit Reached *****");
			return true;
		}


		if(upFirst)
		{
			game.act(Location.UP);
			if(! (game.lost() || game.equals(lastTurn)))
				if(recursivePlay(game.clone(), original, tile, !upFirst))
					return true;

			game.act(Location.LEFT);
			if(! (game.lost() || game.equals(lastTurn)))
				if(recursivePlay(game.clone(), original, tile, !upFirst))
					return true;

		}
		else
		{
			game.act(Location.LEFT);
			if(! (game.lost() || game.equals(lastTurn)))
				if(recursivePlay(game.clone(), original, tile, !upFirst))
					return true;

			game.act(Location.UP);
			if(! (game.lost() || game.equals(lastTurn)))
				if(recursivePlay(game.clone(), original, tile, !upFirst))
					return true;
		}


		game.act(Location.RIGHT);
		if(! (game.lost() || game.equals(lastTurn)))
			if(recursivePlay(game.clone(), original, tile, false))
				return true;

		game.act(Location.DOWN);
		if(! (game.lost() || game.equals(lastTurn)))
			if(recursivePlay(game.clone(), original, tile, false))
				return true;

		System.out.println("**** Undo ****");
		return false;
	}

	public static int getAutoMoveCount()
	{
		return autoMoveCount;
	}

	public static void setAutoMoveCount(int value)
	{
		autoMoveCount = value;
	}

}
