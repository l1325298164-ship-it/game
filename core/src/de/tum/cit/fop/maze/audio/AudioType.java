package de.tum.cit.fop.maze.audio;

/**
 * Enumeration of all audio resources used in the game.
 * <p>
 * {@code AudioType} defines static audio entries including music tracks,
 * sound effects, and UI sounds. Each enum constant encapsulates
 * the metadata required to construct an {@link AudioConfig} instance,
 * which is later consumed by the {@link AudioManager}.
 */
public enum AudioType {
    MUSIC_MENU("sounds_file/BGM/maze_bgm.mp3", AudioCategory.MUSIC, true, 0.7f, true, false),
    MUSIC_MAZE_EASY("sounds_file/BGM/maze_easy.mp3", AudioCategory.MUSIC, true, 0.7f, true, false),
    MUSIC_MAZE_NORMAL("sounds_file/BGM/maze_normal.mp3", AudioCategory.MUSIC, true, 0.7f, true, false),
    MUSIC_MAZE_HARD("sounds_file/BGM/maze_hard.mp3", AudioCategory.MUSIC, true, 0.7f, true, false),
    MUSIC_MAZE_ENDLESS("sounds_file/BGM/maze_endless.mp3", AudioCategory.MUSIC, true, 0.7f, true, false),

    PLAYER_MOVE("sounds_file/ogg/melee_2_01.ogg", AudioCategory.PLAYER, false, 0.3f, true, false),
    PLAYER_GET_KEY("sounds_file/ogg/treasure_01.ogg", AudioCategory.PLAYER, false, 1.0f, false, false),
    PLAYER_ATTACKED("sounds_file/ogg/E01_01.ogg", AudioCategory.PLAYER, false, 1.0f, false, false),
    PLAYER1_ATTACK("sounds_file/ogg/melee_1.ogg", AudioCategory.PLAYER, false, 1.0f, false, false),
    PLAYER2_ATTACK("sounds_file/SFX/magic_set.mp3", AudioCategory.PLAYER, false, 1.0f, false, false),
    MAGIC_EXECUTE_LV1("sounds_file/ogg/magic_1_01.ogg", AudioCategory.PLAYER, false, 1.0f, false, false),
    MAGIC_EXECUTE_LV3("sounds_file/ogg/magic_3_01.ogg", AudioCategory.PLAYER, false, 1.0f, false, false),
    MAGIC_EXECUTE_LV5("sounds_file/ogg/magic_5_01.ogg", AudioCategory.PLAYER, false, 1.0f, false, false),
    ABILITY_UPGRADE_COMMON("sounds_file/ogg/levelup_2_01.ogg", AudioCategory.PLAYER, false, 1.0f, false, false),






    PV_1("sounds_file/BGM/pv/PV1.mp3", AudioCategory.MUSIC, true, 0.7f, false, false),
    PV_2("sounds_file/BGM/pv/PV2.mp3", AudioCategory.MUSIC, true, 0.7f, false, false),
    PV_3("sounds_file/BGM/pv/PV3.mp3", AudioCategory.MUSIC, true, 0.7f, false, false),
    PV_4("sounds_file/BGM/pv/PV4.mp3", AudioCategory.MUSIC, true, 0.7f, false, false),



    ENEMY_ATTACKED("sounds_file/ogg/E_damage_01.ogg", AudioCategory.ENEMY, false, 0.8f, false, false),
    ENEMY_ATTACKED_E01("sounds_file/SFX/attack_E01.ogg", AudioCategory.ENEMY, false, 0.8f, false, false),
    ENEMY_ATTACKED_E02("sounds_file/ogg/E_damage_01.ogg", AudioCategory.ENEMY, false, 0.8f, false, false),
    ENEMY_ATTACKED_E03("sounds_file/SFX/attack_E03.ogg", AudioCategory.ENEMY, false, 0.8f, false, false),
    ENEMY_ATTACK_DEFAULT("sounds_file/SFX/attack_E03.ogg", AudioCategory.ENEMY, false, 0.8f, false, false),
    ENEMY_ATTACK_E01("sounds_file/ogg/E01_01.ogg", AudioCategory.ENEMY, false, 0.8f, false, false),
    ENEMY_ATTACK_E02("sounds_file/ogg/E02_01.ogg", AudioCategory.ENEMY, false, 0.8f, false, false),
    ENEMY_ATTACK_E03("sounds_file/ogg/E03_01.ogg", AudioCategory.ENEMY, false, 0.8f, false, false),
    ENEMY_ATTACK_E04("sounds_file/ogg/E04_01.ogg", AudioCategory.ENEMY, false, 0.8f, false, false),


