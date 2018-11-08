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
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.*;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Global;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.Geometry.Line;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_UI_Base.graphics.PolygonDrawable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import static CB_Core.Api.GroundspeakAPI.IsPremiumMember;

public class DescriptionView extends CB_View_Base {
    final static String STRING_POWERD_BY = "Powerd by Geocaching Live";
    final static String BASIC = "Basic";
    final static String PREMIUM = "Premium";
    final static String BASIC_LIMIT = "3";
    final static String PREMIUM_LIMIT = "6000";
    final static OnClickListener downloadClicked = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            GL.that.RunOnGL(new IRunOnGL() {
                @Override
                public void run() {
                    TabMainView.actionShowDescriptionView.ReloadSelectedCache();
                }
            });
            return true;
        }
    };
    private CacheListViewItem cacheInfo;
    private Button downloadButton;
    private Label MessageLabel, PowerdBy;
    private Image LiveIcon;
    private PolygonDrawable Line;
    private float margin;
    private Cache sel;

    public DescriptionView(CB_RectF rec, String Name) {
        super(rec, Name);
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
            showDownloadButton();
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
        cacheInfo = new CacheListViewItem(UiSizes.that.getCacheListItemRec().asFloat(), 0, sel);
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

        float infoHeight = -(UiSizes.that.getInfoSliderHeight());
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
                PlatformConnector.showView(ViewConst.DESCRIPTION_VIEW, DescriptionView.this.getX(), DescriptionView.this.getY(), DescriptionView.this.getWidth(), DescriptionView.this.getHeight(), 0, (infoHeight + GL_UISizes.margin), 0, 0);
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
        final Thread getLimitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                GroundspeakAPI.fetchCacheLimits();
                if (GroundspeakAPI.APIError > 0) {
                    GL.that.Toast(GroundspeakAPI.LastAPIError);
                    return;
                }
                resetUi();
                showDownloadButton();
            }
        });

        if (GroundspeakAPI.me.remaining == -1)
            getLimitThread.start();

        float contentWidth = this.getWidth() * 0.95f;

        LiveIcon = new Image(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight, "LIVE-ICON", false);
        LiveIcon.setSprite(Sprites.LiveBtn.get(0), false);

        this.addChild(LiveIcon);

        PowerdBy = new Label("");

        PowerdBy.setHeight(Fonts.Measure(STRING_POWERD_BY).height + (margin * 2));
        PowerdBy.setFont(Fonts.getNormal()).setHAlignment(HAlignment.CENTER);
        PowerdBy.setWidth(contentWidth);
        PowerdBy.setWrappedText(STRING_POWERD_BY);
        this.addChild(PowerdBy);

        MessageLabel = new Label("");
        MessageLabel.setWidth(contentWidth);
        MessageLabel.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
        MessageLabel.setHeight(this.getHalfHeight());

        MessageLabel.setWrappedText(getMessage());
        this.addChild(MessageLabel);

        downloadButton = new Button(Translation.Get("DownloadDetails"));
        downloadButton.setWidth(this.getWidth() * 0.8f);

        this.addChild(downloadButton);

        downloadButton.setOnClickListener(downloadClicked);

        if (GroundspeakAPI.me.remaining <= 0)
            downloadButton.disable();
        layout();
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);

        if (PowerdBy != null) {
            if (Line == null) {
                float strokeWidth = 3 * UI_Size_Base.that.getScale();

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
        boolean basic = !IsPremiumMember();
        String MemberType = basic ? BASIC : PREMIUM;
        String limit = basic ? BASIC_LIMIT : PREMIUM_LIMIT;
        String actLimit = Integer.toString(GroundspeakAPI.me.remaining - 1);

        if (GroundspeakAPI.me.remaining == -1) {
            actLimit = "?";
        }

        sb.append(Translation.Get("LiveDescMessage", MemberType, limit));
        sb.append(Global.br);
        sb.append(Global.br);
        if (GroundspeakAPI.me.remaining > 0)
            sb.append(Translation.Get("LiveDescAfter", actLimit));

        if (GroundspeakAPI.me.remaining == 0) {
            sb.append(Translation.Get("LiveDescLimit"));
            sb.append(Global.br);
            sb.append(Global.br);
            if (basic)
                sb.append(Translation.Get("LiveDescLimitBasic"));

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
