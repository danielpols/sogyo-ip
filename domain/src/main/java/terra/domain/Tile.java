package terra.domain;

import java.util.HashSet;
import java.util.List;

public class Tile {

    private final TileLocation location;
    private Terrain terrain;
    private List<Tile> adjacent;
    private Building building;

    protected Tile(TileLocation location, Terrain terrain) {
        this.location = location;
        this.terrain = terrain;
        this.building = Building.NONE;
    }

    protected void setAdjacent(List<Tile> others) {
        this.adjacent = others.stream().filter(t -> this.isAdjacentTo(t))
                .toList();
    }

    protected int[][] getTileLocations() {
        return getTileList().stream().map(t -> t.location.toArray())
                .toArray(int[][]::new);
    }

    protected Terrain getTileTerrain(TileLocation target) {
        return findTile(target).terrain;
    }

    protected Building getTileBuilding(TileLocation target) {
        return findTile(target).building;
    }

    protected List<Tile> getTileList() {
        return getAllTiles().stream().sorted((i, j) -> i.compare(j)).toList();
    }

    protected boolean isAdjacentTo(Tile other) {
        return location.isAdjacentTo(other.location);
    }

    protected void build(TileLocation target, Building newBuilding,
            Player player) {
        findTile(target).build(newBuilding, player);
    }

    private void build(Building newBuilding, Player player) {
        if (isBuildable(player) && building.upgradesTo(newBuilding)) {
            terrain = player.getTerrain();
            building = newBuilding;
        }
    }

    protected boolean isBuildable(TileLocation target, Player player) {
        return findTile(target).isBuildable(player);
    }

    private boolean isBuildable(Player player) {
        boolean sameTerrain = sameTerrainAs(player);

        if (getAllTiles().stream().filter(t -> t.hasPlayerBuilding(player))
                .count() < 2) {
            return !hasBuilding() && sameTerrain;
        }

        boolean adjacentToPlayer = adjacent.stream()
                .filter(t -> t.hasPlayerBuilding(player)).count() > 0;
        return (hasBuilding() && sameTerrain)
                || (!hasBuilding() && adjacentToPlayer);
    }

    private boolean hasPlayerBuilding(Player player) {
        return hasBuilding() && sameTerrainAs(player);
    }

    private boolean hasBuilding() {
        return !building.equals(Building.NONE);
    }

    private boolean sameTerrainAs(Player player) {
        return terrain.equals(player.getTerrain());
    }

    private int compare(Tile other) {
        return location.compare(other.location);
    }

    private Tile findTile(TileLocation target) {
        return target.equals(location) ? this
                : adjacent.stream()
                        .filter(t -> t.location.distance(target) < location
                                .distance(target))
                        .findAny().get().findTile(target);
    }

    private HashSet<Tile> getAllTiles() {
        HashSet<Tile> tiles = new HashSet<Tile>();
        getAllTiles(tiles);
        return tiles;
    }

    private void getAllTiles(HashSet<Tile> set) {
        if (!set.contains(this)) {
            set.add(this);
            adjacent.forEach(t -> t.getAllTiles(set));
        }
    }

}

record TileLocation(int row, int col) {
    protected int[] toArray() {
        return new int[] { row, col };
    }

    protected static TileLocation fromArray(int[] array) {
        return new TileLocation(array[0], array[1]);
    }

    protected static TileLocation fromBoardIndex(int index, int size) {
        // Alternating rows of size and (size-1)
        int doubleRow = index / (2 * size - 1);
        int singleRow = (index - (2 * size - 1) * doubleRow) / size;
        int row = 2 * doubleRow + singleRow;
        int col = index - (2 * size - 1) * doubleRow - size * singleRow;
        return new TileLocation(row, col);
    }

    protected int compare(TileLocation other) {
        return Integer.compare(row, other.row) == 0
                ? Integer.compare(col, other.col)
                : Integer.compare(row, other.row);
    }

    protected boolean isAdjacentTo(TileLocation other) {
        if (other.row - row == 0 && Math.abs(other.col - col) == 1) {
            return true;
        }
        if (Math.abs(other.row - row) == 1 && (other.col == (col - 1 + row % 2)
                || other.col == (col + row % 2))) {
            return true;
        }

        return false;
    }

    protected int distance(TileLocation other) {
        int rowDiff = Math.abs(row - other.row);
        int halfColDiff = Math.abs(2 * col + (row % 2 == 0 ? 0 : 1)
                - (2 * other.col + (other.row % 2 == 0 ? 0 : 1)));
        if (halfColDiff <= rowDiff) {
            return rowDiff;
        }
        return (halfColDiff - rowDiff) / 2 + rowDiff;
    }
}
