package de.droidcachebox.utils;

public abstract class LoopThread {

    private final long sleepTime;
    private boolean isAlive;
    private Thread loop;
    private Thread lifeCycle;

    public LoopThread(long LoopBreakTime) {
        super();
        sleepTime = LoopBreakTime;
    }

    public boolean Alive() {
        return this.isAlive;
    }

    protected abstract void Loop();

    protected abstract boolean LoopBreak();

    public void start() {
        if (isAlive)
            return;
        if (loop == null) {
            loop = new Thread(new Runnable() {
                @Override
                public void run() {
                    do {
                        isAlive = true;
                        if (!LoopBreak())
                            Loop();
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (isAlive);
                }
            });
        }
        try {
            loop.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lifeCycle == null)
            lifeCycleStart();
    }

    private void lifeCycleStart() {
        if (lifeCycle == null) {
            lifeCycle = new Thread(new Runnable() {

                @Override
                public void run() {
                    do {
                        if (!loop.isAlive()) {
                            stop();
                            start();
                        }
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (isAlive);
                }
            });
            lifeCycle.setPriority(Thread.MIN_PRIORITY);
            lifeCycle.start();
        }
    }

    public void stop() {
        isAlive = false;
        loop = null;
        lifeCycle = null;
    }

}