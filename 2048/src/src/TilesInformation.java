package src;

import java.util.HashMap;

public class TilesInformation
{
    private int[] tilesAsc;
    private int[] tilesDesc;
    private int maxValX;
    private int maxValY;
    private int tilesSet;
    private HashMap<Integer, Integer> tileCounts;

    public TilesInformation(
            int[] tilesAsc,
            int[] tilesDesc,
            int maxValX,
            int maxValY,
            int tilesSet,
            HashMap<Integer, Integer> tileCounts)
    {
        this.tilesAsc = tilesAsc;
        this.tilesDesc = tilesDesc;
        this.maxValX = maxValX;
        this.maxValY = maxValY;
        this.tilesSet = tilesSet;
        this.tileCounts = tileCounts;
    }

    public int[] getTilesAsc() {
        return tilesAsc;
    }

    public int[] getTilesDesc() {
        return tilesDesc;
    }

    public int getMaxValX() {
        return maxValX;
    }

    public int getMaxValY() {
        return maxValY;
    }

    public int getTilesSet() {
        return tilesSet;
    }

    public HashMap<Integer, Integer> getTileCounts() {
        return tileCounts;
    }

}
