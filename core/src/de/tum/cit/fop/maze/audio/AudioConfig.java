package de.tum.cit.fop.maze.audio;
/**
 * Configuration object describing a single audio resource.
 * <p>
 * {@code AudioConfig} encapsulates metadata and playback parameters
 * for both sound effects and music tracks, including volume, looping,
 * priority, and lifecycle-related information.
 * It is typically consumed by the audio manager during loading
 * and playback.
 */
public class AudioConfig {
    private String name;
    private String filePath;
    private AudioCategory category;
    private boolean isMusic;
    private float defaultVolume = 1.0f;
    private boolean autoPlay = false;
    private boolean loop = false;
    private boolean enabled = true;
    private float pitch = 1.0f;
    private float pan = 0.0f;
    private boolean priority;

    private long lastPlayTime = 0;
    private int playCount = 0;
    private boolean persistent = false;
    /**
     * Creates a new audio configuration.
     *
     * @param name     the unique audio name
     * @param filePath the file path to the audio asset
     * @param category the audio category
     */
    public AudioConfig(String name, String filePath, AudioCategory category) {
        this.name = name;
        this.filePath = filePath;
        this.category = category;
        this.isMusic = (category == AudioCategory.MUSIC);
    }
    /**
     * Returns the audio name.
     *
     * @return the audio name
     */
    public String getName() { return name; }
    /**
     * Sets the audio name.
     *
     * @param name the new name
     */
    public void setName(String name) { this.name = name; }
    /**
     * Returns the file path of the audio asset.
     *
     * @return the file path
     */
    public String getFilePath() { return filePath; }
    /**
     * Sets the file path of the audio asset.
     *
     * @param filePath the new file path
     */
    public void setFilePath(String filePath) { this.filePath = filePath; }
    /**
     * Returns the audio category.
     *
     * @return the audio category
     */
    public AudioCategory getCategory() { return category; }
    /**
     * Sets the audio category.
     * <p>
     * Updates the {@code isMusic} flag automatically based on the category.
     *
     * @param category the new category
     */
    public void setCategory(AudioCategory category) {
        this.category = category;
        this.isMusic = (category == AudioCategory.MUSIC);
    }
    /**
     * Returns whether this audio is treated as music.
     *
     * @return {@code true} if this is music
     */
    public boolean isMusic() { return isMusic; }
    /**
     * Sets whether this audio is treated as music.
     *
     * @param music {@code true} if music
     */
    public void setMusic(boolean music) { isMusic = music; }
    /**
     * Returns the default playback volume.
     *
     * @return volume in range [0, 1]
     */
    public float getDefaultVolume() { return defaultVolume; }
    /**
     * Sets the default playback volume.
     *
     * @param defaultVolume volume in range [0, 1]
     */
    public void setDefaultVolume(float defaultVolume) {
        this.defaultVolume = Math.max(0, Math.min(1, defaultVolume));
    }

    /**
     * Returns whether the audio is set to auto-play.
     *
     * @return {@code true} if auto-play is enabled
     */
    public boolean isAutoPlay() { return autoPlay; }
    /**
     * Sets whether the audio should auto-play.
     *
     * @param autoPlay {@code true} to enable auto-play
     */
    public void setAutoPlay(boolean autoPlay) { this.autoPlay = autoPlay; }
    /**
     * Returns whether the audio is looped.
     *
     * @return {@code true} if looping is enabled
     */
    public boolean isLoop() { return loop; }

    /**
     * Sets whether the audio should loop.
     *
     * @param loop {@code true} to enable looping
     */
    public void setLoop(boolean loop) { this.loop = loop; }
    /**
     * Returns whether the audio resource is enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() { return enabled; }
    /**
     * Enables or disables the audio resource.
     *
     * @param enabled {@code true} to enable
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    /**
     * Returns the playback pitch.
     *
     * @return pitch multiplier
     */
    public float getPitch() { return pitch; }
    /**
     * Sets the playback pitch.
     *
     * @param pitch pitch multiplier
     */
    public void setPitch(float pitch) {
        this.pitch = Math.max(0.5f, Math.min(2.0f, pitch));
    }
    /**
     * Returns the stereo panning value.
     *
     * @return pan value in range [-1, 1]
     */
    public float getPan() { return pan; }
    /**
     * Sets the stereo panning value.
     *
     * @param pan pan value in range [-1, 1]
     */
    public void setPan(float pan) {
        this.pan = Math.max(-1, Math.min(1, pan));
    }
    /**
     * Returns whether this audio has priority.
     *
     * @return {@code true} if priority audio
     */
    public boolean isPriority() { return priority; }
    /**
     * Sets whether this audio has priority.
     *
     * @param priority {@code true} if priority
     */
    public void setPriority(boolean priority) { this.priority = priority; }
    /**
     * Returns the last playback timestamp.
     *
     * @return last play time in milliseconds
     */
    public long getLastPlayTime() { return lastPlayTime; }
    /**
     * Sets the last playback timestamp.
     *
     * @param lastPlayTime time in milliseconds
     */
    public void setLastPlayTime(long lastPlayTime) { this.lastPlayTime = lastPlayTime; }
    /**
     * Returns the total number of times this audio was played.
     *
     * @return play count
     */
    public int getPlayCount() { return playCount; }
    /**
     * Sets the play count.
     *
     * @param playCount the new play count
     */
    public void setPlayCount(int playCount) { this.playCount = playCount; }

    /**
     * Returns whether this audio resource is persistent.
     *
     * @return {@code true} if persistent
     */
    public boolean isPersistent() { return persistent; }
    /**
     * Sets whether this audio resource should persist in memory.
     *
     * @param persistent {@code true} if persistent
     */
    public void setPersistent(boolean persistent) { this.persistent = persistent; }
    /**
     * Returns the current playback volume.
     * <p>
     * This implementation simply returns the default volume.
     *
     * @return current volume
     */
    public float getCurrentVolume() {
        return getDefaultVolume();
    }
    /**
     * Records a playback event.
     * <p>
     * Updates the play count and last playback timestamp.
     */
    public void recordPlay() {
        this.playCount++;
        this.lastPlayTime = System.currentTimeMillis();
    }
    /**
     * Checks whether this audio resource is considered active.
     * <p>
     * An audio resource is active if it has been played within
     * the last five minutes.
     *
     * @return {@code true} if recently played
     */
    public boolean isActive() {
        return System.currentTimeMillis() - lastPlayTime < 5 * 60 * 1000;
    }
}