package sim;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

public class ShootingRange extends SimProcess {

	private Biathlon myModel;


	public ShootingRange(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
		myModel = (Biathlon) owner;
	}


	public void lifeCycle() {

		while (true) {
			if (myModel.competitorsQueue.isEmpty()) {
				myModel.shootingRangeQueue.insert(this);
				passivate();
			} else {
				Competitor nextCompetitor = myModel.competitorsQueue.first();
				myModel.competitorsQueue.remove(nextCompetitor);

				nextCompetitor.addPenalty(nextCompetitor.computeShotsMissed(this));
				nextCompetitor.activate(nextCompetitor.computeShootingTime());
			}
		}
	}
}
