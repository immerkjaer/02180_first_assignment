package AI;

import java.util.ArrayList;

import src.Game;
import src.Grid;
import src.Location;

public class Autoplayer {
	
	public static int expectimax(Game game) {
        int move = Location.DOWN;
        ArrayList<Integer> invalidMoves = new ArrayList<>();

        while (!(game.lost())) {
            Grid tmp = game.getGrid().clone();
            invalidMoves.clear();
            boolean firstMove = true;
            while (game.getGrid().equals(tmp)) {
                if (!firstMove) {
                    invalidMoves.add(move);
                }

                int bestAlignmentDirection = bestAlignment(game, invalidMoves);
                int minRisk = riskOfStuck(game, invalidMoves);

                if (bestAlignmentDirection >= 0 && !invalidMoves.contains(bestAlignmentDirection)) {
                    move = bestAlignmentDirection;
                } else if (minRisk != -1) {
                    move = minRisk;
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
                System.out.println("Acting " + getLocationString(move));
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
        GameHelper board = new GameHelper(game);
        GameHelper rightBoard = new GameHelper(rightMove);
        GameHelper leftBoard = new GameHelper(leftMove);
        int bottomRow = game.getGrid().getNumRows() - 1;

        // If bottom is not full, always move right
        if (!board.rowIsFull(bottomRow)) {
            return Location.RIGHT;
        }

        rightBoard.actPretend(Location.RIGHT);
        leftBoard.actPretend(Location.LEFT);

        // Hvis noget kan slås sammen i bunden, så gør vi det til højre
        if (!rightBoard.rowIsFull(bottomRow) && board.rowIsFull(bottomRow)) {
            return Location.RIGHT;
        } else if (leftBoard.rowIsFull(bottomRow) && leftBoard.rowAlignments() > 0) {
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
        GameHelper board = new GameHelper(game);
        GameHelper rightBoard = new GameHelper(rightMove);
        GameHelper leftBoard = new GameHelper(leftMove);
        GameHelper downBoard = new GameHelper(downMove);

        rightMove.act(Location.RIGHT);
        leftMove.act(Location.LEFT);
        downMove.act(Location.DOWN);

        int[] alignments = new int[5];

        // before moving
        alignments[0] = board.rowAlignments();
        alignments[1] = board.columnAlignments();
        // after moving
        alignments[2] = invalidMoves.contains(Location.RIGHT) ? 0 : Math.max(rightBoard.rowAlignments(), rightBoard.columnAlignments());
        alignments[3] = invalidMoves.contains(Location.LEFT) ? 0 : Math.max(leftBoard.rowAlignments(), leftBoard.columnAlignments());
        alignments[4] = invalidMoves.contains(Location.DOWN) ? 0 : Math.max(downBoard.rowAlignments(), downBoard.columnAlignments());

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

    public static int riskOfStuck(Game game, ArrayList<Integer> invalidMoves) {
        Game down = game.clone();
        Game right = game.clone();
        Game left = game.clone();
        GameHelper downBoard = new GameHelper(down);
        GameHelper rightBoard = new GameHelper(right);
        GameHelper leftBoard = new GameHelper(left);

        rightBoard.actPretend(Location.RIGHT);
        leftBoard.actPretend(Location.LEFT);
        downBoard.actPretend(Location.DOWN);

        ArrayList<Integer> chanceOfStuck = new ArrayList<>();

        double riskDown = downBoard.eval();
        double riskRight = rightBoard.eval();
        double riskLeft = leftBoard.eval();


        if (riskDown > 0 && !invalidMoves.contains(Location.DOWN)) {
            chanceOfStuck.add(Location.DOWN);
        } else if (invalidMoves.contains(Location.DOWN)) {
            riskDown = 1;
        }
        if (riskRight > 0 && !invalidMoves.contains(Location.RIGHT)) {
            chanceOfStuck.add(Location.RIGHT);
        } else if (invalidMoves.contains(Location.RIGHT)) {
            riskRight = 1;
        }
        if (riskLeft > 0 && !invalidMoves.contains(Location.LEFT)) {
            chanceOfStuck.add(Location.LEFT);
        } else if (invalidMoves.contains(Location.LEFT)) {
            riskLeft = 1;
        }

        if (chanceOfStuck.size() != 0) {
            if (riskDown <= riskLeft && riskDown <= riskRight) {
                return Location.DOWN;
            }
            else if (riskRight <= riskLeft && riskRight <= riskDown) {
                return Location.RIGHT;
            }
            else if (riskLeft <= riskRight && riskLeft <= riskDown) {
                return Location.LEFT;
            }
        }

        return -1;
    }

	public static String getLocationString(int dir){
		switch (dir) {
			case Location.UP:
				return "up";
			case Location.RIGHT:
				return "right";
			case Location.DOWN:
				return "down";
			case Location.LEFT:
				return "left";
		}
		return "";
	}

}
