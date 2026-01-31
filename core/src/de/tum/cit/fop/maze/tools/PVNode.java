package de.tum.cit.fop.maze.tools;

import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.screen.IntroScreen;


/**
 * Represents a single PV (preview / story) node.
 *
 * <p>A PV node defines one step in a story sequence, including:
 * <ul>
 *     <li>the texture atlas used for rendering</li>
 *     <li>the animation region name</li>
 *     <li>the associated audio cue</li>
 *     <li>the exit action after the PV finishes</li>
 * </ul>
 *
 * <p>This record is immutable and intended to be used as a lightweight
 * data holder for story playback logic.
 *
 * @param atlasPath  the asset path of the texture atlas
 * @param regionName the region name used to retrieve animation frames
 * @param audio      the audio cue played during this PV node
 * @param exit       the exit behavior after the PV node ends
 */
public record PVNode(
        String atlasPath,
        String regionName,
        AudioType audio,
        IntroScreen.PVExit exit
) {
}
