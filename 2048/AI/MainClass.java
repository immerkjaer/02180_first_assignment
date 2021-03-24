package AI;

import src.Game;

import java.util.concurrent.ExecutionException;

public class MainClass {
	
	public static void main(String[] args) throws ExecutionException, InterruptedException {
		Game game = new Game();
		game.setMoveLimit(-1);
		game.setUndoLimit(-1);
		game.setTimeLimit(-1);

		AISolver.expectimaxAI(game);
		Autoplayer.expectimax(game);
	}
}
