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
package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Spinner extends Button {
    private NinePatch triangle;
    private int mSelectedIndex = -1;
    private final Spinner that;
    private String prompt;
    private Image icon;

    private SpinnerAdapter mAdapter;

    public interface selectionChangedListner {
	public void selectionChanged(int index);
    }

    private selectionChangedListner mListner;

    public Spinner(String Name, SpinnerAdapter adapter, selectionChangedListner listner) {
	super(new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight()), Name);
	mAdapter = adapter;
	that = this;
	mListner = listner;
    }

    public Spinner(float X, float Y, float Width, float Height, String Name, SpinnerAdapter adapter, selectionChangedListner listner) {
	super(X, Y, Width, Height, Name);
	mAdapter = adapter;
	that = this;
	mListner = listner;
    }

    public Spinner(CB_RectF rec, String Name, SpinnerAdapter adapter, selectionChangedListner listner) {
	super(rec, Name);
	mAdapter = adapter;
	that = this;
	mListner = listner;
    }

    @Override
    protected void Initial() {
	super.Initial();

	if (triangle == null) {
	    Sprite tr = SpriteCacheBase.getThemedSprite("spinner-triangle");
	    int patch = (int) tr.getWidth() / 2;
	    triangle = new NinePatch(tr, 0, patch, patch, 0);
	}

	this.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		if (mAdapter == null)
		    return true; // kann nix anzeigen

		// show Menu to select
		Menu icm = new Menu("SpinnerSelection" + that.name);
		icm.addItemClickListner(new OnClickListener() {
		    @Override
		    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			int sel = ((MenuItem) v).getIndex();
			setSelection(sel);
			if (mListner != null)
			    mListner.selectionChanged(sel);
			return false;
		    }
		});

		for (int index = 0; index < mAdapter.getCount(); index++) {
		    String text = mAdapter.getText(index);
		    Drawable drawable = mAdapter.getIcon(index);

		    icm.addItem(index, text, drawable, true);
		}

		if (prompt != null && !prompt.equalsIgnoreCase("")) {
		    icm.setPrompt(prompt);
		}

		icm.Show();
		return true;
	    }
	});

    }

    @Override
    protected void render(Batch batch) {
	super.render(batch);
	triangle.draw(batch, 0, 0, getWidth(), getHeight());
    }

    @Override
    protected void SkinIsChanged() {
	triangle = null;
	resetInitial();
    }

    public void setSelection(int i) {

	if (mAdapter != null && mAdapter.getCount() >= i && i > -1) {
	    String Text = mAdapter.getText(i);
	    mSelectedIndex = i;
	    this.setText(Text);

	    Drawable drw = mAdapter.getIcon(i);

	    if (lblTxt == null)
		return;

	    if (drw != null) {
		lblTxt.setHAlignment(HAlignment.LEFT);
		if (icon == null) {
		    CB_RectF rec = (new CB_RectF(0, 0, this.getHeight(), this.getHeight())).ScaleCenter(0.7f);

		    icon = new Image(rec, "", false);
		    icon.setY(this.getHalfHeight() - icon.getHalfHeight());

		    float margin = UI_Size_Base.that.getMargin();

		    icon.setX(margin * 2);

		    this.addChild(icon);

		    lblTxt.setX(icon.getMaxX() + margin);
		}
		float margin = UI_Size_Base.that.getMargin();

		icon.setX(margin * 2);

		this.addChild(icon);

		lblTxt.setX(icon.getMaxX() + margin);
		icon.setDrawable(drw);
	    } else {
		lblTxt.setHAlignment(HAlignment.CENTER);
	    }
	    lblTxt.setText(Text);
	}

    }

    public int getSelectedItem() {
	return mSelectedIndex;
    }

    public void setPrompt(String Prompt) {
	prompt = Prompt;
    }

    public SpinnerAdapter getAdapter() {
	return mAdapter;
    }

    public void setAdapter(SpinnerAdapter adapter) {
	mAdapter = adapter;
    }

    public void setSelectionChangedListner(selectionChangedListner selectionChangedListner) {
	mListner = selectionChangedListner;
    }

}
