package src;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Autoplay
{
	private static class runStats
	{
		public final int score;
		public final int firstMove;

		runStats(int score, int firstMove)
		{
			this.score = score;
			this.firstMove = firstMove;
		}
	}

	// Used for the recursive autoplay method to determine
	// the total number of moves
	private static int autoMoveCount = 0;
	
	// Moves up, left, down, right until it loses and returns the final score
	public static int circlePlay(Game game)
	{
		while(!(game.lost()))
		{
			System.out.println(game);
			System.out.println("Moving up");
			game.act(Location.UP);
			System.out.println(game);
			System.out.println("Moving left");
			game.act(Location.LEFT);
			System.out.println(game);
			System.out.println("Moving down");
			game.act(Location.DOWN);
			System.out.println(game);
			System.out.println("Moving right");
			game.act(Location.RIGHT);
		}
		System.out.println(game);
		return game.getScore();
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
					System.out.println("Acting up");
					game.act(Location.UP);
				}
				else
				{
					System.out.println("Acting left");
					game.act(Location.LEFT);
				}
			else
				if(num > .25)
				{
					System.out.println("Acting down");
					game.act(Location.DOWN);
				}
				else
				{
					System.out.println("Acting right");
					game.act(Location.RIGHT);
				}

			System.out.println(game);
		}

		System.out.println("GAME LOST");
		return game.getScore();
	}

	public static runStats randomPlay2(Game game)
	{
		double num;
		int firstMove = Integer.MAX_VALUE;
		while(!(game.lost() || game.won()))
		{
			num = Math.random();

			if(num > 0.75)
			{
				game.act(Location.UP);
				if (firstMove > 3) { firstMove = Location.UP; }
			}

			if(num < 0.75 && num > 0.5)
			{
				game.act(Location.DOWN);
				if (firstMove > 3) { firstMove = Location.DOWN; }
			}

			if(num < 0.5 && num > 0.25)
			{
				game.act(Location.RIGHT);
				if (firstMove > 3) { firstMove = Location.RIGHT; }
			}
			if(num < 0.25)
			{
				game.act(Location.LEFT);
				if (firstMove > 3) { firstMove = Location.LEFT; }
			}
		}

		return new runStats(game.getScore(), firstMove);
	}

	public static int monteCarlo(Game game)
	{
		ArrayList<runStats> runs = new ArrayList<runStats>();
		ArrayList<Game> games = new ArrayList<Game>();
		for (var i = 0; i < 700; i++)
		{
			games.add(game.clone());
		}

		for (Game g : games)
		{
			var run = randomPlay2(g);
			runs.add(run);

			if(g.won()) { break; }
		}
		return getBestMove(runs);
	}

	public static int getBestMove(ArrayList<runStats> runs)
	{
		var runsUp = runs.stream().filter(r -> r.firstMove == Location.UP).collect(Collectors.<runStats>toList());
		var runsDown = runs.stream().filter(r -> r.firstMove == Location.DOWN).collect(Collectors.<runStats>toList());
		var runsRight = runs.stream().filter(r -> r.firstMove == Location.RIGHT).collect(Collectors.<runStats>toList());

		var runsLeft = runs.stream().filter(r -> r.firstMove == Location.LEFT).collect(Collectors.<runStats>toList());

		ArrayList<List<runStats>> runsFiltered = new ArrayList<List<runStats>>();
		runsFiltered.add(runsUp);
		runsFiltered.add(runsDown);
		runsFiltered.add(runsRight);
		runsFiltered.add(runsLeft);

		int bestAvg = 0;
		int chosenDir = Integer.MIN_VALUE;
		for (List<runStats> runFil : runsFiltered)
		{
			var avgSomeDir = getAvg(runFil);
			if (bestAvg < avgSomeDir)
			{
				bestAvg = avgSomeDir;
				// will trigger exception with few runs
				chosenDir = runFil.get(0).firstMove;
			}
		}

		return chosenDir;

	}

	public static int getAvg(List<runStats> runs)
	{
		var sum = 0;
		for (runStats r : runs)
		{
			sum += r.score;
		}
		if (runs.size() == 0)
		{
			return 0;
		}
		return sum / runs.size();
	}

	public static int MonteCarloSolver(Game game)
	{
		while(!(game.lost()))
		{
			var chosenDir = monteCarlo(game);
			game.act(chosenDir);

			// For each direction find how many spots left.
			//

			System.out.println("#######");
			System.out.println(chosenDir);
			System.out.println(game);

		}

		return game.getScore();
	}

	// Moves up, left, up, left until it can't move
	// then goes right, if still can't move goes down
	public static int cornerPlay(Game game)
	{
		while(!(game.lost()))
		{
			while(game.canMove(Location.RIGHT) || game.canMove(Location.UP) ||
					game.canMove(Location.LEFT))
			{
				while(game.canMove(Location.UP) || game.canMove(Location.LEFT))
				{
					System.out.println("Acting up");
					game.act(Location.UP);
					System.out.println(game);

					System.out.println("Acting left");
					game.act(Location.LEFT);
					System.out.println(game);
				}
				System.out.println("Acting right");
				game.act(Location.RIGHT);
				System.out.println(game);
			}
			System.out.println("Acting down");
			game.act(Location.DOWN);
			System.out.println(game);

		}

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
