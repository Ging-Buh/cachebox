package de.droidcachebox.ex_import;

import java.util.ArrayList;

import de.droidcachebox.utils.ProgressChangedEvent;

/**
 * for handling of progress during import
 *
 * @author Longri
 */
public class ImportProgress {

    private final ArrayList<Step> allSteps;
    private final ProgressChangedEvent progressChangedEvent;
    private float overallWeight;

    public ImportProgress(ProgressChangedEvent progressChangedEvent) {
        this.progressChangedEvent = progressChangedEvent;
        allSteps = new ArrayList<>();
        overallWeight = 0f;
    }

    public void addStep(String id, float weight) {
        allSteps.add(new Step(id, weight));
        overallWeight = overallWeight + weight;
    }

    public void incrementStep(String id, String msg) {
        increment(id, msg, false);
    }

    public void finishStep(String id, String msg) {
        increment(id, msg, true);
    }

    /**
     * fires the event
     *
     * @param id  identify the step
     * @param msg text for the increment
     */
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

    /**
     * @param id   identify the step
     * @param msg  text for the increment
     * @param done true, if ready
     */
    private void increment(String id, String msg, boolean done) {
        for (Step step : allSteps) {
            if (step.id.equals(id)) {
                if (done) {
                    step.currentValue = step.finalValue;
                } else {
                    step.currentValue = step.currentValue + 1;
                }
                break;
            }
        }
        changeMsg(id, msg);
    }

    private int calculateDoneTillNowPercent() {
        float progress = 0.0f;
        for (Step step : allSteps) {
            float f = step.weight * step.currentValue / overallWeight / step.finalValue;
            progress = progress + f;
        }
        return (int) (100 * progress);
    }

    /**
     * defining a step of the import
     */
    private static class Step {
        public String id; // identify the step
        public int finalValue; // the final value when the step is done
        public int currentValue; // at finalValue this step is completed (done, ready)
        public float weight; // how much makes this step from all steps (what the programmer expects, in his scale units)

        public Step(String id, float weight) {
            this.weight = weight;
            this.id = id;
            currentValue = 0;
            finalValue = 1;
        }

        public void setFinalValue(int finalValue) {
            if (finalValue == 0) {
                this.finalValue = 1;
            } else {
                this.finalValue = finalValue;
            }
        }
    }

}
