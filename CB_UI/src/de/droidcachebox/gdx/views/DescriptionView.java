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
package de.droidcachebox.gdx.views;

import static de.droidcachebox.core.GroundspeakAPI.UserInfos;
import static de.droidcachebox.core.GroundspeakAPI.fetchMyCacheLimits;
import static de.droidcachebox.core.GroundspeakAPI.fetchMyUserInfos;
import static de.droidcachebox.core.GroundspeakAPI.isPremiumMember;
import static de.droidcachebox.gdx.math.GL_UISizes.mainButtonSize;
import static de.droidcachebox.utils.Config_Core.br;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.ViewConst;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.gdx.graphics.GL_Paint;
import de.droidcachebox.gdx.graphics.PolygonDrawable;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.Line;
import de.droidcachebox.gdx.math.Quadrangle;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn1.contextmenus.CacheContextMenu;
import de.droidcachebox.translation.Translation;

public class DescriptionView extends CB_View_Base implements CacheSelectionChangedListeners.CacheSelectionChangedListener {
    private final static String BASIC = "Basic";
    private final static String PREMIUM = "Premium";
    private final static String BASIC_LIMIT = "3";
    private final static String PREMIUM_LIMIT = "6000";
    private static DescriptionView descriptionView;
    private final String STRING_POWERD_BY;
    private CacheListViewItem cacheListViewItem;
    private CB_Button btnDownload;
    private CB_Label messageLabel, powerdBy;
    private Image liveIcon;
    private PolygonDrawable line;
    private float margin;
    private Cache selectedCache;

    private DescriptionView() {
        super(ViewManager.leftTab.getContentRec(), "DescriptionView");
        STRING_POWERD_BY = Translation.get("GC_title");
    }

    public static DescriptionView getInstance() {
        if (descriptionView == null) descriptionView = new DescriptionView();
        return descriptionView;
    }

