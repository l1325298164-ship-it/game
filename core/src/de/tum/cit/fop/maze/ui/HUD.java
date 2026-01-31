package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

import com.badlogic.gdx.utils.TimeUtils;
import de.tum.cit.fop.maze.abilities.*;
import de.tum.cit.fop.maze.entities.Compass;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.game.achievement.*;
import de.tum.cit.fop.maze.game.score.UpgradeCost;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.utils.TextureManager;

import java.util.*;

import static com.badlogic.gdx.graphics.GL20.*;
/**
 * Heads-up display (HUD) renderer for in-game UI elements.
 *
 * <p>This class is responsible for rendering all on-screen user interface
 * components, including player health, mana bars, abilities, score display,
 * achievements, boss UI, and interaction indicators.
 *
 * <p>The HUD supports both single-player and two-player modes, as well as
 * special boss battle overlays.
 *
 * <p>This class focuses purely on rendering and UI interaction detection
 * and does not contain gameplay logic.
 */

public class HUD {
    private HUDMode hudMode = HUDMode.NORMAL;

    private float bossHp = 0f;
    private float bossMaxHp = 0f;
    public enum HUDMode {
        NORMAL,
        BOSS
    }

    private Ability hoveredUpgradeAbility = null;
    private float upgradeHoverAnim = 0f;
    /**
     * Checks whether the mouse is currently hovering over an interactive HUD element.
     *
     * @return true if the mouse is over an interactive UI component
     */
    public boolean isMouseOverInteractiveUI() {
        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();

        for (Player p : gameManager.getPlayers()) {
            if (p == null || p.getAbilityManager() == null) continue;

            for (Ability ability : p.getAbilityManager().getAbilities().values()) {
                if (!canShowUpgrade(p, ability)) continue;

                float iconX, iconY, iconSize;
                boolean mirror = p.getPlayerIndex() == Player.PlayerIndex.P2;

                if (ability instanceof DashAbility) {
                    iconSize = DASH_ICON_SIZE;
                    iconX = getIconX(iconSize, mirror);
                    iconY = DASH_UI_MARGIN_Y;
                } else if (ability instanceof MeleeAttackAbility) {
                    iconSize = MELEE_ICON_SIZE;
                    float dashX = getIconX(DASH_ICON_SIZE, mirror);
                    iconX = mirror
                            ? dashX - MELEE_UI_OFFSET_X
                            : dashX + MELEE_UI_OFFSET_X + 50;
                    iconY = DASH_UI_MARGIN_Y + (DASH_ICON_SIZE - iconSize) / 2f;
                } else if (ability instanceof MagicAbility) {
                    iconSize = MELEE_ICON_SIZE;
                    float dashX = getIconX(DASH_ICON_SIZE, mirror);
                    iconX = mirror
                            ? dashX - MELEE_UI_OFFSET_X
                            : dashX + MELEE_UI_OFFSET_X;
                    iconY = DASH_UI_MARGIN_Y + (DASH_ICON_SIZE - iconSize) / 2f;
                } else {
                    continue;
                }

                float btnSize = UPG_BTN_SIZE;
                float anchorX = mirror
                        ? iconX - UPG_BTN_OFF_X - btnSize
                        : iconX + iconSize + UPG_BTN_OFF_X;
                float anchorY = iconY + iconSize * 0.5f;

                float bx = anchorX;
                float by = anchorY - btnSize * 0.5f + UPG_BTN_OFF_Y;

                if (ability instanceof MeleeAttackAbility) {
                    bx -= 4f; by -= 2f;
                } else if (ability instanceof MagicAbility) {
                    bx += 6f; by += 3f;
                }

                boolean hover =
                        mx >= bx && mx <= bx + btnSize &&
                                my >= by && my <= by + btnSize;

                if (hover) return true;
            }
        }
        return false;
    }
    private boolean bossFinalLocked = false;
    private final Color bossHpColor = new Color(0.85f, 0.15f, 0.15f, 1f);
    private int bossPhaseIndex = -1;
    private boolean bossRageWarning = false;



    private enum HUDLayoutMode {
        SINGLE,
        TWO_PLAYER
    }
    private long lastUpgradeTime = 0;
    private static final long UPGRADE_COOLDOWN_MS = 300;

    private static final float HOLD_TO_UPGRADE_TIME = 0.8f;

    private BitmapFont font;
    private final GameManager gameManager;
    private final TextureManager textureManager;

    private AchievementPopup achievementPopup;

    private ShaderProgram iceHeartShader;

    private Texture heartFull;
    private Texture heartHalf;

    private static final int MAX_HEARTS_DISPLAY = 40;
    private static final int HEARTS_PER_ROW = 5;
    private static final int HEART_SPACING = 70;
    private static final int ROW_SPACING = 30;

    private static final float SHAKE_DURATION = 0.2f;
    private static final float SHAKE_AMPLITUDE = 4f;

    private final Map<Player.PlayerIndex, Integer> lastLivesMap = new HashMap<>();
    private final Map<Player.PlayerIndex, Boolean> shakingMap = new HashMap<>();
    private final Map<Player.PlayerIndex, Float> shakeTimerMap = new HashMap<>();
    private Texture manaBaseP1;
    private Texture manaFillP1;
    private Texture manaGlowP1;

    private Texture manaBaseP2;
    private Texture manaFillP2;
    private Texture manaGlowP2;

    private Texture manadeco_1;
    private Texture manadeco_2;

    private float manaGlowTime = 0f;


    private TextureAtlas sparkleAtlas;
    private TextureRegion sparkleStar;
    private TextureRegion sparkleFlower;

    private static final int MAX_PARTICLES = 150;
    private final Map<Integer, List<ManaParticle>> manaParticlesMap = new HashMap<>();

    private Texture dashIconP1;
    private Texture dashIconP2;
    private Texture dashIcon;

    private Texture meleeIcon;

    private Texture magicBg;
    private Texture magicGrow;
    private Texture magicIconTop;

    private static final int DASH_ICON_SIZE = 200;
    private static final int MELEE_ICON_SIZE = 160;
    private static final int DASH_UI_MARGIN_X = 20;
    private static final int DASH_UI_MARGIN_Y = 90;
    private static final int MELEE_UI_OFFSET_X = DASH_ICON_SIZE + 20;

