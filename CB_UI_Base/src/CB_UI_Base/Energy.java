/*
 * Copyright (C) 2011-2012 team-cachebox.de
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

package CB_UI_Base;

import CB_Utils.Log.Log;
import CB_Utils.Util.IChanged;

import java.util.ArrayList;

/**
 * Contains the static queries of the state of CacheBox, for the decision whether a job being processed has to do. Thus delivers
 * 'dontRender' the value True, if the display switched off and therefore of no Render jobs are necessary.
 *
 * @author Longri
 */
public class Energy {
    private static final String log = "Energy";

    // ##########################
    // Dont Render
    // ##########################
    protected static ArrayList<IChanged> ChangedEventList = new ArrayList<>();
    /**
     * Explain of no Render jobs!
     */
    private static boolean displayOff = false;
    private static boolean sliderIsShown = false;

    /**
     * Explain of no Render jobs!
     */
    public static boolean DisplayOff() {
        return displayOff;
    }

    // ##############################
    // Slider is Shown
    // ##############################

    /**
     * Set DisplayOff to 'True'
     */
    public static void setDisplayOff() {
        displayOff = true;
        fireChangedEvent();
        Log.info(log, "ENERGY setDisplayOff");
    }

    /**
     * Set DisplayOff to 'False'
     */
    public static void setDisplayOn() {
        displayOff = false;
        fireChangedEvent();
        Log.info(log, "ENERGY setDisplayOn");
    }

    public static boolean SliderIsShown() {
        if (displayOff)
            return true;
        return sliderIsShown;
    }

    public static void setSliderIsShown() {
        sliderIsShown = true;

    }

    public static void resetSliderIsShown() {
        sliderIsShown = false;

    }

    protected static void fireChangedEvent() {
        synchronized (ChangedEventList) {
            for (IChanged event : ChangedEventList) {
                event.handleChange();
            }
        }

    }

    public static void addChangedEventListener(IChanged listener) {
        synchronized (ChangedEventList) {
            if (!ChangedEventList.contains(listener))
                ChangedEventList.add(listener);
        }
    }

    public static void removeChangedEventListener(IChanged listener) {
        synchronized (ChangedEventList) {
            ChangedEventList.remove(listener);
        }
    }

}