    @Override
    public void onShow() {
        CacheSelectionChangedListeners.getInstance().addListener(this);
        margin = GL_UISizes.margin;
        selectedCache = GlobalCore.getSelectedCache();

        if (selectedCache == null) return; // nothing to show

        if (cacheListViewItem != null) {
            if (!cacheListViewItem.getCache().equals(selectedCache)) {
                removeChild(cacheListViewItem);
                getNewCacheInfo();
            }
        } else {
            getNewCacheInfo();
        }

        resetUi();

        if (selectedCache.isLive() || selectedCache.getApiStatus() == Cache.IS_LITE) {
            UserInfos me = fetchMyUserInfos();
            boolean dontAsk = isPremiumMember() && me.remaining > 3;
            if (dontAsk) {
                // simply download, if Premium,..
                GL.that.RunOnGL(() -> CacheContextMenu.getInstance().reloadSelectedCache());
                selectedCache.setApiStatus(Cache.IS_FULL); // hack to prevent endless looping, hopefully does not go into Database
            } else {
                showDownloadButton();
            }
        } else {
            showWebView();
        }

        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                descriptionView.onResized(descriptionView);
            }
        };
        t.schedule(tt, 70);
    }

    private void getNewCacheInfo() {
        if (selectedCache == null) return;
        cacheListViewItem = new CacheListViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), 0, selectedCache);
        cacheListViewItem.setY(getHeight() - cacheListViewItem.getHeight());

        addChild(cacheListViewItem);
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        // onShow();
        if (cacheListViewItem != null)
            cacheListViewItem.setY(getHeight() - cacheListViewItem.getHeight());
        layout();

        float infoHeight = -(UiSizes.getInstance().getInfoSliderHeight());
        if (cacheListViewItem != null)
            infoHeight += cacheListViewItem.getHeight();
        infoHeight += margin * 2;
        CB_RectF world = getWorldRec();

        PlatformUIBase.setContentSize((int) world.getX(), (int) ((GL_UISizes.surfaceSize.getHeight() - (world.getMaxY() - infoHeight))), (int) (GL_UISizes.surfaceSize.getWidth() - world.getMaxX()), (int) world.getY());

    }

    @Override
    public void onHide() {
        CacheSelectionChangedListeners.getInstance().remove(this);
        PlatformUIBase.hideView(ViewConst.DESCRIPTION_VIEW);
    }

    private void showWebView() {
        // Rufe ANDROID VIEW auf
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                float infoHeight = 0;
                if (cacheListViewItem != null)
                    infoHeight = cacheListViewItem.getHeight();
                PlatformUIBase.showView(ViewConst.DESCRIPTION_VIEW, descriptionView.getX(), descriptionView.getY(), descriptionView.getWidth(), descriptionView.getHeight(), 0, (infoHeight + GL_UISizes.margin));
            }
        };
        timer.schedule(task, 50);
    }

    private void resetUi() {
        if (messageLabel != null) {
            removeChildDirect(messageLabel);
            messageLabel.dispose();
            messageLabel = null;
        }
        if (btnDownload != null) {
            removeChildDirect(btnDownload);
            btnDownload.dispose();
            btnDownload = null;
        }
        if (liveIcon != null) {
            removeChildDirect(liveIcon);
            liveIcon.dispose();
            liveIcon = null;
        }
        if (powerdBy != null) {
            removeChildDirect(powerdBy);
            powerdBy.dispose();
            powerdBy = null;
        }
    }

    private void layout() {
        if (liveIcon != null) {
            float IconX = getHalfWidth() - liveIcon.getHalfWidth();
            float IconY = cacheListViewItem.getY() - (liveIcon.getHeight() + margin);
            liveIcon.setPos(IconX, IconY);

            if (powerdBy != null) {
                powerdBy.setY(liveIcon.getY() - (powerdBy.getHeight() + margin));

                if (messageLabel != null) {
                    messageLabel.setY(powerdBy.getY() - (messageLabel.getHeight() + (margin * 3)));
                    messageLabel.setX(getHalfWidth() - messageLabel.getHalfWidth());
                }
                btnDownload.setX(getHalfWidth() - btnDownload.getHalfWidth());
                btnDownload.setY(margin);
            }
        }
        line = null;
    }

    private void showDownloadButton() {

        if (fetchMyUserInfos().remaining <= 0) {
            fetchMyCacheLimits();
            if (fetchMyUserInfos().remaining <= 0) {
                if (isPremiumMember()) {
                    MsgBox.show(Translation.get("LiveDescLimit"), Translation.get("Limit_msg"), MsgBoxButton.OK, MsgBoxIcon.Exclamation, null);
                } else {
                    MsgBox.show(Translation.get("LiveDescLimitBasic"), Translation.get("Limit_msg"), MsgBoxButton.OK, MsgBoxIcon.Exclamation, null);
                }
                return;
            }
        }

        float contentWidth = getWidth() * 0.95f;

        liveIcon = new Image(mainButtonSize, "LIVE-ICON", false);
        liveIcon.setSprite(Sprites.LiveBtn.get(0));

        addChild(liveIcon);

        powerdBy = new CB_Label("");

        powerdBy.setHeight(Fonts.Measure(STRING_POWERD_BY).height + (margin * 2));
        powerdBy.setFont(Fonts.getNormal()).setHAlignment(HAlignment.CENTER);
        powerdBy.setWidth(contentWidth);
        powerdBy.setWrappedText(STRING_POWERD_BY);
        addChild(powerdBy);

        messageLabel = new CB_Label("");
        messageLabel.setWidth(contentWidth);
        messageLabel.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
        messageLabel.setHeight(getHalfHeight());

        messageLabel.setWrappedText(getMessage());
        addChild(messageLabel);

        btnDownload = new CB_Button(Translation.get("DownloadDetails"));
        btnDownload.setWidth(getWidth() * 0.8f);

        addChild(btnDownload);

        btnDownload.setClickHandler((v, x, y, pointer, button) -> {
            GL.that.RunOnGL(() -> CacheContextMenu.getInstance().reloadSelectedCache());
            return true;
        });

        if (GroundspeakAPI.fetchMyUserInfos().remaining <= 0)
            btnDownload.disable();
        layout();
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);

        if (powerdBy != null) {
            if (line == null) {
                float strokeWidth = 3 * UiSizes.getInstance().getScale();

                Line l1 = new Line(margin, powerdBy.getY() - margin, getWidth() - margin, powerdBy.getY() - margin);

                Quadrangle q1 = new Quadrangle(l1, strokeWidth);

                GL_Paint paint = new GL_Paint();
                paint.setColor(Color.DARK_GRAY);
                line = new PolygonDrawable(q1.getVertices(), q1.getTriangles(), paint, getWidth(), getHeight());

                l1.dispose();

                q1.dispose();

            }

            line.draw(batch, 0, 0, getWidth(), getHeight(), 0);
        }
    }

    private String getMessage() {
        StringBuilder sb = new StringBuilder();
        boolean premium = isPremiumMember();
        String MemberType = premium ? PREMIUM : BASIC;
        String limit = premium ? PREMIUM_LIMIT : BASIC_LIMIT;

        String actLimit = Integer.toString(GroundspeakAPI.fetchMyUserInfos().remaining - 1);

        if (GroundspeakAPI.fetchMyUserInfos().remaining < 0) {
            actLimit = "?";
        }

        sb.append(Translation.get("LiveDescMessage", MemberType, limit));
        sb.append(br);
        if (GroundspeakAPI.fetchMyUserInfos().remaining > 0)
            sb.append(Translation.get("LiveDescAfter", actLimit)); // "

        if (GroundspeakAPI.fetchMyUserInfos().remaining == 0) {
            sb.append(Translation.get("LiveDescLimit"));
            sb.append(br);
            if (!premium)
                sb.append(Translation.get("LiveDescLimitBasic"));
        }

        return sb.toString();

    }

    public void forceReload() {
        if (cacheListViewItem != null) {
            removeChild(cacheListViewItem);
        }
        cacheListViewItem = null;
    }

    @Override
    public void handleCacheChanged(Cache selectedCache, Waypoint waypoint) {
        if (!cacheListViewItem.getCache().equals(selectedCache)) {
            //todo  implement more simple by cacheListViewItem.setCache(selectedCache);
            // next doesn't work
            CB_RectF oldRectangle = cacheListViewItem;
            removeChild(cacheListViewItem);
            getNewCacheInfo();
            addChild(cacheListViewItem);
            cacheListViewItem.setRec(oldRectangle);
        }
    }

}
