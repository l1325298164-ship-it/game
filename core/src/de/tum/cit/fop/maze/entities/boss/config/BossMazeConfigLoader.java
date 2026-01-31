package de.tum.cit.fop.maze.entities.boss.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
/**
 * Utility class for loading {@link BossMazeConfig} from JSON files.
 */
public class BossMazeConfigLoader {

    /**
     * Loads a single boss maze configuration from the given internal file path.
     * <p>
     * The configuration is deserialized from JSON and phase indices
     * are assigned sequentially.
     *
     * @param path internal file path to the boss configuration JSON
     * @return the loaded {@link BossMazeConfig}
     */
    public static BossMazeConfig loadOne(String path) {
        Json json = new Json();

        BossMazeConfig config = json.fromJson(
                BossMazeConfig.class,
                Gdx.files.internal(path)
        );

        for (int i = 0; i <3; i++) {
            config.phases.get(i).index = i;
        }

        return config;
    }

}
