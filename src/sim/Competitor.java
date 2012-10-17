package sim;

import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

/**
 * The core of the simulation.
 *
 * Models the biathlete process - running, shooting at the <code>ShootingRange</code>,
 * tiredness (attribute change over time), stress (randomized panic level) and various
 * random events.
 */
public class Competitor extends SimProcess {

    private Biathlon myModel;
    private Logger logger;
    private int ID = -1;

    /**
     * The distance left to cover given in meters (1.0 == 1 meter).
     */
    private double distanceToCover;

    /**
     * The number of shooting sessions left before finishing the run.
     */
    private int shootingsLeft;

    /**
     * Normal distribution of this biathletes speed.
     * Parameterized with <code>Biathlon.SPEED_MEAN</code> and
     * <code>Biathlon.SPEED_STD_DEV</code>.
     */
    private ContDistNormal speed;

    /**
     * Speed modifier used by various models (e.g. tiredness of a competitor).
     * It modifies the values sampled from <code>speed</code>.
     */
    private float speedFactor = 1.0f;

    /**
     * Aiming time normal distribution.
     * Parameterize with <code>Biathlon.SHOOTING_TIME_MEAN</code> and
     * <code>Biathlon.SHOOTING_TIME_STD_DEV</code>.
     */
    private ContDistNormal aimingTime;

    /**
     * Aiming time modifier used by various models.
     * It modifies the values sampled from <code>aimingTime</code>.
     */
    private float aimingFactor = 1.0f;

    /**
     * The accuracy normal distribution.
     * Parameterized by <code>Biathlon.ACCURACY_MEAN</code> and
     * <code>Biathlon.ACCURACY_STD_DEV</code>.
     */
    private ContDistNormal accuracy;

    /**
     * Accuracy modifier used by various models.
     * It modifies the values sampled from <code>accuracy</code>.
     */
    private float accuracyFactor = 1.0f;

    /**
     * A uniformly distributed random stress level.
     * Used by the stress model to add additional, random noise to the simulation.
     * Parameterized by <code>Biathlon.MIN_DESPERATION</code> and
     * <code>Biathlon.MAX_DESPERATION</code>.
     */
    private ContDistUniform desperation;

    /**
     * The current stress level of a competitor, used by the stress model.
     *
     * Once it reaches the <code>Biathlon.PANIC_THRESHOLD</code> the competitor
     * will start rushing to the finish line.
     * The changes to this parameter happen over time caused by random events
     * and shot misses.
     */
    private int currentDesperation = 0;

    /**
     * Flag determining wether a competitors <code>currentDesperation</code>
     * reached the <code>Biathlon.PANIC_THRESHOLD</code>.
     */
    private boolean panic = false;

    /**
     * The c-tor.
     *
     * @param owner The model owning this process.
     * @param name The name of this proccess.
     * @param showInTrace A flag toggling tracing in this process.
     * @param id The identification number of this competitor.
     */
    public Competitor(Model owner, String name, boolean showInTrace, int id) {
        super(owner, name, showInTrace);
        myModel = (Biathlon) owner;
        logger = new Logger(String.format("competitor_%02d.txt", id));
        ID = id;

        distanceToCover = Biathlon.INITIAL_DISTANCE;
        shootingsLeft = Biathlon.NUM_SHOOTING_RANGES;

        speed = new ContDistNormal(myModel, "Speed", Biathlon.SPEED_MEAN, Biathlon.SPEED_STD_DEV, true, false);

        aimingTime = new ContDistNormal(myModel, "Aiming", Biathlon.SHOOTING_TIME_MEAN, Biathlon.SHOOTING_TIME_STD_DEV,
                true, false);

        accuracy = new ContDistNormal(myModel, "Accuracy", Biathlon.ACCURACY_MEAN, Biathlon.ACCURACY_STD_DEV, true,
                false);

        desperation = new ContDistUniform(myModel, "Desperation", Biathlon.MIN_DESPERATION, Biathlon.MAX_DESPERATION,
                true, false);

        aimingTime.setNonNegative(true);
        accuracy.setNonNegative(true);
        speed.setNonNegative(true);
    }

    /**
     * Implements the frame life cycle of this process.
     */
    public void lifeCycle() {
        logger.log("Starts the competition!");

        while (distanceToCover > 0) {
            hold(new TimeSpan(Biathlon.STEP_TIME));
            shoot();
            run();
        }

        logger.log("Finishes the competition!");
        Results.getInstance().registerFinish(ID);
    }

    /**
     * Implements the shooting sessions performed by the biathlete.
     */
    private void shoot() {
        // strzelnica co 1/n dystansu (n = liczba strzelań)
        double nextShootingDist = (shootingsLeft + 1) * Biathlon.INITIAL_DISTANCE / (Biathlon.NUM_SHOOTING_RANGES + 2);

        if (shootingsLeft > 0 && distanceToCover < nextShootingDist) {
            --shootingsLeft;
            myModel.competitorsQueue.insert(this);

            if (!myModel.shootingRangeQueue.isEmpty()) {
                ShootingRange shootingRange = myModel.shootingRangeQueue.first();
                myModel.shootingRangeQueue.remove(shootingRange);
                shootingRange.activateAfter(this);

                logger.log(String.format("Enters the shooting range for the %dth time.", Biathlon.NUM_SHOOTING_RANGES
                        - shootingsLeft));

                passivate(); // Simulates the actual shooting.

                logger.log("Leaves the shooting range.");
            }
        }
    }

