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
package CB_UI_Base.Events;

import org.slf4j.LoggerFactory;

import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.Gdx;

public class KeyboardFocusChangedEventList
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(KeyboardFocusChangedEventList.class);
	public static CB_List<KeyboardFocusChangedEvent> list = new CB_List<KeyboardFocusChangedEvent>();

	public static void Add(KeyboardFocusChangedEvent event)
	{
		synchronized (list)
		{
			log.debug("FocusChangedEventList register" + event.toString());
			if (!list.contains(event)) list.add(event);
		}
	}

	public static void Remove(KeyboardFocusChangedEvent event)
	{
		synchronized (list)
		{
			log.debug("FocusChangedEventList unregister" + event.toString());
			list.remove(event);
		}
	}

	public static void Call(final EditTextFieldBase focus)
	{
		if (focus != null && !focus.dontShowKeyBoard())
		{
			Gdx.input.setOnscreenKeyboardVisible(true);
		}
		else
		{
			Gdx.input.setOnscreenKeyboardVisible(false);
		}
		synchronized (list)
		{

			for (int i = 0, n = list.size(); i < n; i++)
			{
				KeyboardFocusChangedEvent event = list.get(i);
				// log.debug("FocusChangedEventList fire to " + event.toString());
				event.KeyboardFocusChanged(focus);
			}
		}
	}

}
