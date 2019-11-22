package de.droidcachebox;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Plattform;
import de.droidcachebox.utils.log.Log;

import java.util.Timer;
import java.util.TimerTask;

public class AppRater {
    private static final String log = "AppRater";
    private final static String APP_TITLE = "Cachebox";
    private final static String APP_PACKAGE_NAME = "de.droidcachebox";

    private final static int DAYS_UNTIL_PROMPT = 30;// 30;
    private final static int LAUNCHES_UNTIL_PROMPT = 15;// 15;
    private final static int MINIMUM_RUN = 10 * 60 * 1000;// 10 min
    private static MessageBox msgBox;

    public static void app_launched() {
        if (Config.AppRaterDontShowAgain.getValue())
            return;

        // Increment launch counter
        final int launch_count = Config.AppRaterlaunchCount.getValue() + 1;
        Timer t = new Timer();
        TimerTask ta = new TimerTask() {
            @Override
            public void run() {
                Config.AppRaterlaunchCount.setValue(launch_count);
                Config.AcceptChanges();
                Log.info(log, "10 min usage, increment launch count");
            }
        };
        t.schedule(ta, MINIMUM_RUN);

        // Get date of first launch
        String dateString = Config.AppRaterFirstLunch.getValue();
        long date_firstLaunch = Long.parseLong(dateString);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            Config.AppRaterFirstLunch.setValue(Long.toString(date_firstLaunch));
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + (long) DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) {
                GL.that.RunOnGL(AppRater::showRateDialog);
            }
        }

        Config.AcceptChanges();
    }

    private static void showRateDialog() {
        String message = Translation.get("Rate_Message", APP_TITLE);
        String title = Translation.get("Rate_Title", APP_TITLE);
        String now = Translation.get("Rate_now");
        String later = Translation.get("Rate_later");
        String never = Translation.get("Rate_never");

        msgBox = MessageBox.show(message, title, MessageBoxButtons.YesNoCancel, MessageBoxIcon.Question,
                (which, data) -> {
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
                            Config.AppRaterDontShowAgain.setValue(true);
                            Config.AcceptChanges();
                            break;
                    }
                    return true;
                });
        msgBox.setButtonText(MessageBox.BTN_LEFT_POSITIVE, now);
        msgBox.setButtonText(MessageBox.BTN_MIDDLE_NEUTRAL, later);
        msgBox.setButtonText(MessageBox.BTN_RIGHT_NEGATIVE, never);

    }
}
// see http://androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater
