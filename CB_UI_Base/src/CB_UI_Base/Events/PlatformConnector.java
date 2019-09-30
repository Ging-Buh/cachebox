/*
 * Copyright (C) 2011-2014 team-cachebox.de
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
package CB_UI_Base.Events;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.ViewID;
import CB_Utils.Settings.SettingBase;
import de.cb.sqlite.SQLiteInterface;

import java.util.ArrayList;

/**
 * @author Longri
 */
public class PlatformConnector {
    public static int AndroidVersion = 999;
    private static Thread threadVibrate;
    private static IPlatformListener platformListener;
    private static IShowViewListener showViewListener;
    private static ArrayList<String> sendToMediaScannerList;

    public static void setShowViewListener(IShowViewListener listener) {
        showViewListener = listener;
    }

    public static void showView(ViewID viewID, float x, float y, float width, float height) {
        showView(viewID, x, y, width, height, 0, 0, 0, 0);
    }

    public static void showView(ViewID viewID, float x, float y, float width, float height, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        if (showViewListener != null) {
            GL.that.clearRenderViews();

            int left = (int) (x + leftMargin);
            int right = (int) rightMargin;
            int bottom = (int) (y + bottomMargin);
            int top = (int) topMargin;

            showViewListener.show(viewID, left, top, right, bottom);
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
            GL.that.clearRenderViews();
            showViewListener.showForDialog();
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

    public static SettingBase<?> ReadSetting(SettingBase<?> setting) {
        if (platformListener != null)
            setting = platformListener.readSetting(setting);
        return setting;
    }

    public static <T> void WriteSetting(SettingBase<T> setting) {
        if (platformListener != null)
            platformListener.writeSetting(setting);
    }

    public static boolean canUsePlatformSettings() {
        return (platformListener != null);
    }

    public static void vibrate() {
        if (platformListener != null) {
            if (threadVibrate == null) {
                threadVibrate = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        platformListener.vibrate();
                    }
                });
            }
            threadVibrate.run();
        }
    }

    public static boolean isOnline() {
        if (platformListener != null) {
            return platformListener.isOnline();
        }
        return false;
    }

    public static boolean isGPSon() {
        if (platformListener != null) {
            return platformListener.isGPSon();
        }

        return false;
    }

    public static boolean isTorchAvailable() {
        if (platformListener != null) {
            return platformListener.isTorchAvailable();
        }
        return false;
    }

    public static boolean isTorchOn() {
        if (platformListener != null) {
            return platformListener.isTorchOn();
        }
        return false;
    }

    public static void switchTorch() {
        if (platformListener != null) {
            platformListener.switchTorch();
        }
    }

    public static void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener) {
        if (platformListener != null)
            platformListener.getFile(initialPath, extension, TitleText, ButtonText, returnListener);
    }

    public static void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener) {
        if (platformListener != null)
            platformListener.getFolder(initialPath, TitleText, ButtonText, returnListener);
    }

    public static void quit() {
        if (platformListener != null)
            platformListener.quit();
    }

    public static void getApiKey() {
        if (platformListener != null)
            platformListener.getApiKey();
    }

    public static void setScreenLockTime(int value) {
        if (platformListener != null)
            platformListener.setScreenLockTime(value);
    }

    public static void setPlatformListener(IPlatformListener listener) {
        platformListener = listener;
    }

    public static void handleExternalRequest() {
        // after ViewManager is initialized
        if (platformListener != null)
            platformListener.handleExternalRequest();
    }

    public static void callUrl(String url) {
        if (platformListener != null)
            platformListener.callUrl(url);
    }

    public static void startPictureApp(String file) {
        if (platformListener != null)
            platformListener.startPictureApp(file);
    }

    public static SQLiteInterface getSQLInstance() {
        if (platformListener != null) {
            return platformListener.getSQLInstance();
        } else return null;
    }

    public static void freeSQLInstance(SQLiteInterface sqlInstance) {
        if (platformListener != null) {
            platformListener.freeSQLInstance(sqlInstance);
        }
    }


    public static void switchToGpsMeasure() {
        if (platformListener != null) {
            platformListener.switchToGpsMeasure();
        }
    }

    public static void switchToGpsDefault() {
        if (platformListener != null) {
            platformListener.switchtoGpsDefault();
        }
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

    public interface IShowViewListener {
        void show(ViewID viewID, int left, int top, int right, int bottom);

        void setContentSize(int left, int top, int right, int bottom);

        void hideView(ViewID viewID);

        void showForDialog();

        void hideForDialog();

        void dayNightSwitched();

        void onResume();

        void onDestroyWithFinishing();

        void onDestroyWithoutFinishing();

        int getAktViewId();

        void requestLayout();
    }

    public interface IgetFileReturnListener {
        void returnFile(String PathAndName);
    }

    public interface IgetFolderReturnListener {
        void returnFolder(String Path);
    }

    public interface IPlatformListener {
        SettingBase<?> readSetting(SettingBase<?> setting);

        void writeSetting(SettingBase<?> setting);

        void setScreenLockTime(int value);

        boolean isOnline();

        boolean isGPSon();

        void vibrate();

        boolean isTorchAvailable();

        boolean isTorchOn();

        void switchTorch();

        void switchToGpsMeasure();

        void switchtoGpsDefault();

        void getApiKey();

        void callUrl(String url);

        void handleExternalRequest();

        void startPictureApp(String file);

        SQLiteInterface getSQLInstance();

        void freeSQLInstance(SQLiteInterface sqlInstance);

        void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener);

        void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener);

        void quit();
    }
}
