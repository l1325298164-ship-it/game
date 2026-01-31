package de.tum.cit.fop.maze.entities.Obstacle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.entities.PushSource;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.utils.TextureManager;

/**
 * A dynamic wall that moves back and forth between two grid positions.
 * <p>
 * The wall blocks movement, can push players out of the way,
 * and reverses direction when blocked or reaching its endpoints.
 */
public class MovingWall extends DynamicObstacle implements PushSource {

    /**
     * @return push strength applied to players
     */
    @Override
    public int getPushStrength() {
        return 1;
    }
    /**
     * @return {@code false}, this wall does not instantly kill players
     */
    @Override
    public boolean isLethal() {
        return false;
    }

    /**
     * Defines the visual type of the moving wall.
     */
    public enum WallType {
        SINGLE, // 000
        DOUBLE  // 001
    }


    private final int dirX;
    private final int dirY;

    private final int startX, startY;
    private final int endX, endY;

    private boolean forward = true;


    private final WallType wallType;
    private TextureRegion wallRegion;

    /**
     * Creates a moving wall that travels between two positions.
     * <p>
     * The wall must move strictly horizontally or vertically,
     * and the distance between start and end must be at least two cells.
     *
     * @param startX start grid x-position
     * @param startY start grid y-position
     * @param endX   end grid x-position
     * @param endY   end grid y-position
     * @param type   visual wall type
     *
     * @throws IllegalArgumentException if the movement is diagonal
     *                                  or the distance is too short
     */
    public MovingWall(int startX, int startY, int endX, int endY, WallType type) {

        super(startX, startY);
        if (startX != endX && startY != endY) {
            throw new IllegalArgumentException("MovingWall must move horizontally or vertically, not diagonally");
        }

        int dist = Math.abs(endX - startX) + Math.abs(endY - startY);
        if (dist < 2) {
            throw new IllegalArgumentException("MovingWall move distance must be at least 2 cells");
        }
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.wallType = type;

        this.x = startX;
        this.y = startY;
        this.worldX = startX;
        this.worldY = startY;
        this.targetX = startX;
        this.targetY = startY;

        this.dirX = Integer.compare(endX, startX);
        this.dirY = Integer.compare(endY, startY);

        this.moveInterval = 0.6f;
        this.moveCooldown = 0f;
        this.isMoving = false;

        loadTexture();

        Logger.debug("MovingWall constructor: grid=(" + x + "," + y +
                ") world=(" + worldX + "," + worldY + ")");
    }



    private void loadTexture() {
        TextureAtlas atlas = TextureManager.getInstance().getWallAtlas();
        if (atlas == null) return;

        int index = (wallType == WallType.SINGLE) ? 0 : 1;
        wallRegion = atlas.findRegion("Wallpaper", index);

        if (wallRegion == null) {
            Logger.error("MovingWall texture missing: Wallpaper index=" + index);
        }
    }

    /**
     * Updates wall movement, direction changes,
     * and player pushing interactions.
     *
     * @param delta time elapsed since last frame
     * @param gm    active game manager
     */
    @Override
    public void update(float delta, GameManager gm) {
        if (moveCooldown < 0) moveCooldown = 0;
        moveCooldown -= delta;

        if (isMoving) {
            moveContinuously(delta);

            if (!isMoving) {
                x = (int)Math.round(worldX);
                y = (int)Math.round(worldY);
            }
            return;
        }

        if (moveCooldown > 0f) return;

        float tolerance = 0.1f;
        if (forward) {
            float dx = endX - worldX;
            float dy = endY - worldY;
            if (dx * dx + dy * dy < tolerance) {
                forward = !forward;
                worldX = endX;
                worldY = endY;
                x = endX;
                y = endY;
                moveCooldown = moveInterval * 2;
                return;
            }
        } else {
            float dx = startX - worldX;
            float dy = startY - worldY;
            if (dx * dx + dy * dy < tolerance) {
                forward = !forward;
                worldX = startX;
                worldY = startY;
                x = startX;
                y = startY;
                moveCooldown = moveInterval * 2;
                return;
            }
        }

        int nx = x + (forward ? dirX : -dirX);
        int ny = y + (forward ? dirY : -dirY);

        boolean playerBlocked = false;

        for (Player p : gm.getPlayers()) {
            if (p == null || p.isDead()) continue;

            if (p.getX() == nx && p.getY() == ny) {

                int pushDirX = Integer.compare(nx, x);
                int pushDirY = Integer.compare(ny, y);

                boolean pushed = p.onPushedBy(this, pushDirX, pushDirY, gm);

                if (pushed) {
                    startMoveTo(nx, ny);
                    moveCooldown = moveInterval;
                } else {
                    forward = !forward;
                    moveCooldown = moveInterval;
                }

                playerBlocked = true;
                break;
            }
        }

        if (playerBlocked) {
            return;
        }

        if (gm.isObstacleValidMove(nx, ny)) {
            startMoveTo(nx, ny);
            moveCooldown = moveInterval;
        } else {
            forward = !forward;
            moveCooldown = moveInterval;
        }
    }

