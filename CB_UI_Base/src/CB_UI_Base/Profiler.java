package CB_UI_Base;

public class Profiler {
    private static ProfilerBase mProfiler;
    private static boolean Profile = false;
    private static boolean hasProfiled = false;
    private static boolean canProfile = false;

    public static void setProfiler(ProfilerBase profiler) {
        mProfiler = profiler;
    }

    public static void setCanProfile(boolean value) {
        canProfile = value;
    }

    public static void startMethodTracing() {
        if (!canProfile || Profile || hasProfiled)
            return;
        Profile = true;
        if (mProfiler != null)
            mProfiler.startMethodTracing();
    }

    public static void stopMethodTracing() {
        if (!Profile)
            return;
        Profile = false;
        hasProfiled = true;
        if (mProfiler != null)
            mProfiler.stopMethodTracing();
    }
}
