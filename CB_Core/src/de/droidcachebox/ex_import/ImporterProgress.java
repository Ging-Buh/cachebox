package de.droidcachebox.ex_import;

import java.util.ArrayList;

import de.droidcachebox.utils.ProgressChangedEvent;

/**
 * for handling of progress during import
 *
 * @author Longri
 */
public class ImporterProgress {

    private final ArrayList<Step> allSteps;
    private float overallWeight;
    private ProgressChangedEvent progressChangedEvent;

    public ImporterProgress(ProgressChangedEvent progressChangedEvent) {
        this.progressChangedEvent = progressChangedEvent;
        allSteps = new ArrayList<>();
        overallWeight = 0f;
    }

    public void addStep(String id, float weight) {
        allSteps.add(new Step(id, weight));
        overallWeight = overallWeight + weight;
    }

    /**
     * fires the event
     *
     * @param id   identify the step
     * @param msg  text for the increment
     * @param done true, if ready
     */
    public void incrementProgress(String id, String msg, boolean done) {
        int doneTillNowPercent = 0;
        for (Step step : allSteps) {
            if (step.id.equals(id)) {
                if (done) {
                    step.currentValue = step.finalValue;
                } else {
                    step.currentValue = step.currentValue + step.incrementValue;
                }
                doneTillNowPercent = calculateDoneTillNowPercent();
                break;
            }
        }

        if (progressChangedEvent != null)
            progressChangedEvent.progressChanged(id, msg, doneTillNowPercent);
    }

    public void changeMsg(String id, String msg) {
        if (progressChangedEvent != null)
            progressChangedEvent.progressChanged(id, msg, calculateDoneTillNowPercent());
    }

    public void setStepFinalValue(String id, int value) {
        for (Step step : allSteps) {
            if (step.id.equals(id)) {
                step.setFinalValue(value);
                break;
            }
        }
    }

    protected int calculateDoneTillNowPercent() {
        float progress = 0.0f;
        for (Step step : allSteps) {
            progress = progress + (step.weight / overallWeight) * (step.currentValue / step.finalValue);
        }
        return (int) (100 * progress);
    }

    /**
     * defining a step of the import
     */
    public static class Step {
        public String id; // identify the step
        public float incrementValue; // add per increment call
        public float finalValue; // the final value when the step is done
        public float currentValue; // at finalValue this step is completed (done, ready)
        public float weight; // how much makes this step from all steps (what the programmer expects, in his scale units)

        public Step(String id, float weight) {
            this.weight = weight;
            this.id = id;
            this.currentValue = 0f;
        }

        public void setFinalValue(int finalValue) {
            if (finalValue == 0) {
                this.incrementValue = 1f; // increment only once
                this.finalValue = 1f;
            } else {
                this.incrementValue = 1f / finalValue;
                this.finalValue = finalValue;
            }
        }
    }

}
