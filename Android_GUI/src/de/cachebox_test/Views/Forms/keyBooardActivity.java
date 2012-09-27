package de.cachebox_test.Views.Forms;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Custom_Controls.hiddenTextField;

public class keyBooardActivity extends Activity implements OnTouchListener
{

	private static keyBooardActivity that;
	private static hiddenTextField mTextField;
	private static RelativeLayout layout;
	private static boolean isInitial = false;

	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		that = this;
		setContentView(R.layout.key_activity);

		mTextField = new hiddenTextField(this);

		mTextField.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);

		mTextField.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				// if (v == mTextField)
				// {
				// if (hasFocus)
				// {
				// // open keyboard
				// ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mTextField,
				// InputMethodManager.SHOW_FORCED);
				//
				// }
				// else
				// { // close keyboard
				// ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
				// mTextField.getWindowToken(), 0);
				// }
				// }
			}
		});

		mTextField.setOnKeyListener(new OnKeyListener()
		{

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				// nach GL umleiten
				boolean handeld = false;
				isInitial = true;
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == 4)
				{
					that.finish();
					return true;
				}

				if (event.getAction() == KeyEvent.ACTION_UP && getNumericValueFromKeyCode(keyCode) != null)
				{
					return CB_Core.Events.platformConector.sendKey((Character) getNumericValueFromKeyCode(keyCode));
				}

				if (event.getAction() == KeyEvent.ACTION_DOWN)
				{
					if (keyCode == 66)// Enter
					{

						return true;
					}
					else if (keyCode == 67)// Enter
					{

						return true;
					}
					else
					{
						handeld = CB_Core.Events.platformConector.sendKeyDown(keyCode);
					}

				}
				else if (event.getAction() == KeyEvent.ACTION_UP)
				{
					if (keyCode == 66)// Enter
					{
						handeld = CB_Core.Events.platformConector.sendKey('\n');
						return handeld;
					}
					else if (keyCode == 67)// Enter
					{
						// Back
						char BACKSPACE = 8;
						return CB_Core.Events.platformConector.sendKey(BACKSPACE);
					}
					else
					{
						handeld = CB_Core.Events.platformConector.sendKeyUp(keyCode);
					}
				}

				return handeld;
			}

		});

		mTextField.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				try
				{
					boolean handeld = CB_Core.Events.platformConector.sendKeyDown(event.getKeyCode());
					// boolean handeld2 = CB_Core.Events.platformConector.sendKey(chr);
					return handeld;
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}

			}
		});

		mTextField.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// int BreakPoint = 1;
				// if (BreakPoint == 1) BreakPoint++;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// int BreakPoint = 1;
				// if (BreakPoint == 1) BreakPoint++;
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				isInitial = true;
				try
				{
					CB_Core.Events.platformConector.sendKey(s.charAt(s.length() - 1));
					mTextField.selectAll();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		mTextField.setBackgroundDrawable(null);
		mTextField.setClickable(false);

		layout = (RelativeLayout) findViewById(R.id.layoutTextField);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.height = 30;
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layout.addView(mTextField, lp);

		layout.setOnTouchListener(this);

		mTextField.setCursorVisible(true);
		mTextField.setFocusable(true);
		mTextField.setFocusableInTouchMode(true);
		mTextField.requestFocus();

		mTextField.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.showSoftInput(mTextField, 0);

			}
		}, 200);

	}

	protected Object getNumericValueFromKeyCode(int keyCode)
	{
		if (keyCode == KeyEvent.KEYCODE_0) return '0';
		if (keyCode == KeyEvent.KEYCODE_1) return '1';
		if (keyCode == KeyEvent.KEYCODE_2) return '2';
		if (keyCode == KeyEvent.KEYCODE_3) return '3';
		if (keyCode == KeyEvent.KEYCODE_4) return '4';
		if (keyCode == KeyEvent.KEYCODE_5) return '5';
		if (keyCode == KeyEvent.KEYCODE_6) return '6';
		if (keyCode == KeyEvent.KEYCODE_7) return '7';
		if (keyCode == KeyEvent.KEYCODE_8) return '8';
		if (keyCode == KeyEvent.KEYCODE_9) return '9';

		return null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.d("CACHEBOX", "on KeyActivity Key event code " + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			return true;
		}
		return false;
	}

	public static boolean isKeyBoardVisible()
	{
		if (!isInitial) return true;
		if (mTextField.getTop() == 0) return true;
		else if (mTextField.getTop() > 1000) return false;
		return true;
	}

	public static void close()
	{
		if (!isInitial) return;
		isInitial = false;
		that.finish();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		int mOffset[] = new int[2];
		v.getLocationOnScreen(mOffset);

		event.offsetLocation(mOffset[0], mOffset[1]);

		return ((main) main.mainActivity).sendMotionEvent(event);
	}
}
