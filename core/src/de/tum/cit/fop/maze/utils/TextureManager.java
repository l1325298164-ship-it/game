// TextureManager.java
package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;
import de.tum.cit.fop.maze.game.GameConstants;

import java.util.HashMap;
import java.util.Map;
/**
 * Central manager for loading, caching, and providing textures and texture atlases.
 * <p>
 * Supports multiple texture modes (color, image, pixel, minimal) and provides
 * fallback color textures when image files are missing.
 * <p>
 * Implemented as a singleton and implements {@link com.badlogic.gdx.utils.Disposable}
 * for proper resource cleanup.
 */
public class TextureManager implements Disposable {
    private static TextureManager instance;
    private Map<String, Texture> textures;
    private Texture whitePixel;




    /**
     * Defines available texture loading modes.
     */
    public enum TextureMode {
        /**
         * Uses solid color textures only.
         */
        COLOR,
        /**
         * Uses image files for textures.
         */
        IMAGE,

        /**
         * Uses pixel-style textures based on image files.
         */
        PIXEL,

        /**
         * Uses minimal fallback textures only.
         */
        MINIMAL
    }


    public static final String FLOOR = "floor";
    public static final String WALL = "wall";
    public static final String PLAYER = "player";
    public static final String KEY = "key";
    public static final String DOOR = "door";
    public static final String LOCKED_DOOR = "locked_door";
    public static final String ENEMY1 = "enemy1";
    public static final String ENEMY2 = "enemy2";
    public static final String ENEMY3 = "enemy3";
    public static final String ENEMY3AOE = "ENEMY3AOE";
    public static final String CAT= "cat";




    public static final String HEART = "heart";
    public static final String TRAP = "trap";


    private static final Color DEFAULT_COLOR = Color.WHITE;

    private Map<TextureMode, Map<String, String>> textureFileMap;
    private TextureMode currentMode = TextureMode.IMAGE; // 默认纯色模式


    private TextureAtlas wallAtlas;
    private TextureAtlas E01AtlasLR;
    private TextureAtlas E01AtlasFront;
    private TextureAtlas E01AtlasBack;
    private TextureAtlas E02Atlas;
    private TextureAtlas E03Atlas;
    private TextureAtlas E04Atlas;
    private TextureAtlas T01Atlas;
    private TextureAtlas T02Atlas;
    private TextureAtlas T03Atlas;
    private TextureAtlas T04Atlas;
    private TextureAtlas chipAtlas;
    private TextureAtlas catRightAtlas;
    private TextureAtlas catLeftAtlas;
    private TextureAtlas catBackAtlas;
    private TextureAtlas catFrontAtlas;

    private TextureManager() {
        textures = new HashMap<>();
        textureFileMap = new HashMap<>();
        loadWallAtlas();
        loadE01Atlas();
        loadE02_T04Atlas();
        loadCatAtlas();


        initializeTextureMappings();

        Logger.debug("TextureManager initialized, mode: " + currentMode);
    }

    private void loadCatAtlas() {
        catRightAtlas=new TextureAtlas(Gdx.files.internal("ani/cat/right/cat_right.atlas"));
          catLeftAtlas=new TextureAtlas(Gdx.files.internal("ani/cat/left/cat_left.atlas"));
          catBackAtlas=new TextureAtlas(Gdx.files.internal("ani/cat/back/cat_back.atlas"));
          catFrontAtlas=new TextureAtlas(Gdx.files.internal("ani/cat/front/cat_front.atlas"));

    }

    private void loadE02_T04Atlas() {
        E02Atlas=new TextureAtlas(Gdx.files.internal("ani/E02/E02.atlas"));
        E03Atlas=new TextureAtlas(Gdx.files.internal("ani/E03/E03.atlas"));
        E04Atlas=new TextureAtlas(Gdx.files.internal("ani/E04/E04.atlas"));
        T01Atlas=new TextureAtlas(Gdx.files.internal("ani/T01/T01.atlas"));
        T02Atlas=new TextureAtlas(Gdx.files.internal("ani/T02/T02.atlas"));
        T03Atlas=new TextureAtlas(Gdx.files.internal("ani/T03/T03.atlas"));
        T04Atlas=new TextureAtlas(Gdx.files.internal("ani/T04/T04.atlas"));
        chipAtlas=new TextureAtlas(Gdx.files.internal("ani/chip/chip.atlas"));
    }

