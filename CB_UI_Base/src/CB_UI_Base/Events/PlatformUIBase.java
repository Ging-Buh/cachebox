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
import CB_UI_Base.graphics.extendedInterfaces.ext_GraphicFactory;
import CB_Utils.Settings.SettingBase;
import com.badlogic.gdx.utils.Clipboard;
import de.cb.sqlite.SQLiteInterface;
import org.mapsforge.core.graphics.GraphicFactory;

import java.io.IOException;
import java.util.ArrayList;

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

    public static void setMethods(Methods methods) {
        PlatformUIBase.methods = methods;
    }

    public static SettingBase<?> readPlatformSetting(SettingBase<?> setting) {
        if (methods != null)
            setting = methods.readPlatformSetting(setting);
        return setting;
    }

    public static <T> void writePlatformSetting(SettingBase<T> setting) {
        if (methods != null)
            methods.writePlatformSetting(setting);
    }

    public static boolean canUsePlatformSettings() {
        return (methods != null);
    }

    public static void vibrate() {
        if (methods != null) {
            if (threadVibrate == null) {
                threadVibrate = new Thread(() -> {
                    methods.vibrate();
                });
            }
            threadVibrate.run();
        }
    }

    public static boolean isOnline() {
        if (methods != null) {
            return methods.isOnline();
        }
        return false;
    }

    public static boolean isGPSon() {
        if (methods != null) {
            return methods.isGPSon();
        }

        return false;
    }

    public static boolean isTorchAvailable() {
        if (methods != null) {
            return methods.isTorchAvailable();
        }
        return false;
    }

    public static boolean isTorchOn() {
        if (methods != null) {
            return methods.isTorchOn();
        }
        return false;
    }

    public static void switchTorch() {
        if (methods != null) {
            methods.switchTorch();
        }
    }

    public static void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener) {
        if (methods != null)
            methods.getFile(initialPath, extension, TitleText, ButtonText, returnListener);
    }

    public static void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener) {
        if (methods != null)
            methods.getFolder(initialPath, TitleText, ButtonText, returnListener);
    }

    public static void quit() {
        if (methods != null)
            methods.quit();
    }

    public static void getApiKey() {
        if (methods != null)
            methods.getApiKey();
    }

    public static void callUrl(String url) {
        if (methods != null)
            methods.callUrl(url);
    }

    public static void startPictureApp(String file) {
        if (methods != null)
            methods.startPictureApp(file);
    }

    public static SQLiteInterface getSQLInstance() {
        if (methods != null) {
            return methods.getSQLInstance();
        } else return null;
    }

    public static void freeSQLInstance(SQLiteInterface sqlInstance) {
        if (methods != null) {
            methods.freeSQLInstance(sqlInstance);
        }
    }


    public static void switchToGpsMeasure() {
        if (methods != null) {
            methods.switchToGpsMeasure();
        }
    }

    public static void switchToGpsDefault() {
        if (methods != null) {
            methods.switchtoGpsDefault();
        }
    }

    public static void handleExternalRequest() {
        if (methods != null) {
            methods.handleExternalRequest();
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

    public static byte[] getImageFromFile(String cachedTileFilename) throws IOException {
        return methods.getImageFromFile(cachedTileFilename);
    }

    public static ImageData getImagePixel(byte[] b) {
        return methods.getImagePixel(b);
    }

    public static byte[] getImageFromData(ImageData imageDataWithColorMatrixManipulation) {
        return methods.getImageFromData(imageDataWithColorMatrixManipulation);
    }

    public static GraphicFactory getGraphicFactory(float scaleFactor) {
        return methods.getGraphicFactory(scaleFactor);
    }

    public interface IShowViewListener {
        void show(ViewID viewID, int left, int top, int right, int bottom);

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

        void switchtoGpsDefault();

        void getApiKey();

        void callUrl(String url);

        void startPictureApp(String file);

        SQLiteInterface getSQLInstance();

        void freeSQLInstance(SQLiteInterface sqlInstance);

        void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener);

        void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener);

        void quit();

        void handleExternalRequest();

        byte[] getImageFromFile(String cachedTileFilename) throws IOException;

        ImageData getImagePixel(byte[] img);

        byte[] getImageFromData(ImageData imgData);

        ext_GraphicFactory getGraphicFactory(float Scalefactor);
    }

    public interface IgetFileReturnListener {
        void returnFile(String PathAndName);
    }

    public interface IgetFolderReturnListener {
        void returnFolder(String Path);
    }

    public static class ImageData {
        public int[] PixelColorArray;
        public int width;
        public int height;
    }

}
