/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.locator;

/**
 * This class holds the geo-coordinate memory save as Byte[]!<br>
 * The method getLatidude() and getLongitude() calculates the return values from the Byte[]!
 *
 * @author Longri
 */
public class TestCoordinate {
    /**
     * Conversion factor from degrees to microdegrees.
     */
    protected static final double CONVERSION_FACTOR = 1000000.0;
    /**
     * First byte holds the split position between Latitude and Longitude!<br>
     * The length of Bytes for Latitude and Longitude are Variable
     */
    private final byte[] store;

    public TestCoordinate(double Lat, double Lon) {
        final byte[] la = getVariableByteSigned(degreesToMicrodegrees(Lat));
        final byte[] lo = getVariableByteSigned(degreesToMicrodegrees(Lon));

        final int length = la.length + lo.length + 1;

        store = new byte[length];

        // Store the split position at first Byte
        store[0] = (byte) (la.length + 1);

        // Store Latitude
        System.arraycopy(la, 0, store, 1, la.length);

        // Store Longitude
        System.arraycopy(lo, 0, store, store[0], lo.length);
    }

    /**
     * Converts a coordinate from degrees to microdegrees (degrees * 10^6). No validation is performed.
     *
     * @param coordinate the coordinate in degrees.
     * @return the coordinate in microdegrees (degrees * 10^6).
     */
    public static int degreesToMicrodegrees(double coordinate) {
        return (int) (coordinate * CONVERSION_FACTOR);
    }

    /**
     * Converts a coordinate from microdegrees (degrees * 10^6) to degrees. No validation is performed.
     *
     * @param coordinate the coordinate in microdegrees (degrees * 10^6).
     * @return the coordinate in degrees.
     */
    public static double microdegreesToDegrees(int coordinate) {
        return coordinate / CONVERSION_FACTOR;
    }

    /**
     * Converts a variable amount of bytes from the read buffer to an unsigned int.
     * <p>
     * The first bit is for continuation info, the other seven bits are for data.
     *
     * @return the int value.
     */
    public static int getSignedInt(byte[] Data) {
        int bufferPosition = 0;
        int variableByteDecode = 0;
        byte variableByteShift = 0;

        // check if the continuation bit is set
        while ((Data[bufferPosition] & 0x80) != 0) {
            variableByteDecode |= (Data[bufferPosition++] & 0x7f) << variableByteShift;
            variableByteShift += 7;
        }

        // read the six data bits from the last byte
        if ((Data[bufferPosition] & 0x40) != 0) {
            // negative
            return -(variableByteDecode | ((Data[bufferPosition++] & 0x3f) << variableByteShift));
        }
        // positive
        return variableByteDecode | ((Data[bufferPosition++] & 0x3f) << variableByteShift);
    }

    /**
     * Converts a signed int to a variable length byte array.
     * <p>
     * The first bit is for continuation info, the other six (last byte) or seven (all other bytes) bits for data. The second bit in the
     * last byte indicates the sign of the number.
     *
     * @param value the int value.
     * @return an array with 1-5 bytes.
     */
    public static byte[] getVariableByteSigned(int value) {
        long absValue = Math.abs((long) value);
        if (absValue < 64) { // 2^6
            // encode the number in a single byte
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x40)};
            }
            return new byte[]{(byte) absValue};
        } else if (absValue < 8192) { // 2^13
            // encode the number in two bytes
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x40)};
            }
            return new byte[]{(byte) (absValue | 0x80), (byte) (absValue >> 7)};
        } else if (absValue < 1048576) { // 2^20
            // encode the number in three bytes
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80), (byte) ((absValue >> 14) | 0x40)};
            }
            return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80), (byte) (absValue >> 14)};
        } else if (absValue < 134217728) { // 2^27
            // encode the number in four bytes
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80), (byte) ((absValue >> 14) | 0x80), (byte) ((absValue >> 21) | 0x40)};
            }
            return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80), (byte) ((absValue >> 14) | 0x80), (byte) (absValue >> 21)};
        } else {
            // encode the number in five bytes
            if (value < 0) {
                return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80), (byte) ((absValue >> 14) | 0x80), (byte) ((absValue >> 21) | 0x80), (byte) ((absValue >> 28) | 0x40)};
            }
            return new byte[]{(byte) (absValue | 0x80), (byte) ((absValue >> 7) | 0x80), (byte) ((absValue >> 14) | 0x80), (byte) ((absValue >> 21) | 0x80), (byte) (absValue >> 28)};
        }
    }

    public double getLatitude() {
        final byte[] la = new byte[store[0] - 1];
        System.arraycopy(store, 1, la, 0, la.length);
        return microdegreesToDegrees(getSignedInt(la));
    }

    public double getLongitude() {
        final byte[] lo = new byte[store.length - store[0]];
        System.arraycopy(store, store[0], lo, 0, lo.length);
        return microdegreesToDegrees(getSignedInt(lo));
    }
}
