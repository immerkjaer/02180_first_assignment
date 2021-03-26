package src;

import game.Game;

public class Main {
	
	public static void main(String[] args)
	{
		try
		{
			double variC = 0.035;
			double emptyC = 30;
			double placeC = 0.8;
			double mergeC = 7.5;
			double groupspreadC = 0.55;

			if (args.length == 0)
			{
				var solver = new AISolver(variC, placeC, groupspreadC, emptyC, mergeC, false);
				solver.expectimaxAI(new Game());

				Autoplayer.expectimax(new Game());
				System.exit(0);
			}

			else
			{
				int i = 0;
				while (i < args.length)
				{
					if (args[i].equals("-vari")) {
						variC = Double.valueOf(args[i+1]);
						i += 2;
						continue;
					}

					if (args[i].equals("-empty")) {
						emptyC = Double.valueOf(args[i+1]);
						i += 2;
						continue;
					}

					if (args[i].equals("-place")) {
						placeC = Double.valueOf(args[i+1]);
						i += 2;
						continue;
					}

					if (args[i].equals("-merge")) {
						mergeC = Double.valueOf(args[i+1]);
						i += 2;
						continue;
					}

					if (args[i].equals("-groupspread")) {
						groupspreadC = Double.valueOf(args[i + 1]);
						i += 2;
						continue;
					}
				}

				var solver = new AISolver(variC, placeC, groupspreadC, emptyC, mergeC, true);
				solver.expectimaxAI(new Game());
				solver.expectimaxAI(new Game());
				System.out.println("__________________________________________________________________________________");
				System.exit(0);
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
