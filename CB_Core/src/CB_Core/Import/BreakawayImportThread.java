package CB_Core.Import;

import java.util.concurrent.atomic.AtomicBoolean;

public class BreakawayImportThread extends Thread {

    private static BreakawayImportThread that;

    private static AtomicBoolean isCanceld = new AtomicBoolean(false);

    public BreakawayImportThread() {
        if (that != null && that.isAlive())
            throw new IllegalStateException("Import is running");
        that = this;
        isCanceld.set(false);
    }

    public static boolean isCanceled() {
        return isCanceld.get();
    }

    public static void reset() {
        if (that != null) {
            that.interrupt();
            that = null;
        }

        isCanceld.set(false);
    }

    public void cancel() {
        isCanceld.set(true);
        this.interrupt();
    }

}
