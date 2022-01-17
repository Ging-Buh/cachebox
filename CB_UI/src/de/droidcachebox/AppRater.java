package de.droidcachebox;

import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_LEFT_POSITIVE;
import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_MIDDLE_NEUTRAL;
import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_RIGHT_NEGATIVE;

import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Plattform;
import de.droidcachebox.utils.log.Log;

public class AppRater {
    private static final String log = "AppRater";
    private final static String APP_TITLE = "Cachebox";
    private final static String APP_PACKAGE_NAME = "de.droidcachebox";

    private final static int DAYS_UNTIL_PROMPT = 30;// 30;
    private final static int LAUNCHES_UNTIL_PROMPT = 15;// 15;
    private final static int MINIMUM_RUN = 10 * 60 * 1000;// 10 min

    public static void app_launched() {
        if (Settings.AppRaterDontShowAgain.getValue())
            return;

        // Increment launch counter
        final int launch_count = Settings.AppRaterlaunchCount.getValue() + 1;
        Timer t = new Timer();
        TimerTask ta = new TimerTask() {
            @Override
            public void run() {
                Settings.AppRaterlaunchCount.setValue(launch_count);
                Settings.getInstance().acceptChanges();
                Log.info(log, "10 min usage, increment launch count");
            }
        };
        t.schedule(ta, MINIMUM_RUN);

        // Get date of first launch
        String dateString = Settings.AppRaterFirstLunch.getValue();
        long date_firstLaunch = Long.parseLong(dateString);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            Settings.AppRaterFirstLunch.setValue(Long.toString(date_firstLaunch));
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + (long) DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) {
                GL.that.runOnGL(AppRater::showRateDialog);
            }
        }

        Settings.getInstance().acceptChanges();
    }

    private static void showRateDialog() {
        String message = Translation.get("Rate_Message", APP_TITLE);
        String title = Translation.get("Rate_Title", APP_TITLE);
        String now = Translation.get("Rate_now");
        String later = Translation.get("Rate_later");
        String never = Translation.get("Rate_never");

        ButtonDialog msgBox = new ButtonDialog(message, title, MsgBoxButton.YesNoCancel, MsgBoxIcon.Question);
        msgBox.setButtonClickHandler((which, data) -> {
            switch (which) {
                case 1:
                    // Rate

                    StringBuilder sb = new StringBuilder();
                    if (Plattform.used == Plattform.Android) {
                        sb.append("market://details?id=");
                    } else {
                        sb.append("https://play.google.com/store/apps/details?id=");
                    }

                    sb.append(APP_PACKAGE_NAME);

                    PlatformUIBase.callUrl(sb.toString());
                    break;
                case 2:
                    // later
                    if (msgBox != null)
                        msgBox.close();
                    break;
                case 3:
                    // never
                    Settings.AppRaterDontShowAgain.setValue(true);
                    Settings.getInstance().acceptChanges();
                    break;
            }
            return true;
        });

        msgBox.setButtonText(BTN_LEFT_POSITIVE, now);
        msgBox.setButtonText(BTN_MIDDLE_NEUTRAL, later);
        msgBox.setButtonText(BTN_RIGHT_NEGATIVE, never);
        msgBox.show();

    }
}
// see http://androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater
