/* 
 * Copyright (C) 2011 team-cachebox.de
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

package de.droidcachebox.Ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.StaticLayout;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;

;

public class ActivityUtils
{
	private static int sTheme = 1;

	public final static int THEME_DEFAULT = 0;
	public final static int THEME_DAY = 1;
	public final static int THEME_NIGHT = 2;

	public static void changeToTheme(Activity activity, int theme)
	{
		changeToTheme(activity, theme, false);
	}

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme, boolean firstStart)
	{
		sTheme = theme;
		main.isRestart = true;
		main.isFirstStart = firstStart;
		activity.finish();

		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity)
	{
		switch (sTheme)
		{
		default:
		case THEME_DEFAULT:
			break;
		case THEME_DAY:
			activity.setTheme(R.style.Theme_day);
			break;
		case THEME_NIGHT:
			activity.setTheme(R.style.Theme_night);
			break;

		}

		Global.initTheme(activity);

	}

	public static int drawStaticLayout(Canvas canvas, StaticLayout layout, int x, int y)
	{
		canvas.translate(x, y);
		layout.draw(canvas);
		canvas.translate(-x, -y);
		return layout.getHeight();
	}

	public static void drawFillRoundRecWithBorder(Canvas canvas, Rect rec, int BorderSize, int BorderColor, int FillColor)
	{
		drawFillRoundRecWithBorder(canvas, rec, BorderSize, BorderColor, FillColor, Sizes.getCornerSize());
	}

	public static void drawFillRoundRecWithBorder(Canvas canvas, Rect rec, int BorderSize, int BorderColor, int FillColor, int CornerSize)
	{
		Paint drawPaint = new Paint();
		drawPaint.setAntiAlias(true);
		drawPaint.setStyle(Style.STROKE);
		drawPaint.setStrokeWidth(BorderSize);

		final Rect outerRect = rec;
		final RectF OuterRectF = new RectF(outerRect);

		drawPaint.setColor(BorderColor);
		canvas.drawRoundRect(OuterRectF, CornerSize, CornerSize, drawPaint);

		final Rect rect = new Rect(rec.left + BorderSize, rec.top + BorderSize, rec.right - BorderSize, rec.bottom - BorderSize);
		final RectF rectF = new RectF(rect);

		drawPaint.setColor(FillColor);
		// drawPaint.setShader(new LinearGradient(0, 0, 0, rec.height(),
		// FillColor, Color.WHITE, Shader.TileMode.MIRROR)); nur ein versuch
		drawPaint.setStyle(Style.FILL_AND_STROKE);
		canvas.drawRoundRect(rectF, CornerSize - BorderSize, CornerSize - BorderSize, drawPaint);

	}

	// / <summary>
	// / Zeichnet das Bild und skaliert es proportional so, dass es die
	// / übergebene füllt.
	// / </summary>
	// / <param name="graphics"></param>
	// / <param name="image"></param>
	// / <param name="x"></param>
	// / <param name="y"></param>
	// / <param name="height"></param>
	public static int PutImageTargetHeight(Canvas canvas, Drawable image, int x, int y, int height)
	{
		// float scale = (float)height / (float)image.getBounds().height();
		// int width = (int)Math.round(image.getBounds().width() * scale);

		float scale = (float) height / (float) image.getIntrinsicHeight();
		int width = (int) Math.round((float) image.getIntrinsicWidth() * scale);

		Rect oldBounds = image.getBounds();
		image.setBounds(x, y, x + width, y + height);
		image.draw(canvas);
		image.setBounds(oldBounds);

		return width;
	}

	public static int PutImageTargetHeightColor(Canvas canvas, Drawable image, int x, int y, int height, int color)
	{
		return PutImageTargetHeightColor(canvas, image, x, y, height, color, Mode.MULTIPLY);
	}

	public static int PutImageTargetHeightColor(Canvas canvas, Drawable image, int x, int y, int height, int color, Mode porterDuff)
	{

		float scale = (float) height / (float) image.getIntrinsicHeight();
		int width = (int) Math.round(image.getIntrinsicWidth() * scale);

		Rect oldBounds = image.getBounds();
		image.setBounds(x, y, x + width, y + height);
		image.setColorFilter(color, porterDuff);
		image.draw(canvas);
		image.setBounds(oldBounds);
		image.clearColorFilter();
		return width;
	}

	// / <summary>
	// / Zeichnet das Bild und skaliert es proportional so, dass es die
	// / übergebene füllt.
	// / </summary>
	// / <param name="graphics"></param>
	// / <param name="image"></param>
	// / <param name="x"></param>
	// / <param name="y"></param>
	// / <param name="height"></param>
	public static int PutImageTargetHeight(Canvas canvas, Drawable image, double Angle, int x, int y, int newHeight)
	{

		float scale = (float) newHeight / (float) image.getIntrinsicHeight();
		float newWidth = (int) Math.round((float) image.getIntrinsicWidth() * scale);

		Bitmap bmp = ((BitmapDrawable) image).getBitmap();
		int width = bmp.getWidth();
		int height = bmp.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// rotate the Bitmap
		matrix.postRotate((float) Angle);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
		// make a Drawable from Bitmap to allow to set the BitMap
		// to the ImageView, ImageButton or what ever
		BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);

		bmd.setBounds(x, y, x + bmd.getIntrinsicWidth(), y + bmd.getIntrinsicHeight());
		bmd.draw(canvas);

		return bmd.getIntrinsicWidth();

	}

	public static int PutImageScale(Canvas canvas, Drawable image, double Angle, int x, int y, double scale)
	{

		if (scale == 0.0) return 0;

		float newWidth = (int) Math.round((float) image.getIntrinsicWidth() * scale);
		float newHeight = (int) Math.round((float) image.getIntrinsicHeight() * scale);

		Bitmap bmp = ((BitmapDrawable) image).getBitmap();
		int width = bmp.getWidth();
		int height = bmp.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// rotate the Bitmap
		matrix.postRotate((float) Angle);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
		// make a Drawable from Bitmap to allow to set the BitMap
		// to the ImageView, ImageButton or what ever
		BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);

		bmd.setBounds(x, y, x + bmd.getIntrinsicWidth(), y + bmd.getIntrinsicHeight());
		bmd.draw(canvas);

		return bmd.getIntrinsicWidth();

	}

	/**
	 * Set the Height of a ListView to the additional height of all ListItems
	 * 
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView)
	{
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null)
		{
			// pre-condition
			return;
		}

		int totalHeight = 0;
		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
		for (int i = 0; i < listAdapter.getCount(); i++)
		{
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	public static void drawIconBounds(Canvas canvas, Drawable icon, Rect bounds)
	{
		Rect oldBounds = icon.getBounds();
		icon.setBounds(bounds);
		icon.draw(canvas);
		icon.setBounds(oldBounds);
	}

	public static void setListViewPropertys(ListView list)
	{
		list.setBackgroundColor(Global.getColor(R.attr.EmptyBackground));
		list.setCacheColorHint(0);
		list.setDividerHeight(Sizes.getHalfCornerSize() * 2);
		list.setDivider(list.getBackground());
	}

	/**
	 * Initialisiert das NumPad welches mit </br> </br> <include
	 * android:layout_height="wrap_content" </br>
	 * layout="@layout/numerik_keypad_layout"
	 * android:layout_width="match_parent" </br>
	 * android:layout_marginLeft="10dip" android:layout_marginRight="10dip"
	 * </br> android:layout_weight="1"></include> </br> </br> in ein layout
	 * eingebunden wurde mit den einstellungen für eine Integer eingabe.
	 * 
	 * @param activity
	 *            Aktivity die das NumPad enthält
	 * @param numPadLayout
	 *            Id zum NumPad
	 * @param TextBox
	 *            editText Feld welches benutzt wird
	 * @param initialValue
	 *            der Wert, der in das EditText Feld eingetragen wird
	 */
	public static void initialNumPadInt(Activity activity, View numPadLayout, EditText TextBox, String string, OnClickListener OkListner,
			OnClickListener cancelListner)
	{
		// disable soft keyboard
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		TextBox.setInputType(0);

		// NumButton Handler
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num0), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num1), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num2), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num3), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num4), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num5), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num6), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num7), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num8), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num9), numButtonClickListner);
		setNumPadButton_Gone((Button) numPadLayout.findViewById(R.id.numPoint));
		setNumPadButton_Weight((Button) numPadLayout.findViewById(R.id.negativeButton), 2.0f);

		((Button) numPadLayout.findViewById(R.id.del)).setOnClickListener(delButtonClickListner);

		// set the value
		editText = TextBox;
		editText.setText(string);
		// editText.setTextSize((float) (Sizes.getScaledFontSize_small()));

		initialCurserKeys(numPadLayout, TextBox);

	}

	/**
	 * Initialisiert das NumPad welches mit </br> </br> <include
	 * android:layout_height="wrap_content" </br>
	 * layout="@layout/numerik_keypad_layout"
	 * android:layout_width="match_parent" </br>
	 * android:layout_marginLeft="10dip" android:layout_marginRight="10dip"
	 * </br> android:layout_weight="1"></include> </br> </br> in ein layout
	 * eingebunden wurde mit den einstellungen für eine Double eingabe.
	 * 
	 * @param activity
	 *            Aktivity die das NumPad enthält
	 * @param numPadLayout
	 *            Id zum NumPad
	 * @param TextBox
	 *            editText Feld welches benutzt wird
	 * @param string
	 *            der Wert, der in das EditText Feld eingetragen wird als String
	 */
	public static void initialNumPadDbl(Activity activity, View numPadLayout, EditText TextBox, String string, OnClickListener OkListner,
			OnClickListener cancelListner)
	{
		// disable soft keyboard
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		TextBox.setInputType(0);

		// NumButton Handler
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num0), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num1), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num2), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num3), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num4), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num5), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num6), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num7), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num8), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.num9), numButtonClickListner);
		setNumPadButton_Visible((Button) numPadLayout.findViewById(R.id.numPoint), numButtonClickListner, 1.0f);
		setNumPadButton_Weight((Button) numPadLayout.findViewById(R.id.negativeButton), 1.0f);

		((Button) numPadLayout.findViewById(R.id.del)).setOnClickListener(delButtonClickListner);

		// set the value
		editText = TextBox;
		editText.setText(string);
		// editText.setTextSize((float) (Sizes.getScaledFontSize_small()));
		initialCurserKeys(numPadLayout, TextBox);
	}

	private static void setNumPadButton_Visible(Button button, OnClickListener clickListner)
	{
		setNumPadButton_Visible(button, clickListner, -1);
	}

	private static void setNumPadButton_Visible(Button button, OnClickListener clickListner, float weight)
	{
		button.setVisibility(View.VISIBLE);
		if (clickListner != null) button.setOnClickListener(clickListner);
		if (weight > -1)
		{
			LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) button.getLayoutParams();
			lp.weight = weight;
			button.setLayoutParams(lp);
		}
	}

	private static void setNumPadButton_Weight(Button button, float weight)
	{

		if (weight > -1)
		{
			LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) button.getLayoutParams();
			lp.weight = weight;
			button.setLayoutParams(lp);
		}
	}

	private static void setNumPadButton_Gone(Button button)
	{
		button.setVisibility(View.GONE);
		button.setOnClickListener(null);
	}

	private static void initialCurserKeys(View numPadLayout, EditText TextBox)
	{
		Button btnLeft = (Button) numPadLayout.findViewById(R.id.numLeft);
		Button btnRight = (Button) numPadLayout.findViewById(R.id.numRight);
		btnLeft.setText("<");
		btnRight.setText(">");
		btnLeft.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				// give feedback
				main.vibrator.vibrate(50);

				int cursor = editText.getSelectionStart() - 1;
				if (cursor >= 0) editText.setSelection(cursor, cursor);
			}
		});

		btnRight.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				// give feedback
				main.vibrator.vibrate(50);

				int cursor = editText.getSelectionStart() + 1;
				if (cursor <= editText.getText().toString().length()) editText.setSelection(cursor, cursor);
			}
		});
	}

	/**
	 * Das Text element, welches über InitialNumPadInt oder InitialNumPadDbl
	 * angesprochen wird.
	 */
	public static EditText editText;

	/**
	 * Ein buttonClickListner der den String des Buttons in editText einträgt.
	 * editText wird beim Aufruf von InitialNumPadInt oder InitialNumPadDbl zu
	 * gewiesen und ist eine Rferenz auf das zu bearbeitende TextFeld
	 */
	private static View.OnClickListener numButtonClickListner = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// give feedback
			main.vibrator.vibrate(50);

			int cursor = editText.getSelectionStart();
			int selLength = editText.getSelectionEnd() - editText.getSelectionStart();
			String text = editText.getText().toString();
			int pos = cursor + selLength + 1;
			if (pos > text.length())
			{
				text = text + ((Button) v).getText();
			}
			else
			{
				text = text.substring(0, cursor) + ((Button) v).getText() + text.substring(pos);
			}
			editText.setText(text);
			editText.setSelection(cursor + 1, cursor + 1);
		}
	};

	/**
	 * Ein buttonClickListner der den letzten String von editText löscht.
	 * editText wird beim Aufruf von InitialNumPadInt oder InitialNumPadDbl zu
	 * gewiesen und ist eine Rferenz auf das zu bearbeitende TextFeld
	 */
	private static View.OnClickListener delButtonClickListner = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			// give feedback
			main.vibrator.vibrate(50);

			int cursor = editText.getSelectionStart();
			int selLength = editText.getSelectionEnd() - editText.getSelectionStart();
			String text = editText.getText().toString();
			if (editText.getSelectionStart() > 0 && selLength == 0)
			{
				editText.setText(text.substring(0, cursor - 1) + text.substring(cursor));
				editText.setSelection(cursor - 1, cursor - 1);

				return;
			}

			if (selLength > 0)
			{
				editText.setText(text.substring(0, cursor) + text.substring(cursor + selLength));
				editText.setSelection(cursor, cursor);

				return;
			}

		}
	};

	/**
	 * Da bei dem Verwendeten Themes die disabled TextColor nicht richtig
	 * übernommen wird, wird mit dieser Methode der Status überprüft und
	 * gegebenenfalls die Farbe angepasst.
	 * 
	 * @param btn
	 *            (Button)
	 */
	public static void chkBtnState(Button btn)
	{
		if (btn.isEnabled())
		{
			btn.setTextColor(Global.getColor(R.attr.TextColor));
		}
		else
		{
			btn.setTextColor(Global.getColor(R.attr.TextColor_disable));
		}
	}

	/**
	 * Da bei dem Verwendeten Themes die disabled TextColor nicht richtig
	 * übernommen wird, wird mit dieser Methode der Status gesetzt und
	 * gegebenenfalls die Farbe angepasst.
	 * 
	 * @param btn
	 *            (Button)
	 * @param state
	 *            (boolean)
	 */
	public static void setBtnState(Button btn, boolean state)
	{
		btn.setEnabled(state);

		if (state)
		{
			btn.setTextColor(Global.getColor(R.attr.TextColor));
		}
		else
		{
			btn.setTextColor(Global.getColor(R.attr.TextColor_disable));
		}
	}

}
