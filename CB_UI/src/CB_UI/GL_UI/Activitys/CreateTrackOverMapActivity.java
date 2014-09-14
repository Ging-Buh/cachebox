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
package CB_UI.GL_UI.Activitys;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.MapView.MapMode;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

/**
 * A Activity for create a Track over the Map.<br>
 * Set TrackPoints over MapCenter!
 * 
 * @author Longri
 */
public class CreateTrackOverMapActivity extends ActivityBase
{
	private Label lblName;
	private EditTextField editName;
	private MapView mapView;
	private Button btnOk, btnAdd, btnCancel;

	public CreateTrackOverMapActivity(String Name)
	{
		super(Name);
		createControls();
	}

	private void createControls()
	{
		float btWidth = innerWidth / 3;

		btnOk = new Button(new CB_RectF(leftBorder, this.getBottomHeight(), btWidth, UI_Size_Base.that.getButtonHeight()), onOkClik);
		btnAdd = new Button(new CB_RectF(btnOk.getMaxX(), this.getBottomHeight(), btWidth, UI_Size_Base.that.getButtonHeight()), onAddClik);
		btnCancel = new Button(new CB_RectF(btnAdd.getMaxX(), this.getBottomHeight(), btWidth, UI_Size_Base.that.getButtonHeight()), onCancelClik);

		this.addChild(btnOk);
		this.addChild(btnAdd);
		this.addChild(btnCancel);

		lblName = new Label(Translation.Get("Name"));
		editName = new EditTextField();
		lblName.setRec(new CB_RectF(leftBorder, this.getHeight() - (lblName.getHeight() + margin), lblName.getWidth(), lblName.getHeight()));
		editName.setRec(new CB_RectF(lblName.getMaxX() + margin, lblName.getY(), innerWidth - (margin + lblName.getWidth()), lblName.getHeight()));
		this.addChild(lblName);
		this.addChild(editName);

		CB_RectF mapRec = new CB_RectF(leftBorder, btnOk.getMaxY() + margin, innerWidth, innerHeight - (btnOk.getHalfHeight() + editName.getHeight() + (4 * margin) + topBorder));

		mapView = new MapView(mapRec, MapMode.Track, "MapView");
		this.addChild(mapView);

	}

	private OnClickListener onOkClik = new OnClickListener()
	{
		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			// TODO Auto-generated method stub
			return false;
		}
	};

	private OnClickListener onAddClik = new OnClickListener()
	{
		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			// TODO Auto-generated method stub
			return false;
		}
	};

	private OnClickListener onCancelClik = new OnClickListener()
	{
		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			GL.that.RunOnGL(new IRunOnGL()
			{
				@Override
				public void run()
				{
					finish();
				}
			});
			return true;
		}
	};

	public void dispose()
	{
		super.dispose();
		if (btnOk != null) btnOk.dispose();
		btnOk = null;

		if (btnAdd != null) btnAdd.dispose();
		btnAdd = null;

		if (btnCancel != null) btnCancel.dispose();
		btnCancel = null;

		if (lblName != null) lblName.dispose();
		lblName = null;

		onOkClik = null;
		onAddClik = null;
		onCancelClik = null;
	}

}
