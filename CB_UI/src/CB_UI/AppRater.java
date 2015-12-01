package CB_UI;


import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Plattform;

public class AppRater
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(AppRater.class);
	private final static String APP_TITLE = "Cachebox";
	private final static String APP_PACKAGE_NAME = "de.droidcachebox";

	private final static int DAYS_UNTIL_PROMPT = 30;// 30;
	private final static int LAUNCHES_UNTIL_PROMPT = 15;// 15;
	private final static int MINIMUM_RUN = 10 * 60 * 1000;// 10 min

	public static void app_launched()
	{
		if (Config.AppRaterDontShowAgain.getValue()) return;

		// Increment launch counter
		final int launch_count = Config.AppRaterlaunchCount.getValue() + 1;
		Timer t = new Timer();
		TimerTask ta = new TimerTask()
		{
			@Override
			public void run()
			{
				Config.AppRaterlaunchCount.setValue(launch_count);
				Config.AcceptChanges();
				log.info("10 min usage, increment launch count");
			}
		};
		t.schedule(ta, MINIMUM_RUN);

		// Get date of first launch
		String dateString = Config.AppRaterFirstLunch.getValue();
		Long date_firstLaunch = Long.parseLong(dateString);
		if (date_firstLaunch == 0)
		{
			date_firstLaunch = System.currentTimeMillis();
			Config.AppRaterFirstLunch.setValue(Long.toString(date_firstLaunch));
		}

		// Wait at least n days before opening
		if (launch_count >= LAUNCHES_UNTIL_PROMPT)
		{
			if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000))
			{
				GL.that.RunOnGL(new IRunOnGL()
				{
					@Override
					public void run()
					{
						showRateDialog();
					}
				});
			}
		}

		Config.AcceptChanges();
	}

	private static GL_MsgBox msgBox;

	public static void showRateDialog()
	{
		String message = Translation.Get("Rate_Message", APP_TITLE);
		String title = Translation.Get("Rate_Title", APP_TITLE);
		String now = Translation.Get("Rate_now");
		String later = Translation.Get("Rate_later");
		String never = Translation.Get("Rate_never");

		msgBox = GL_MsgBox.Show(message, title, MessageBoxButtons.YesNoCancel, MessageBoxIcon.Question, new OnMsgBoxClickListener()
		{
			@Override
			public boolean onClick(int which, Object data)
			{
				switch (which)
				{
				case 1:
					// Rate

					StringBuilder sb = new StringBuilder();
					if (Plattform.used == Plattform.Android)
					{
						sb.append("market://details?id=");
					}
					else
					{
						sb.append("https://play.google.com/store/apps/details?id=");
					}

					sb.append(APP_PACKAGE_NAME);

					platformConector.callUrl(sb.toString());
					break;
				case 2:
					// later
					if (msgBox != null) msgBox.close();
					break;
				case 3:
					// never
					Config.AppRaterDontShowAgain.setValue(true);
					Config.AcceptChanges();
					break;
				}
				return true;
			}
		});

		msgBox.button1.setText(now);
		msgBox.button2.setText(later);
		msgBox.button3.setText(never);

	}
}
// see http://androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater
