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
import de.cb.sqlite.Database_Core;
import de.cb.sqlite.SQLiteInterface;

import java.util.ArrayList;

/**
 * @author Longri
 */
public class PlatformConnector {
    public static iStartPictureApp startPictureApp;
    static Thread threadVibrate;
    static IQuit quitListener;
    static IGetApiKey getApiKeyListener;
    static IsetScreenLockTime setScreenLockTimeListener;
    static ICallUrl CallUrlListener;
    private static IShowViewListener showViewListener;
    private static IHardwarStateListener hardwareListener;
    private static KeyEventListener mKeyListener;
    private static IgetFileListener getFileListener;
    private static IgetFolderListener getFolderListener;
    private static ArrayList<String> sendToMediaScannerList;
    private static IConnection connection;

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

    public static void FirstShow() {
        if (showViewListener != null) {
            showViewListener.firstShow();
        }
    }

    public static void DayNightSwitched() {
        if (showViewListener != null) {
            showViewListener.dayNightSwitched();
        }
    }

    public static void hideView(ViewID viewID) {
        if (showViewListener != null) {
            showViewListener.hide(viewID);
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

    public static void setisOnlineListener(IHardwarStateListener listener) {
        hardwareListener = listener;
    }

    public static void vibrate() {
        if (hardwareListener != null) {
            if (threadVibrate == null)
                threadVibrate = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        hardwareListener.vibrate();
                    }
                });
            threadVibrate.run();
        }

    }

    public static boolean isOnline() {
        if (hardwareListener != null) {
            return hardwareListener.isOnline();
        }

        return false;
    }

    public static boolean isGPSon() {
        if (hardwareListener != null) {
            return hardwareListener.isGPSon();
        }

        return false;
    }

    public static boolean isTorchAvailable() {
        if (hardwareListener != null) {
            return hardwareListener.isTorchAvailable();
        }
        return false;
    }

    public static boolean isTorchOn() {
        if (hardwareListener != null) {
            return hardwareListener.isTorchOn();
        }
        return false;
    }

    public static void switchTorch() {
        if (hardwareListener != null) {
            hardwareListener.switchTorch();
        }
    }

    public static void setKeyEventListener(KeyEventListener listener) {
        mKeyListener = listener;
    }

    public static boolean sendKey(Character character) {
        if (mKeyListener != null) {
            return mKeyListener.onKeyPressed(character);
        }

        return false;
    }

    public static boolean sendKeyDown(int KeyCode) {
        if (mKeyListener != null) {
            return mKeyListener.keyDown(KeyCode);
        }

        return false;
    }

    public static boolean sendKeyUp(int KeyCode) {
        if (mKeyListener != null) {
            return mKeyListener.keyUp(KeyCode);
        }

        return false;
    }

    public static void setGetFileListener(IgetFileListener listener) {
        getFileListener = listener;
    }

    public static void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener) {
        if (getFileListener != null)
            getFileListener.getFile(initialPath, extension, TitleText, ButtonText, returnListener);
    }

    public static void setGetFolderListener(IgetFolderListener listener) {
        getFolderListener = listener;
    }

    public static void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener) {
        if (getFolderListener != null)
            getFolderListener.getFolder(initialPath, TitleText, ButtonText, returnListener);
    }

    public static void setQuitListener(IQuit listener) {
        quitListener = listener;
    }

    public static void callQuit() {
        if (quitListener != null)
            quitListener.Quit();
    }

    public static void setGetApiKeyListener(IGetApiKey listener) {
        getApiKeyListener = listener;
    }

    public static void callGetApiKey() {
        if (getApiKeyListener != null)
            getApiKeyListener.getApiKey();
    }

    public static void setScreenLockTimeListener(IsetScreenLockTime listener) {
        setScreenLockTimeListener = listener;
    }

    public static void setScreenLockTime(int value) {
        if (setScreenLockTimeListener != null)
            setScreenLockTimeListener.setScreenLockTime(value);
    }

    public static void setCallUrlListener(ICallUrl listener) {
        CallUrlListener = listener;
    }

    public static void callUrl(String url) {
        if (CallUrlListener != null)
            CallUrlListener.call(url);
    }

    public static void setStartPictureApp(iStartPictureApp listener) {
        startPictureApp = listener;
    }

    public static void StartPictureApp(String file) {
        if (startPictureApp != null)
            startPictureApp.Start(file);
    }

    public static void switchToGpsMeasure() {
        if (hardwareListener != null) {
            hardwareListener.switchToGpsMeasure();
        }
    }

    public static void switchToGpsDefault() {
        if (hardwareListener != null) {
            hardwareListener.switchtoGpsDefault();
        }
    }

    public static ArrayList<String> getMediaScannerList() {
        return sendToMediaScannerList;
    }

    public static void addToMediaScannerList(String filename) {
        if (sendToMediaScannerList == null) {
            sendToMediaScannerList = new ArrayList<String>();
        }
        if (sendToMediaScannerList.contains(filename)) return;
        sendToMediaScannerList.add(filename);
    }

    public static void setConnection(IConnection c) {
        connection = c;
    }

    public static SQLiteInterface getSQLInstance() {
        if (connection != null) {
            return connection.getSQLInstance();
        }
        else return null;
    }

    public static void freeSQLInstance(SQLiteInterface sqlInstance) {
        if (connection != null) {
            connection.freeSQLInstance(sqlInstance);
        }
    }

    public interface IConnection {
        SQLiteInterface getSQLInstance();
        void freeSQLInstance(SQLiteInterface sqlInstance);
    }

    public interface IShowViewListener {
        void show(ViewID viewID, int left, int top, int right, int bottom);

        void setContentSize(int left, int top, int right, int bottom);

        void hide(ViewID viewID);

        void showForDialog();

        void hideForDialog();

        void dayNightSwitched();

        void firstShow();
    }

    public interface IHardwarStateListener {
        boolean isOnline();

        boolean isGPSon();

        void vibrate();

        boolean isTorchAvailable();

        boolean isTorchOn();

        void switchTorch();

        void switchToGpsMeasure();

        void switchtoGpsDefault();
    }

    public interface KeyEventListener {
        boolean onKeyPressed(Character character);

        boolean keyUp(int KeyCode);

        boolean keyDown(int keycode);
    }

    public interface IgetFileReturnListener {
        void returnFile(String PathAndName);
    }

    public interface IgetFileListener {
        void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener);
    }

    public interface IgetFolderReturnListener {
        void returnFolder(String Path);
    }

    // ----------------------------------------

    public interface IgetFolderListener {
        void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener);
    }

    public interface IQuit {
        void Quit();
    }

    public interface IGetApiKey {
        void getApiKey();
    }

    public interface IsetScreenLockTime {
        void setScreenLockTime(int value);
    }

    public interface ICallUrl {
        void call(String url);
    }

    // ----- startPictureApp -----
    public interface iStartPictureApp {
        void Start(String file);
    }
}
