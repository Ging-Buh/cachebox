/*
 * Copyright (C) 2011-2020 team-cachebox.de
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

import java.util.ArrayList;

import de.droidcachebox.database.SQLiteInterface;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.ViewID;
import de.droidcachebox.settings.SettingBase;
import de.droidcachebox.utils.log.Log;

/**
 * This is the possibility of static access to functions that are specific to the platform the app is running on
 */
public class PlatformUIBase {
    public static int AndroidVersion = 999;
    private static Thread threadVibrate;
    private static Methods methods;
    private static IShowViewListener showViewListener;
    private static ArrayList<String> sendToMediaScannerList;
    private static Clipboard clipBoard;

    public static void setShowViewListener(IShowViewListener listener) {
        showViewListener = listener;
    }

    public static void showView(ViewID viewID, float x, float y, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        if (showViewListener != null) {
            GL.that.clearRenderViews();

            int left = (int) (x + leftMargin);
            int right = (int) rightMargin;
            int bottom = (int) (y + bottomMargin);
            int top = (int) topMargin;

            showViewListener.showView(viewID, left, top, right, bottom);
        }
    }

    public static void dayNightSwitched() {
        if (showViewListener != null) {
            showViewListener.dayNightSwitched();
        }
    }

    public static void hideView(ViewID viewID) {
        if (showViewListener != null) {
            showViewListener.hideView(viewID);
        }
    }

    public static void showForDialog() {
        if (showViewListener != null) {
            try {
                GL.that.clearRenderViews();
                showViewListener.showForDialog();
            } catch (Exception ex) {
                Log.err("PlatformUIBase", "showForDialog", ex);
            }
        }
    }

    public static void hideForDialog() {
        if (showViewListener != null) {
            showViewListener.hideForDialog();
        }
    }

    public static void setContentSize(final int left, final int top, final int right, final int bottom) {
        if (showViewListener != null) {
            showViewListener.setContentSize(left, top, right, bottom);
        }
    }

    public static void setMethods(Methods _methods) {
        methods = _methods;
    }

    public static boolean canNotUsePlatformSettings() {
        return (methods == null);
    }

    public static SettingBase<?> readPlatformSetting(SettingBase<?> setting) {
        setting = methods.readPlatformSetting(setting);
        return setting;
    }

    public static <T> void writePlatformSetting(SettingBase<T> setting) {
        methods.writePlatformSetting(setting);
    }

    public static void vibrate() {
        if (threadVibrate == null) {
            threadVibrate = new Thread(() -> methods.vibrate());
        }
        threadVibrate.run(); // do not replace with start()
    }

    public static boolean isOnline() {
        return methods.isOnline();
    }

    public static boolean isGPSon() {
        return methods.isGPSon();
    }

    public static boolean isTorchAvailable() {
        return methods.isTorchAvailable();
    }

    public static boolean isTorchOn() {
        return methods.isTorchOn();
    }

    public static void switchTorch() {
        methods.switchTorch();
    }

    public static void quit() {
        methods.quit();
    }

    public static void getApiKey() {
        methods.getApiKey();
    }

    public static void callUrl(String url) {
        methods.callUrl(url);
    }

    public static void startPictureApp(String file) {
        methods.startPictureApp(file);
    }

    public static SQLiteInterface createSQLInstance() {
        return methods.createSQLInstance();
    }

    public static void freeSQLInstance(SQLiteInterface sqlInstance) {
        methods.freeSQLInstance(sqlInstance);
    }

    public static void switchToGpsMeasure() {
        methods.switchToGpsMeasure();
    }

    public static void switchToGpsDefault() {
        methods.switchToGpsDefault();
    }

    public static void handleExternalRequest() {
        methods.handleExternalRequest();
    }

    public static String removeHtmlEntyties(String text) {
        return methods.removeHtmlEntyties(text);
    }

    public static String getFileProviderContentUrl(String localFile) {
        return methods.getFileProviderContentUrl(localFile);
    }

    public static void getDirectoryAccess(String directory) {
        methods.getDirectoryAccess(directory);
    }

    public static void startRecordTrack() {
        methods.startRecordTrack();
    }

    public static boolean request_getLocationIfInBackground() {
        return methods.request_getLocationIfInBackground();
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
        return methods.getCacheCountInDB(absolutePath);
    }

    public interface IShowViewListener {
        void showView(ViewID viewID, int left, int top, int right, int bottom);

        void setContentSize(int left, int top, int right, int bottom);

        void hideView(ViewID viewID);

        void showForDialog();

        void hideForDialog();

        void dayNightSwitched();

        int getAktViewId();

        void requestLayout();
    }

    public interface Methods {
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

        void freeSQLInstance(SQLiteInterface sqlInstance);

        void quit();

        void handleExternalRequest();

        String removeHtmlEntyties(String text);

        String getFileProviderContentUrl(String localFile);

        void getDirectoryAccess(String directory);

        void startRecordTrack();

        boolean request_getLocationIfInBackground();

        int getCacheCountInDB(String absolutePath);
    }

}
