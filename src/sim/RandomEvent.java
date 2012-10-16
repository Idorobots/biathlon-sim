package sim;

import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.DiscreteDistUniform;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.TimeSpan;

/**
 * The <code>RandomEvent</code> enum represents a set of event which can happen
 * to a competitor and which affect his/her performance.
 * 
 * @author Pawel Kleczek
 * @version 0.1
 * @since 16-10-2012
 * 
 */
public enum RandomEvent {
    // TODO: tewak me!
    FALL(new ContDistNormal(Biathlon.getInstance(), "Fall down (likehood)", 0.05, 0.03, false, false),
            new DiscreteDistUniform(Biathlon.getInstance(), "Fall down (duration)", 4, 10, false, false),
            new DiscreteDistUniform(Biathlon.getInstance(), "Fall down (desperation)", 3, 5, false, false)), SLIP(
            new ContDistNormal(Biathlon.getInstance(), "Slip (likehood)", 0.15, 0.05, false, false),
            new DiscreteDistUniform(Biathlon.getInstance(), "Slip (duration)", 1, 2, false, false),
            new DiscreteDistUniform(Biathlon.getInstance(), "Slip (desperation)", 3, 5, false, false)), PROBLEM_WITH_SKIS(
            new ContDistNormal(Biathlon.getInstance(), "Problem with skis (likehood)", 0.05, 0.03, false, false),
            new DiscreteDistUniform(Biathlon.getInstance(), "Problem with skis (duration)", 7, 20, false, false),
            new DiscreteDistUniform(Biathlon.getInstance(), "Problem with skis (desperation)", 3, 5, false, false));

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
        // TODO : tweak me!
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
