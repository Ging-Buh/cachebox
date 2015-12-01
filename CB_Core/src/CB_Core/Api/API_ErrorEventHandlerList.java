/* 
 * Copyright (C) 2015 team-cachebox.de
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
package CB_Core.Api;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class API_ErrorEventHandlerList {
    public enum API_ERROR {
	INVALID, EXPIRED, NO
    };

    private static ArrayList<API_ErrorEventHandler> list = new ArrayList<API_ErrorEventHandler>();

    public static void addHandler(API_ErrorEventHandler handler) {
	synchronized (list) {
	    if (!list.contains(handler))
		list.add(handler);
	}
    }

    private static Thread threadCall;
    private static long lastCall;
    private static final long MIN_CALL_TIME = 5000;

    public static void callInvalidApiKey(final API_ERROR type) {
	if (lastCall != 0 && lastCall > System.currentTimeMillis() - MIN_CALL_TIME)
	    return;
	lastCall = System.currentTimeMillis();

	if (threadCall == null)
	    threadCall = new Thread(new Runnable() {

		@Override
		public void run() {
		    synchronized (list) {
			synchronized (list) {
			    for (API_ErrorEventHandler handler : list) {
				switch (type) {
				case EXPIRED:
				    handler.ExpiredAPI_Key();
				    break;
				case INVALID:
				    handler.InvalidAPI_Key();
				    break;
				case NO:
				    handler.NoAPI_Key();
				    break;
				default:
				    break;
				}
			    }
			}
		    }

		}
	    });

	// Zeit verzögerter Fehler aufruf
	Timer timer = new Timer();
	TimerTask task = new TimerTask() {
	    @Override
	    public void run() {
		threadCall.run();
	    }
	};
	timer.schedule(task, 700);

    }
}