    private Texture iconAtk;
    private Texture iconRegen;
    private Texture iconMana;
    private boolean lastMouseDown = false;

    private TextureAtlas catAtlas;
    private Animation<TextureRegion> catNoKeyAnim;
    private Animation<TextureRegion> catHasKeyAnim;
    private float catStateTime = 0f;

    private static final float CAT_SIZE = 506f;
    private static final float CAT_MARGIN = 10f;
    private static final float CAT_COMPASS_GAP = 40f;
    private static final float CAT_Y_OFFSET = -150f;
    private static final float COMPASS_Y_OFFSET = -350f;

    private ShapeRenderer shapeRenderer;


    private static final float UPG_BTN_SIZE = 36f;      // 按钮尺寸
    private static final float UPG_BTN_OFF_X = 10f;     // 相对 icon 右边偏移
    private static final float UPG_BTN_OFF_Y = 0f;      // 相对 icon 中心偏移（向上为 +）

    private static final float UPG_BTN_FLOAT_AMP = 6f;  // 上下浮动幅度
    private static final float UPG_BTN_FLOAT_SPEED = 0.005f; // 浮动速度(TimeUtils.millis()*speed)

    private static final float UPG_HOVER_SCALE = 0.15f; // hover 放大幅度
    private static final float UPG_HOVER_ALPHA_BASE = 0.70f;
    private static final float UPG_HOVER_ALPHA_ADD  = 0.30f;

    private static final float UPG_PLUS_FONT_SCALE = 2.0f;

    private static final float LV_FONT_SCALE = 1.0f;
    private static final float LV_PAD_RIGHT = 6f;       // 距离 icon 右边
    private static final float LV_PAD_BOTTOM = 18f;     // 距离 icon 底部（注意：是 baseline 位置）

    private boolean uiHoverThisFrame = false;

    /**
     * Creates a new HUD instance for the given game manager.
     *
     * @param gameManager the game manager providing game state and player data
     */
    public HUD(GameManager gameManager) {
        this.gameManager = gameManager;
        this.textureManager = TextureManager.getInstance();

        this.font = new BitmapFont();
        this.font.getData().setScale(1.2f);

        this.shapeRenderer = new ShapeRenderer();


        achievementPopup = new AchievementPopup(font);

        String vertexSrc = SpriteBatch.createDefaultShader().getVertexShaderSource();
        String fragmentSrc = Gdx.files.internal("shaders/ice_heart.frag").readString();
        iceHeartShader = new ShaderProgram(vertexSrc, fragmentSrc);
        if (!iceHeartShader.isCompiled()) {
            Logger.error("IceHeartShader compile error:\n" + iceHeartShader.getLog());
        }

        sparkleAtlas = new TextureAtlas(Gdx.files.internal("effects/sparkle.atlas"));
        sparkleStar = sparkleAtlas.findRegion("star");
        sparkleFlower = sparkleAtlas.findRegion("flower");

        manaBaseP1 = new Texture(Gdx.files.internal("ui/HUD/manabar_base.png"));
        manaBaseP2 = manaBaseP1;

        manaFillP1 = new Texture(Gdx.files.internal("ui/HUD/manabar_1_fill.png"));
        manaGlowP1 = new Texture(Gdx.files.internal("ui/HUD/manabar_1_grow.png"));
        manadeco_1 = new Texture(Gdx.files.internal("ui/HUD/bar_star1.png"));

        manaFillP2 = new Texture(Gdx.files.internal("ui/HUD/manabar_2_fill.png"));
        manaGlowP2 = new Texture(Gdx.files.internal("ui/HUD/manabar_2_grow.png"));
        manadeco_2 = new Texture(Gdx.files.internal("ui/HUD/bar_star2.png"));

        heartFull = new Texture(Gdx.files.internal("ui/HUD/live_000.png"));
        heartHalf = new Texture(Gdx.files.internal("ui/HUD/live_001.png"));

        dashIconP1 = new Texture(Gdx.files.internal("ui/HUD/icon_dash.png"));
        dashIconP2 = new Texture(Gdx.files.internal("ui/HUD/icon_dash_2.png"));

        meleeIcon = new Texture(Gdx.files.internal("ui/HUD/icon_melee.png"));
        magicBg = new Texture(Gdx.files.internal("ui/HUD/magicicon_bg.png"));
        magicGrow = new Texture(Gdx.files.internal("ui/HUD/magicicon_grow.png"));
        magicIconTop = new Texture(Gdx.files.internal("ui/HUD/icon_magic_base.png"));

        iconAtk = new Texture(Gdx.files.internal("imgs/Items/icon_atk.png"));
        iconRegen = new Texture(Gdx.files.internal("imgs/Items/icon_regen.png"));
        iconMana = new Texture(Gdx.files.internal("imgs/Items/icon_mana.png"));

        catAtlas = new TextureAtlas(Gdx.files.internal("ani/Character/cat/cat.atlas"));
        catNoKeyAnim = new Animation<>(0.25f, catAtlas.findRegions("cat_nokey"), Animation.PlayMode.LOOP);
        catHasKeyAnim = new Animation<>(0.25f, catAtlas.findRegions("cat_key"), Animation.PlayMode.LOOP);

        Logger.debug("HUD initialized (Part 1)");
    }

    /**
     * Renders the in-game HUD.
     *
     * @param uiBatch the sprite batch used for UI rendering
     * @param allowInteraction whether UI interactions (e.g. upgrades) are enabled
     */
    public void renderInGameUI(SpriteBatch uiBatch, boolean allowInteraction) {
        uiHoverThisFrame = false;

        if (hudMode == HUDMode.BOSS) {
            renderBossHUD(uiBatch);
        }

        if (gameManager.isTwoPlayerMode()) {
            renderTwoPlayerHUD(uiBatch, allowInteraction);
        } else {
            renderSinglePlayerHUD(uiBatch, allowInteraction);
        }

        renderScore(uiBatch);


        renderBottomCenterHUD(uiBatch);

        lastMouseDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    }

