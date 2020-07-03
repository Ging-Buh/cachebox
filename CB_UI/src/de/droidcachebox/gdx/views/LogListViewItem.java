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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.WrapType;
import de.droidcachebox.database.LogEntry;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.popups.ICopyPaste;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LogListViewItem extends ListViewItemBackground implements ICopyPaste {
    private static NinePatch headerBackground;
    private static float measuredLabelHeight = 0;
    private static float headHeight;
    private LogEntry logEntry;
    private float secondTab = 0;

    public LogListViewItem(CB_RectF rec, int index, LogEntry logEntry) {
        super(rec, index, "");
        setLongClickable(false);
        this.logEntry = logEntry;
        backGroundIsInitialized = false;
        measuredLabelHeight = Fonts.Measure("T").height * 1.5f;
        headHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + (UiSizes.getInstance().getMargin());

        iniImage();
        iniFoundLabel();
        iniDateLabel();
        iniCommentLabel();

        setClickHandler((v1, x, y, pointer, button) -> {
            Menu menu = new Menu("LogEntryContextMenuTitle");
            menu.addMenuItem("LogtextToClipboard", null, this::copyToClipboard);
            menu.addMenuItem("ShowLogInBrowser", null, () -> {
                PlatformUIBase.callUrl("https://www.geocaching.com/seek/log.aspx?LID=" + logEntry.logId);
                // PlatformUIBase.callUrl("https://coord.info/" + getGcIdFromLogId(logEntry.logId));
            });
            menu.addMenuItem("MailToFinder", Sprites.getSprite("bigLetterbox"), () -> {
                try {
                    if (PlatformUIBase.getClipboard() != null) {
                        PlatformUIBase.getClipboard().setContents("Concerning https://coord.info/" + GlobalCore.getSelectedCache().getGeoCacheCode() + " " + GlobalCore.getSelectedCache().getGeoCacheName() + "\r");
                        String finder = URLEncoder.encode(logEntry.finder, "UTF-8");
                        PlatformUIBase.callUrl("https://www.geocaching.com/email/?u=" + finder);
                    }
                } catch (Exception ignored) {
                }
            });
            /*
            // we can't get the Log, cause we are not logged in
            menu.addMenuItem("MessageToFinder", Sprites.getSprite("bigLetterbox"), () -> GL.that.postAsync(() -> {
                try {
                    String mGCCode = GlobalCore.getSelectedCache().getGcCode();
                    try {
                        String page = Webb.create()
                                .get("https://www.geocaching.com/seek/log.aspx?LID=" + logEntry.logId)
                                .ensureSuccess()
                                .asString()
                                .getBody();
                        String toSearch = "guid=";
                        int pos = page.indexOf(toSearch);
                        if (pos > -1) {
                            int start = pos + toSearch.length();
                            int stop = page.indexOf("\"", start);
                            String guid = page.substring(start, stop);
                            PlatformUIBase.callUrl("https://www.geocaching.com/account/messagecenter?recipientId=" + guid + "&gcCode=" + mGCCode);
                        }
                    } catch (Exception ignored) {
                    }
                } catch (Exception ignored) {
                }
            }));
             */
            menu.show();
            return true;
        });
    }

    /*
    // we use
    //          PlatformUIBase.callUrl("https://www.geocaching.com/seek/log.aspx?LID=" + logEntry.logId);
    // instead of            // PlatformUIBase.callUrl("https://coord.info/" + getGcIdFromLogId(logEntry.logId));
    private String getGcIdFromLogId(long logId) {
        String referenceCode;
        if (logId < 65536)
            referenceCode = Long.toHexString(logId); // Decimal to Hex
        else
            referenceCode = base31(logId + 411120);
        return "GL" + referenceCode;
    }

    private String base31(long modLogId) {
        final String base31chars = "0123456789ABCDEFGHJKMNPQRTVWXYZ";
        StringBuilder referenceCode = new StringBuilder();
        while (modLogId > 0) {
            long r = modLogId % 31;
            modLogId = modLogId / 31;
            referenceCode.append(base31chars.charAt((int) r));
        }
        return referenceCode.reverse().toString();
    }

     */


    private void iniImage() {
        Image ivTyp = new Image(getLeftWidth(), getHeight() - (headHeight / 2) - (UiSizes.getInstance().getButtonHeight() / 1.5f / 2), UiSizes.getInstance().getButtonHeight() / 1.5f, UiSizes.getInstance().getButtonHeight() / 1.5f, "", false);
        addChild(ivTyp);
        ivTyp.setDrawable(new SpriteDrawable(Sprites.LogIcons.get(logEntry.logType.getIconID())));
        secondTab = ivTyp.getMaxX() + (UiSizes.getInstance().getMargin() * 2);
    }

    private void iniFoundLabel() {
        CB_Label lblFinder = new CB_Label(name + " lblFinder", secondTab, getHeight() - (headHeight / 2) - (measuredLabelHeight / 2), getWidth() - secondTab - getRightWidth() - UiSizes.getInstance().getMargin(), measuredLabelHeight, logEntry.finder);
        addChild(lblFinder);
    }

    private void iniDateLabel() {
        // SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
        SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        String dateString = postFormater.format(logEntry.logDate);
        float DateLength = Fonts.Measure(dateString).width;

        CB_Label lblDate = new CB_Label(name + " lblDate", getWidth() - getRightWidth() - DateLength, getHeight() - (headHeight / 2) - (measuredLabelHeight / 2), DateLength, measuredLabelHeight, dateString);
        addChild(lblDate);
    }

    // static Member

    private void iniCommentLabel() {
        CB_RectF rectF = new CB_RectF(getLeftWidth(), 0, getWidth() - getLeftWidthStatic() - getRightWidthStatic() - (UiSizes.getInstance().getMargin() * 2), getHeight() - headHeight - UiSizes.getInstance().getMargin());
        EditTextField mComment = new EditTextField(rectF, this, "mComment");
        mComment.setWrapType(WrapType.WRAPPED);
        mComment.setText(logEntry.logText);
        mComment.setEditable(false);
        mComment.setClickable(false);
        mComment.setBackground(null, null);
        mComment.showFromLineNo(0);
        mComment.setCursorPosition(0);
        addChild(mComment);
    }

    @Override
    protected void initialize() {
        headerBackground = new NinePatch(Sprites.getSprite("listrec-header"), 8, 8, 8, 8);
        super.initialize();
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
        if (headerBackground != null) {
            headerBackground.draw(batch, 0, getHeight() - headHeight, getWidth(), headHeight);
        } else {
            resetIsInitialized();
        }

    }

    public boolean onTouchDown(int x, int y, int pointer, int button) {

        isPressed = true;

        return false;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        isPressed = false;

        return false;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        isPressed = false;

        return false;
    }

    @Override
    public String pasteFromClipboard() {
        return null;
    }

    @Override
    public String copyToClipboard() {
        if (PlatformUIBase.getClipboard() != null) {
            PlatformUIBase.getClipboard().setContents(logEntry.logText);
            // GL.that.toast(Translation.get("CopyToClipboard"));
            return logEntry.logText;
        }
        return "";
    }

    @Override
    public String cutToClipboard() {
        return null;
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
