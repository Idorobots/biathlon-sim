package sim;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeInstant;

public class Biathlon extends Model {
    private static Biathlon instance = null;

    protected static final float STEP_TIME = 1.0f;                    /// Simulation step in seconds.

    protected static final int NUM_COMPETITORS = 30;                  /// Number of competitors.
    protected static final int NUM_SHOOTING_RANGES = 4;               /// Number of shooting ranges.
    protected static final int SHOTS_PER_SHOOTING = 5;                /// Number of shots per shooting.

    protected static final float INITIAL_DISTANCE = 15000f;           /// Initial distance in meters.
    protected static final float PENALTY_DISTANCE = 150f;             /// Penalty distance in meters.

    protected static final float ACCURACY_MEAN = 0.9f;                /// Mean for the accuracy model.
    protected static final float ACCURACY_STD_DEV = 0.1f;             /// Stddev for the accuracy model.

    protected static final float ACCURACY_FACTOR_DELTA = -0.0001f;    /// Accuracy loss over time.
    protected static final float ACCURACY_DELTA_PER_MISS = -0.02f;    /// Accuracy "gain" per miss.

    protected static final float SHOOTING_TIME_MEAN = 5.0f;           /// Mean for the shooting time model.
    protected static final float SHOOTING_TIME_STD_DEV = 1.0f;        /// Stddev for the shootin time modl.

    protected static final float SHOOTING_TIME_FACTOR_DELTA = 0.0001f;/// Shooting time "gain" over time.
    protected static final float SHOOTING_TIME_DELTA_PER_MISS = 0.05f;/// Shooting time "gain" per miss.

    protected static final float MIN_SPEED = 2.0f;                    /// Minimal speed of a competitor.
    protected static final float MAX_SPEED = 10.0f;                   /// Maximal humanly possible speed.

    protected static final float SPEED_MEAN = 8.0f;                   /// Mean in m/s for the speed model.
    protected static final float SPEED_STD_DEV = 1.0f;                /// Stddev for the speed model.

    protected static final float SPEED_FACTOR_DELTA = -0.0001f;       /// Speed loss over time.
    protected static final float SPEED_DELTA_PER_MISS = -0.02f;       /// Speed loss per miss.

    protected static final int MIN_DESPERATION = 2;                   /// Minimal random stress level.
    protected static final int MAX_DESPERATION = 10;                  /// Maximal random stress level.
    protected static final int DESPERATION_DELTA_PER_MISS = 7;        /// Stress level gain due to miss.

    protected static final int PANIC_THRESHOLD = 95;                  /// Panic threshold.
    protected static final float PANIC_GAIN_MODIFIER = 1.1f;          /// Modifier when stressed.
    protected static final float PANIC_LOSS_MODIFIER = 0.9f;          /// Modifier when stressed.

    /** Competitors queueing at the shooting range. */
    protected desmoj.core.simulator.ProcessQueue<Competitor> competitorsQueue;

    /** Not really needed, no? */
    protected desmoj.core.simulator.ProcessQueue<ShootingRange> shootingRangeQueue;


    private Biathlon(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
        super(owner, modelName, showInReport, showInTrace);
    }

    public static Biathlon getInstance() {
        if (instance == null)
            instance = new Biathlon(null, "Biathlon", true, true);
        return instance;
    }


    public String description() {
        return "<description>";
    }


    public void doInitialSchedules() {
        ShootingRange shootingRange = new ShootingRange(this, "Shooting Range", true);
        shootingRange.activate();

        CompetitorGenerator generator = new CompetitorGenerator(this, "BiathlonStart", true);
        generator.activate();
    }


    public void init() {
        competitorsQueue = new ProcessQueue<Competitor>(this, "Competitors Queue", true, true);
        shootingRangeQueue = new ProcessQueue<ShootingRange>(this, "ShootingRange Queue", true, true);
    }


    public static void main(java.lang.String[] args) {

        Biathlon model = Biathlon.getInstance();
        Experiment exp = new Experiment("Biathlon");
        model.connectToExperiment(exp);

        TimeInstant simStartTime = new TimeInstant(0);
        TimeInstant simStopTime = new TimeInstant(3000); // 50 minute span.

        exp.stop(simStopTime);
        exp.traceOn(simStartTime);
        exp.debugOn(simStartTime);

        exp.start();
        exp.finish();

        System.exit(0);
    }
}
