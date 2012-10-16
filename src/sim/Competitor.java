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

    private double distanceToCover; // / Distance left to cover.
    private int shootingsLeft; // / Number of shooting range visits before
                               // finishing the run.
    private ContDistNormal speed; // / Competitor speed modeled by a gaussian.
    private float speedFactor = 1.0f; // / Speed factor that models competitors
                                      // effort in the run.
    private ContDistNormal aimingTime; // / Competitor aiming time modeled by a
                                       // gaussian.
    private float aimingFactor = 1.0f; // / Aiming time factor that models
                                       // competitors effort in the run.
    private ContDistNormal accuracy; // / Competitor accuracy modeled by a
                                     // gaussian.
    private float accuracyFactor = 1.0f; // / Accuracy factor that models
                                         // competitors effort in the run.
    private ContDistUniform desperation; // / Random desperation experienced at
                                         // any time.
    private int currentDesperation = 0; // / Competitor desperation affecting
                                        // his performance.
    private boolean panic = false; // / Flag determining wether a competitor is
                                   // rushing to finish.


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


    public void lifeCycle() {
        logger.log("Starts the competition!");

        while (distanceToCover > 0) {
            hold(new TimeSpan(Biathlon.STEP_TIME));
            run();
            shoot();
        }

        logger.log("Finishes the competition!");
        // logger.close();
    }


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


    private void run() {
        // TODO : jakieś magiczne symulacje

        double v = computeSpeed() * Biathlon.STEP_TIME;

        double dist = Helpers.clamp(v, Biathlon.MIN_SPEED, Biathlon.MAX_SPEED);

        distanceToCover -= dist;

        // Models linear change in these following parameters.
        // TODO Needs a little tweaking.
        speedFactor += Biathlon.SPEED_FACTOR_DELTA;
        accuracyFactor += Biathlon.ACCURACY_FACTOR_DELTA;
        aimingFactor += Biathlon.SHOOTING_TIME_FACTOR_DELTA;

        // Check for random events.
        double totalTimePenalty = 0.0;
        for (RandomEvent e : RandomEvent.values()) {
            if (e.hasHappened(0.0)) {
                TimeSpan duration = e.getDuration();
                int desperationMod = e.getDesperationMod();

                totalTimePenalty += duration.getTimeAsDouble();
                currentDesperation += desperationMod;

                // TODO : do better formatting
                logger.log(String.format("Random event (%s) happened (duration : %s ; desperation : +%d).",
                        e.toString(), duration.toString(), desperationMod));
            }
        }
        hold(new TimeSpan(totalTimePenalty));
    }


    public void addPenalties(int missed) {
        double penalty = missed * Biathlon.PENALTY_DISTANCE;

        logger.log(String.format("Receives %.0f m penalty distance.", penalty));

        if (missed != 0) {
            // Add a little stress, what could possibly go wrong!?
            currentDesperation = Helpers.clamp(currentDesperation + missed * Biathlon.DESPERATION_DELTA_PER_MISS, 0,
                    100);

            logger.log(String.format("Desperation increases to %d%%.", currentDesperation));

            if (computeDesperation() >= Biathlon.PANIC_THRESHOLD) {
                logger.log("Desperation increases past the panic treshold.");
                logger.log("Competitor starts rushing.");

                panic = true;
            }

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


    public int computeShotsMissed() {
        int sps = Biathlon.SHOTS_PER_SHOOTING;

        float acc = (float) computeAccuracy(); // Haters gonna hate...

        int missed = Math.round(Helpers.clamp(sps - (acc * sps), 0.0f, sps));

        logger.log(String.format("Missed %d times.", missed));
        return missed;
    }


    public TimeSpan computeShootingTime() {
        double at = aimingTime.sample() * aimingFactor;

        if (panic) {
            at *= Biathlon.PANIC_GAIN_MODIFIER; // Rushing...
        }

        return new TimeSpan(at);
    }


    public double computeSpeed() {
        double v = speed.sample() * speedFactor;

        if (panic) {
            v *= Biathlon.PANIC_GAIN_MODIFIER; // Rushing to the finish.
        }

        return v;
    }


    public double computeAccuracy() {
        double acc = accuracy.sample() * accuracyFactor;

        if (panic) {
            acc *= Biathlon.PANIC_LOSS_MODIFIER;
        }

        return acc;
    }


    public int computeDesperation() {
        return currentDesperation + (int) Math.round(desperation.sample());
    }


    public String toString() {
        return String.format("Competitor_%d", ID);
    }
}
