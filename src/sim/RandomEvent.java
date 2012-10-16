package sim;

import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.DiscreteDistUniform;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.TimeSpan;

/**
 * The <code>RandomEvent</code> enum represents a set of event which can happen
 * to a competitor and which affect his/her performance.
 * <p>
 * The following random events may happen:
 * <ul>
 * <li><b>fall</b> : A competitor lost his balance and fell. It happens
 * relatively seldom but greatly increases desperation.</li>
 * <li><b>slip</b> : The track was a bit slippy and a competitor's ski went
 * apart. It happens quite often and is regarded as something normal (thus
 * results in minimal increase of competitor's desperation level).</li>
 * <li><b>problem with skis</b> : A binding malfunctioned. It takes a while
 * before a competitor repairs it and ensures that everything is again in good
 * working order.</li>
 * </ul>
 * 
 * @author Pawel Kleczek
 * @version 0.1
 * @since 16-10-2012
 * 
 */
public enum RandomEvent {
    // Estimated number of falls (per run) : 1x
    // Estimated number of problems with skis (per run) : 2x
    // Estimated number of slips: every minute
    FALL(new ContDistNormal(Biathlon.getInstance(), "Fall down (likehood)", 0.8 / Biathlon.estimateDuration(),
            1.0 / Biathlon.estimateDuration() * 0.1, false, false), new DiscreteDistUniform(Biathlon.getInstance(),
            "Fall down (duration)", 4, 8, false, false), new DiscreteDistUniform(Biathlon.getInstance(),
            "Fall down (desperation)", 5, 7, false, false)), SLIP(new ContDistNormal(Biathlon.getInstance(),
            "Slip (likehood)", 40.0 / Biathlon.estimateDuration(), 60.0 / Biathlon.estimateDuration() * 0.25, false,
            false), new DiscreteDistUniform(Biathlon.getInstance(), "Slip (duration)", 1, 2, false, false),
            new DiscreteDistUniform(Biathlon.getInstance(), "Slip (desperation)", 0, 1, false, false)), PROBLEM_WITH_SKIS(
            new ContDistNormal(Biathlon.getInstance(), "Problem with skis (likehood)",
                    1.5 / Biathlon.estimateDuration(), 2.0 / Biathlon.estimateDuration() * 0.08, false, false),
            new DiscreteDistUniform(Biathlon.getInstance(), "Problem with skis (duration)", 7, 15, false, false),
            new DiscreteDistUniform(Biathlon.getInstance(), "Problem with skis (desperation)", 3, 6, false, false));

    /**
     * Models likehood of occurence of an event.
     */
    private final NumericalDist<Double> likehood;

    /**
     * Models duration of an event (in seconds).
     */
    private final NumericalDist<Long> duration;

    /**
     * Shows how an event affects competotor's desperation.
     */
    private final NumericalDist<Long> desperationMod;


    private RandomEvent(NumericalDist<Double> likehood, NumericalDist<Long> duration, NumericalDist<Long> desperationMod) {
        this.likehood = likehood;
        this.duration = duration;
        this.desperationMod = desperationMod;
    }


    /**
     * Determines whether an event has happened.
     * 
     * @param modifier
     *            Affects likehood of event's occurence. The higher its value
     *            the more likely an event occures.
     * @return <code>true</code> if an event occured, otherwise
     *         <code>false</code>
     */
    public boolean hasHappened(double modifier) {
        return (likehood.sample() + modifier > Helpers.rand());
    }


    /**
     * Calculates the duration of an event.
     * 
     * @return Event's duration.
     */
    public TimeSpan getDuration() {
        return duration.sampleTimeSpan();
    }


    /**
     * Calculates how an event could affect competitor's psyche.
     * 
     * @return Change in a desperation level of a competitor.
     */
    public int getDesperationMod() {
        return desperationMod.sample().intValue();
    }


    @Override
    public String toString() {
        return this.name().replace('_', ' ').toLowerCase();
    }

}
