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
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Controls.Button;
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
	private EditTextField notes;
	private float notesDefaultYPos;
	private float notesHeight;
	private Button uploadButton;
	private Cache aktCache;
	boolean mustLoadNotes;

	public NotesView(CB_RectF rec, String Name) {
		super(rec, Name);

		aktCache = GlobalCore.getSelectedCache();
		mustLoadNotes = true;

		initRow(BOTTOMUP);
		uploadButton = new Button("Upload");
		addLast(uploadButton);
		notesHeight = getAvailableHeight();
		notes = new EditTextField(this, new CB_RectF(0, 0, getWidth(), notesHeight), WrapType.WRAPPED, "Note");
		this.addLast(notes);
		notesDefaultYPos = notes.getY();

		SelectedCacheEventList.Add(this);

		KeyboardFocusChangedEventList.Add(new KeyboardFocusChangedEvent() {
			@Override
			public void KeyboardFocusChanged(EditTextFieldBase focus) {
				chkFocus(focus);
			}
		});

		uploadButton.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				if (notes.getText().length() > 0) {
					CB_Core.Api.GroundspeakAPI.uploadNotes(aktCache.getGcCode(), notes.getText());
				}
				return false;
			}

		});

	}

	private void chkFocus(EditTextFieldBase focus) {
		if (focus == notes) {
			notes.setHeight(this.getHalfHeight());
			notes.setY(this.getHalfHeight());
		} else {
			notes.setHeight(notesHeight);
			notes.setY(notesDefaultYPos);
		}
	}

	@Override
	public void onShow() {
		chkFocus(GL.that.getFocusedEditTextField());
		if (mustLoadNotes) {
			String text = aktCache != null ? Database.GetNote(aktCache) : "";
			if (text == null)
				text = "";
			notes.setText(text);
			notes.showFromLineNo(0);
			mustLoadNotes = false;
		}
	}

	@Override
	public void onHide() {
		// Save changed Note text
		String text = notes.getText().toString();
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