    UI_CLICK("sounds_file/SFX/btn_3.mp3", AudioCategory.UI, false, 0.6f, false, true),
    UI_SUCCESS("sounds_file/SFX/btn_3.mp3", AudioCategory.UI, false, 0.8f, false, true),
    UI_FAILURE("sounds_file/ogg/magic_1_01.ogg", AudioCategory.UI, false, 0.8f, false, true),
    UI_HIT_DAZZLE("sounds_file/SFX/btn_2.mp3", AudioCategory.UI, false, 1.0f, false, true),
    UI_THROW_ATTACK("sounds_file/SFX/btn_2.mp3", AudioCategory.UI, false, 1.0f, false, true),

    SKILL_DASH("sounds_file/ogg/DASH_01.ogg", AudioCategory.PLAYER, false, 0.9f, false, false),
    SKILL_SLASH("sounds_file/ogg/melee_1.ogg", AudioCategory.PLAYER, false, 0.8f, false, false),
    BUFF_GAIN("sounds_file/ogg/treasure_01.ogg", AudioCategory.UI, false, 0.9f, false, false),

    ENEMY_DEATH("sounds_file/ogg/E03_damage_01.ogg", AudioCategory.ENEMY, false, 0.6f, false, false),


    TUTORIAL_MAIN_BGM(
            "sounds_file/BGM/tutorial_bgm.mp3",
            AudioCategory.MUSIC,
            true,
            0.6f,
            true,
            false
    ),
    BOSS_BGM("sounds_file/BGM/boss_bgm.mp3",AudioCategory.MUSIC, true, 1f, false, false),
    MUSIC_MENU_END("sounds_file/BGM/menu_bgm2.mp3",AudioCategory.MUSIC, true, 0.7f, true, false),
    BOSS_LOADING("sounds_file/BGM/BOSS_loading.mp3",AudioCategory.MUSIC, true, 0.7f, true, false),
    BOSS_AOE_WARNING("sounds_file/ogg/boss_hit_01.ogg",AudioCategory.PLAYER, false, 0.3f, false, false);





    private final String path;
    private final AudioCategory category;
    private final boolean isMusic;
    private final float defaultVolume;
    private final boolean loop;
    private final boolean isPriority;

    AudioType(String path, AudioCategory category, boolean isMusic,
              float defaultVolume, boolean loop, boolean isPriority) {
        this.path = path;
        this.category = category;
        this.isMusic = isMusic;
        this.defaultVolume = Math.max(0, Math.min(1, defaultVolume));
        this.loop = loop;
        this.isPriority = isPriority;
    }

    /**
     * Creates a new {@link AudioConfig} instance from this audio type.
     *
     * @return a configured {@code AudioConfig}
     */
    public AudioConfig getConfig() {
        AudioConfig config = new AudioConfig(this.name(), this.path, this.category);
        config.setDefaultVolume(this.defaultVolume);
        config.setLoop(this.loop);
        config.setMusic(this.isMusic);
        config.setPriority(this.isPriority);
        return config;
    }
    /**
     * Returns the file path of this audio asset.
     *
     * @return audio file path
     */
    public String getPath() { return path; }
    /**
     * Returns the audio category.
     *
     * @return audio category
     */
    public AudioCategory getCategory() { return category; }
    /**
     * Returns whether this audio is music.
     *
     * @return {@code true} if music
     */
    public boolean isMusic() { return isMusic; }
    /**
     * Returns the default playback volume.
     *
     * @return default volume
     */
    public float getDefaultVolume() { return defaultVolume; }
    /**
     * Returns whether this audio should loop.
     *
     * @return {@code true} if looping
     */
    public boolean isLoop() { return loop; }
    /**
     * Returns whether this audio has priority.
     *
     * @return {@code true} if priority audio
     */
    public boolean isPriority() { return isPriority; }
}