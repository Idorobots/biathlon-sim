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
	 * Competitor desperation modeled by uniformly distributed random variable (and miss/shot ratio).
     * Determines wether a competitor starts hauling ass.
	 */
    private ContDistUniform desperation; // TODO Actually use this :D
    private int currentDesperation = 0;

	public Competitor(Model owner, String name, boolean showInTrace, int id) {
		super(owner, name, showInTrace);
		myModel = (Biathlon) owner;
		logger = new Logger(String.format("competitor_%d.txt", id));
        ID = id;

		distanceToCover = Biathlon.INITIAL_DISTANCE;
		shootingsLeft = Biathlon.NUM_SHOOTING_RANGES;

		speed = new ContDistNormal(myModel, "Speed",
                                            Biathlon.AVERAGE_SPEED,
                                            (Biathlon.MAX_SPEED - Biathlon.MIN_SPEED)/2,
                                            true, false);
		aimingTime = new ContDistNormal(myModel, "Aiming",
                                                 Biathlon.AVERAGE_SHOOTING_TIME,
                                                 (Biathlon.MAX_SHOOTING_TIME-Biathlon.MIN_SHOOTING_TIME)/2,
                                                 true, false);

		accuracy = new ContDistNormal(myModel, "Accuracy",
                                               Biathlon.AVERAGE_ACCURACY,
                                               (Biathlon.MAX_ACCURACY-Biathlon.MIN_ACCURACY)/2,
                                               true, false);
        desperation = new ContDistUniform(myModel, "Desperation",
                                                   Biathlon.MIN_DESPERATION,
                                                   Biathlon.MAX_DESPERATION,
                                                   true, false);

        aimingTime.setNonNegative(true);
        accuracy.setNonNegative(true);
		speed.setNonNegative(true);
	}


	public void lifeCycle() {
		while (distanceToCover > 0) {
			hold(new TimeSpan(Biathlon.COMPETITOR_STEP_TIME));
			run();

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

    				log(String.format("Enters the shooting range for the %dth time.",
                                      Biathlon.NUM_SHOOTING_RANGES - shootingsLeft));
   				passivate(); // Simulates the actual shooting.
    				log("Leaves the shooting range.");

				}
			}
		}

		log("Finishes the competition!");
//		logger.close();
	}

	private void log(String message) {
		String msgWithTime = String.format("[ %8s ]\t%s", this.presentTime().toString(), message);
		logger.log(msgWithTime);
	}

	private void run() {
		// TODO : jakieś magiczne symulacje

        double v = speed.sample() * speedFactor;

		double dist = Helpers.clamp(v, Biathlon.MIN_SPEED, Biathlon.MAX_SPEED);

		distanceToCover -= dist;

        // Models linear change in these following parameters.
        // TODO Move these to Biathlon class.
        speedFactor *= 0.98f;
        accuracyFactor *= 0.98f;
        aimingFactor *= 1.02f;
	}


	public void addPenalties(int missed) {
        double penalty = missed * Biathlon.PENALTY_DISTANCE;

        log(String.format("Receives %.2f km penalty distance.", penalty));

        if(missed != 0) {
            // Add a little stress, what could possibly go wrong!?
            int desperationGain = missed * Biathlon.DESPERATION_GAIN_PER_MISS;
            currentDesperation = Helpers.clamp(currentDesperation + desperationGain,
                                               Biathlon.MIN_DESPERATION,
                                               Biathlon.MAX_DESPERATION);

            log(String.format("Desperation increases to %d%%.", currentDesperation));

            if(currentDesperation >= Biathlon.DESPERATION_THRESHOLD) {
                log("Desperation increases past the panic treshold.");
                log("Competitor starts to panic.");

                speedFactor *= 1.1f;
                accuracyFactor *= 0.9f;
                aimingFactor *= 1.1f;
            }
            else {
                speedFactor *= 1.05f;
                aimingFactor *= 1.05f;
                accuracyFactor *= 0.95f;
            }
        }
        else {
            speedFactor *= 0.95f;
            aimingFactor *= 0.95f;
            accuracyFactor *= 1.05f;
        }

        log(String.format("Speed factor changes to %.2f.", speedFactor));
        log(String.format("Aiming time factor changes to %.2f.", aimingFactor));
        log(String.format("Accuracy factor changes to %.2f.", accuracyFactor));

		distanceToCover += penalty;
	}


	public int computeShotsMissed(ShootingRange shootingRange) {
		// TODO : jakieś magiczne symulacje

        int sps = Biathlon.SHOTS_PER_SHOOTING;
		int missed = Math.round(Helpers.clamp(sps-(accuracy.sample().floatValue() * sps * accuracyFactor),
                                              0.0f,
                                              Biathlon.SHOTS_PER_SHOOTING));

		log(String.format("Missed %d times.", missed));
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
