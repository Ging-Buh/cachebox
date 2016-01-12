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

import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import CB_Utils.Util.IChanged;

/**
 * Contains the static queries of the state of CacheBox, for the decision whether a job being processed has to do. Thus delivers
 * 'dontRender' the value True, if the display switched off and therefore of no Render jobs are necessary.
 * 
 * @author Longri
 */
public class Energy {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(Energy.class);

    // ##########################
    // Dont Render
    // ##########################

    /**
     * Explain of no Render jobs!
     */
    private static boolean displayOff = false;

    /**
     * Explain of no Render jobs!
     */
    public static boolean DisplayOff() {
	return displayOff;
    }

    /**
     * Set DisplayOff to 'True'
     */
    public static void setDisplayOff() {
	displayOff = true;
	fireChangedEvent();
	log.info("ENERGY setDisplayOff");
    }

    /**
     * Set DisplayOff to 'False'
     */
    public static void setDisplayOn() {
	displayOff = false;
	fireChangedEvent();
	log.info("ENERGY setDisplayOn");
    }

    // ##############################
    // Slider is Shown
    // ##############################

    private static boolean sliderIsShown = false;

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

    protected static ArrayList<IChanged> ChangedEventList = new ArrayList<IChanged>();

    protected static void fireChangedEvent() {
	synchronized (ChangedEventList) {
	    for (IChanged event : ChangedEventList) {
		event.isChanged();
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
