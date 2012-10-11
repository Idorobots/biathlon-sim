package sim;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeInstant;

public class Biathlon extends Model {

    protected static final float COMPETITOR_STEP_TIME = 1.0f;
    protected static final int NUM_COMPETITORS = 30;
    protected static final int NUM_SHOOTING_RANGES = 4;
    protected static final int SHOTS_PER_SHOOTING = 5;

    protected static final float INITIAL_DISTANCE = 15f;
    protected static final float PENALTY_DISTANCE = 0.15f;

    protected static final float MIN_ACCURACY = 0.9f;
    protected static final float AVERAGE_ACCURACY = 0.95f;
    protected static final float MAX_ACCURACY = 1.0f;

    protected static final float MIN_SHOOTING_TIME = 2.2f;
    protected static final float AVERAGE_SHOOTING_TIME = 2.8f;
    protected static final float MAX_SHOOTING_TIME = 3.5f;

    protected static final float MIN_SPEED = 0.65f;
    protected static final float AVERAGE_SPEED = 0.7f;
    protected static final float MAX_SPEED = 0.73f;

    protected static final int MIN_DESPERATION = 0;
    protected static final int MAX_DESPERATION = 100;
    protected static final int DESPERATION_THRESHOLD = 95;
    protected static final int DESPERATION_GAIN_PER_MISS = 5;
    
    private static Biathlon instance = null;

    /**
     * Zawodnicy czekajacy na strzelanie.
     */
    protected desmoj.core.simulator.ProcessQueue<Competitor> competitorsQueue;

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

        TimeInstant simStopTime = new TimeInstant(1500);
        TimeInstant simStartTime = new TimeInstant(0);
        exp.stop(simStopTime);
        exp.traceOn(simStartTime);
        exp.debugOn(simStartTime);

        exp.start();
        exp.finish();

        System.exit(0);
    }
}