    private void renderBossHUD(SpriteBatch batch) {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        float barWidth  = screenW * 0.6f;
        float barHeight = 26f;

        float x = (screenW - barWidth) / 2f;
        float y = screenH - 80f;

        float ratio = bossMaxHp <= 0f ? 0f : bossHp / bossMaxHp;
        ratio = Math.max(0f, Math.min(1f, ratio));

        batch.setColor(0f, 0f, 0f, 0.65f);
        batch.draw(
                TextureManager.getInstance().getWhitePixel(),
                x - 6, y - 6,
                barWidth + 12, barHeight + 12
        );

        batch.setColor(0.25f, 0.05f, 0.05f, 1f);
        batch.draw(
                TextureManager.getInstance().getWhitePixel(),
                x, y,
                barWidth, barHeight
        );

        if (bossFinalLocked) {
            float blink =
                    0.6f + 0.4f *
                            MathUtils.sin(TimeUtils.nanoTime() * 0.00000001f);

            bossHpColor.set(0.75f, 0.6f, 0.25f, blink);
        } else {
            bossHpColor.set(0.85f, 0.15f, 0.15f, 1f);
        }

        batch.setColor(bossHpColor);

        batch.draw(
                TextureManager.getInstance().getWhitePixel(),
                x, y,
                barWidth * ratio, barHeight
        );

        batch.setColor(1f, 1f, 1f, 1f);

        font.getData().setScale(1.4f);
        font.setColor(Color.WHITE);

        String text = "BOSS  " + (int)bossHp + " / " + (int)bossMaxHp;
        if (bossPhaseIndex >= 0) {
            font.getData().setScale(1.1f);
            font.setColor(0.85f, 0.85f, 0.85f, 0.9f);

            String phaseText = "PHASE " + (bossPhaseIndex + 1);

            GlyphLayout phaseLayout = new GlyphLayout(font, phaseText);

            font.draw(
                    batch,
                    phaseText,
                    screenW / 2f - phaseLayout.width / 2f,
                    y - 18
            );
        }

        font.getData().setScale(1.2f);
        batch.setColor(1f, 1f, 1f, 1f);
        if (bossRageWarning || bossFinalLocked) {

            float blink =
                    0.6f + 0.4f *
                            MathUtils.sin(TimeUtils.nanoTime() * 0.00000001f);

            font.getData().setScale(1.3f);

            if (bossFinalLocked) {
                font.setColor(0.85f, 0.65f, 0.25f, blink);
            } else {
                font.setColor(1.0f, 0.2f, 0.2f, blink);
            }

            String warn =
                    bossFinalLocked
                            ? "FINAL LOCK"
                            : "RAGE";

            GlyphLayout warnLayout = new GlyphLayout(font, warn);

            font.draw(
                    batch,
                    warn,
                    screenW / 2f - warnLayout.width / 2f,
                    y + barHeight + 64
            );

            font.getData().setScale(1.2f);
            font.setColor(Color.WHITE);
        }


        font.getData().setScale(1.2f);
        batch.setColor(1f, 1f, 1f, 1f);

    }


    private void renderSinglePlayerHUD(SpriteBatch uiBatch, boolean allowInteraction) {
        var player = gameManager.getPlayer();
        if (player == null) return;

        float barWidth = Gdx.graphics.getWidth() * 0.66f;
        float x = (Gdx.graphics.getWidth() - barWidth) / 2f - 50;
        float y = 10;

        renderManaBarForPlayer(uiBatch, player, 0,x, y, barWidth);

        renderLivesAsHearts(
                uiBatch,
                player,
                20,
                Gdx.graphics.getHeight() - 90,
                false
        );


        renderCat(uiBatch);
        renderCompassAsUI(uiBatch);

        renderDashIcon(uiBatch, player, false, allowInteraction);
        renderMeleeIcon(uiBatch, player, false, allowInteraction);

        renderAchievementPopup(uiBatch);

        float startX = 20;
        float startY = Gdx.graphics.getHeight() - 250;
        float iconSize = 48;
        float gap = 60;

        if (player.hasBuffAttack()) {
            if (iconAtk != null) uiBatch.draw(iconAtk, startX, startY, iconSize, iconSize);
            font.getData().setScale(2.0f);
            font.setColor(Color.RED);
            font.draw(uiBatch, "ATK +50%", startX + iconSize + 10, startY + 35);
            startY -= gap;
        }
        if (player.hasBuffRegen()) {
            if (iconRegen != null) uiBatch.draw(iconRegen, startX, startY, iconSize, iconSize);
            font.getData().setScale(2.0f);
            font.setColor(Color.GREEN);
            font.draw(uiBatch, "REGEN ON", startX + iconSize + 10, startY + 35);
            startY -= gap;
        }

        if (player.hasBuffManaEfficiency()) {
            if (iconMana != null) uiBatch.draw(iconMana, startX, startY, iconSize, iconSize);
            font.getData().setScale(2.0f);
            font.setColor(Color.CYAN);
            font.draw(uiBatch, "MANA COST -50%", startX + iconSize + 10, startY + 35);
            startY -= gap;
        }

        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);


