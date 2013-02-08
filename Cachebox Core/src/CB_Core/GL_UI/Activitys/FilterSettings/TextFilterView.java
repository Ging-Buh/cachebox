/* 
 * Copyright (C) 2013 team-cachebox.de
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

package CB_Core.GL_UI.Activitys.FilterSettings;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.TranslationEngine.Translation;

/**
 * @author Longri
 */
public class TextFilterView extends CB_View_Base
{

	public static TextFilterView that;

	public TextFilterView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		that = this;

		float margin = UiSizes.getMargin() * 2;
		float btnWidth = (this.width - (margin * 7)) / 3;

		CB_RectF btnRrec = new CB_RectF(0, 0, btnWidth, UiSizes.getButtonHeight());

		mTglBtnTitle = new MultiToggleButton(btnRrec, "mTglBtnTitle");
		mTglBtnGc = new MultiToggleButton(btnRrec, "mTglBtnGc");
		mTglBtnOwner = new MultiToggleButton(btnRrec, "mTglBtnOwner");

		float y = this.getHeight() - margin - btnRrec.getHeight();

		mTglBtnTitle.setPos(margin + margin, y);
		mTglBtnGc.setPos(mTglBtnTitle.getMaxX() + margin, y);
		mTglBtnOwner.setPos(mTglBtnGc.getMaxX() + margin, y);

		btnRrec.setWidth(this.width - (margin * 2));

		mEingabe = new EditWrapedTextField(this, btnRrec, EditWrapedTextField.TextFieldType.SingleLine, "");

		mEingabe.setTextFieldListener(new TextFieldListener()
		{

			@Override
			public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight)
			{

			}

			@Override
			public void keyTyped(EditTextFieldBase textField, char key)
			{
				// textBox_TextChanged();
			}
		});

		mEingabe.setText("");
		mEingabe.setPos(margin, mTglBtnTitle.getY() - margin - mEingabe.getHeight());

		mBtnClear = new Button("clear");
		mBtnClear.setY(mEingabe.getY() - margin - mBtnClear.getHeight());
		mBtnClear.setX(this.width - margin - mBtnClear.getWidth());
		mBtnClear.setText(Translation.Get("clear"));
		mBtnClear.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				mEingabe.setText("");
				return true;
			}
		});

		// Controls zum Dialog hinzufügen
		this.addChild(mTglBtnTitle);
		this.addChild(mTglBtnGc);
		this.addChild(mTglBtnOwner);
		this.addChild(mEingabe);
		this.addChild(mBtnClear);

		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnTitle, Translation.Get("Title"), Translation.Get("Title"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnGc, Translation.Get("GCCode"), Translation.Get("GCCode"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOwner, Translation.Get("Owner"), Translation.Get("Owner"));

		mTglBtnTitle.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchFilterMode(0);
				return true;
			}
		});

		mTglBtnGc.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchFilterMode(1);
				return true;
			}
		});

		mTglBtnOwner.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchFilterMode(2);
				return true;
			}
		});

		switchFilterMode(0);

	}

	/**
	 * Clear button, for clearing text input
	 */
	private Button mBtnClear;

	/**
	 * Option Title, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnTitle;

	/**
	 * Option GC-Code, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnGc;

	/**
	 * Option Owner, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnOwner;

	/**
	 * Eingabe Feld
	 */
	private EditWrapedTextField mEingabe;

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * represented the actual filter mode <br/>
	 * 0 = Title <br/>
	 * 1 = Gc-Code <br/>
	 * 2 = Owner <br/>
	 */
	private int maktFilterMode = 0;

	/**
	 * switch filter mode.
	 * 
	 * @param state
	 * <br/>
	 *            0 = Title <br/>
	 *            1 = Gc-Code <br/>
	 *            2 = Owner <br/>
	 */
	private void switchFilterMode(int state)
	{
		maktFilterMode = state;

		if (state == 0)
		{
			mTglBtnTitle.setState(1);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(0);
		}
		if (state == 1)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(1);
			mTglBtnOwner.setState(0);
		}
		if (state == 2)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(1);
		}

	}

	/**
	 * Returns the text from EditTextField </br> Formated to lower case!
	 * 
	 * @return String
	 */
	public String getFilterString()
	{
		return mEingabe.getText().toLowerCase();
	}

	/**
	 * Returns the selected Filter state!</br> 0 = Title </br> 1 = GcCode </br> 2 = Owner </br>
	 * 
	 * @return
	 */
	public int getFilterState()
	{
		return maktFilterMode;
	}

	/**
	 * Sets the filter to the EditText Field and activate the given filterstate
	 * 
	 * @param filter
	 *            String for EditTextField
	 * @param filterState
	 *            Filter state!</br> 0 = Title </br> 1 = GcCode </br> 2 = Owner </br>
	 */
	public void setFilterString(String filter, int filterState)
	{
		mEingabe.setText(filter);
		switchFilterMode(filterState);
	}

}
