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

package de.droidcachebox.gdx.controls;

import de.droidcachebox.KeyboardFocusChangedEvent;
import de.droidcachebox.KeyboardFocusChangedEventList;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

/**
 * @author Longri
 */
public class TextFilterView extends CB_View_Base implements KeyboardFocusChangedEvent {
    private static FilterProperties tmpFilterProps;

    /**
     * Clear button, for clearing text input
     */
    private CB_Button mBtnClear, getSql;
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
    private EditTextField mEingabe, sql;
    private float originalSqlY;
    /**
     * represented the actual filter mode <br/>
     * 0 = Title <br/>
     * 1 = Gc-Code <br/>
     * 2 = Owner <br/>
     */
    private int aktFilterMode = 0;

    public TextFilterView(CB_RectF rec, String Name) {
        super(rec, Name);

        float margin = UiSizes.getInstance().getMargin();
        setBorders(margin, margin);
        topBorder = margin;
        bottomBorder = margin;

        mTglBtnTitle = new MultiToggleButton("mTglBtnTitle");
        mTglBtnGc = new MultiToggleButton("mTglBtnGc");
        mTglBtnOwner = new MultiToggleButton("mTglBtnOwner");
        mEingabe = new EditTextField(this, "mEingabe");
        mEingabe.setText("");
        mBtnClear = new CB_Button("clear");
        mBtnClear.setText(Translation.get("clear"));
        mBtnClear.setClickHandler((view, x, y, pointer, button) -> {
            mEingabe.setText("");
            sql.setText("");
            if (tmpFilterProps.isUserDefinedSQL()) {
                tmpFilterProps.setUserDefinedSQL("");
            }
            return true;
        });

        addNext(mTglBtnTitle);
        addNext(mTglBtnGc);
        addLast(mTglBtnOwner);
        addLast(mEingabe);
        addLast(mBtnClear, -0.5f);

        mTglBtnTitle.initialOn_Off_ToggleStates(Translation.get("Title"), Translation.get("Title"));
        mTglBtnGc.initialOn_Off_ToggleStates(Translation.get("GCCode"), Translation.get("GCCode"));
        mTglBtnOwner.initialOn_Off_ToggleStates(Translation.get("Owner"), Translation.get("Owner"));

        mTglBtnTitle.setClickHandler((view, x, y1, pointer, button) -> {
            switchFilterMode(0);
            return true;
        });

        mTglBtnGc.setClickHandler((view, x, y12, pointer, button) -> {
            switchFilterMode(1);
            return true;
        });

        mTglBtnOwner.setClickHandler((view, x, y13, pointer, button) -> {
            switchFilterMode(2);
            return true;
        });

        switchFilterMode(0);

        addNext(new CB_Label("select * from Caches as c "));
        getSql = new CB_Button(Translation.get("getSql"));
        getSql.setClickHandler((view, x, y, pointer, button) -> {
            String sqlString = tmpFilterProps.getSqlWhere("").trim();
            if (tmpFilterProps.isUserDefinedSQL() || sqlString.length() == 0)
                sql.setText(sqlString);
            else {
                sql.setText("where " + sqlString);
            }
            return true;
        });
        addLast(getSql, -0.3f);
        sql = new EditTextField(this, "sql");
        sql.setWrapType(WrapType.WRAPPED);
        sql.setHeight(getAvailableHeight());
        addLast(sql);
        originalSqlY = sql.getY();
    }

    @Override
    public void onShow() {
        KeyboardFocusChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.remove(this);
    }

    /**
     * switch filter mode.
     *
     * @param state <br/>
     *              0 = Title <br/>
     *              1 = Gc-Code <br/>
     *              2 = Owner <br/>
     */
    private void switchFilterMode(int state) {
        aktFilterMode = state;

        if (state == 0) {
            mTglBtnTitle.setState(1);
            mTglBtnGc.setState(0);
            mTglBtnOwner.setState(0);
        }
        if (state == 1) {
            mTglBtnTitle.setState(0);
            mTglBtnGc.setState(1);
            mTglBtnOwner.setState(0);
        }
        if (state == 2) {
            mTglBtnTitle.setState(0);
            mTglBtnGc.setState(0);
            mTglBtnOwner.setState(1);
        }

    }

    /**
     * Sets the filter to the EditText Field and activate the given filterstate
     *
     * @param filter      String for EditTextField
     * @param filterState Filter state!</br> 0 = Title </br> 1 = GcCode </br> 2 = Owner </br>
     */
    private void setFilterString(String filter, int filterState) {
        mEingabe.setText(filter);
        switchFilterMode(filterState);
    }

    public void setFilter(FilterProperties filter) {
        tmpFilterProps = filter;
        if (filter.isUserDefinedSQL()) {
            sql.setText(filter.getSqlWhere("").trim());
        } else {
            if (tmpFilterProps.filterName.length() > 0)
                setFilterString(tmpFilterProps.filterName, 0);
            else if (tmpFilterProps.filterGcCode.length() > 0)
                setFilterString(tmpFilterProps.filterGcCode, 1);
            else if (tmpFilterProps.filterOwner.length() > 0)
                setFilterString(tmpFilterProps.filterOwner, 2);
        }
    }

    public FilterProperties updateFilterProperties(FilterProperties filter) {
        if (sql.getText().length() > 0) {
            filter.setUserDefinedSQL(sql.getText());
        } else {
            String txtFilter = mEingabe.getText().toLowerCase(); // only the text to search for
            if (aktFilterMode == 0)
                filter.filterName = txtFilter;
            else if (aktFilterMode == 1)
                filter.filterGcCode = txtFilter;
            else if (aktFilterMode == 2)
                filter.filterOwner = txtFilter;
        }
        return filter;
    }

    @Override
    public void KeyboardFocusChanged(EditTextField focus) {
        if (focus == sql) {
            sql.setY(mTglBtnTitle.getY() + mTglBtnTitle.getHeight() - sql.getHeight());
        } else {
            sql.setY(originalSqlY);
        }
    }
}
