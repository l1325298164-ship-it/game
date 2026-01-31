package de.tum.cit.fop.maze.entities.boss.config;

import de.tum.cit.fop.maze.entities.boss.BossFightScreen;
/**
 * Executes a {@link BossTimeline} by triggering events
 * when their scheduled time is reached.
 * <p>
 * Each event is executed once and delegated to the
 * {@link BossFightScreen} for handling.
 */
public class BossTimelineRunner {

    private final BossTimeline timeline;
    /**
     * Creates a runner for the given boss timeline.
     *
     * @param timeline timeline to execute
     */
    public BossTimelineRunner(BossTimeline timeline) {
        this.timeline = timeline;
    }

    /**
     * Updates the timeline and triggers pending events.
     * <p>
     * This method should be called every frame with the current
     * elapsed time of the boss fight.
     *
     * @param time   elapsed boss fight time in seconds
     * @param screen active boss fight screen
     */
    public void update(float time, BossFightScreen screen) {
        for (BossTimelineEvent e : timeline.events) {
            if (!e.triggered && time >= e.time) {
                e.triggered = true;
                execute(e, screen);
            }
        }
    }
    /**
     * Executes a single timeline event by dispatching it
     * to the boss fight screen.
     *
     * @param e event to execute
     * @param s boss fight screen instance
     */
    private void execute(BossTimelineEvent e, BossFightScreen s) {
        switch (e.type) {

            case "RAGE_CHECK" -> {
                s.enterRageCheck();
            }

            case "LOCK_HP" -> {
                s.handleHpThreshold(e.threshold, null);
            }

            case "GLOBAL_AOE" -> {
                s.startGlobalAoe(
                        e.duration,
                        e.tickInterval,
                        e.damage
                );
            }

            case "LOCK_FINAL_HP" -> {
                s.lockFinalHp(e.threshold);
            }

            case "CUP_SHAKE" -> {
                s.startCupShake(
                        e.duration != null ? e.duration : 0f,
                        e.xAmp != null ? e.xAmp : 0f,
                        e.yAmp != null ? e.yAmp : 0f,
                        e.xFreq != null ? e.xFreq : 1f,
                        e.yFreq != null ? e.yFreq : 1f
                );
            }

            case "DIALOGUE" -> {
                s.playBossDialogue(e.speaker, e.text, e.voice);
            }

            case "TIMELINE_END" -> {
                s.markTimelineFinished();
            }

            default -> {
            }
        }
    }


}
