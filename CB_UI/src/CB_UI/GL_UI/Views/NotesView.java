/* 
 * Copyright (C) 2014-2015 team-cachebox.de
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
package CB_UI.GL_UI.Views;

import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

/**
 * 
 * @author Longri
 *
 */
public class NotesView extends CB_View_Base implements SelectedCacheEvent {
	NotesView that;
	EditTextField edNotes;
	Cache aktCache;
	boolean mustLoadNotes;

	//TODO implement ScrollBar. see SolverView => "private class ScrollBarParent implements IScrollbarParent {"

	public NotesView(CB_RectF rec, String Name) {
		super(rec, Name);
		that = this;
		mustLoadNotes = true;
		edNotes = new EditTextField(this, this, WrapType.WRAPPED, "Note");
		edNotes.setZeroPos();
		this.addChild(edNotes);
		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
		SelectedCacheEventList.Add(this);

		KeyboardFocusChangedEventList.Add(new KeyboardFocusChangedEvent() {

			@Override
			public void KeyboardFocusChanged(EditTextFieldBase focus) {
				chkFocus(focus);
			}
		});
	}

	private void chkFocus(EditTextFieldBase focus) {
		if (focus == edNotes) {
			edNotes.setHeight(NotesView.this.getHalfHeight());
			edNotes.setY(NotesView.this.getHalfHeight());
		} else {
			edNotes.setHeight(NotesView.this.getHeight());
			edNotes.setY(0);
		}
	}

	@Override
	public void onShow() {
		chkFocus(GL.that.getFocusedEditTextField());

		if (mustLoadNotes) {
			String text = aktCache != null ? Database.GetNote(aktCache) : "";
			if (text == null)
				text = "";
			edNotes.setText(text);
			mustLoadNotes = false;
		}
	}

	@Override
	public void onHide() {
		// Save changed Note text
		String text = edNotes.getText().toString();
		if (text != null) {
			try {
				Database.SetNote(aktCache, text);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void Initial() {

	}

	private void SetSelectedCache(Cache cache, Waypoint waypoint) {
		if (aktCache != cache) {
			mustLoadNotes = true;
			aktCache = cache;
		}
	}

	@Override
	protected void SkinIsChanged() {
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		aktCache = cache;
		mustLoadNotes = true;
	}

	@Override
	public void onResized(CB_RectF rec) {
		chkFocus(GL.that.getFocusedEditTextField());
	}
}
