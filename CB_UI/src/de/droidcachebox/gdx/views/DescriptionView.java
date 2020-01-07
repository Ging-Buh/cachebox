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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import de.droidcachebox.AbstractGlobal;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.Cache;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.graphics.GL_Paint;
import de.droidcachebox.gdx.graphics.PolygonDrawable;
import de.droidcachebox.gdx.math.*;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn1.contextmenus.CacheContextMenu;
import de.droidcachebox.translation.Translation;

import java.util.Timer;
import java.util.TimerTask;

import static de.droidcachebox.core.GroundspeakAPI.*;
import static de.droidcachebox.gdx.math.GL_UISizes.mainButtonSize;

public class DescriptionView extends CB_View_Base {
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
                GL.that.RunOnGL(CacheContextMenu::reloadSelectedCache);
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
            removeChildsDirekt(messageLabel);
            messageLabel.dispose();
            messageLabel = null;
        }
        if (btnDownload != null) {
            removeChildsDirekt(btnDownload);
            btnDownload.dispose();
            btnDownload = null;
        }
        if (liveIcon != null) {
            removeChildsDirekt(liveIcon);
            liveIcon.dispose();
            liveIcon = null;
        }
        if (powerdBy != null) {
            removeChildsDirekt(powerdBy);
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
                    MessageBox.show(Translation.get("LiveDescLimit"), Translation.get("Limit_msg"), MessageBoxButton.OK, MessageBoxIcon.Exclamation, null);
                } else {
                    MessageBox.show(Translation.get("LiveDescLimitBasic"), Translation.get("Limit_msg"), MessageBoxButton.OK, MessageBoxIcon.Exclamation, null);
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
            GL.that.RunOnGL(CacheContextMenu::reloadSelectedCache);
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
        sb.append(AbstractGlobal.br);
        if (GroundspeakAPI.fetchMyUserInfos().remaining > 0)
            sb.append(Translation.get("LiveDescAfter", actLimit)); // "

        if (GroundspeakAPI.fetchMyUserInfos().remaining == 0) {
            sb.append(Translation.get("LiveDescLimit"));
            sb.append(AbstractGlobal.br);
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
}
