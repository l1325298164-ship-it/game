package de.tum.cit.fop.maze.entities.boss.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
/**
 * Utility class for loading {@link BossTimeline} instances from JSON files.
 */
public class BossTimelineLoader {
    /**
     * Loads a boss timeline from the given internal file path.
     * <p>
     * The timeline length is automatically derived from the latest
     * event time and extended by a small margin.
     *
     * @param path internal file path to the timeline JSON
     * @return the loaded {@link BossTimeline}
     */
    public static BossTimeline load(String path) {
        Json json = new Json();

        BossTimeline timeline = json.fromJson(
                BossTimeline.class,
                Gdx.files.internal(path)
        );

        float maxTime = 0f;

        if (timeline.events != null) {
            for (BossTimelineEvent e : timeline.events) {
                if (e.time > maxTime) {
                    maxTime = e.time;
                }
            }
        }

        timeline.length = maxTime + 1.0f;

        return timeline;
    }
}
