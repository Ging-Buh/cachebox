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
package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuItem;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class Spinner extends CB_Button {
    private NinePatch triangle;
    private int mSelectedIndex = -1;
    private String prompt;
    private Image icon;

    private SpinnerAdapter mAdapter;
    private ISpinnerSelectionChanged mListener;

    public Spinner(String TranslationId, SpinnerAdapter adapter, ISpinnerSelectionChanged listener) {
        super(new CB_RectF(0, 0, UiSizes.getInstance().getButtonWidthWide(), UiSizes.getInstance().getButtonHeight()), TranslationId);
        mAdapter = adapter;
        mListener = listener;
    }

    public Spinner(float X, float Y, float Width, float Height, String TranslationId, SpinnerAdapter adapter, ISpinnerSelectionChanged listener) {
        super(X, Y, Width, Height, TranslationId);
        mAdapter = adapter;
        mListener = listener;
    }

    public Spinner(CB_RectF rec, String TranslationId, SpinnerAdapter adapter, ISpinnerSelectionChanged listener) {
        super(rec, TranslationId);
        mAdapter = adapter;
        mListener = listener;
    }

    @Override
    protected void renderInit() {
        super.renderInit();

        if (triangle == null) {
            Sprite tr = Sprites.getSprite("spinner-triangle");
            int patch = (int) tr.getWidth() / 2;
            triangle = new NinePatch(tr, 0, patch, patch, 0);
        }

        this.setClickHandler((v, x, y, pointer, button) -> {
            if (mAdapter == null)
                return true; // kann nix anzeigen

            // show Menu to select
            Menu icm = new Menu(getName());
            for (int index = 0; index < mAdapter.getCount(); index++) {
                icm.addMenuItem("", mAdapter.getText(index), mAdapter.getIcon(index),
                        (v1, x1, y1, pointer1, button1) -> {
                            icm.close();
                            int sel = ((MenuItem) v1).getIndex();
                            setSelection(sel);
                            if (mListener != null)
                                mListener.selectionChanged(sel);
                            return false;
                        });
            }
            if (prompt != null && !prompt.equalsIgnoreCase("")) {
                icm.setPrompt(prompt);
            }
            icm.show();
            return true;
        });

    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);
        triangle.draw(batch, 0, 0, getWidth(), getHeight());
    }

    @Override
    protected void skinIsChanged() {
        triangle = null;
        resetRenderInitDone();
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
                    CB_RectF rec = (new CB_RectF(0, 0, this.getHeight(), this.getHeight())).scaleCenter(0.7f);

                    icon = new Image(rec, "", false);
                    icon.setY(this.getHalfHeight() - icon.getHalfHeight());

                    float margin = UiSizes.getInstance().getMargin();

                    icon.setX(margin * 2);

                    this.addChild(icon);

                    lblTxt.setX(icon.getMaxX() + margin);
                }
                float margin = UiSizes.getInstance().getMargin();

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

    public void setSelectionChangedListener(ISpinnerSelectionChanged selectionChangedListener) {
        mListener = selectionChangedListener;
    }

    public interface ISpinnerSelectionChanged {
        void selectionChanged(int index);
    }

}
