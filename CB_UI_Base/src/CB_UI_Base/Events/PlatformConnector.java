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

import CB_UI_Base.GL_UI.ViewID;
import CB_UI_Base.GL_UI.GL_Listener.GL;

/**
 * @author Longri
 */
public class PlatformConnector {
	/**
	 * Interface definition for a callback to be invoked when a platform must show a view.
	 */
	public interface IShowViewListener {
		void show(ViewID viewID, int left, int top, int right, int bottom);

		void setContentSize(int left, int top, int right, int bottom);

		void hide(ViewID viewID);

		void showForDialog();

		void hideForDialog();

		void dayNightSwitched();

		void firstShow();

	}

	private static IShowViewListener showViewListener;

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

	private static IHardwarStateListener hardwareListener;

	public static void setisOnlineListener(IHardwarStateListener listener) {
		hardwareListener = listener;
	}

	static Thread threadVibrate;

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

	private static KeyEventListener mKeyListener;

	public interface KeyEventListener {
		public boolean onKeyPressed(Character character);

		public boolean keyUp(int KeyCode);

		public boolean keyDown(int keycode);
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

	// ------ get File from File Dialog ------

	public interface IgetFileReturnListener {
		public void returnFile(String Path);
	}

	public interface IgetFileListener {
		public void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener);
	}

	private static IgetFileListener getFileListener;

	public static void setGetFileListener(IgetFileListener listener) {
		getFileListener = listener;
	}

	public static void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener) {
		if (getFileListener != null)
			getFileListener.getFile(initialPath, extension, TitleText, ButtonText, returnListener);
	}

	// ------ get folder from Folder Dialog ------

	public interface IgetFolderReturnListener {
		public void returnFolder(String Path);
	}

	public interface IgetFolderListener {
		public void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener);
	}

	private static IgetFolderListener getFolderListener;

	public static void setGetFolderListener(IgetFolderListener listener) {
		getFolderListener = listener;
	}

	public static void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener) {
		if (getFolderListener != null)
			getFolderListener.getFolder(initialPath, TitleText, ButtonText, returnListener);
	}

	// ----------------------------------------

	// ------ Quit ------

	public interface IQuit {
		public void Quit();
	}

	static IQuit quitListener;

	public static void setQuitListener(IQuit listener) {
		quitListener = listener;
	}

	public static void callQuit() {
		if (quitListener != null)
			quitListener.Quit();
	}

	// ----------------------------------------

	// ------ GetApiKey ------

	public interface IGetApiKey {
		public void getApiKey();
	}

	static IGetApiKey getApiKeyListener;

	public static void setGetApiKeyListener(IGetApiKey listener) {
		getApiKeyListener = listener;
	}

	public static void callGetApiKeyt() {
		if (getApiKeyListener != null)
			getApiKeyListener.getApiKey();
	}

	// ----------------------------------------

	// ------ setScreenLockTime ------

	public interface IsetScreenLockTime {
		public void setScreenLockTime(int value);
	}

	static IsetScreenLockTime setScreenLockTimeListener;

	public static void setsetScreenLockTimeListener(IsetScreenLockTime listener) {
		setScreenLockTimeListener = listener;
	}

	public static void setScreenLockTime(int value) {
		if (setScreenLockTimeListener != null)
			setScreenLockTimeListener.setScreenLockTime(value);
	}

	// ----------------------------------------

	// ------ setKeybordFocus ------

	public interface IsetKeybordFocus {
		public void setKeybordFocus(boolean value);
	}

	static IsetKeybordFocus setKeybordFocusListener;

	public static void setsetKeybordFocusListener(IsetKeybordFocus listener) {
		setKeybordFocusListener = listener;
	}

	public static void callsetKeybordFocus(boolean value) {
		if (setKeybordFocusListener != null)
			setKeybordFocusListener.setKeybordFocus(value);
	}

	// ----------------------------------------

	// ------ setCallUrl ------

	public interface ICallUrl {
		public void call(String url);
	}

	static ICallUrl CallUrlListener;

	public static void setCallUrlListener(ICallUrl listener) {
		CallUrlListener = listener;
	}

	public static void callUrl(String url) {
		if (CallUrlListener != null)
			CallUrlListener.call(url);
	}

	// ----------------------------------------

	// ----- startPictureApp -----
	public interface iStartPictureApp {
		public void Start(String file);
	}

	public static iStartPictureApp startPictureApp;

	public static void setStartPictureApp(iStartPictureApp listener) {
		startPictureApp = listener;
	}

	public static void StartPictureApp(String file) {
		if (startPictureApp != null)
			startPictureApp.Start(file);
	}

	// -----------------------------------------

	public static void switchToGpsMeasure() {
		if (hardwareListener != null) {
			hardwareListener.switchToGpsMeasure();
			;
		}
	}

	public static void switchToGpsDefault() {
		if (hardwareListener != null) {
			hardwareListener.switchtoGpsDefault();
		}
	}

}
