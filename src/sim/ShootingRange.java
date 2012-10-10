package sim;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

public class ShootingRange extends SimProcess {

	private Biathlon myModel;
    private Logger myLogger;

	public ShootingRange(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
		myModel = (Biathlon) owner;
        myLogger = new Logger("ShootingRange.txt");
    }

	private void log(String message) {
        // FIXME presentTime() differs from Competitor.presentTime(), should be the same.

		String msgWithTime = String.format("[ %8s ]\t%s", this.presentTime().toString(), message);
		myLogger.log(msgWithTime);
	}

	public void lifeCycle() {

		while (true) {
			if (myModel.competitorsQueue.isEmpty()) {
				myModel.shootingRangeQueue.insert(this);
				passivate();
			} else {
				Competitor nextCompetitor = myModel.competitorsQueue.first();
				myModel.competitorsQueue.remove(nextCompetitor);

                log(String.format("Competitor %s enters the shooting range.", nextCompetitor.toString()));

                int misses = nextCompetitor.computeShotsMissed(this);
				nextCompetitor.addPenalties(misses);

                log(String.format("Competitor %s misses %d times.", nextCompetitor.toString(), misses));

                // Models concrete shooting time.
				nextCompetitor.activate(nextCompetitor.computeShootingTime());

                log(String.format("Competitor %s leaves the shooting range.", nextCompetitor.toString()));
			}
		}

//        log("Competition ends.");
//		myLogger.close();
	}
}
