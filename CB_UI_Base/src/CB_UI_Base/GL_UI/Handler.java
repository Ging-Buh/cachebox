package CB_UI_Base.GL_UI;

import java.util.Timer;
import java.util.TimerTask;

public class Handler {

    public void postDelayed(final Runnable task, int time) {
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };

        final Timer timer = new Timer();
        timer.schedule(timerTask, time);

    }

    public void post(Runnable downloadComplete) {
        Thread th = new Thread(downloadComplete);
        th.start();
    }

}
