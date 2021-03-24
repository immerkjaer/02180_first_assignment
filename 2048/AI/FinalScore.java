package AI;

public class FinalScore
{
    private int varianceScore;
    private int placementScore;
    private int maxValScore;
    private int spaceScore;
    private int mergeScore;
    private int groupSpreadScore;
    private int statesDiscarded;
    private int statesConsidered;
    private int totalScore;

    public FinalScore(
            int varianceScore,
            int placementScore,
            int maxValScore,
            int spaceScore,
            int mergeScore,
            int groupSpreadScore,
            int statesDiscarded,
            int statesConsidered,
            int totalScore)
    {
        this.varianceScore = varianceScore;
        this.placementScore = placementScore;
        this.maxValScore = maxValScore;
        this.spaceScore = spaceScore;
        this.mergeScore = mergeScore;
        this.groupSpreadScore = groupSpreadScore;
        this.totalScore = totalScore;
        this.statesDiscarded = statesDiscarded;
        this.statesConsidered = statesConsidered;
    }

    public int getVarianceScore() {
        return varianceScore;
    }

    public int getPlacementScore() {
        return placementScore;
    }

    public int getMaxValScore() {
        return maxValScore;
    }

    public int getSpaceScore() {
        return spaceScore;
    }

    public int getMergeScore() {
        return mergeScore;
    }

    public int getGroupSpreadScore() {
        return groupSpreadScore;
    }

    public int getStatesDiscarded() {
        return statesDiscarded;
    }

    public int getStatesConsidered() {
        return statesConsidered;
    }

    public int getTotalScore() {
        return totalScore;
    }
}