        String msg = player.getNotificationMessage();
        if (msg != null && !msg.isEmpty()) {
            float w = Gdx.graphics.getWidth();
            float h = Gdx.graphics.getHeight();

            font.getData().setScale(2.5f);

            font.setColor(Color.BLACK);
            font.draw(uiBatch, msg, w / 2f - 200 + 3, h / 2f + 100 - 3);

            font.setColor(Color.YELLOW);
            font.draw(uiBatch, msg, w / 2f - 200, h / 2f + 100);

            font.setColor(Color.WHITE);
            font.getData().setScale(1.2f);
        }

    }


    private void renderTwoPlayerHUD(SpriteBatch uiBatch, boolean allowInteraction) {
        var players = gameManager.getPlayers();
        if (players == null || players.isEmpty()) return;

        float barWidth = 500f;
        float marginX  = 40f;
        float marginY  = 30f;
        renderReviveProgressBar(uiBatch);
        renderManaBarForPlayer(
                uiBatch,
                players.get(0),
                0,
                marginX,
                marginY,
                barWidth
        );

        if (players.size() > 1) {
            float x2 = Gdx.graphics.getWidth() - barWidth - marginX;
            renderManaBarForPlayer(
                    uiBatch,
                    players.get(1),
                    1,
                    x2,
                    marginY,
                    barWidth
            );
        }

        renderAchievementPopup(uiBatch);

        int topY = Gdx.graphics.getHeight() - 90;

        renderLivesAsHearts(
                uiBatch,
                players.get(0),
                20,
                topY,
                false
        );

        if (players.size() > 1) {
            int rightStartX =
                    Gdx.graphics.getWidth()
                            - 20
                            - heartFull.getWidth();

            renderLivesAsHearts(
                    uiBatch,
                    players.get(1),
                    rightStartX,
                    topY,
                    true
            );
        }


        renderDashIcon(uiBatch, players.get(0), false, allowInteraction);
        renderMeleeIcon(uiBatch, players.get(0), false, allowInteraction);

        if (players.size() > 1) {
            renderDashIcon(uiBatch, players.get(1), true, allowInteraction);
            renderMagicIcon(uiBatch, players.get(1), true, allowInteraction);
        }
    }

    private void renderDashIcon(
            SpriteBatch uiBatch,
            Player player,
            boolean mirror,
            boolean allowInteraction
    ) {
        if (player == null) return;

        DashAbility dash = null;
        for (Ability a : player.getAbilityManager().getAbilities().values()) {
            if (a instanceof DashAbility d) {
                dash = d;
                break;
            }
        }
        if (dash == null) return;

        Texture icon =
                player.getPlayerIndex() == Player.PlayerIndex.P1
                        ? dashIconP1
                        : dashIconP2;

        if (icon == null) return;

        int dashCharges = dash.getCurrentCharges();
        float progress = dash.getCooldownProgress();

        float x = mirror
                ? Gdx.graphics.getWidth() - DASH_ICON_SIZE - DASH_UI_MARGIN_X
                : DASH_UI_MARGIN_X;
        float y = DASH_UI_MARGIN_Y;

        if (dashCharges >= 2) {
            uiBatch.setColor(1.0f, 0.9f, 0.8f, 1f);
        } else if (dashCharges == 1) {
            uiBatch.setColor(0.8f, 0.9f, 0.8f, 1f);
        } else {
            uiBatch.setColor(0.25f, 0.25f, 0.2f, 0.8f);
        }

        uiBatch.draw(icon, x, y, DASH_ICON_SIZE, DASH_ICON_SIZE);
        renderAbilityLevel(uiBatch, dash, x, y, DASH_ICON_SIZE, mirror);

        renderUpgradeButton(
                uiBatch,
                player,
                dash,
                x,
                y,
                DASH_ICON_SIZE, mirror, allowInteraction
        );

        if (dashCharges < 2) {
            float maskHeight = DASH_ICON_SIZE * (1f - progress);
            uiBatch.setColor(0f, 0f, 0f, 0.5f);
            uiBatch.draw(
                    TextureManager.getInstance().getWhitePixel(),
                    x, y,
                    DASH_ICON_SIZE,
                    maskHeight
            );
        }

        uiBatch.setColor(1f, 1f, 1f, 1f);
    }
    private void renderMeleeIcon(
            SpriteBatch uiBatch,
            Player player,
            boolean mirror,
            boolean allowInteraction
    ) {
        if (meleeIcon == null || player == null) return;

        de.tum.cit.fop.maze.abilities.MeleeAttackAbility melee = null;
        for (Ability a : player.getAbilityManager().getAbilities().values()) {
            if (a instanceof de.tum.cit.fop.maze.abilities.MeleeAttackAbility m) {
                melee = m;
                break;
            }
        }
        if (melee == null) return;

        float progress = melee.getCooldownProgress();
        boolean onCooldown = progress > 0f && progress < 1f;

        float size = MELEE_ICON_SIZE;
        float dashX = getIconX(DASH_ICON_SIZE, mirror);

        float x = mirror
                ? dashX - MELEE_UI_OFFSET_X
                : dashX + MELEE_UI_OFFSET_X+50;

        float y = DASH_UI_MARGIN_Y + (DASH_ICON_SIZE - size) / 2f;

        if (onCooldown && iceHeartShader != null) {
            uiBatch.setShader(iceHeartShader);
            iceHeartShader.setUniformf("u_intensity", 0.0f);
            iceHeartShader.setUniformf("u_cooldown", progress);
            iceHeartShader.setUniformf("u_cdDarkness", 0.7f);
        }

        uiBatch.draw(meleeIcon, x, y, size, size);
        renderAbilityLevel(uiBatch, melee, x, y, size, mirror);

        renderUpgradeButton(
                uiBatch,
                player,
                melee,
                x,
                y,
                size, mirror, allowInteraction
        );

        if (onCooldown) {
            uiBatch.setShader(null);
        }
    }
    private void renderMagicIcon(
            SpriteBatch batch,
            Player player,
            boolean mirror,
            boolean allowInteraction
    ) {
        if (player == null) return;

        MagicAbility magic = null;
        for (Ability a : player.getAbilityManager().getAbilities().values()) {
            if (a instanceof MagicAbility m) {
                magic = m;
                break;
            }
        }
        if (magic == null) return;

        MagicAbility.Phase phase = magic.getPhase();
        float time = magic.getPhaseTime();

        float baseSize = MELEE_ICON_SIZE;
        float size = mirror ? baseSize * 1.15f : baseSize;

        float baseX = getIconX(DASH_ICON_SIZE, mirror);
        float x = mirror
                ? baseX - MELEE_UI_OFFSET_X
                : baseX + MELEE_UI_OFFSET_X;
        float y = DASH_UI_MARGIN_Y + (DASH_ICON_SIZE - size) / 2f;

        if (phase != MagicAbility.Phase.IDLE) {


            float originX = size * 0.5f;
            float originY = size * 0.62f;

            float rotation =
                    phase == MagicAbility.Phase.AIMING
                            ? time * 720f
                            : 0f;

            if (phase == MagicAbility.Phase.COOLDOWN) {
                batch.setColor(0.35f, 0.35f, 0.35f, 1f);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }

            batch.draw(
                    magicBg,
                    x, y,
                    originX, originY,
                    size, size,
                    1f, 1f,
                    rotation,
                    0, 0,
                    magicBg.getWidth(),
                    magicBg.getHeight(),
                    false, false
            );


        }
        renderAbilityLevel(batch, magic, x, y, size, mirror);

        renderUpgradeButton(
                batch,
                player,
                magic,
                x,
                y,
                size, mirror, allowInteraction
        );

        if (phase != MagicAbility.Phase.IDLE
                && phase != MagicAbility.Phase.COOLDOWN) {

            float pulse = 0.6f + 0.4f * (float)Math.sin(time * 6.5f);

            Color glow;
            switch (phase) {
                case AIMING, EXECUTED -> glow = new Color(0.9f, 0.2f, 0.9f, pulse); // 紫红
                default -> glow = Color.WHITE;
            }

            batch.setBlendFunction(GL_SRC_ALPHA, GL_ONE);
            batch.setColor(glow);
            batch.draw(magicGrow, x, y, size, size);
            batch.setBlendFunction(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        if (phase == MagicAbility.Phase.COOLDOWN) {
            batch.setColor(0.35f, 0.35f, 0.35f, 1f);
        } else {
            batch.setColor(1f, 1f, 1f, 1f);
        }

        batch.draw(magicIconTop, x, y, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
    }


    private static class ManaParticle {
        float x, y, vx, vy, life;
        Color color;
    }

    private void renderAchievementPopup(SpriteBatch uiBatch) {
        if (!achievementPopup.isBusy()) {
            AchievementManager am = gameManager.getAchievementManager();
            if (am != null) {
                AchievementType next = am.pollNotification();
                if (next != null) {
                    achievementPopup.show(next);
                }
            }
        }
        achievementPopup.render(uiBatch);
    }

    private void renderScore(SpriteBatch uiBatch) {
        int score = gameManager.getScore();
        String text = "SCORE: " + formatScore(score);

        font.getData().setScale(1.5f);
        GlyphLayout layout = new GlyphLayout(font, text);

        float x;
        if (gameManager.isTwoPlayerMode()) {
            x = (Gdx.graphics.getWidth() - layout.width) / 2f;
        } else {
            x = Gdx.graphics.getWidth() - layout.width - 30;
        }

        float y = Gdx.graphics.getHeight() - 60;

        font.setColor(0f, 0f, 0f, 0.7f);
        font.draw(uiBatch, text, x + 2, y - 2);

        font.setColor(Color.GOLD);
        font.draw(uiBatch, text, x, y);

        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
    }

    private void renderManaBarForPlayer(
            SpriteBatch uiBatch,
            Player player,
            int playerId,
            float x,
            float y,
            float barWidth
    ) {
        Texture manaBase = (playerId == 0) ? manaBaseP1 : manaBaseP2;
        Texture manaFill = (playerId == 0) ? manaFillP1 : manaFillP2;
        Texture manaGlow = (playerId == 0) ? manaGlowP1 : manaGlowP2;
        Texture manaDeco = (playerId == 0) ? manadeco_1 : manadeco_2;

        if (player == null || manaFill == null || manaBase == null) return;
        List<ManaParticle> particles =
                manaParticlesMap.computeIfAbsent(playerId, k -> new ArrayList<>());




        float maxMana = Math.max(1f, player.getMaxMana());
        float percent = Math.max(
                0f,
                Math.min(1f, player.getMana() / maxMana)
        );

        float barHeight = barWidth * (32f / 256f);

        float fillInsetLeft  = barWidth * 0.02f;
        float fillInsetRight = barWidth * 0.02f;

        float fillStartX = x + fillInsetLeft;
        float fillWidth  = barWidth - fillInsetLeft - fillInsetRight;

        float capW = fillWidth * 0.06f;
        capW = Math.max(8f, capW);

        int capSrcW = (int)(manaFill.getWidth() * 0.09f);

        float liquidMaxW = Math.max(0f, fillWidth - capW * 2f);
        float liquidW    = liquidMaxW * percent;

        uiBatch.setColor(1f, 1f, 1f, 1f);
        uiBatch.draw(manaBase, x, y, barWidth, barHeight);

        if (percent <= 0f) {
            uiBatch.setColor(1f, 1f, 1f, 1f);
            return;
        }

        uiBatch.draw(
                manaFill,
                fillStartX,
                y,
                capW,
                barHeight,
                0, 0,
                capSrcW,
                manaFill.getHeight(),
                false, false
        );

        if (liquidW > 0f) {
            int midSrcX = capSrcW;
            int midSrcW = manaFill.getWidth() - capSrcW * 2;

            uiBatch.draw(
                    manaFill,
                    fillStartX + capW,
                    y,
                    liquidW,
                    barHeight,
                    midSrcX, 0,
                    midSrcW,
                    manaFill.getHeight(),
                    false, false
            );
        }

        uiBatch.draw(
                manaFill,
                fillStartX + capW + liquidW,
                y,
                capW,
                barHeight,
                manaFill.getWidth() - capSrcW,
                0,
                capSrcW,
                manaFill.getHeight(),
                false, false
        );

        renderManaGlowEffect(uiBatch,  manaGlow, fillStartX, y, fillWidth, barHeight, percent);
        updateAndRenderLongTrail(
                uiBatch,
                manaGlow,
                particles,
                playerId,
                fillStartX,
                y,
                fillWidth,
                barHeight,
                percent
        );

        if (manaDeco != null) {
            float decoWidth = barWidth * 0.12f;

            float startCenterX = x + barWidth * 0.10f;
            float endCenterX   = x + barWidth * 0.87f;

            float t = Math.max(0f, Math.min(1f, percent));
            float decoCenterX = startCenterX + (endCenterX - startCenterX) * t;
            float decoX = decoCenterX - decoWidth * 0.5f;
            uiBatch.setBlendFunction(
                    GL_SRC_ALPHA,
                    GL_ONE_MINUS_SRC_ALPHA
            );
            uiBatch.setColor(1f, 1f, 1f, 1f);
            uiBatch.draw(manaDeco, decoX, y, decoWidth, barHeight);
        }

        uiBatch.setColor(1f, 1f, 1f, 1f);
    }


    private void renderManaGlowEffect(
            SpriteBatch uiBatch,
            Texture manaGlow,
            float fillStartX,
            float y,
            float fillWidth,
            float h,
            float percent
    ){
        if (manaGlow == null || percent <= 0f) return;

        manaGlowTime += Gdx.graphics.getDeltaTime();

        float glowAlpha = 0.4f + 0.3f * (float)Math.sin(manaGlowTime * 3.0f);

        uiBatch.setBlendFunction(
                GL_SRC_ALPHA,
                GL_ONE
        );
        uiBatch.setColor(1f, 0.8f, 0.95f, glowAlpha);

        int srcW = (int)(manaGlow.getWidth() * percent);
        if (srcW > 0) {
            TextureRegion glowRegion =
                    new TextureRegion(manaGlow, 0, 0, srcW, manaGlow.getHeight());

            uiBatch.draw(
                    glowRegion,
                    fillStartX,
                    y + h * 0.15f,
                    fillWidth * percent,
                    h * 0.7f
            );
        }

        uiBatch.setBlendFunction(
                GL_SRC_ALPHA,
                GL_ONE_MINUS_SRC_ALPHA
        );
        uiBatch.setColor(1f, 1f, 1f, 1f);
    }

    private void updateAndRenderLongTrail(
            SpriteBatch uiBatch,
            Texture manaGlow,
            List<ManaParticle> particles,
            int playerId,
            float fillStartX,
            float y,
            float fillWidth,
            float h,
            float percent
    )
    {
        if (percent < 0.999f) {
            particles.clear();
            return;
        }
        if (manaGlow == null) return;

        float endX = fillStartX + fillWidth * percent;
        float delta = Gdx.graphics.getDeltaTime();

        float centerOffset = h / 3f;
        float activeHeight = h * (2f / 3f);

        for (int i = 0; i < 6; i++) {
            if (particles.size() < 150) {
                ManaParticle p = new ManaParticle();
                p.x = endX;
                p.y = y + centerOffset + (float)(Math.random() * activeHeight);

                p.vx = (float)(Math.random() * -300 - 150);
                p.vy = (float)(Math.random() * 40 - 20);
                p.life = 1.2f + (float)Math.random() * 0.8f;

                p.color = (playerId == 0)
                        ? new Color(1.0f, 0.85f, 0.3f, 1f)   // P1 金色
                        : new Color(0.3f, 0.8f, 1.0f, 1f);   // P2 蓝色


                particles.add(p);
            }
        }

        uiBatch.setBlendFunction(GL_SRC_ALPHA, GL_ONE);

        for (int i = particles.size() - 1; i >= 0; i--) {
            ManaParticle p = particles.get(i);
            p.life -= delta;

            if (p.life <= 0 || p.x < fillStartX) {
                particles.remove(i);
                continue;
            }

            p.x += p.vx * delta;
            p.y += p.vy * delta;
            p.vx *= 0.97f;

            float size = 14f * (p.life / 2.0f);
            uiBatch.setColor(p.color.r, p.color.g, p.color.b, p.life * 0.7f);
            TextureRegion particleRegion =
                    (playerId == 0) ? sparkleStar : sparkleFlower;

            uiBatch.draw(
                    particleRegion,
                    p.x - size / 2f,
                    p.y - size / 2f,
                    size,
                    size
            );

        }

        uiBatch.setBlendFunction(
                GL_SRC_ALPHA,
                GL_ONE_MINUS_SRC_ALPHA
        );
    }


    private void renderLivesAsHearts(
            SpriteBatch uiBatch,
            Player player,
            int startX,
            int startY,
            boolean mirror
    ) {
        if (player == null) return;

        Player.PlayerIndex idx = player.getPlayerIndex();
        boolean useIceShader = mirror && iceHeartShader != null;
        int lastLives = lastLivesMap.getOrDefault(idx, -1);
        boolean shaking = shakingMap.getOrDefault(idx, false);
        float shakeTimer = shakeTimerMap.getOrDefault(idx, 0f);

        if (mirror) {
            uiBatch.setShader(iceHeartShader);

            iceHeartShader.setUniformf(
                    "u_tintColor",
                    0.5f, 0.8f, 1.0f     // 冰蓝
            );
            iceHeartShader.setUniformf(
                    "u_intensity",
                    1.0f                // 1 = 完全冰化
            );
            iceHeartShader.setUniformf("u_cooldown", -1.0f);
        }
        uiBatch.setColor(1f, 1f, 1f, 1f);


        int lives = player.getLives();

        if (lastLives != -1 && lives < lastLives) {
            int oldSlot = (lastLives - 1) / 10;
            int newSlot = (lives - 1) / 10;

            int oldInSlot = lastLives - oldSlot * 10;
            int newInSlot = lives - newSlot * 10;

            boolean wasFull = oldInSlot > 5;
            boolean nowHalf = newInSlot <= 5;

            if (oldSlot == newSlot && wasFull && nowHalf) {
                shaking = true;
                shakeTimer = 0f;
            }
        }
        lastLives = lives;

        float delta = Gdx.graphics.getDeltaTime();
        if (shaking) {
            shakeTimer += delta;
            if (shakeTimer >= SHAKE_DURATION) {
                shaking = false;
            }
        }

        int fullHearts = lives / 10;
        int remainder = lives % 10;

        boolean hasHalf = remainder > 0 && remainder <= 5;
        boolean hasExtraFull = remainder > 5;

        int totalHearts = fullHearts
                + (hasHalf ? 1 : 0)
                + (hasExtraFull ? 1 : 0);

        totalHearts = Math.min(totalHearts, MAX_HEARTS_DISPLAY);



        float shakeOffsetX =
                shaking ? (float) Math.sin(shakeTimer * 40f) * SHAKE_AMPLITUDE : 0f;

        int drawn = 0;

        for (int i = 0; i < fullHearts && drawn < totalHearts; i++) {
            int row = drawn / HEARTS_PER_ROW;
            int col = drawn % HEARTS_PER_ROW;

            boolean shakeThis =
                    shaking && i == fullHearts - 1 && !hasExtraFull;
            float x =
                    mirror
                            ? startX - col * HEART_SPACING
                            : startX + col * HEART_SPACING;
            uiBatch.draw(
                    heartFull,
                    x,
                    startY - row * ROW_SPACING
            );
            drawn++;
        }

        if (hasHalf && drawn < totalHearts) {
            int row = drawn / HEARTS_PER_ROW;
            int col = drawn % HEARTS_PER_ROW;
            float x =
                    mirror
                            ? startX - col * HEART_SPACING
                            : startX + col * HEART_SPACING;
            uiBatch.draw(
                    heartHalf,
                    x,
                    startY - row * ROW_SPACING
            );
            drawn++;
        }

        if (hasExtraFull && drawn < totalHearts) {
            int row = drawn / HEARTS_PER_ROW;
            int col = drawn % HEARTS_PER_ROW;
            float x =
                    mirror
                            ? startX - col * HEART_SPACING
                            : startX + col * HEART_SPACING;

            uiBatch.draw(
                    heartFull,
                    x,
                    startY - row * ROW_SPACING
            );
        }
        if (useIceShader) {
            uiBatch.setShader(null);
        }
        uiBatch.setColor(1f, 1f, 1f, 1f);

        lastLivesMap.put(idx, lives);
        shakingMap.put(idx, shaking);
        shakeTimerMap.put(idx, shakeTimer);
    }

    private void renderCat(SpriteBatch uiBatch) {
        if (gameManager.getPlayer() == null) return;

        catStateTime += Gdx.graphics.getDeltaTime();
        boolean hasKey = gameManager.getPlayer().hasKey();
        Animation<TextureRegion> anim =
                hasKey ? catHasKeyAnim : catNoKeyAnim;

        TextureRegion frame = anim.getKeyFrame(catStateTime, true);

        float x = Gdx.graphics.getWidth() - CAT_SIZE - CAT_MARGIN + 170;
        float y = CAT_MARGIN - 80;
        uiBatch.setColor(1f, 1f, 1f, 1f);
        uiBatch.draw(frame, x, y, CAT_SIZE, CAT_SIZE);
    }

    public void renderCompassAsUI(SpriteBatch uiBatch) {
        Compass compass = gameManager.getCompass();
        if (compass == null || !compass.isActive()) return;

        uiBatch.setProjectionMatrix(
                new Matrix4().setToOrtho2D(
                        0, 0,
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight()
                )
        );

        compass.drawAsUI(uiBatch);
    }

    private void renderBottomCenterHUD(SpriteBatch uiBatch) {
        if (gameManager == null || gameManager.getCompass() == null) return;

        HUDLayoutMode mode = getHUDLayoutMode();

        float catW = CAT_SIZE;
        float catH = CAT_SIZE;

        Compass compass = gameManager.getCompass();
        float compassW = compass.getUIWidth();
        float compassH = compass.getUIHeight();

        float totalHeight = compassH + CAT_COMPASS_GAP + catH;

        float centerX;
        float baseY;

        if (mode == HUDLayoutMode.SINGLE) {
            renderCat(uiBatch);
            renderCompassAsUI(uiBatch);
            return;
        }

        centerX = Gdx.graphics.getWidth() / 2f;
        baseY   = 10f;

        float catX = centerX - catW / 2f+150;
        float catY = baseY + CAT_Y_OFFSET;

        float compassX = centerX - compassW / 2f;
        float compassY =
                catY
                        + catH
                        + CAT_COMPASS_GAP
                        + COMPASS_Y_OFFSET;


        renderCatAt(uiBatch, catX, catY);
        renderCompassAt(uiBatch, compassX, compassY);
    }

    private HUDLayoutMode getHUDLayoutMode() {
        if (gameManager != null && gameManager.isTwoPlayerMode()) {
            return HUDLayoutMode.TWO_PLAYER;
        }
        return HUDLayoutMode.SINGLE;
    }

    private void renderCatAt(SpriteBatch uiBatch, float x, float y) {
        catStateTime += Gdx.graphics.getDeltaTime();
        boolean hasKey = gameManager.getPlayer().hasKey();
        Animation<TextureRegion> anim =
                hasKey ? catHasKeyAnim : catNoKeyAnim;

        TextureRegion frame = anim.getKeyFrame(catStateTime, true);
        uiBatch.setColor(1f, 1f, 1f, 1f);
        uiBatch.draw(frame, x, y, CAT_SIZE, CAT_SIZE);
    }

    private void renderCompassAt(SpriteBatch uiBatch, float x, float y) {
        Compass compass = gameManager.getCompass();
        if (!compass.isActive()) return;
        uiBatch.setProjectionMatrix(
                new Matrix4().setToOrtho2D(
                        0, 0,
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight()
                )
        );

        compass.drawAsUIAt(uiBatch, x, y);
    }

    private float getIconX(float iconWidth, boolean mirror) {
        if (!mirror) {
            return DASH_UI_MARGIN_X;
        } else {
            return Gdx.graphics.getWidth()
                    - DASH_UI_MARGIN_X
                    - iconWidth;
        }
    }
    private String formatScore(int score) {
        return String.format("%,d", score);
    }
    private void renderReviveProgressBar(SpriteBatch batch) {
        if (!gameManager.isTwoPlayerMode()) return;
        if (!gameManager.isReviving()) return;

        Player target = gameManager.getRevivingTarget();
        if (target == null) return;

        float progress = Math.min(1f, gameManager.getReviveProgress());

        float barWidth  = 420f;
        float barHeight = 24f;

        float x = (Gdx.graphics.getWidth() - barWidth) / 2f-30;
        float y = Gdx.graphics.getHeight()  - 290;

        batch.setColor(0f, 0f, 0f, 0.65f);
        batch.draw(
                TextureManager.getInstance().getWhitePixel(),
                x, y,
                barWidth, barHeight
        );

        batch.setColor(0.2f, 0.9f, 0.3f, 0.9f);
        batch.draw(
                TextureManager.getInstance().getWhitePixel(),
                x + 2,
                y + 2,
                (barWidth - 4) * progress,
                barHeight - 4
        );

        font.getData().setScale(1.4f);
        font.setColor(Color.WHITE);

        String text = "REVIVING " +
                (target.getPlayerIndex() == Player.PlayerIndex.P1 ? "P1" : "P2");

        font.draw(batch, text,
                x + barWidth / 2f - 70,
                y + barHeight + 26
        );

        font.getData().setScale(1.2f);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void renderAbilityLevel(
            SpriteBatch batch,
            Ability ability,
            float iconX,
            float iconY,
            float iconSize,
            boolean mirror
    ) {
        if (ability == null) return;

        font.getData().setScale(1.0f);
        font.setColor(1f, 1f, 1f, 0.85f);

        String lv = "Lv." + ability.getLevel();

        GlyphLayout layout = new GlyphLayout(font, lv);

        float x = mirror
                ? iconX + LV_PAD_RIGHT
                : iconX + iconSize - layout.width - LV_PAD_RIGHT; // P1
        float y = iconY + LV_PAD_BOTTOM + layout.height;

        font.setColor(0f, 0f, 0f, 0.8f);
        font.draw(batch, lv, x + 1, y - 1);

        font.setColor(Color.WHITE);
        font.draw(batch, lv, x, y);

        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
    }

    private boolean canShowUpgrade(Player player, Ability ability) {
        if (player == null || ability == null) return false;
        if (!ability.canUpgrade()) return false;

        return gameManager.getScore() >= UpgradeCost.SCORE_PER_UPGRADE;
    }

    private void renderUpgradeButton(
            SpriteBatch batch,
            Player player,
            Ability ability,
            float iconX,
            float iconY,
            float iconSize,
            boolean mirror,
            boolean allowInteraction
    ) {

        if (!canShowUpgrade(player, ability)) return;


        final float BTN_SIZE = UPG_BTN_SIZE;

        float floatY = (float) Math.sin(TimeUtils.millis() * UPG_BTN_FLOAT_SPEED) * UPG_BTN_FLOAT_AMP;

        float anchorX = mirror
                ? iconX - UPG_BTN_OFF_X - BTN_SIZE
                : iconX + iconSize + UPG_BTN_OFF_X;
        float anchorY = iconY + iconSize * 0.5f;

        float bx = anchorX ;
        float by = anchorY - BTN_SIZE * 0.5f + UPG_BTN_OFF_Y + floatY;


        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        boolean hover =
                mx >= bx && mx <= bx + BTN_SIZE &&
                        my >= by && my <= by + BTN_SIZE;
        if (hover) {
            uiHoverThisFrame = true;
        }


        float perAbilityX = 0f;
        float perAbilityY = 0f;

        if (ability instanceof DashAbility) {
            perAbilityX = 0f;
            perAbilityY = 0f;
        } else if (ability instanceof MeleeAttackAbility) {
            perAbilityX = -4f;
            perAbilityY = -2f;
        } else if (ability instanceof MagicAbility) {
            perAbilityX = 6f;
            perAbilityY = 3f;
        }

        bx += perAbilityX;
        by += perAbilityY;


        if (hover) {
            hoveredUpgradeAbility = ability;
            upgradeHoverAnim = Math.min(1f, upgradeHoverAnim + Gdx.graphics.getDeltaTime() * 8f);
        } else if (hoveredUpgradeAbility == ability) {
            upgradeHoverAnim = Math.max(0f, upgradeHoverAnim - Gdx.graphics.getDeltaTime() * 8f);
            if (upgradeHoverAnim <= 0f) hoveredUpgradeAbility = null;
        }

        float scale = 1f + UPG_HOVER_SCALE * upgradeHoverAnim;
        float alpha = UPG_HOVER_ALPHA_BASE + UPG_HOVER_ALPHA_ADD * upgradeHoverAnim;


        batch.setColor(1f, 0.85f, 0.2f, alpha);
        batch.draw(
                TextureManager.getInstance().getWhitePixel(),
                bx - (BTN_SIZE * (scale - 1f) / 2f),
                by - (BTN_SIZE * (scale - 1f) / 2f),
                BTN_SIZE * scale,
                BTN_SIZE * scale
        );


        font.getData().setScale(2.0f * scale);

        GlyphLayout layout = new GlyphLayout(font, "+");

        float tx = bx + BTN_SIZE / 2f - layout.width / 2f;
        float ty = by + BTN_SIZE / 2f + layout.height / 2f;

        // 阴影
        font.setColor(0f, 0f, 0f, 0.6f);
        font.draw(batch, "+", tx + 2, ty - 2);

        // 正文
        font.setColor(Color.WHITE);
        font.draw(batch, "+", tx, ty);


        boolean mouseDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        if (allowInteraction && hover && mouseDown) {
            long now = TimeUtils.millis();
            if (now - lastUpgradeTime > UPGRADE_COOLDOWN_MS) {
                lastUpgradeTime = now;
                boolean success =  gameManager
                        .getScoreManager()
                        .spendUpgradeScore(UpgradeCost.SCORE_PER_UPGRADE);
                Logger.error(
                        "UPGRADE TRY | score=" + gameManager.getScore()
                                + " success=" + success
                );
                if (success) {
                    ability.upgrade();
                }
            }
        }



        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Indicates whether the mouse hovered an interactive HUD element during this frame.
     *
     * @return true if an interactive UI element was hovered
     */
    public boolean isHoveringInteractiveUI() {
        return uiHoverThisFrame;
    }
    /**
     * Disposes all resources used by the HUD.
     */
    public void dispose() {
        font.dispose();
        heartFull.dispose();
        heartHalf.dispose();
        shapeRenderer.dispose();
        catAtlas.dispose();
        iconAtk.dispose();
        iconRegen.dispose();
        iconMana.dispose();
        sparkleAtlas.dispose();
        if (manaBaseP2 != manaBaseP1) manaBaseP2.dispose();
        manaFillP1.dispose();
        manaFillP2.dispose();
        manaGlowP1.dispose();
        manaGlowP2.dispose();
    }

    /**
     * Enables the boss HUD overlay.
     *
     * @param maxHp the maximum health of the boss
     */
    public void enableBossHUD(float maxHp) {
        this.hudMode = HUDMode.BOSS;
        this.bossMaxHp = maxHp;
        this.bossHp = maxHp;
    }
    /**
     * Updates the current boss health value.
     *
     * @param hp the new boss health
     */
    public void updateBossHp(float hp) {
        this.bossHp = Math.max(0f, hp);
    }
    /**
     * Disables the boss HUD overlay.
     */
    public void disableBossHUD() {
        this.hudMode = HUDMode.NORMAL;
    }
    /**
     * Sets whether the boss is in a final locked phase.
     *
     * @param locked true if the boss is locked in the final phase
     */
    public void setBossFinalLocked(boolean locked) {
        this.bossFinalLocked = locked;
    }
    /**
     * Updates the current boss phase index.
     *
     * @param phaseIndex the current boss phase
     */
    public void setBossPhase(int phaseIndex) {
        this.bossPhaseIndex = phaseIndex;
    }
    /**
     * Enables or disables the boss rage warning indicator.
     *
     * @param warning true to enable the rage warning
     */
    public void setBossRageWarning(boolean warning) {
        this.bossRageWarning = warning;
    }

}