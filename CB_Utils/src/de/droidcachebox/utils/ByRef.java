package de.droidcachebox.utils;

import java.util.ArrayList;

/**
 * Usage: <br>
 * <br>
 * Initial: <br>
 * Trackable tb = null;<br>
 * ByRef<Trackable> ref = new ByRef<Trackable>(tb);<br>
 * <br>
 * Call Methode: <br>
 * int result = GroundspeakAPI.getTBbyTreckNumber(Config.GetAccessToken(true), TBCode, ref);<br>
 * <br>
 * <br>
 * On Methode:<br>
 * public static int getTBbyTreckNumber(String accessToken, String TrackingCode, ByRef<Trackable> TB) <br>
 * { <br>
 * .... <br>
 * <br>
 * TB.set(new Trackable(jTrackable)); <br>
 * TB.get().setTrackingCode(TrackingCode); <br>
 * return IO; <br>
 * } <br>
 * <br>
 * After return: <br>
 * // get RefValue<br>
 * tb = ref.get();<br>
 *
 * @param <T>
 * @author Longri
 */
public class ByRef<t> extends ArrayList<t> {

    /**
     *
     */
    private static final long serialVersionUID = -4250037651008928615L;

    public ByRef(t value) {
        this.add(value);
    }

    public t get() {
        return this.get(0);
    }

    public void set(t value) {
        this.clear();
        this.add(value);
    }
}
