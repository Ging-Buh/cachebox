package de.droidcachebox.utils;

import java.io.Serializable;

public class DLong implements Serializable {
    public static final long UL1 = 1L;
    private static final long serialVersionUID = -3369610485833873224L;
    private long low;
    private long high;

    // / <summary>
    // / Constructor for create with lower and higher Long
    // / </summary>
    // / <param name="High">higher Long</param>
    // / <param name="Low">lower Long</param>
    public DLong(long high, long low) {
        this.low = low;
        this.high = high;
    }

    public static DLong shift(int value) {
        long low = 0;
        long high = 0;

        if (value > 62) {
            high = UL1 << (value - 63);
        } else {
            low = UL1 << value;
        }

        return new DLong(high, low);
    }

    public long getLow() {
        return low;
    }

    public void setLow(long value) {
        low = value;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long value) {
        high = value;
    }

    public DLong bitAdd(DLong value) {
        low = low + value.getLow();
        high = high + value.getHigh();
        return this;
    }

    public void bitAnd(DLong value) {
        low = low & value.getLow();
        high = high & value.getHigh();
    }

    public void bitOr(DLong value) {
        low = low | value.getLow();
        high = high | value.getHigh();
    }

    public boolean bitAndBiggerNull(DLong value) {
        return ((low & value.getLow()) > 0) || ((high & value.getHigh()) > 0);
    }

    public String toString() {
        return "low =" + low + "high=" + high + "high:" + getUInt64BitString(high) + "  low:" + getUInt64BitString(low)
                + "True Bits[]=" + getTrueArray(low) + getTrueArray(high, 64);
    }

    private String getUInt64BitString(long value) {
        return Long.toBinaryString(value);
    }

    private String getTrueArray(long value) {
        return getTrueArray(value, 0);
    }

    private String getTrueArray(long value, int add) {
        StringBuilder Sb = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            long mask = UL1 << i;
            if ((mask & value) > 0) {
                Sb.append("[").append(add + i).append("],");
            }
        }
        return Sb.toString();
    }

}
