package de.tum.cit.fop.maze.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central manager responsible for loading, playing, and controlling audio.
 * <p>
 * {@code AudioManager} handles sound effects and music playback, global
 * volume control, resource lifecycle management, and active audio tracking.
 * It follows the singleton pattern and is intended to be used globally.
 */
public class AudioManager implements Disposable {
    /** Singleton instance of the audio manager. */
    private static AudioManager instance;
    /**
     * Returns the singleton instance of the audio manager.
     *
     * @return the audio manager instance
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private final ObjectMap<String, Sound> sounds;
    private final ObjectMap<String, Music> musicTracks;
    private final ObjectMap<String, AudioConfig> configs;

    private static class ActiveSound {
        String id;
        long soundId;
        long startTime;

        ActiveSound(String id, long soundId) {
            this.id = id;
            this.soundId = soundId;
            this.startTime = System.currentTimeMillis();
        }
    }

    private final Map<String, ActiveSound> activeSounds;
    private final Map<String, Music> activeMusic;

    private float masterVolume = 1.0f;
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;
    private boolean masterEnabled = true;
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;

    private String currentMusicId;
    private Music currentMusic;
    private AudioConfig currentMusicConfig;

    private AudioManager() {
        sounds = new ObjectMap<>();
        musicTracks = new ObjectMap<>();
        configs = new ObjectMap<>();
        activeSounds = new HashMap<>();
        activeMusic = new HashMap<>();

        initialize();
    }


    private void initialize() {
        for (AudioType type : AudioType.values()) {
            registerAudio(type);
        }

        preloadCoreAudio();
    }


    /**
     * Registers an audio type using its predefined configuration.
     *
     * @param type the audio type
     */
    public void registerAudio(AudioType type) {
        AudioConfig config = type.getConfig();
        configs.put(type.name(), config);
    }
    /**
     * Registers an audio configuration manually.
     *
     * @param id     the audio ID
     * @param config the audio configuration
     */
    public void registerAudio(String id, AudioConfig config) {
        configs.put(id, config);
    }


    private void preloadCoreAudio() {
        for (AudioType type : AudioType.values()) {
            if (type.isMusic()) {
                loadMusic(type.name());
            }
        }

        loadSound(AudioType.UI_CLICK.name());
        loadSound(AudioType.UI_SUCCESS.name());
        loadSound(AudioType.UI_FAILURE.name());
        loadSound(AudioType.PLAYER_MOVE.name());
    }


