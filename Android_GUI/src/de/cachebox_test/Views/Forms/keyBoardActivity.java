package de.cachebox_test.Views.Forms;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.cachebox_test.R;
import de.cachebox_test.main;

public class keyBoardActivity extends Activity implements OnTouchListener
{

	public static keyBoardActivity that;
	private static hiddenTextField mTextField;
	private static RelativeLayout layout;
	public static boolean isInitial = false;
	private String beforeS;
	private int beforeStart;
	private int beforeCount;
	private int beforeAfter;

	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		that = this;
		KeyBoardActivityLayout searchLayout = new KeyBoardActivityLayout(this, null);

		setContentView(searchLayout);

		mTextField = new hiddenTextField(this);

		mTextField.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);

		mTextField.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (!mTextField.hasFocus()) mTextField.requestFocus();
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
				String newText = s.toString().substring(start, start + count);
				String oldText = beforeS.substring(beforeStart, beforeStart + beforeCount);

				// OldText mit newText vergleichen. Alle Zeichen, die in oldText stehen, in newText aber nicht mehr drin sind im Editor
				// löschen
				for (int i = beforeCount; i >= 0; i--)
				{
					if (newText.length() < i)
					{
						// 1 Zeichen aus dem Editor muß mit Sicherheit gelöscht werden!
						char BACKSPACE = 8;
						CB_Core.Events.platformConector.sendKey(BACKSPACE);
						System.out.println("DEL");
					}
					else
					{
						// oldText mit newText vergleichen und zwar immer von Anfang an bis zu i
						String tmpNew = newText.substring(0, i);
						String tmpOld = oldText.substring(0, i);
						if (tmpOld.equals(tmpNew))
						{
							// bis i ist alles gleiche -> nichts mehr muß gelöscht werden
							// Neue Zeichen können eingefügt werden, und zwar ab dem Zeichen i in newText
							for (int j = i; j < newText.length(); j++)
							{
								System.out.println("NEW: " + newText.charAt(j));

								CB_Core.Events.platformConector.sendKey(newText.charAt(j));
							}
							// Fertig
							break;
						}
						else
						{
							// bis i sind noch Unterschiede -> ein Zeichen löschen
							System.out.println("DEL");
							char BACKSPACE = 8;
							CB_Core.Events.platformConector.sendKey(BACKSPACE);
						}
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// int BreakPoint = 1;
				// if (BreakPoint == 1) BreakPoint++;
				beforeS = s.toString();
				beforeStart = start;
				beforeCount = count;
				beforeAfter = after;
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				isInitial = true;
				try
				{
					// CB_Core.Events.platformConector.sendKey(s.charAt(s.length() - 1));
					// mTextField.selectAll();
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

		showSoftKeyBord();

	}

	boolean wasVisible = false;

	@Override
	protected void onPause()
	{
		// wenn die Activity ein on Pause erhällt, wird sie Kommplett geschlossen
		if (isFinishing())
		{
			isInitial = false;
			wasVisible = false;
		}
		else
		{
			Log.d("CACHEBOX_INPUT", "onPause => force finish");
			this.finish();
		}
		super.onPause();
	}

	@Override
	protected void onStart()
	{
		isInitial = true;
		super.onStart();
	}

	public void showSoftKeyBord()
	{
		mTextField.setCursorVisible(true);
		mTextField.setFocusable(true);
		mTextField.setFocusableInTouchMode(true);

		mTextField.requestFocus();

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{
			if (event.getAction() == KeyEvent.ACTION_UP)
			{
				// Close the Activity if Back-Key Pressed, like hide soft-KeyBoard
				this.finish();
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
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

	public static boolean isKeyBoardVisible()
	{
		if (!isInitial) return true;
		if (mTextField.getTop() == 0) return true;
		else if (mTextField.getTop() > 1000) return false;
		return true;
	}

	public static void close()
	{
		Log.d("CACHEBOX_INPUT", "close()");
		if (!isInitial) return;
		isInitial = false;
		that.finish();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// int mOffset[] = new int[2];
		// v.getLocationOnScreen(mOffset);
		//
		// mOffset[1] *= -1;
		//
		// event.offsetLocation(mOffset[0], mOffset[1]);

		return ((main) main.mainActivity).sendMotionEvent(event);
	}

	// ''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

	// ###########################################################

	private class hiddenTextField extends EditText
	{

		public hiddenTextField(Context context)
		{
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			canvas.drawColor(Color.TRANSPARENT);

			// Debug
			// canvas.drawColor(Color.argb(50, 0, 0, 255));
		}

		@Override
		public boolean onKeyPreIme(int keyCode, KeyEvent event)
		{
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			{
				close();
			}
			return super.onKeyPreIme(keyCode, event);
		}

	}

	public class KeyBoardActivityLayout extends LinearLayout
	{

		public KeyBoardActivityLayout(Context context, AttributeSet attributeSet)
		{
			super(context, attributeSet);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.key_activity, this);

		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			Log.d("CACHEBOX_INPUT", "Handling Keyboard Window shown");

			final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
			final int actualHeight = getHeight();

			if (actualHeight >= proposedheight)
			{
				// Keyboard is shown
				wasVisible = true;
				Log.d("CACHEBOX_INPUT", "Keyboard is shown");
			}
			else
			{
				// Keyboard is hidden
				Log.d("CACHEBOX_INPUT", "Keyboard is hidden");

				if (wasVisible) close();
			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}
