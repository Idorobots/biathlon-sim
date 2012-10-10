package sim;

import desmoj.core.dist.ContDistExponential;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

public class Competitor extends SimProcess {

	private Biathlon myModel;
	private Logger logger;

	/**
	 * Dystans pozostały wciąż do przebycia.
	 */
	private double distanceToCover;

	/**
	 * Ile razy trzeba jeszcze odwiedzić strzelnicę.
	 */
	private int shootingsLeft;

	// http://desmoj.sourceforge.net/doc/desmoj/core/dist/ContDistExponential.html
	private ContDistExponential speed;

	// http://desmoj.sourceforge.net/doc/desmoj/core/dist/RealDistUniform.html
	private ContDistUniform aimingTime;

	private ContDistUniform accuracy;


	public Competitor(Model owner, String name, boolean showInTrace, int id) {
		super(owner, name, showInTrace);
		myModel = (Biathlon) owner;
		logger = new Logger(String.format("competitor_%d.txt", id));

		distanceToCover = Biathlon.INITIAL_DISTANCE;
		shootingsLeft = Biathlon.NUM_SHOOTING_RANGES;

		speed = new ContDistExponential(myModel, "Speed", 1.3, true, false);
		aimingTime = new ContDistUniform(myModel, "Aiming", 5.0, 10.0, true, false);
		accuracy = new ContDistUniform(myModel, "Accuracy", 0.0, Biathlon.SHOTS_PER_SHOOTING, true, false);

		speed.setNonNegative(true);
	}


	public void lifeCycle() {
		while (distanceToCover > 0) {
			hold(new TimeSpan(1));
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
				}

				passivate();
				log("Visited shooting range.");
			}
		}

		log("Finished the competition!");
//		logger.close();
	}

	private void log(String message) {
		String msgWithTime = String.format("[ %8s ]\t%s", this.presentTime().toString(), message);
		logger.log(msgWithTime);
	}

	private void run() {
		// TODO : jakieś magiczne symulacje
		double dist = Helpers.clamp(speed.sample(), 0.5, 2.5);
		distanceToCover -= dist;
		sendTraceNote(String.format("Distance left: %.2f", distanceToCover));
	}


	public void addPenalty(int n) {
		distanceToCover += n * Biathlon.PENALTY_DISTANCE;
	}


	public int computeShotsMissed(ShootingRange shootingRange) {
		// TODO : jakieś magiczne symulacje
		int missed = Math.round(accuracy.sample().floatValue());
		sendTraceNote(String.format("Missed: %d", missed));
		return missed;
	}


	public TimeSpan computeShootingTime() {
		// TODO : jakieś magiczne symulacje
		return aimingTime.sampleTimeSpan();
	}
}