    private Sound loadSound(String id) {
        if (sounds.containsKey(id)) {
            return sounds.get(id);
        }

        AudioConfig config = configs.get(id);
        if (config == null) {
            Gdx.app.error("AudioManager", "Config not found: " + id);
            return null;
        }

        try {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(config.getFilePath()));
            sounds.put(id, sound);
            return sound;
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Failed to load sound: " + id + " - " + e.getMessage());
            return null;
        }
    }


    private Music loadMusic(String id) {
        if (musicTracks.containsKey(id)) {
            return musicTracks.get(id);
        }

        AudioConfig config = configs.get(id);
        if (config == null || !config.isMusic()) {
            Gdx.app.error("AudioManager", "Music config not found or not music: " + id);
            return null;
        }

        try {
            Music music = Gdx.audio.newMusic(Gdx.files.internal(config.getFilePath()));
            musicTracks.put(id, music);
            return music;
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Failed to load music: " + id + " - " + e.getMessage());
            return null;
        }
    }
    public void warmUpMusic(AudioType type) {
        loadMusic(type.name());   // ⭐ 只加载，不播放
    }
    public void warmUpMusic(String id) {
        loadMusic(id);
    }

    /**
     * Plays an audio resource by ID.
     *
     * @param id the audio ID
     * @return sound instance ID, or {@code -1} if playback failed
     */
    public long play(String id) {
        AudioConfig config = configs.get(id);
        if (config == null) {
            Gdx.app.error("AudioManager", "Audio not registered: " + id);
            return -1;
        }

        if (config.isMusic()) {
            playMusic(id);
            return 0;
        } else {
            return playSound(id);
        }
    }

    /**
     * Plays an audio resource defined by {@link AudioType}.
     *
     * @param type the audio type
     * @return sound instance ID, or {@code -1} if playback failed
     */
    public long play(AudioType type) {
        return play(type.name());
    }

    /**
     * Plays a sound effect.
     *
     * @param id the sound ID
     * @return sound instance ID
     */
    public long playSound(String id) {
        return playSound(id, 1.0f);
    }
    /**
     * Plays a sound effect with a volume multiplier.
     *
     * @param id               the sound ID
     * @param volumeMultiplier volume multiplier
     * @return sound instance ID
     */
    public long playSound(String id, float volumeMultiplier) {
        return playSound(id, volumeMultiplier, 1.0f, 0.0f);
    }
    /**
     * Plays a sound effect with full parameter control.
     *
     * @param id               the sound ID
     * @param volumeMultiplier volume multiplier
     * @param pitch            pitch multiplier
     * @param pan              stereo pan value
     * @return sound instance ID
     */
    public long playSound(String id, float volumeMultiplier, float pitch, float pan) {
        if (!masterEnabled || !sfxEnabled) return -1;

        AudioConfig config = configs.get(id);
        if (config == null || !config.isEnabled()) {
            Gdx.app.debug("AudioManager", "Sound disabled or not found: " + id);
            return -1;
        }


        if (config.isLoop() && activeSounds.containsKey(id)) {
            Sound existingSound = sounds.get(id);
            if (existingSound != null) {
                existingSound.stop(activeSounds.get(id).soundId);
            }
            activeSounds.remove(id);
        }
        Sound sound = loadSound(id);
        if (sound == null) return -1;

        float volume = config.getDefaultVolume() * sfxVolume * masterVolume * volumeMultiplier;
        volume = Math.max(0, Math.min(1, volume));

        config.recordPlay();

        long soundId;
        if (config.isLoop()) {
            soundId = sound.loop(volume);
        } else {
            soundId = sound.play(volume);
        }

        if (pitch != 1.0f) sound.setPitch(soundId, pitch);
        if (pan != 0.0f) sound.setPan(soundId, pan, volume);

        if (config.isLoop()) {
            ActiveSound activeSound = new ActiveSound(id, soundId);
            activeSounds.put(id, activeSound);
        }

        return soundId;
    }

    /**
     * Plays a music track by ID.
     *
     * @param id the music ID
     */
    public void playMusic(String id) {
        playMusic(id, true);
    }
    /**
     * Plays a music track with loop control.
     *
     * @param id   the music ID
     * @param loop whether the music should loop
     */
    public void playMusic(String id, boolean loop) {
        if (!masterEnabled || !musicEnabled) return;

        AudioConfig config = configs.get(id);
        if (config == null || !config.isEnabled() || !config.isMusic()) {
            Gdx.app.error("AudioManager", "Music not found or disabled: " + id);
            return;
        }

        if (id.equals(currentMusicId) && currentMusic != null && currentMusic.isPlaying()) {
            return;
        }

        if (currentMusic != null && !id.equals(currentMusicId)) {
            currentMusic.stop();
        }

        Music music = loadMusic(id);
        if (music == null) return;

        config.recordPlay();

        float volume = config.getDefaultVolume() * musicVolume * masterVolume;
        volume = Math.max(0, Math.min(1, volume));

        music.setVolume(volume);
        music.setLooping(loop);
        music.play();

        currentMusicId = id;
        currentMusic = music;
        currentMusicConfig = config;
        activeMusic.put(id, music);
    }

    /**
     * Plays a music track defined by {@link AudioType}.
     *
     * @param type the music type
     */
    public void playMusic(AudioType type) {
        playMusic(type.name(), type.isLoop());
    }



    public void playPlayerMove() {
        String moveId = AudioType.PLAYER_MOVE.name();

        if (activeSounds.containsKey(moveId)) {
            return;
        }

        playSound(moveId, 1.0f, 1.0f, 0.0f);
    }


    public void stopPlayerMove() {
        stopSound(AudioType.PLAYER_MOVE.name());
    }


    private long lastClickTime = 0;
    private static final long CLICK_COOLDOWN = 50; // 50ms冷却

    public void playUIClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > CLICK_COOLDOWN) {
            playSound(AudioType.UI_CLICK.name(), 1.0f);
            lastClickTime = currentTime;
        }
    }



    public void stopSound(String id) {
        if (sounds.containsKey(id)) {
            sounds.get(id).stop();
            activeSounds.remove(id);
        }
    }


    public void stopSound(AudioType type) {
        stopSound(type.name());
    }


    public void stopSoundInstance(String id, long soundId) {
        if (sounds.containsKey(id)) {
            sounds.get(id).stop(soundId);
            ActiveSound activeSound = activeSounds.get(id);
            if (activeSound != null && activeSound.soundId == soundId) {
                activeSounds.remove(id);
            }
        }
    }


    public void stopAllSounds() {
        for (Sound sound : sounds.values()) {
            sound.stop();
        }
        activeSounds.clear();
    }


    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
            currentMusicId = null;
            currentMusicConfig = null;
        }
    }


    public void stopAllMusic() {
        for (Music music : musicTracks.values()) {
            music.stop();
        }
        activeMusic.clear();
        stopMusic();
    }


    public void stopAll() {
        stopAllSounds();
        stopAllMusic();
    }


    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }


    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying() &&
                masterEnabled && musicEnabled) {
            currentMusic.play();
        }
    }


    public void pauseAll() {
        pauseMusic();
    }


    public void resumeAll() {
        resumeMusic();
    }


    /**
     * Returns the global master volume.
     *
     * @return master volume
     */
    public float getMasterVolume() { return masterVolume; }

    /**
     * Sets the global master volume.
     *
     * @param volume volume in range [0, 1]
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0, Math.min(1, volume));
        updateAllVolumes();
    }

    public float getMusicVolume() { return musicVolume; }
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        updateMusicVolumes();
    }

    public float getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume));
    }

    public boolean isMasterEnabled() { return masterEnabled; }
    public void setMasterEnabled(boolean enabled) {
        this.masterEnabled = enabled;
        if (!enabled) {
            stopAll();
        } else {
            resumeAll();
        }
    }

    public boolean isMusicEnabled() { return musicEnabled; }
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            pauseMusic();
        } else if (masterEnabled) {
            resumeMusic();
        }
    }

    public boolean isSfxEnabled() { return sfxEnabled; }
    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
        if (!enabled) {
            stopAllSounds();
        }
    }


    public void setAudioConfig(String id, AudioConfig config) {
        configs.put(id, config);
    }

    public AudioConfig getAudioConfig(String id) {
        return configs.get(id);
    }

    public AudioConfig getAudioConfig(AudioType type) {
        return configs.get(type.name());
    }



    public void cleanupUnusedAudio() {
        Gdx.app.debug("AudioManager", "Cleaning up unused audio...");
        int unloadedCount = 0;

        com.badlogic.gdx.utils.Array<String> soundKeys = sounds.keys().toArray();
        for (String id : soundKeys) {
            AudioConfig config = configs.get(id);
            if (config != null && !config.isPersistent() && !config.isActive()) {
                if (!activeSounds.containsKey(id)) {
                    Sound sound = sounds.remove(id);
                    if (sound != null) {
                        sound.dispose();
                        unloadedCount++;
                        Gdx.app.debug("AudioManager", "Unloaded sound: " + id);
                    }
                }
            }
        }

        com.badlogic.gdx.utils.Array<String> musicKeys = musicTracks.keys().toArray();
        for (String id : musicKeys) {
            if (!id.equals(currentMusicId)) {
                AudioConfig config = configs.get(id);
                if (config != null && !config.isPersistent() && !config.isActive()) {
                    Music music = musicTracks.remove(id);
                    if (music != null) {
                        music.dispose();
                        unloadedCount++;
                        Gdx.app.debug("AudioManager", "Unloaded music: " + id);
                    }
                }
            }
        }

        Gdx.app.debug("AudioManager", "Cleaned up " + unloadedCount + " unused audio resources");
    }


    public String getMemoryStats() {
        int soundCount = sounds.size;
        int musicCount = musicTracks.size;
        int activeSoundCount = activeSounds.size();
        int activeMusicCount = activeMusic.size();

        return String.format(
                "Audio Memory Stats: Sounds=%d, Music=%d, ActiveSounds=%d, ActiveMusic=%d",
                soundCount, musicCount, activeSoundCount, activeMusicCount
        );
    }


    public boolean isPlaying(String id) {
        AudioConfig config = configs.get(id);
        if (config == null) return false;

        if (config.isMusic()) {
            return id.equals(currentMusicId) && currentMusic != null && currentMusic.isPlaying();
        } else {
            return activeSounds.containsKey(id);
        }
    }

    public boolean isPlaying(AudioType type) {
        return isPlaying(type.name());
    }
    /**
     * Returns whether any music is currently playing.
     *
     * @return {@code true} if music is playing
     */
    public boolean isMusicPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }
    /**
     * Returns the ID of the currently playing music.
     *
     * @return music ID, or {@code null} if none
     */
    public String getCurrentMusicId() {
        return currentMusicId;
    }

    public AudioType getCurrentMusicType() {
        if (currentMusicId == null) return null;
        try {
            return AudioType.valueOf(currentMusicId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    private void updateMusicVolumes() {
        for (Music music : musicTracks.values()) {
            String musicId = musicTracks.findKey(music, false);
            if (musicId != null) {
                AudioConfig config = configs.get(musicId);
                if (config != null) {
                    float volume = config.getDefaultVolume() * musicVolume * masterVolume;
                    volume = Math.max(0, Math.min(1, volume));
                    music.setVolume(volume);
                }
            }
        }
    }

    private void updateAllVolumes() {
        updateMusicVolumes();
    }
    /**
     * Disposes all audio resources managed by this audio manager.
     */
    @Override
    public void dispose() {
        Gdx.app.debug("AudioManager", "Disposing AudioManager...");

        stopAll();

        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();

        for (Music music : musicTracks.values()) {
            music.dispose();
        }
        musicTracks.clear();

        activeSounds.clear();
        activeMusic.clear();
        configs.clear();

        currentMusic = null;
        currentMusicId = null;
        currentMusicConfig = null;

        instance = null;

        Gdx.app.debug("AudioManager", "AudioManager disposed");
    }
}