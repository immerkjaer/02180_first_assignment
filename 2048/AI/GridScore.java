package AI;

public class GridScore
{
    private double varianceScore;
    private double placementScore;
    private double mergeScore;
    private double groupSpread;

    public GridScore(
            double varianceScore,
            double placementScore,
            double mergeScore,
            double groupSpread)
    {
        this.varianceScore = varianceScore;
        this.placementScore = placementScore;
        this.mergeScore = mergeScore;
        this.groupSpread = groupSpread;
    }

    public double getVarianceScore() {
        return varianceScore;
    }

    public double getPlacementScore() {
        return placementScore;
    }

    public double getMergeScore() {
        return mergeScore;
    }

    public double getGroupSpread() {
        return groupSpread;
    }
}