package sim;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeInstant;

/**
 * The model of the Simulation.
 * Defines a number of adjustable simulation parameters used by the processes.
 */
public class Biathlon extends Model {
    private static Biathlon instance = null;

     /**
      * A universal seed used for random generators.
      */
    public static final long UNIVERSAL_SEED = 0xDEADBEEF;

     /**
      * Simulation step in seconds (1.0f == 1 second).
      */
    public static final float STEP_TIME = 1.0f;

    /**
     * The number of competitors in a Biathlon competition.
     */
    public static final int NUM_COMPETITORS = 30;

    /**
     * The number of shooting sessions per Biathlon competition.
     */
    public static final int NUM_SHOOTING_RANGES = 4;

    /**
     * The number of shots fired per single shooting range visit.
     */
    public static final int SHOTS_PER_SHOOTING = 5;

    /**
     * The initial race distance in meters (1.0f == 1 meter).
     */
    public static final float INITIAL_DISTANCE = 15000f;

    /**
     * The penalty distance in meters per missed shot (1.0f == 1 meter).
     */
    public static final float PENALTY_DISTANCE = 150f;

    /**
     * The mean for the accuracy normal distribution model of the competitors.
     */
    public static final float ACCURACY_MEAN = 0.90f;

    /**
     * The standard deviation for the accuracy normal distribution model.
     */
    public static final float ACCURACY_STD_DEV = 0.1f;

    /**
     * Accuracy change over time. Used for tiredness modeling.
     */
    public static final float ACCURACY_FACTOR_DELTA = -0.0001f;

    /**
     * Accuracy change per missed shot. Used for stress model.
     */
    public static final float ACCURACY_DELTA_PER_MISS = -0.02f;

    /**
     * The mean for the shooting time normal distribution of the competitors.
     */
    public static final float SHOOTING_TIME_MEAN = 25.0f;

    /**
     * The standard deviation of the shooting time normal distribution.
     */
    public static final float SHOOTING_TIME_STD_DEV = 4.0f;

    /**
     * Shooting time change over time. Used by the tiredness model.
     */
    public static final float SHOOTING_TIME_FACTOR_DELTA = 0.0f;//0.0001f;

    /**
     * Shooting time change per missed shot. Used by the tiredness model.
     */
    public static final float SHOOTING_TIME_DELTA_PER_MISS = 0.02f;

    /**
     * Minimal valid speed of a competitor. Given in meters per second (1.0f == 1 m/s).
     */
    public static final float MIN_SPEED = 1.0f;

    /**
     * Maximal humanly possible speed of a competitor. Given in meters per second (1.0f == 1 m/s).
     */
    public static final float MAX_SPEED = 12.0f;

    /**
     * The mean of the competitor speed normal distribution. In meters per second.
     */
    public static final float SPEED_MEAN = 8.88f;

    /**
     * The standard deviation of the competitor speed normal distribution.
     */
    public static final float SPEED_STD_DEV = 1.0f;

    /**
     * Speed change over time. Used by the tiredness model.
     */
    public static final float SPEED_FACTOR_DELTA = -0.0001f;

    /**
     * Speed change per missed shot. Used by the stress model.
     */
    public static final float SPEED_DELTA_PER_MISS = -0.02f;

    /**
     * Minimal random stress level in percent (1 == 1%). Used by the stress model.
     */
    public static final int MIN_DESPERATION = 2;

    /**
     * Maximal random stress level in percent (1 == 1%).
     */
    public static final int MAX_DESPERATION = 10;

    /**
     * Stress change per missed shot. Given in percent (1 == 1%). Used by the stress model.
     */
    public static final int DESPERATION_DELTA_PER_MISS = 7;

    /**
     * Panic threshold - the minimal stress value causing a competitor to go batshit crazy.
     */
    public static final int PANIC_THRESHOLD = 90;

    /**
     * Attribute modifier used by the stress model.
     * Increases a value of an attribute.
     */
    public static final float PANIC_GAIN_MODIFIER = 1.1f;

    /**
     * Attribute modifier used by the stress model.
     * Decreases a value of an attribute.
     */
    public static final float PANIC_LOSS_MODIFIER = 0.9f;

    /**
     * Competitors queueing at the shooting range.
     */
    protected desmoj.core.simulator.ProcessQueue<Competitor> competitorsQueue;

    /**
     * A queue of "idle" shooting ranges.
     * <p>
     * This solution may seem unnecessary, but it was implemented to avoid busy
     * waiting and some minor synchronization problems.
     */
    protected desmoj.core.simulator.ProcessQueue<ShootingRange> shootingRangeQueue;


    private Biathlon(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
        super(owner, modelName, showInReport, showInTrace);
    }

    /**
     * Returns the singleton instance of this class.
     * @return The singleton instance of this class.
     */
    public static Biathlon getInstance() {
        if (instance == null)
            instance = new Biathlon(null, "Biathlon", true, true);
        return instance;
    }

    /**
     * The description of the Biathlon model.
     * @return The description of the Biathlon model.
     */
    public String description() {
        // TODO: dokończyć (wpływ desperacji itp.)
        return String.format("This is a model of a biathlon mass-start" +
        		"competition. A certain number of competitors (%d) participate" +
        		"in such an event and every one of them has to cover a certain" +
        		"distance (%dm) as fast as they can. During the run, in equal" +
        		"distance intervals, competitors have to visit a shooting range" +
        		" (total of %d times) and each time they shoot %d bullets. For" +
        		"every miss they have to cover additional %dm. The competitor's" +
        		" performance is modelled by the speed factor and the accuracy " +
        		" (both decrease linearly during the physical effort) and is " +
        		"also affected by an additional parameter, the desperation. " +
        		"Desperation is a result of adversities such as misses, fells " +
        		"or problems with skis.", Biathlon.NUM_COMPETITORS, (int) Biathlon.INITIAL_DISTANCE, Biathlon.NUM_SHOOTING_RANGES, Biathlon.SHOTS_PER_SHOOTING, (int) Biathlon.PENALTY_DISTANCE);
    }

    /**
     * Initialization of the simulation processes.
     */
    public void doInitialSchedules() {
        ShootingRange shootingRange = new ShootingRange(this, "Shooting Range", true);
        shootingRange.activate();

        CompetitorGenerator generator = new CompetitorGenerator(this, "BiathlonStart", true);
        generator.activate();
    }

    /**
     * Initialization of the simulation. Rhymation of the documentation.
     */
    public void init() {
        competitorsQueue = new ProcessQueue<Competitor>(this, "Competitors Queue", true, true);
        shootingRangeQueue = new ProcessQueue<ShootingRange>(this, "ShootingRange Queue", true, true);
    }

    /**
     * Estimates the duration of the simulation (in second, 1.0 == 1 s).
     * @return The estimated duration of the simulation in seconds (1.0 == 1s).
     */
    public static double estimateDuration() {
        return INITIAL_DISTANCE / SPEED_MEAN;
    }

    /**
     * Entry point of the simulation. Initializes and carries out the simulation logging results.
     * @param args Program arguments.
     */
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

        Results.getInstance().dumpResults("results.txt");

        System.exit(0);
    }
}
