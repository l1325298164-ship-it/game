package de.tum.cit.fop.maze.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.utils.TextureManager;
/**
 * A cosmetic companion entity that follows the player.
 * <p>
 * The cat mirrors player movement with slight delays and
 * performs idle wandering behavior when the player stops.
 * It does not interact with gameplay mechanics.
 */
public class CatFollower extends GameObject {


    private final Player player;


    private float worldX;
    private float worldY;


    private static final float BASE_SPEED = 2.5f;

    private static final float PLAYER_SPEED_RATIO = 0.75f;

    private static final float FOLLOW_EPSILON = 0.05f;


    private enum State {
        FOLLOW_PLAYER,
        IDLE_WANDER
    }
    private float idleTimer = 0f;
    private float nextIdleDecisionTime = 1.5f;
    private final GameManager gm;
    private float idleTargetX;
    private float idleTargetY;

    private State state = State.FOLLOW_PLAYER;

    private static Animation<TextureRegion> animLeft;
    private static Animation<TextureRegion> animRight;
    private static Animation<TextureRegion> animFront;
    private static Animation<TextureRegion> animBack;

    private float animTime = 0f;

    private enum Facing {
        LEFT, RIGHT, FRONT, BACK
    }

    private Facing facing = Facing.FRONT;
    private static void loadAnimations(TextureManager tm) {
        if (animLeft != null) return;

        animLeft = new Animation<>(
                0.18f,
                tm.getCatLeftAtlas().getRegions()
        );

        animRight = new Animation<>(
                0.18f,
                tm.getCatRightAtlas().getRegions()
        );

        animFront = new Animation<>(
                0.18f,
                tm.getCatFrontAtlas().getRegions()
        );

        animBack = new Animation<>(
                0.18f,
                tm.getCatBackAtlas().getRegions()
        );

        animLeft.setPlayMode(Animation.PlayMode.LOOP);
        animRight.setPlayMode(Animation.PlayMode.LOOP);
        animFront.setPlayMode(Animation.PlayMode.LOOP);
        animBack.setPlayMode(Animation.PlayMode.LOOP);
    }
    /**
     * Creates a cat follower bound to the given player.
     *
     * @param player the player to follow
     * @param gm     game manager used for collision checks
     */
    public CatFollower(Player player, GameManager gm) {
        super(player.getX(), player.getY());
        this.player = player;
        this.gm = gm;
        this.worldX = player.getX() + 0.5f;
        this.worldY = player.getY() + 0.2f;

        loadAnimations(TextureManager.getInstance());
    }



    /**
     * Updates the follower behavior and movement state.
     *
     * @param delta time elapsed since last frame
     */
    public void update(float delta) {
        if (!active) return;

        if (player.isMoving()) {
            state = State.FOLLOW_PLAYER;
        } else {
            if (state != State.IDLE_WANDER) {
                enterIdleWander();
            }
        }

        switch (state) {
            case FOLLOW_PLAYER -> updateFollow(delta);
            case IDLE_WANDER -> updateIdle(delta);
        }

        x = (int) worldX;
        y = (int) worldY;
    }

    private void updateFollow(float delta) {
        float targetX = player.getX() + 0.5f;
        float targetY = player.getY() + 0.2f;

        moveToward(targetX, targetY, delta, player.getMoveSpeed() * 0.75f);
    }
    private void enterIdleWander() {
        state = State.IDLE_WANDER;
        idleTimer = 0f;
        pickNewIdleTarget();
    }
    private void updateIdle(float delta) {
        idleTimer += delta;

        if (idleTimer >= nextIdleDecisionTime) {
            idleTimer = 0f;
            pickNewIdleTarget();
        }

        moveToward(idleTargetX, idleTargetY, delta, player.getMoveSpeed() * 0.5f);
    }
    private void pickNewIdleTarget() {
        int px = player.getX();
        int py = player.getY();

        for (int i = 0; i < 10; i++) {

            int dx = MathUtils.random(-2, 2);
            int dy = MathUtils.random(-2, 2);

            int tx = px + dx;
            int ty = py + dy;

            if (tx == px && ty == py) continue;

            if (gm.getMazeCell(tx, ty) != 1) continue;

            idleTargetX = tx + 0.5f;
            idleTargetY = ty + 0.2f;
            return;
        }

        idleTargetX = px + 0.5f;
        idleTargetY = py + 0.2f;
    }

    private void moveToward(float targetX, float targetY, float delta, float speed) {

        float dx = targetX - worldX;
        float dy = targetY - worldY;
        if (Math.abs(dx) > Math.abs(dy)) {
            facing = dx > 0 ? Facing.RIGHT : Facing.LEFT;
        } else {
            facing = dy > 0 ? Facing.BACK : Facing.FRONT;
        }
        float distSq = dx * dx + dy * dy;
        if (distSq < 0.0001f) return;

        float dist = (float)Math.sqrt(distSq);
        float step = speed * delta;

        float nextX = worldX;
        float nextY = worldY;

        if (step >= dist) {
            nextX = targetX;
            nextY = targetY;
        } else {
            nextX += dx / dist * step;
            nextY += dy / dist * step;
        }


        int curGX = (int)(worldX);
        int curGY = (int)(worldY);

        int nextGX = (int)(nextX);
        int nextGY = (int)(nextY);

        if (nextGX != curGX || nextGY != curGY) {

            if (gm.getMazeCell(nextGX, nextGY) != 1) {
                return;
            }
        }

        worldX = nextX;
        worldY = nextY;
    }






    /**
     * Draws the cat sprite with directional animation.
     *
     * @param batch sprite batch used for rendering
     */
    @Override
    public void drawSprite(SpriteBatch batch) {
        if (!active) return;

        float cs = GameConstants.CELL_SIZE;

        float size = cs * 0.8f;
        float drawX = worldX * cs - size * 0.5f;
        float drawY = worldY * cs - size * 0.2f;

        Animation<TextureRegion> anim = switch (facing) {
            case LEFT  -> animLeft;
            case RIGHT -> animRight;
            case BACK  -> animBack;
            case FRONT -> animFront;
        };

        boolean isMoving = player.isMoving() || state == State.IDLE_WANDER;

        TextureRegion frame = isMoving
                ? anim.getKeyFrame(animTime)
                : anim.getKeyFrames()[0];

        batch.draw(frame, drawX, drawY, size, size);
    }

    /**
     * Cat follower does not render debug shapes.
     *
     * @param shapeRenderer shape renderer
     */
    @Override
    public void drawShape(ShapeRenderer shapeRenderer) {
    }
    /**
     * @return render type used by this entity
     */
    @Override
    public RenderType getRenderType() {
        return RenderType.SPRITE;
    }

    /**
     * @return current world x-position
     */
    public float getWorldX() {
        return worldX;
    }
    /**
     * @return current world y-position
     */
    public float getWorldY() {
        return worldY;
    }
    private int[] getPreferredGrid(Player p) {
        int px = p.getX();
        int py = p.getY();

        return switch (p.getDirection()) {
            case UP    -> new int[]{px, py - 1};
            case DOWN  -> new int[]{px, py + 1};
            case LEFT  -> new int[]{px + 1, py};
            case RIGHT -> new int[]{px - 1, py};
        };
    }

}
