package sim;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * Generates competitors for the Biathlon simulation.
 */
public class CompetitorGenerator extends SimProcess {

    /**
     * The c-tor.
     * @param owner The model owning this process.
     * @param name The name of this process.
     * @param showInTrace A flag toggling tracing for this process.
     */
    public CompetitorGenerator(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    /**
     * Defines and carries out the process modeled by this class.
     * Generates several competitors parttaking in the Biathlon competition.
     */
    public void lifeCycle() {
        Biathlon model = (Biathlon) getModel();

        for (int i = 0; i < Biathlon.NUM_COMPETITORS; i++) {
            Competitor competitor = new Competitor(model, "Competitor", true, i);

            Results.getInstance().registerCompetitor(i, competitor.toString());

            competitor.activateAfter(this);
        }
    }
}
