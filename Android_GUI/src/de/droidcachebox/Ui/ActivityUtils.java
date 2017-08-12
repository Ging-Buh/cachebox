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

import com.badlogic.gdx.backends.android.AndroidApplication;

import CB_UI_Base.Math.CB_Rect;
import CB_UI_Base.Math.UiSizes;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;

;

public class ActivityUtils {
	private static int sTheme = 1;

	public final static int THEME_DEFAULT = 0;
	public final static int THEME_DAY = 1;
	public final static int THEME_NIGHT = 2;
	public final static int THEME_DAY_TRANSPARENT = 3;
	public final static int THEME_NIGHT_TRANSPARENT = 4;

	public static void changeToTheme(AndroidApplication activity, int theme) {
		changeToTheme(activity, theme, false);
	}

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
	 */
	public static void changeToTheme(AndroidApplication activity, int theme, boolean firstStart) {
		sTheme = theme;
		main.isRestart = true;
		main.isFirstStart = firstStart;
		activity.finish();

		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(AndroidApplication activity) {
		switch (sTheme) {
		default:
		case THEME_DEFAULT:
			break;
		case THEME_DAY:
			activity.setTheme(R.style.Theme_day);
			break;
		case THEME_NIGHT:
			activity.setTheme(R.style.Theme_night);
			break;
		case THEME_DAY_TRANSPARENT:
			activity.setTheme(R.style.Theme_day_transparent);
			break;
		case THEME_NIGHT_TRANSPARENT:
			activity.setTheme(R.style.Theme_night_transparent);
			break;

		}

		Global.initTheme(activity);

	}

	public static void changeToTheme(Activity activity, int theme) {
		changeToTheme(activity, theme, false);
	}

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme, boolean firstStart) {
		sTheme = theme;
		main.isRestart = true;
		main.isFirstStart = firstStart;
		activity.finish();

		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity) {
		switch (sTheme) {
		default:
		case THEME_DEFAULT:
			break;
		case THEME_DAY:
			activity.setTheme(R.style.Theme_day);
			break;
		case THEME_NIGHT:
			activity.setTheme(R.style.Theme_night);
			break;
		case THEME_DAY_TRANSPARENT:
			activity.setTheme(R.style.Theme_day_transparent);
			break;
		case THEME_NIGHT_TRANSPARENT:
			activity.setTheme(R.style.Theme_night_transparent);
			break;

		}

		Global.initTheme(activity);

	}

	public static int drawStaticLayout(Canvas canvas, StaticLayout layout, int x, int y) {
		canvas.translate(x, y);
		layout.draw(canvas);
		canvas.translate(-x, -y);
		return layout.getHeight();
	}

	public static void drawFillRoundRecWithBorder(Canvas canvas, CB_Rect rec, int BorderSize, int BorderColor, int FillColor) {
		drawFillRoundRecWithBorder(canvas, rec, BorderSize, BorderColor, FillColor, UiSizes.that.getCornerSize());
	}

	public static void drawFillRoundRecWithBorder(Canvas canvas, CB_Rect rec, int BorderSize, int BorderColor, int FillColor, int CornerSize) {
		Paint drawPaint = new Paint();
		drawPaint.setAntiAlias(true);
		drawPaint.setStyle(Style.STROKE);
		drawPaint.setStrokeWidth(BorderSize);

		final Rect outerRect = new Rect(rec.getPos().x, rec.getPos().y, rec.getCrossPos().x, rec.getCrossPos().y);
		final RectF OuterRectF = new RectF(outerRect);

		drawPaint.setColor(BorderColor);
		canvas.drawRoundRect(OuterRectF, CornerSize, CornerSize, drawPaint);

		// final Rect rect = new Rect(rec.getLeft() + BorderSize, rec.getBottom() + BorderSize, rec.getRight() - BorderSize, rec.getTop()
		// - BorderSize);

		final Rect rect = new Rect(rec.getPos().x + BorderSize, rec.getPos().y + BorderSize, rec.getCrossPos().x - BorderSize * 2, rec.getCrossPos().y - BorderSize * 2);

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
	public static int PutImageTargetHeight(Canvas canvas, Drawable image, int x, int y, int height) {
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

	public static int PutImageTargetHeightColor(Canvas canvas, Drawable image, int x, int y, int height, int color) {
		return PutImageTargetHeightColor(canvas, image, x, y, height, color, Mode.MULTIPLY);
	}

	public static int PutImageTargetHeightColor(Canvas canvas, Drawable image, int x, int y, int height, int color, Mode porterDuff) {

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
	@SuppressWarnings("deprecation")
	public static int PutImageTargetHeight(Canvas canvas, Drawable image, double Angle, int x, int y, int newHeight) {

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

	@SuppressWarnings("deprecation")
	public static int PutImageScale(Canvas canvas, Drawable image, double Angle, int x, int y, double scale) {

		if (scale == 0.0)
			return 0;

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
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	public static void drawIconBounds(Canvas canvas, Drawable icon, CB_Rect bounds) {
		Rect oldBounds = icon.getBounds();
		icon.setBounds(bounds.getX(), bounds.getY(), bounds.getCrossPos().x, bounds.getCrossPos().y);
		icon.draw(canvas);
		icon.setBounds(oldBounds);
	}

	public static void setListViewPropertys(ListView list) {
		list.setBackgroundColor(Global.getColor(R.attr.EmptyBackground));
		list.setCacheColorHint(0);
		list.setDividerHeight(UiSizes.that.getHalfCornerSize() * 2);
		list.setDivider(list.getBackground());
	}

	/**
	 * Da bei dem Verwendeten Themes die disabled TextColor nicht richtig übernommen wird, wird mit dieser Methode der Status überprüft und
	 * gegebenenfalls die Farbe angepasst.
	 * 
	 * @param btn
	 *            (Button)
	 */
	public static void chkBtnState(Button btn) {
		if (btn.isEnabled()) {
			btn.setTextColor(Global.getColor(R.attr.TextColor));
		} else {
			btn.setTextColor(Global.getColor(R.attr.TextColor_disable));
		}
	}

	/**
	 * Da bei dem Verwendeten Themes die disabled TextColor nicht richtig übernommen wird, wird mit dieser Methode der Status gesetzt und
	 * gegebenenfalls die Farbe angepasst.
	 * 
	 * @param btn
	 *            (Button)
	 * @param state
	 *            (boolean)
	 */
	public static void setBtnState(Button btn, boolean state) {
		btn.setEnabled(state);

		if (state) {
			btn.setTextColor(Global.getColor(R.attr.TextColor));
		} else {
			btn.setTextColor(Global.getColor(R.attr.TextColor_disable));
		}
	}

}
