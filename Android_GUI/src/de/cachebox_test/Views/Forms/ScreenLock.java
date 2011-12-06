package de.cachebox_test.Views.Forms;

import de.cachebox_test.R;
import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ScreenLock extends Activity
{
	public static boolean SliderMoves= false;
	
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenlock);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		RelativeLayout layout = (RelativeLayout) findViewById(R.layout.screenlock);

		Slider = (SeekBar) findViewById(R.id.unlock_slider);
		Slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onStopTrackingTouch(SeekBar arg0)
			{
				SliderMoves=false;
				if (arg0.getProgress() > 80 || arg0.getProgress() < 20) 
				{
					finish();
				}
				else
				{
					SliderBackCount = Slider.getProgress();
					if (SliderBackCount < 50)
					{
						SliderBackCount = 5;
					}
					else
					{
						SliderBackCount = -5;
					}
					counter = new MyCount(1000, 30);
					counter.start();

				}

			}

			private int lastValue;

			@Override
			public void onStartTrackingTouch(SeekBar arg0)
			{
				SliderMoves=true;
				if (counter != null) counter.cancel();
				lastValue = arg0.getProgress();
			}

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2)
			{
				int setvalue = arg1;
				if (arg1 > 50)// springen verhindern
				{
					if (lastValue + 10 < arg1) setvalue = 50;
				}
				else
				{
					if (lastValue - 10 > arg1) setvalue = 50;
				}
				arg0.setProgress(setvalue);
				lastValue = setvalue;
			}
		});

	}

	static SeekBar Slider;
	static int SliderBackCount = 0;
	MyCount counter = null;

	private class MyCount extends CountDownTimer
	{
		public MyCount(long millisInFuture, long countDownInterval)
		{
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish()
		{

			// Toast.makeText(getApplicationContext(), "timer",
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			if (ScreenLock.Slider.getProgress() < 45 || ScreenLock.Slider.getProgress() > 55)
			{
				ScreenLock.Slider.setProgress(ScreenLock.Slider.getProgress() + SliderBackCount);
			}
			else
			{
				ScreenLock.Slider.setProgress(50);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.d("SolHunter", "Key event code " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			return true;
		}
		return false;
	}

}
