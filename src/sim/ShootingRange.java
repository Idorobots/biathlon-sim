package sim;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * Models a shooting range session.
 * Each <code>Competitor</code> enters a <code>ShootingRange</code> and performs shooting.
 */
public class ShootingRange extends SimProcess {

    private Biathlon myModel;
    private Logger myLogger;

    /**
     * The c-tor.
     * @param owner The model owning this process.
     * @param name The name of this process.
     * @param showInTrace The flag toggling tracing for this process.
     */
    public ShootingRange(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        myModel = (Biathlon) owner;
        myLogger = new Logger("ShootingRange.txt");
    }

    /**
     * Implements the lifecycle of this process.
     */
    public void lifeCycle() {

        while (true) {
            if (myModel.competitorsQueue.isEmpty()) {
                myModel.shootingRangeQueue.insert(this);
                passivate();
            } else {
                Competitor nextCompetitor = myModel.competitorsQueue.first();
                myModel.competitorsQueue.remove(nextCompetitor);

                myLogger.log(String.format("Competitor %s enters the shooting range.", nextCompetitor.toString()));

                int misses = nextCompetitor.computeShotsMissed();
                nextCompetitor.addPenalties(misses);

                myLogger.log(String.format("Competitor %s misses %d times.", nextCompetitor.toString(), misses));

                // Models concrete shooting time.
                nextCompetitor.activate(nextCompetitor.computeShootingTime());

                // myLogger.log(String.format("Competitor %s leaves the shooting range.",
                // nextCompetitor.toString()));
            }
        }
    }
}
