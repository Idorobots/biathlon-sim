package sim;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * Wypuść wszystkich zawodników równocześnie.
 */
public class CompetitorGenerator extends SimProcess {

	public CompetitorGenerator(Model owner, String name, boolean showInTrace) {
		super(owner, name, showInTrace);
	}


	public void lifeCycle() {
		Biathlon model = (Biathlon) getModel();

		for (int i = 0; i < Biathlon.NUM_COMPETITORS; i++) {
			Competitor competitor = new Competitor(model, "Competitor", true, i + 1);
			competitor.activateAfter(this);
		}
	}
}