    private void loadE01Atlas() {
        E01AtlasLR=new TextureAtlas(Gdx.files.internal("ani/E01/E01.atlas"));
        E01AtlasFront=new TextureAtlas(Gdx.files.internal("ani/E01/front/E01_front.atlas"));
        E01AtlasBack=new TextureAtlas(Gdx.files.internal("ani/E01/back/E01_back.atlas"));
    }


    private void loadWallAtlas() {
        wallAtlas = new TextureAtlas(Gdx.files.internal("Wallpaper/Wallpaper.atlas"));
    }
    /**
     * Returns the singleton instance of the {@code TextureManager}.
     *
     * @return the global {@code TextureManager} instance
     */
    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }
    /**
     * Returns the texture atlas used for wall tiles.
     *
     * @return the wall {@link TextureAtlas}
     */
    public TextureAtlas getWallAtlas() {
        return wallAtlas;
    }


    private void initializeTextureMappings() {
        textureFileMap.put(TextureMode.COLOR, new HashMap<>());

        Map<String, String> imageMode = new HashMap<>();

        imageMode.put(FLOOR, "imgs/floor/780.jpg");
        imageMode.put(KEY, "imgs/Items/key_1.png");





        textureFileMap.put(TextureMode.IMAGE, imageMode);

        Map<String, String> pixelMode = new HashMap<>();
        pixelMode.putAll(imageMode);
        textureFileMap.put(TextureMode.PIXEL, pixelMode);

        Map<String, String> minimalMode = new HashMap<>();
        textureFileMap.put(TextureMode.MINIMAL, minimalMode);
    }

    /**
     * Switches the current texture mode.
     * <p>
     * Clears cached non-color textures and preloads textures
     * required for the specified mode.
     *
     * @param mode the new {@link TextureMode} to activate
     */

    public void switchMode(TextureMode mode) {
        if (this.currentMode == mode) return;

        Logger.debug("Switching texture mode from " + currentMode + " to " + mode);

        clearAllNonColorTextures();

        this.currentMode = mode;

        preloadTexturesForMode(mode);
    }



    private void preloadTexturesForMode(TextureMode mode) {
        Map<String, String> fileMap = textureFileMap.get(mode);

        if (fileMap != null && !fileMap.isEmpty()) {
            for (Map.Entry<String, String> entry : fileMap.entrySet()) {
                loadImageTexture(entry.getKey(), entry.getValue());
            }
        }
    }

    private void loadImageTexture(String key, String filePath) {
        try {
            FileHandle file = Gdx.files.internal(filePath);
            if (file.exists()) {
                Texture texture = new Texture(file);
                textures.put(key, texture);
                Logger.debug("Loaded image texture: " + key + " from " + filePath);
            } else {
                Logger.warning("Image file not found: " + filePath + ", using fallback");
                createFallbackTexture(key);
            }
        } catch (Exception e) {
            Logger.error("Failed to load texture: " + key + " - " + e.getMessage());
            createFallbackTexture(key);
        }
    }


    private void createFallbackTexture(String key) {
        Color fallbackColor = getFallbackColor(key);
        Texture colorTexture = createColorTexture(fallbackColor);
        textures.put(key, colorTexture);
    }


    private Color getFallbackColor(String key) {
        switch (key) {
            case FLOOR: return GameConstants.FLOOR_COLOR;
            case WALL: return GameConstants.WALL_COLOR;
            case PLAYER: return GameConstants.PLAYER_COLOR;
            case KEY: return GameConstants.KEY_COLOR;
            case DOOR: return GameConstants.DOOR_COLOR;
            case LOCKED_DOOR: return GameConstants.LOCKED_DOOR_COLOR;
            case ENEMY1: return Color.PURPLE; // 敌人备用颜色
            case HEART: return GameConstants.HEART_COLOR;
            case TRAP: return Color.RED; // 或你想要的陷阱颜色

            default: return DEFAULT_COLOR;
        }
    }


    private void clearAllNonColorTextures() {
        Map<String, Texture> toKeep = new HashMap<>();

        for (Map.Entry<String, Texture> entry : textures.entrySet()) {
            String key = entry.getKey();

            if (key.startsWith("color_")) {
                toKeep.put(key, entry.getValue());
            } else {
                entry.getValue().dispose();
            }
        }

        textures = toKeep;
        Logger.debug("Cleared all non-color textures, kept " + textures.size());
    }


    /**
     * Returns a texture associated with the given key.
     * <p>
     * Depending on the current texture mode, this may return an image texture
     * or a fallback color texture.
     *
     * @param key the texture key
     * @return the requested {@link Texture}
     */

    public Texture getTexture(String key) {
        if (textures.containsKey(key)) {
            return textures.get(key);
        }

        switch (currentMode) {
            case COLOR:
                return getColorTextureByKey(key);

            case IMAGE:
            case PIXEL:
                Map<String, String> fileMap = textureFileMap.get(currentMode);
                if (fileMap != null && fileMap.containsKey(key)) {
                    loadImageTexture(key, fileMap.get(key));
                    return textures.get(key);
                }
                return getColorTextureByKey(key);

            case MINIMAL:
                return getColorTextureByKey(key);

            default:
                return getColorTextureByKey(key);
        }
    }

    private Texture getColorTextureByKey(String key) {
        String colorKey = "color_" + key;
        if (!textures.containsKey(colorKey)) {
            Color color = getFallbackColor(key);
            textures.put(colorKey, createColorTexture(color));
        }
        return textures.get(colorKey);
    }

    /**
     * Returns a solid-color texture for the given color.
     * <p>
     * The texture is cached and reused for future requests.
     *
     * @param color the color to use
     * @return a {@link Texture} filled with the given color
     */

    public Texture getColorTexture(Color color) {
        String key = "color_custom_" + color.toString();
        if (!textures.containsKey(key)) {
            textures.put(key, createColorTexture(color));
        }
        return textures.get(key);
    }

    private Texture createColorTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
    /**
     * Returns the floor texture.
     *
     * @return the floor {@link Texture}
     */
    public Texture getFloorTexture() {
        return getTexture(FLOOR);
    }
    /**
     * Returns the wall texture.
     *
     * @return the wall {@link Texture}
     */
    public Texture getWallTexture() {
        return getTexture(WALL);
    }
    /**
     * Returns the player texture.
     *
     * @return the player {@link Texture}
     */
    public Texture getPlayerTexture() {
        return getTexture(PLAYER);
    }
    /**
     * Returns the trap texture.
     *
     * @return the trap {@link Texture}
     */
    public Texture getTrapTexture() {
        return getTexture(TRAP);
    }
    /**
     * Returns the key texture.
     *
     * @return the key {@link Texture}
     */
    public Texture getKeyTexture() {
        return getTexture(KEY);
    }
    /**
     * Returns the door texture.
     *
     * @return the door {@link Texture}
     */
    public Texture getDoorTexture() {
        return getTexture(DOOR);
    }
    /**
     * Returns the locked door texture.
     *
     * @return the locked door {@link Texture}
     */
    public Texture getLockedDoorTexture() {
        return getTexture(LOCKED_DOOR);
    }
    /**
     * Returns the primary enemy texture.
     *
     * @return the enemy texture
     */
    public Texture getEnemy1Texture() {
        return getTexture(ENEMY1);
    }
    public Texture getEnemy2Texture() {return getTexture(ENEMY2);
    }

    public Texture getEnemy3Texture() {
        return getTexture(ENEMY3);
    }

    public Texture getEnemy3AOETexture() {
        return getTexture(ENEMY3AOE);
    }

    public Texture getEnemy4ShellTexture() {
        return getTexture(ENEMY3);
    }
    /**
     * Returns the heart texture.
     *
     * @return the heart {@link Texture}
     */
    public Texture getHeartTexture() {
        return getTexture(HEART);
    }
    /**
     * Returns the cat texture.
     *
     * @return the cat {@link Texture}
     */
    public Texture getCatTexture() {
        return getTexture(CAT);

    }
    /**
     * Returns the currently active texture mode.
     *
     * @return the current {@link TextureMode}
     */
    public TextureMode getCurrentMode() {
        return currentMode;
    }

    /**
     * Preloads commonly used textures for all texture modes.
     * <p>
     * Restores the original texture mode after preloading.
     */
    public void preloadAllModes() {
        Logger.debug("Preloading textures for all modes...");

        getFloorTexture();
        getWallTexture();
        getPlayerTexture();
        getKeyTexture();
        getDoorTexture();

        TextureMode originalMode = currentMode;

        for (TextureMode mode : TextureMode.values()) {
            if (mode != originalMode) {
                switchMode(mode);
                getPlayerTexture();
                getWallTexture();
            }
        }

        switchMode(originalMode);
    }
    /**
     * Disposes all loaded textures and clears internal caches.
     * <p>
     * After calling this method, the {@code TextureManager} instance
     * becomes invalid and must be reinitialized.
     */
    public void dispose() {
        Logger.debug("Disposing TextureManager");
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        textureFileMap.clear();
        instance = null;
    }
    /**
     * Returns a cached 1x1 white pixel texture.
     * <p>
     * This texture is commonly used for UI rendering and color tinting.
     *
     * @return a 1x1 white {@link Texture}
     */
    public Texture getWhitePixel() {
        if (whitePixel == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(Color.WHITE);
            pm.fill();
            whitePixel = new Texture(pm);
            pm.dispose();
        }
        return whitePixel;
    }
    /**
     * Returns the texture atlas for E01 enemies (left/right).
     *
     * @return the E01 left/right {@link TextureAtlas}
     */
    public TextureAtlas getEnemy1AtlasRL() {
        Logger.debug("Getting E01 Atlas LR: " + (E01AtlasLR != null));
        if (E01AtlasLR == null) {
            Logger.error("E01AtlasLR is null! Attempting to reload...");
            loadE01Atlas();
        }
        return E01AtlasLR;
    }
    /**
     * Returns the texture atlas for E01 enemies (front).
     *
     * @return the E01 front {@link TextureAtlas}
     */
    public TextureAtlas getEnemy1AtlasFront() {
        return E01AtlasFront;
    }
    /**
     * Returns the texture atlas for E01 enemies (back).
     *
     * @return the E01 back {@link TextureAtlas}
     */
    public TextureAtlas getEnemy1AtlasBack() {
        return E01AtlasBack;
    }
    /**
     * Returns the texture atlas for E02 enemies.
     *
     * @return the E02 {@link TextureAtlas}
     */
    public TextureAtlas getEnemyE02Atla() {
        return E02Atlas;
    }
    /**
     * Returns the texture atlas for E03 enemies.
     *
     * @return the E03 {@link TextureAtlas}
     */
    public TextureAtlas getEnemyE03Atla() {
        return E03Atlas;
    }
    /**
     * Returns the texture atlas for E04 enemies.
     *
     * @return the E04 {@link TextureAtlas}
     */
    public TextureAtlas getEnemyE04Atlas() {
        return E04Atlas;
    }
    /**
     * Returns the texture atlas for trap type T01.
     *
     * @return the T01 {@link TextureAtlas}
     */
    public TextureAtlas getTrapT01Atlas() {
        return T01Atlas;
    }
    /**
     * Returns the texture atlas for trap type T02.
     *
     * @return the T02 {@link TextureAtlas}
     */
    public TextureAtlas getTrapT02Atlas() {
        return T02Atlas;
    }
    /**
     * Returns the texture atlas for trap type T03.
     *
     * @return the T03 {@link TextureAtlas}
     */
    public TextureAtlas getTrapT03Atlas() {
        return T03Atlas;
    }
    /**
     * Returns the texture atlas for trap type T04.
     *
     * @return the T04 {@link TextureAtlas}
     */
    public TextureAtlas getTrapT04Atlas() {
        return T04Atlas;
    }
    /**
     * Returns the texture atlas for chip effects.
     *
     * @return the chip {@link TextureAtlas}
     */
    public TextureAtlas getTrapChipAtlas() {
        return chipAtlas;
    }
    /**
     * Returns the texture atlas for the cat facing left.
     *
     * @return the left-facing cat {@link TextureAtlas}
     */
    public TextureAtlas getCatLeftAtlas() {
        return catLeftAtlas;
    }
    /**
     * Returns the texture atlas for the cat facing right.
     *
     * @return the right-facing cat {@link TextureAtlas}
     */
    public TextureAtlas getCatRightAtlas() {return catRightAtlas;
    }/**
     * Returns the texture atlas for the cat facing front.
     *
     * @return the front-facing cat {@link TextureAtlas}
     */
    public TextureAtlas getCatFrontAtlas() {return catFrontAtlas;
    }
    /**
     * Returns the texture atlas for the cat facing back.
     *
     * @return the back-facing cat {@link TextureAtlas}
     */
    public TextureAtlas getCatBackAtlas() {return catBackAtlas;
    }
}
