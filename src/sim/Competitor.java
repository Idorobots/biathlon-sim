package sim;

import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

public class Competitor extends SimProcess {

    private Biathlon myModel;
    private Logger logger;
    private int ID = -1;

    /**
     * Distance left to cover.
     */
    private double distanceToCover;

    /**
     * Number of shooting range visits before finishing the run.
     */
    private int shootingsLeft;

    /**
     * Competitor speed modeled by a gaussian.
     */
    private ContDistNormal speed;

    /**
     * Speed factor that models competitors effort in the run.
     */
    private float speedFactor = 1.0f;

    /**
     * Competitor aiming time modeled by a gaussian.
     */
    private ContDistNormal aimingTime;

    /**
     * Aiming time factor that models competitors effort in the run.
     */
    private float aimingFactor = 1.0f;

    /**
     * Competitor accuracy modeled by a gaussian.
     */
    private ContDistNormal accuracy;

    /**
     * Accuracy factor that models competitors effort in the run.
     */
    private float accuracyFactor = 1.0f;

    /**
     * Competitor desperation modeled by uniformly distributed random variable
     * (and miss/shot ratio). Determines wether a competitor starts hauling ass.
     */
    private ContDistUniform desperation; // TODO Actually use this :D
    private int currentDesperation = 0;


    public Competitor(Model owner, String name, boolean showInTrace, int id) {
        super(owner, name, showInTrace);
        myModel = (Biathlon) owner;
        logger = new Logger(String.format("competitor_%02d.txt", id));
        ID = id;

        distanceToCover = Biathlon.INITIAL_DISTANCE;
        shootingsLeft = Biathlon.NUM_SHOOTING_RANGES;

        speed = new ContDistNormal(myModel, "Speed", Biathlon.AVERAGE_SPEED,
                (Biathlon.MAX_SPEED - Biathlon.MIN_SPEED) / 2, true, false);
        aimingTime = new ContDistNormal(myModel, "Aiming", Biathlon.AVERAGE_SHOOTING_TIME,
                (Biathlon.MAX_SHOOTING_TIME - Biathlon.MIN_SHOOTING_TIME) / 2, true, false);

        accuracy = new ContDistNormal(myModel, "Accuracy", Biathlon.AVERAGE_ACCURACY,
                (Biathlon.MAX_ACCURACY - Biathlon.MIN_ACCURACY) / 2, true, false);
        desperation = new ContDistUniform(myModel, "Desperation", Biathlon.MIN_DESPERATION, Biathlon.MAX_DESPERATION,
                true, false);

        aimingTime.setNonNegative(true);
        accuracy.setNonNegative(true);
        speed.setNonNegative(true);
    }


    public void lifeCycle() {
        while (distanceToCover > 0) {
            hold(new TimeSpan(Biathlon.COMPETITOR_STEP_TIME));
            run();
            doShooting();
        }

        logger.log("Finishes the competition!");
        // logger.close();
    }

    private void doShooting() {
        // strzelnica co 1/n dystansu (n = liczba strzelań)
        double nextShootingDist = (shootingsLeft + 1) * Biathlon.INITIAL_DISTANCE
                / (Biathlon.NUM_SHOOTING_RANGES + 2);

        if (shootingsLeft > 0 && distanceToCover < nextShootingDist) {
            --shootingsLeft;
            myModel.competitorsQueue.insert(this);

            if (!myModel.shootingRangeQueue.isEmpty()) {
                ShootingRange shootingRange = myModel.shootingRangeQueue.first();
                myModel.shootingRangeQueue.remove(shootingRange);
                shootingRange.activateAfter(this);

                logger.log(String.format("Enters the shooting range for the %dth time.",
                       Biathlon.NUM_SHOOTING_RANGES - shootingsLeft));
                passivate(); // Simulates the actual shooting.
                logger.log("Leaves the shooting range.");
            }
        }
    }

    private void run() {
        // TODO : jakieś magiczne symulacje

        double v = speed.sample() * speedFactor;

        double dist = Helpers.clamp(v, Biathlon.MIN_SPEED, Biathlon.MAX_SPEED);

        distanceToCover -= dist;

        // Models linear change in these following parameters.
        speedFactor *= Biathlon.SPEED_LOSS_FACTOR;
        accuracyFactor *= Biathlon.ACCURACY_LOSS_FACTOR;
        aimingFactor *= Biathlon.SHOOTING_TIME_GAIN_FACTOR;
    }


    public void addPenalties(int missed) {
        double penalty = missed * Biathlon.PENALTY_DISTANCE;

        logger.log(String.format("Receives %.2f km penalty distance.", penalty));

        if (missed != 0) {
            // Add a little stress, what could possibly go wrong!?
            int desperationGain = missed * Biathlon.DESPERATION_GAIN_PER_MISS;
            currentDesperation = Helpers.clamp(currentDesperation + desperationGain, Biathlon.MIN_DESPERATION,
                    Biathlon.MAX_DESPERATION);

            logger.log(String.format("Desperation increases to %d%%.", currentDesperation));

            if (currentDesperation >= Biathlon.DESPERATION_THRESHOLD) {
                logger.log("Desperation increases past the panic treshold.");
                logger.log("Competitor starts rushing.");

                speedFactor *= Biathlon.PANIC_GAIN_MODIFIER;
                accuracyFactor *= Biathlon.PANIC_LOSS_MODIFIER;
                aimingFactor *= Biathlon.PANIC_GAIN_MODIFIER;
            }
        }

        // Competitor gets his score and acts accordingly.
        speedFactor *= 1.0f - missed * Biathlon.SPEED_LOSS_PER_MISS;
        aimingFactor *= 1.0f + missed * Biathlon.SHOOTING_TIME_GAIN_PER_MISS;
        accuracyFactor *= 1.0f + missed * Biathlon.ACCURACY_GAIN_PER_MISS;

        logger.log(String.format("Speed factor changes to %.2f.", speedFactor));
        logger.log(String.format("Aiming time factor changes to %.2f.", aimingFactor));
        logger.log(String.format("Accuracy factor changes to %.2f.", accuracyFactor));

        distanceToCover += penalty;
    }


    public int computeShotsMissed(ShootingRange shootingRange) {
        // TODO : jakieś magiczne symulacje

        int sps = Biathlon.SHOTS_PER_SHOOTING;
        int missed = Math.round(Helpers.clamp(sps - (accuracy.sample().floatValue() * sps * accuracyFactor), 0.0f,
                Biathlon.SHOTS_PER_SHOOTING));

        logger.log(String.format("Missed %d times.", missed));
        return missed;
    }


    public TimeSpan computeShootingTime() {
        // TODO : jakieś magiczne symulacje

        return new TimeSpan(aimingTime.sample() * aimingFactor);
    }


    public String toString() {
        return String.format("Competitor_%d", ID);
    }
}
