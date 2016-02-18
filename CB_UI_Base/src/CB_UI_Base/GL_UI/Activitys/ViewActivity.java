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
package CB_UI_Base.GL_UI.Activitys;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

/**
 * A Activity for showing an View with one close button on bottom of this Activity
 * 
 * @author Longri 2014
 */
public class ViewActivity extends ActivityBase {
	private CB_View_Base view;
	private Button btnClose;
	private boolean disposeViewWithFinish = false;
	private onFinishListener listener = null;
	private OnClickListener onOkClik;

	public interface onFinishListener {
		public void onFinish();
	}

	/**
	 * Constructor
	 * 
	 * @param View
	 *            the View that will showing
	 */
	public ViewActivity(CB_View_Base View) {
		super(View.getName());

		dontRenderDialogBackground = true;
		this.setBackground(Sprites.activityBackground);

		this.onOkClik = new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				ViewActivity.this.finish();
				return true;
			}
		};

		// add close button on bottom
		this.btnClose = new Button(new CB_RectF(leftBorder, bottomBorder, this.innerWidth, UI_Size_Base.that.getButtonHeight()), onOkClik);
		this.btnClose.setText(Translation.Get("close".hashCode()));

		this.addChild(btnClose);
		// btnClose.setVisible(false);

		// add view over the close button
		this.view = View;
		this.view.set(leftBorder, this.btnClose.getMaxY() + margin, this.innerWidth, this.innerHeight - (this.btnClose.getHeight() + margin));
		this.addChild(this.view);

	}

	/**
	 * Set listener for finish is called.
	 * 
	 * @param listener
	 * @param disposeView
	 *            Set flag for dispose View with finish or not.
	 */
	public void setOnFinishListener(onFinishListener listener, boolean disposeView) {
		this.listener = listener;
		this.disposeViewWithFinish = disposeView;
	}

	@Override
	public void finish() {

		if (this.disposeViewWithFinish) {
			if (this.view != null)
				this.view.dispose();
			this.view = null;
			if (this.btnClose != null)
				this.btnClose.dispose();
			this.btnClose = null;
		} else { // must remove View from content !! otherwise view will be disposed with super.finish()
			this.removeChildsDirekt(this.view);
		}

		super.finish();

		if (listener != null)
			listener.onFinish();
	}

	@Override
	public void show() {
		super.show();
		this.view.onShow();
	}

}
