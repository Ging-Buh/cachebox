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
package CB_UI.GL_UI.Views;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Types.Cache;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Main.Actions.CacheContextMenu;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GlobalCore;
import CB_UI_Base.AbstractGlobal;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.CB_Button;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.CB_Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.Geometry.Line;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_UI_Base.graphics.PolygonDrawable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.Timer;
import java.util.TimerTask;

import static CB_Core.Api.GroundspeakAPI.*;
import static CB_UI_Base.Math.GL_UISizes.MainBtnSize;

public class DescriptionView extends CB_View_Base {
    final static String BASIC = "Basic";
    final static String PREMIUM = "Premium";
    final static String BASIC_LIMIT = "3";
    final static String PREMIUM_LIMIT = "6000";
    private static DescriptionView that;
    final static OnClickListener downloadClicked = (v, x, y, pointer, button) -> {
        GL.that.RunOnGL(() -> CacheContextMenu.ReloadSelectedCache());
        return true;
    };
    private String STRING_POWERD_BY;
    private CacheListViewItem cacheInfo;
    private CB_Button downloadButton;
    private CB_Label MessageLabel, PowerdBy;
    private Image LiveIcon;
    private PolygonDrawable Line;
    private float margin;
    private Cache sel;

    private DescriptionView() {
        super(ViewManager.leftTab.getContentRec(), "DescriptionView");
        STRING_POWERD_BY = Translation.get("GC_title");
    }

    public static DescriptionView getInstance() {
        if (that == null) that = new DescriptionView();
        return that;
    }

    @Override
    public void onShow() {
        margin = GL_UISizes.margin;
        sel = GlobalCore.getSelectedCache();

        if (sel == null) return; // nothing to show

        if (cacheInfo != null) {
            if (!cacheInfo.getCache().equals(sel)) {
                this.removeChild(cacheInfo);
                getNewCacheInfo();
            }
        } else {
            getNewCacheInfo();
        }

        resetUi();

        if (sel.isLive() || sel.getApiStatus() == Cache.IS_LITE) {
            UserInfos me = fetchMyUserInfos();
            boolean dontAsk = isPremiumMember() && me.remaining > 3;
            if (dontAsk) {
                // simply download, if Premium,..
                GL.that.RunOnGL(CacheContextMenu::ReloadSelectedCache);
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
                DescriptionView.this.onResized(DescriptionView.this);
            }
        };
        t.schedule(tt, 70);
    }

