package AI;

import src.Game;

public class MainClass {
	
	static Autoplayer play = new Autoplayer();
	
	public static void main(String[] args)
	{
		
		Game game = new Game();
		game.setMoveLimit(-1);
		game.setUndoLimit(-1);
		game.setTimeLimit(-1);
		
		Autoplayer.expectimax(game);
	
	}

}