    /**
     * Implements the running performed by the biathlete.
     * Contains the tiredness, stress and random event simulation.
     */
    private void run() {
        double v = computeSpeed() * Biathlon.STEP_TIME;

        double dist = Helpers.clamp(v, Biathlon.MIN_SPEED, Biathlon.MAX_SPEED);

        distanceToCover -= dist;

        // Models linear change in these following parameters.
        speedFactor += Biathlon.SPEED_FACTOR_DELTA;
        accuracyFactor += Biathlon.ACCURACY_FACTOR_DELTA;
        aimingFactor += Biathlon.SHOOTING_TIME_FACTOR_DELTA;

        // Check for random events.
        double totalTimePenalty = 0.0;
        for (RandomEvent e : RandomEvent.values()) {
            // TODO : Tweak me! [hasHappened() modifier; optional]
            if (e.hasHappened(0.0)) {
                TimeSpan duration = e.getDuration();
                int desperationMod = e.getDesperationMod();

                totalTimePenalty += duration.getTimeAsDouble();
                currentDesperation += desperationMod;

                logger.log(String.format("Random event occurs - %s. [t= %s , desp +%d].",
                        e.toString(), duration.toString(), desperationMod));

                logger.log(String.format("Desperation increases to %d%%.", currentDesperation));
            }
        }

        if (!panic && computeDesperation() >= Biathlon.PANIC_THRESHOLD) {
            logger.log("Desperation increases past the panic treshold.");
            logger.log("Competitor starts rushing.");

            panic = true;
        }

        hold(new TimeSpan(totalTimePenalty));
    }

    /**
     * Adds penalties related to the number of missed shots, such as penalty distance
     * and additional stress.
     *
     * @param missed The number of shots missed in a shooting session.
     */
    public void addPenalties(int missed) {
        double penalty = missed * Biathlon.PENALTY_DISTANCE;

        logger.log(String.format("Receives %.0f m penalty distance.", penalty));

        if (missed != 0) {
            // Add a little stress, what could possibly go wrong!?
            currentDesperation = Helpers.clamp(currentDesperation + missed * Biathlon.DESPERATION_DELTA_PER_MISS, 0,
                    100);

            logger.log(String.format("Desperation increases to %d%%.", currentDesperation));

            // Competitor gets his score and acts accordingly.
            speedFactor += missed * Biathlon.SPEED_DELTA_PER_MISS;
            aimingFactor += missed * Biathlon.SHOOTING_TIME_DELTA_PER_MISS;
            accuracyFactor += missed * Biathlon.ACCURACY_DELTA_PER_MISS;
        }

        logger.log(String.format("Speed factor is %.2f.", speedFactor));
        logger.log(String.format("Aiming time factor is %.2f.", aimingFactor));
        logger.log(String.format("Accuracy factor is %.2f.", accuracyFactor));

        distanceToCover += penalty;
    }


    /**
     * Computes the number of missed shots in a shooting session.
     * Uses <code>accuracy</code> model.
     *
     * @return The number of missed shots in a shooting session.
     */
    public int computeShotsMissed() {
        int sps = Biathlon.SHOTS_PER_SHOOTING;

        float acc = (float) computeAccuracy(); // Haters gonna hate...

        int missed = Math.round(Helpers.clamp(sps - (acc * sps), 0.0f, sps));

        logger.log(String.format("Missed %d times.", missed));
        Results.getInstance().registerMisses(ID, missed);

        return missed;
    }

    /**
     * Computes the time spent on a shooting range modified by all the relevant models.
     * Uses <code>aimingTime</code> model.
     *
     * @return The <code>TimeSpan</code> spent shooting.
     */
    public TimeSpan computeShootingTime() {
        double at = aimingTime.sample() * aimingFactor;

        if (panic) {
            at *= Biathlon.PANIC_GAIN_MODIFIER; // Rushing...
        }

        return new TimeSpan(at);
    }

    /**
     * Computes the speed of a competitor modified by all the relevant models.
     * Uses <code>speed</code>.
     *
     * @return The instantaneus speed of the competitor.
     */
    public double computeSpeed() {
        double v = speed.sample() * speedFactor;

        if (panic) {
            v *= Biathlon.PANIC_GAIN_MODIFIER; // Rushing to the finish.
        }

        return v;
    }

    /**
     * Computes the accuracy of a competitor modified by all the relevant models.
     * Uses <code>accuracy</code>.
     *
     * @return The value of accuracy (in range [0, 1]) of the competitor.
     */
    public double computeAccuracy() {
        double acc = accuracy.sample() * accuracyFactor;

        if (panic) {
            acc *= Biathlon.PANIC_LOSS_MODIFIER;
        }

        return acc;
    }

    /**
     * Computes the stress level of a competitor modifed by all the relevant models.
     * Uses <code>desperation</code>.
     *
     * @return Current stress level of the competitor.
     */
    public int computeDesperation() {
        return currentDesperation + (int) Math.round(desperation.sample());
    }

    /**
     * Returns the <code>String</code> representation of thys competitor.
     *
     * @return A string identifing this competitor.
     */
    public String toString() {
        return String.format("Competitor #%d", ID+1);
    }
}
