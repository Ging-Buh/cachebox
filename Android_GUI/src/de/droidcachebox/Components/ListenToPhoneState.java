package de.droidcachebox.Components;


import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;


// ACTUALLY NOT USED AT ALL!
public class ListenToPhoneState extends PhoneStateListener {
        
        private int recentState = -1;
        
    public void onCallStateChanged(int state, String incomingNumber) {
        Lg.info("ListenToPhoneState.onCallStateChanged(state=" + state + ", incomingNumber=" + incomingNumber + ") " +
                        "... state changed=" + stateName(state));
        
        if(this.recentState == -1 && state == TelephonyManager.CALL_STATE_IDLE) {
                // very first when registerting listener
        }
    }
    
    private String stateName(final int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE: return "Idle"; // 0
            case TelephonyManager.CALL_STATE_OFFHOOK: return "Off hook"; // 2
            case TelephonyManager.CALL_STATE_RINGING: return "Ringing";
        }
        return Integer.toString(state);
    }
}