    private void getNewCacheInfo() {
        if (sel == null) return;
        cacheInfo = new CacheListViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), 0, sel);
        cacheInfo.setY(this.getHeight() - cacheInfo.getHeight());

        this.addChild(cacheInfo);
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        // onShow();
        if (cacheInfo != null)
            cacheInfo.setY(this.getHeight() - cacheInfo.getHeight());
        layout();

        float infoHeight = -(UiSizes.getInstance().getInfoSliderHeight());
        if (cacheInfo != null)
            infoHeight += cacheInfo.getHeight();
        infoHeight += margin * 2;
        CB_RectF world = this.getWorldRec();

        PlatformConnector.setContentSize((int) world.getX(), (int) ((GL_UISizes.SurfaceSize.getHeight() - (world.getMaxY() - infoHeight))), (int) (GL_UISizes.SurfaceSize.getWidth() - world.getMaxX()), (int) world.getY());

    }

    @Override
    public void onHide() {
        PlatformConnector.hideView(ViewConst.DESCRIPTION_VIEW);
    }

    private void showWebView() {
        // Rufe ANDROID VIEW auf
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                float infoHeight = 0;
                if (cacheInfo != null)
                    infoHeight = cacheInfo.getHeight();
                PlatformConnector.showView(ViewConst.DESCRIPTION_VIEW, that.getX(), that.getY(), that.getWidth(), that.getHeight(), 0, (infoHeight + GL_UISizes.margin), 0, 0);
            }
        };
        timer.schedule(task, 50);
    }

    private void resetUi() {
        if (MessageLabel != null) {
            this.removeChildsDirekt(MessageLabel);
            MessageLabel.dispose();
            MessageLabel = null;
        }
        if (downloadButton != null) {
            this.removeChildsDirekt(downloadButton);
            downloadButton.dispose();
            downloadButton = null;
        }
        if (LiveIcon != null) {
            this.removeChildsDirekt(LiveIcon);
            LiveIcon.dispose();
            LiveIcon = null;
        }
        if (PowerdBy != null) {
            this.removeChildsDirekt(PowerdBy);
            PowerdBy.dispose();
            PowerdBy = null;
        }
    }

    private void layout() {
        if (LiveIcon != null) {
            float IconX = this.getHalfWidth() - LiveIcon.getHalfWidth();
            float IconY = this.cacheInfo.getY() - (LiveIcon.getHeight() + margin);
            LiveIcon.setPos(IconX, IconY);

            if (PowerdBy != null) {
                PowerdBy.setY(LiveIcon.getY() - (PowerdBy.getHeight() + margin));

                if (MessageLabel != null) {
                    MessageLabel.setY(this.PowerdBy.getY() - (MessageLabel.getHeight() + (margin * 3)));
                    MessageLabel.setX(this.getHalfWidth() - MessageLabel.getHalfWidth());
                }
                downloadButton.setX(this.getHalfWidth() - downloadButton.getHalfWidth());
                downloadButton.setY(margin);
            }
        }
        Line = null;
    }

    private void showDownloadButton() {

        if (fetchMyUserInfos().remaining <= 0) {
            fetchMyCacheLimits();
            if (fetchMyUserInfos().remaining <= 0) {
                if (isPremiumMember()) {
                    MessageBox.show(Translation.get("LiveDescLimit"), Translation.get("Limit_msg"), MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
                } else {
                    MessageBox.show(Translation.get("LiveDescLimitBasic"), Translation.get("Limit_msg"), MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
                }
                return;
            }
        }

        float contentWidth = this.getWidth() * 0.95f;

        LiveIcon = new Image(MainBtnSize, "LIVE-ICON", false);
        LiveIcon.setSprite(Sprites.LiveBtn.get(0), false);

        this.addChild(LiveIcon);

        PowerdBy = new CB_Label("");

        PowerdBy.setHeight(Fonts.Measure(STRING_POWERD_BY).height + (margin * 2));
        PowerdBy.setFont(Fonts.getNormal()).setHAlignment(HAlignment.CENTER);
        PowerdBy.setWidth(contentWidth);
        PowerdBy.setWrappedText(STRING_POWERD_BY);
        this.addChild(PowerdBy);

        MessageLabel = new CB_Label("");
        MessageLabel.setWidth(contentWidth);
        MessageLabel.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
        MessageLabel.setHeight(this.getHalfHeight());

        MessageLabel.setWrappedText(getMessage());
        this.addChild(MessageLabel);

        downloadButton = new CB_Button(Translation.get("DownloadDetails"));
        downloadButton.setWidth(this.getWidth() * 0.8f);

        this.addChild(downloadButton);

        downloadButton.addClickHandler(downloadClicked);

        if (GroundspeakAPI.fetchMyUserInfos().remaining <= 0)
            downloadButton.disable();
        layout();
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);

        if (PowerdBy != null) {
            if (Line == null) {
                float strokeWidth = 3 * UiSizes.getInstance().getScale();

                Line l1 = new Line(margin, PowerdBy.getY() - margin, this.getWidth() - margin, PowerdBy.getY() - margin);

                Quadrangle q1 = new Quadrangle(l1, strokeWidth);

                GL_Paint paint = new GL_Paint();
                paint.setGLColor(Color.DARK_GRAY);
                Line = new PolygonDrawable(q1.getVertices(), q1.getTriangles(), paint, this.getWidth(), this.getHeight());

                l1.dispose();

                q1.dispose();

            }

            Line.draw(batch, 0, 0, this.getWidth(), this.getHeight(), 0);
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
        if (cacheInfo != null) {
            this.removeChild(cacheInfo);
        }
        cacheInfo = null;
    }
}
