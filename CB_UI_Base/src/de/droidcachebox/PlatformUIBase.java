/*
 * Copyright (C) 2011-2022 team-cachebox.de
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
package de.droidcachebox;

import com.badlogic.gdx.utils.Clipboard;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import de.droidcachebox.database.SQLiteInterface;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.ViewID;
import de.droidcachebox.settings.SettingBase;
import de.droidcachebox.utils.StringReturner;
import de.droidcachebox.utils.log.Log;

/**
 * This is the possibility of static access to functions that are specific to the platform the app is running on
 */
public class PlatformUIBase {
    public static int AndroidVersion = 999;
    private static UIBaseMethods m;
    private static ShowViewMethods sm;
    private static Thread threadVibrate;
    private static Clipboard clipBoard;
    private static ArrayList<String> sendToMediaScannerList;

    public static void initShowViewMethods(ShowViewMethods showViewMethods) {
        sm = showViewMethods;
    }

    public static void showView(ViewID viewID, float x, float y, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        GL.that.clearRenderViews();

        int left = (int) (x + leftMargin);
        int right = (int) rightMargin;
        int bottom = (int) (y + bottomMargin);
        int top = (int) topMargin;

        sm.showView(viewID, left, top, right, bottom);
    }

    public static void dayNightSwitched() {
        sm.dayNightSwitched();
    }

    public static void hideView(ViewID viewID) {
        sm.hideView(viewID);
    }

    public static void showForDialog() {
        try {
            GL.that.clearRenderViews();
            sm.showForDialog();
        } catch (Exception ex) {
            Log.err("PlatformUIBase", "showForDialog", ex);
        }
    }

    public static void hideForDialog() {
        sm.hideForDialog();
    }

    public static void setContentSize(final int left, final int top, final int right, final int bottom) {
        sm.setContentSize(left, top, right, bottom);
    }

    public static void init(UIBaseMethods _m) {
        m = _m;
    }

    public static boolean canNotUsePlatformSettings() {
        return (m == null);
    }

    public static SettingBase<?> readPlatformSetting(SettingBase<?> setting) {
        setting = m.readPlatformSetting(setting);
        return setting;
    }

    public static <T> void writePlatformSetting(SettingBase<T> setting) {
        m.writePlatformSetting(setting);
    }

    public static void vibrate() {
        if (threadVibrate == null) {
            threadVibrate = new Thread(() -> m.vibrate());
        }
        threadVibrate.run(); // do not replace with start() (or always create new Thread)
    }

    public static boolean isOnline() {
        return m.isOnline();
    }

    public static boolean isGPSon() {
        return m.isGPSon();
    }

    public static boolean isTorchAvailable() {
        return m.isTorchAvailable();
    }

    public static boolean isTorchOn() {
        return m.isTorchOn();
    }

    public static void switchTorch() {
        m.switchTorch();
    }

    public static void quit() {
        m.quit();
    }

    public static void getApiKey() {
        m.getApiKey();
    }

    public static void callUrl(String url) {
        m.callUrl(url);
    }

    public static void startPictureApp(String file) {
        m.startPictureApp(file);
    }

    public static SQLiteInterface createSQLInstance() {
        return m.createSQLInstance();
    }

    public static void switchToGpsMeasure() {
        m.switchToGpsMeasure();
    }

    public static void switchToGpsDefault() {
        m.switchToGpsDefault();
    }

    public static void handleExternalRequest() {
        m.handleExternalRequest();
    }

    public static String removeHtmlEntyties(String text) {
        return m.removeHtmlEntyties(text);
    }

    public static String getFileProviderContentUrl(String localFile) {
        return m.getFileProviderContentUrl(localFile);
    }

    public static void getDirectoryAccess(String directory, StringReturner stringReturner) {
        m.getDirectoryAccess(directory, stringReturner);
    }

    public static void getDocumentAccess(String directory, StringReturner stringReturner) {
        m.getDocumentAccess(directory, stringReturner);
    }

    public static boolean request_getLocationIfInBackground() {
        return m.request_getLocationIfInBackground();
    }

    public static ArrayList<String> getMediaScannerList() {
        return sendToMediaScannerList;
    }

    public static void addToMediaScannerList(String filename) {
        if (sendToMediaScannerList == null) {
            sendToMediaScannerList = new ArrayList<>();
        }
        if (sendToMediaScannerList.contains(filename)) return;
        sendToMediaScannerList.add(filename);
    }

    public static Clipboard getClipboard() {
        if (clipBoard == null) {
            return null;
        } else {
            return clipBoard;
        }
    }

    public static void setClipboard(Clipboard clipBoard) {
        PlatformUIBase.clipBoard = clipBoard;
    }

    public static int getCacheCountInDB(String absolutePath) {
        return m.getCacheCountInDB(absolutePath);
    }

    public static InputStream getInputStream(String absolutePath) throws FileNotFoundException {
        return m.getInputStream(absolutePath);
    }

    public static OutputStream getOutputStream(String contentFile) throws FileNotFoundException {
        return m.getOutputStream(contentFile);
    }

    /**
     these methods need platform specific implementations
     */
    public interface ShowViewMethods {
        void showView(ViewID viewID, int left, int top, int right, int bottom);

        void setContentSize(int left, int top, int right, int bottom);

        void hideView(ViewID viewID);

        void showForDialog();

        void hideForDialog();

        void dayNightSwitched();

        int getAktViewId();

        void requestLayout();
    }

    /**
     these methods need platform specific implementations
     */
    public interface UIBaseMethods {
        SettingBase<?> readPlatformSetting(SettingBase<?> setting);

        void writePlatformSetting(SettingBase<?> setting);

        boolean isOnline();

        boolean isGPSon();

        void vibrate();

        boolean isTorchAvailable();

        boolean isTorchOn();

        void switchTorch();

        void switchToGpsMeasure();

        void switchToGpsDefault();

        void getApiKey();

        void callUrl(String url);

        void startPictureApp(String file);

        SQLiteInterface createSQLInstance();

        void quit();

        void handleExternalRequest();

        String removeHtmlEntyties(String text);

        String getFileProviderContentUrl(String localFile);

        void getDirectoryAccess(String directory, StringReturner value);

        void getDocumentAccess(String directory, StringReturner value);

        InputStream getInputStream(String absolutePath) throws FileNotFoundException;

        OutputStream getOutputStream(String contentFile) throws FileNotFoundException;

        boolean request_getLocationIfInBackground();

        int getCacheCountInDB(String absolutePath);
    }

}
