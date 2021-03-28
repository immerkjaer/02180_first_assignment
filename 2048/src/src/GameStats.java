package src;

import game.Grid;

import java.util.ArrayList;

public class GameStats
{
    private FinalScore finalScore;
    private int turn;
    private ArrayList<Integer> noGoMoves;
    private Grid grid;
    private int move;
    private int score;
    private ArrayList<MoveToConsider> otherOptions;
    private int maxValue;


    public GameStats(
            FinalScore finalScore,
            int turn,
            ArrayList<Integer> noGoMoves,
            Grid grid,
            int move,
            int score,
            ArrayList<MoveToConsider> otherOptions,
            int maxValue)
    {
        this.finalScore = finalScore;
        this.turn = turn;
        this.noGoMoves = noGoMoves;
        this.grid = grid;
        this.move = move;
        this.score = score;
        this.otherOptions = otherOptions;
        this.maxValue = maxValue;
    }
}
