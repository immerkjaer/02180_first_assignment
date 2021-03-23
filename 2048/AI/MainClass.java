package AI;

import src.Game;

import java.util.concurrent.ExecutionException;

public class MainClass {
	
	static Autoplayer play = new Autoplayer();
	
	public static void main(String[] args) throws ExecutionException, InterruptedException {
		var stats = AISolver.expectimaxAi(new Game());

		Game game = new Game();
		game.setMoveLimit(-1);
		game.setUndoLimit(-1);
		game.setTimeLimit(-1);
		
		Autoplayer.expectimax(game);
	
	}

}