    /**
     * Checks whether this wall occupies the given grid cell,
     * including its current movement target.
     *
     * @param cx grid x-position
     * @param cy grid y-position
     * @return {@code true} if the wall occupies the cell
     */
    public boolean occupiesCell(int cx, int cy) {
        if (x == cx && y == cy) return true;

        if (isMoving && targetX == cx && targetY == cy) return true;

        return false;
    }


    /**
     * Draws the moving wall sprite.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void draw(SpriteBatch batch) {
        if (wallRegion == null) return;

        float cs = GameConstants.CELL_SIZE;
        float height = cs * 2.4f;
        int overlap = 6;

        batch.draw(
                wallRegion,
                worldX * cs,
                worldY * cs - overlap,
                cs,
                height
        );

        if (wallType == WallType.DOUBLE) {
            batch.draw(
                    wallRegion,
                    worldX * cs,
                    worldY * cs - height + overlap,
                    cs,
                    height
            );
        }
    }
    /**
     * Draws debug visualization for the wall path and position.
     *
     * @param shapeRenderer shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {
        float cs = GameConstants.CELL_SIZE;

        shapeRenderer.setColor(1, 0, 0, 0.3f);
        shapeRenderer.rectLine(
                startX * cs + cs/2, startY * cs + cs/2,
                endX * cs + cs/2, endY * cs + cs/2,
                3
        );

        shapeRenderer.setColor(0, 1, 0, 0.5f);
        shapeRenderer.circle(
                worldX * cs + cs/2,
                worldY * cs + cs/2,
                cs/4
        );
    }

    @Override
    public void drawSprite(SpriteBatch batch) {
        if (wallRegion == null) return;

        float cs = GameConstants.CELL_SIZE;
        float height = cs * 2.4f;
        int overlap = 6;

        batch.draw(
                wallRegion,
                worldX * cs,
                worldY * cs - overlap,
                cs,
                height
        );

        if (wallType == WallType.DOUBLE) {
            batch.draw(
                    wallRegion,
                    worldX * cs,
                    worldY * cs - height + overlap,
                    cs,
                    height
            );
        }

    }


    /**
     * @return render type used by this wall
     */
    @Override
    public RenderType getRenderType() {
        return RenderType.SPRITE;
    }
    /**
     * Moving walls are not passable.
     *
     * @return {@code false}
     */
    @Override
    public boolean isPassable() {
        return false;
    }


    protected void startMoveTo(int nx, int ny) {

        targetX = nx;
        targetY = ny;
        isMoving = true;

        Logger.debug("startMoveTo: from=(" + x + "," + y +
                ") to target=(" + targetX + "," + targetY + ")");
    }

    protected void moveContinuously(float delta) {
        float speed = 1f / moveInterval;

        float dx = targetX - worldX;
        float dy = targetY - worldY;

        float distSq = dx * dx + dy * dy;

        if (distSq < 1e-4f) {
            worldX = targetX;
            worldY = targetY;
            isMoving = false;

            x = (int) Math.round(worldX);
            y = (int) Math.round(worldY);

            Logger.debug("moveContinuously DONE: grid=(" + x + "," + y +
                    ") world=(" + worldX + "," + worldY + ")");
            return;
        }

        float dist = (float) Math.sqrt(distSq);

        float nx = dx / dist;
        float ny = dy / dist;

        float step = speed * delta;

        if (step >= dist) {
            worldX = targetX;
            worldY = targetY;
            isMoving = false;

            x = (int) Math.round(worldX);
            y = (int) Math.round(worldY);

            Logger.debug("moveContinuously DONE (snap): grid=(" + x + "," + y +
                    ") world=(" + worldX + "," + worldY + ")");
            return;
        }

        worldX += nx * step;
        worldY += ny * step;
    }

}
