/*
 * Copyright (C) 2011-2022 team-cachebox.de
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

package de.droidcachebox;

import java.util.ArrayList;

import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.log.Log;

/**
 * Contains the static queries of the state of CacheBox, for the decision whether a job being processed has to do. Thus delivers
 * 'dontRender' the value True, if the display switched off and therefore of no Render jobs are necessary.
 *
 * @author Longri
 */
public class Energy {
    private static final String sClass = "Energy";
    protected static final ArrayList<IChanged> ChangedEventList = new ArrayList<>();
    private static boolean isDisplayOff = false;
    private static boolean sliderIsShown = false;

    public static boolean isDisplayOff() {
        return isDisplayOff;
    }

    public static void setDisplayOff() {
        isDisplayOff = true;
        fireChangedEvent();
        Log.debug(sClass, "ENERGY setDisplayOff");
    }

    public static void setDisplayOn() {
        isDisplayOff = false;
        fireChangedEvent();
        Log.debug(sClass, "ENERGY setDisplayOn");
    }

    public static boolean SliderIsShown() {
        if (isDisplayOff)
            return true;
        return sliderIsShown;
    }

    public static void setSliderIsShown() {
        sliderIsShown = true;
    }

    public static void resetSliderIsShown() {
        sliderIsShown = false;
    }

    private static void fireChangedEvent() {
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
