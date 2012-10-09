package sim;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeInstant;

public class Biathlon extends Model {

	protected static final int NUM_COMPETITORS = 2;

	protected static final int NUM_SHOOTING_RANGES = 1;
	protected static final int SHOTS_PER_SHOOTING = 5;
	protected static final double INITIAL_DISTANCE = 10;
	protected static final double PENALTY_DISTANCE = 5;

	protected static final boolean showInTrace = false;
	protected static final boolean inDebugMode = false;

	/**
	 * Zawodnicy czekajacy na strzelanie.
	 */
	protected desmoj.core.simulator.ProcessQueue<Competitor> competitorsQueue;

	protected desmoj.core.simulator.ProcessQueue<ShootingRange> shootingRangeQueue;


	public Biathlon(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
		super(owner, modelName, showInReport, showInTrace);
	}


	public String description() {
		return "<description>";
	}


	public void doInitialSchedules() {
		ShootingRange shootingRange = new ShootingRange(this, "Shooting Range", showInTrace);
		shootingRange.activate();

		CompetitorGenerator generator = new CompetitorGenerator(this, "BiathlonStart", showInTrace);
		generator.activate();
	}


	public void init() {
		competitorsQueue = new ProcessQueue<Competitor>(this, "Competitors Queue", true, showInTrace);
		shootingRangeQueue = new ProcessQueue<ShootingRange>(this, "ShootingRange Queue", true, showInTrace);
	}


	public static void main(java.lang.String[] args) {

		Biathlon model = new Biathlon(null, "ProcessesExample", true, Biathlon.showInTrace);
		Experiment exp = new Experiment("Biathlon");
		model.connectToExperiment(exp);

		TimeInstant simStopTime = new TimeInstant(1500);
		TimeInstant simStartTime = new TimeInstant(0);
		exp.stop(simStopTime);
		exp.traceOn(simStartTime);
		exp.debugOn(simStartTime);

		exp.start();
		exp.report();
		exp.finish();

		System.exit(0);
	}
}
