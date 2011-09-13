package de.droidcachebox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.Log.Logger;
import CB_Core.TranslationEngine.LangStrings;
import CB_Core.Types.MoveableList;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Menu;
import android.view.MenuItem;
import de.droidcachebox.Custom_Controls.QuickButtonList.QuickButtonItem;
import de.droidcachebox.Geocaching.JokerList;
import de.droidcachebox.Locator.Locator;
import de.droidcachebox.Map.RouteOverlay;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.MapView.SmoothScrollingTyp;

public class Global
{
	public static final int CurrentRevision = 420;
	public static final String CurrentVersion = "0.1.";
	public static final String VersionPrefix = "alpha";
	public static final int LatestDatabaseChange = 1016;
	public static final int LatestDatabaseFieldNoteChange = 1001;

	public static final String br = String.format("%n");
	public static final String splashMsg = "Team Cachebox (2011)" + br
			+ "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br
			+ "Groundspeak Inc.Used with permission";

	public static final boolean Debug = true;
	public static JokerList Jokers = new JokerList();

	/**
	 * Liste der Buttons in der QuickButton Leiste
	 */
	public static MoveableList<QuickButtonItem> QuickButtonList;

	public static FilterProperties LastFilter = null;
	public static boolean autoResort;
	public static Bitmap EmptyBmp = Bitmap.createBitmap(1, 1,
			Bitmap.Config.RGB_565); // kleinst mögliches Bmp

	// for MapView
	public static SmoothScrollingTyp SmoothScrolling = SmoothScrollingTyp.normal;
	public static RouteOverlay.Route AktuelleRoute = null;
	public static int aktuelleRouteCount = 0;

	public static long TrackDistance;

	/**
	 * Nacht Color Matrix
	 */
	public static final float[] mx =
		{ -1.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, -1.5f, 0.0f, 0.0f, 200.0f,
				0.0f, 0.0f, -1.5f, 0.0f, 0.f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f };

	/**
	 * Nacht Color Matrix
	 */
	public static final ColorMatrix cm = new ColorMatrix(mx);

	/**
	 * paint with invert Matrix
	 */
	public static Paint invertPaint = new Paint();

	/**
	 * gibt die resultierende Farbe für Schwarz zurück, wenn der Invert Filter
	 * angewandt wurde.
	 * 
	 * @return int (Color)
	 */
	public static int getInvertMatrixBlack()
	{
		if (mInvertBlack == -1)
		{
			Bitmap onelPixel = Bitmap.createBitmap(1, 1,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(onelPixel);
			c.drawColor(Color.BLACK);

			Bitmap onelPixel2 = Bitmap.createBitmap(1, 1,
					Bitmap.Config.ARGB_8888);
			Canvas c2 = new Canvas(onelPixel2);
			c2.drawBitmap(onelPixel, 0, 0, invertPaint);

			mInvertBlack = onelPixel2.getPixel(0, 0);

		}

		return mInvertBlack;

	}

	/**
	 * enthält die resultierende Farbe für Schwarz, wenn der Invert Filter
	 * angewandt wurde.
	 */
	private static int mInvertBlack = -1;

	/**
	 * gibt die resultierende Farbe für Schwarz zurück, wenn der Invert Filter
	 * angewandt wurde.
	 * 
	 * @return int (Color)
	 */
	public static int getInvertMatrixWhite()
	{
		if (mInvertWhite == -1)
		{
			Bitmap onelPixel = Bitmap.createBitmap(1, 1,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(onelPixel);
			Paint p = new Paint();
			p.setColor(Color.WHITE);
			c.drawRect(new Rect(0, 0, 1, 1), p);

			Bitmap onelPixel2 = Bitmap.createBitmap(1, 1,
					Bitmap.Config.ARGB_8888);
			Canvas c2 = new Canvas(onelPixel2);
			c2.drawBitmap(onelPixel, 0, 0, invertPaint);

			mInvertWhite = onelPixel2.getPixel(0, 0);

		}

		return mInvertWhite;

	}

	/**
	 * enthält die resultierende Farbe für Schwarz, wenn der Invert Filter
	 * angewandt wurde.
	 */
	private static int mInvertWhite = -1;

	// Sizes

	public static LangStrings Translations = new LangStrings();

	// ausgelagert in den Core
	// public static Coordinate Marker = new Coordinate();

	// Icons
	/**
	 * <b>Ein Array mit Icons als Drawable</b> <br>
	 * <br>
	 * Index 0 =<img src="doc-files/night_tb.png" width=32 height=32> <img
	 * src="doc-files/day_tb.png" width=32 height=32> <br>
	 * Index 1 =<img src="doc-files/addwaypoint.png" width=32 height=32> <br>
	 * Index 2 =<img src="doc-files/smilie_gross.gif" width=32 height=32> <br>
	 * Index 3 =<img src="doc-files/download.png" width=32 height=32> <br>
	 * Index 4 =<img src="doc-files/log1.png" width=32 height=32> <br>
	 * Index 5 =<img src="doc-files/maintenance.png" width=32 height=32> <br>
	 * Index 6 =<img src="doc-files/checkbox_checked.png" width=32 height=32> <br>
	 * Index 7 =<img src="doc-files/checkbox_unchecked.png" width=32 height=32> <br>
	 * Index 8 =<img src="doc-files/sonne.png" width=32 height=32> <br>
	 * Index 9 =<img src="doc-files/mond.png" width=32 height=32> <br>
	 * Index 10 =<img src="doc-files/travelbug.gif" width=32 height=32> <br>
	 * Index 11 =<img src="doc-files/collapse.png" width=32 height=32> <br>
	 * Index 12 =<img src="doc-files/expand.png" width=32 height=32> <br>
	 * Index 13 =<img src="doc-files/enabled.png" width=32 height=32> <br>
	 * Index 14 =<img src="doc-files/disabled.png" width=32 height=32> <br>
	 * Index 15 =<img src="doc-files/retrieve_tb.png" width=32 height=32> <br>
	 * Index 16 =<img src="doc-files/drop_tb.png" width=32 height=32> <br>
	 * Index 17 =<img src="doc-files/star.png" width=32 height=32> <br>
	 * Index 18 =<img src="doc-files/earth.png" width=32 height=32> <br>
	 * Index 19 =<img src="doc-files/favorit.png" width=32 height=32> <br>
	 * Index 20 =<img src="doc-files/file.png" width=32 height=32> <br>
	 * Index 21 =<img src="doc-files/userdata.jpg" width=32 height=32> <br>
	 * Index 22 =<img src="doc-files/delete.jpg" width=32 height=32> <br>
	 * Index 23 =<img src="doc-files/archiv.png" width=32 height=32> <br>
	 * Index 24 =<img src="doc-files/not_available.jpg" width=32 height=32> <br>
	 * Index 25 =<img src="doc-files/checkbox_crossed.png" width=32 height=32> <br>
	 * Index 26 =<img src="doc-files/map22.png" width=32 height=32> <br>
	 * Index 27 =<img src="doc-files/chk_icon.png" width=32 height=32> <br>
	 * Index 28 =<img src="doc-files/delete_icon.png" width=32 height=32> <br>
	 * Index 29 =<img src="doc-files/voice_rec_icon.png" width=32 height=32> <br>
	 * Index 30 =<img src="doc-files/satellite.png" width=32 height=32> <br>
	 * Index 31 =<img src="doc-files/close_icon.png" width=32 height=32> <br>
	 * Index 32 =<img src="doc-files/info_icon.png" width=32 height=32> <br>
	 * Index 33 =<img src="doc-files/warning_icon.png" width=32 height=32> <br>
	 * Index 34 =<img src="doc-files/help_icon.png" width=32 height=32> <br>
	 * Index 35 =<img src="doc-files/power_gc_live.png" width=32 height=32> <br>
	 * Index 36 =<img src="doc-files/gc_live_icon.png" width=32 height=32> <br>
	 * Index 37 =<img src="doc-files/pin_icon.png" width=32 height=32> <br>
	 * Index 38 =<img src="doc-files/pin_icon_disable.png" width=32 height=32> <br>
	 * Index 39 =<img src="doc-files/chk_icon_disable.png" width=32 height=32> <br>
	 * Index 40 =<img src="doc-files/night_slider_down.png" width=32 height=32>
	 * <img src="doc-files/day_slider_down.png" width=32 height=32> <br>
	 * Index 41 =<img src="doc-files/night_slider_up_down.png" width=32
	 * height=32> <img src="doc-files/day_slider_up_down.png" width=32
	 * height=32>
	 */
	public static Drawable[] Icons = null;
	public static Drawable[] SmallStarIcons = null;
	public static Drawable[] StarIcons = null;
	public static Drawable[] SizeIcons = null;

	/**
	 * <b>Ein Array mit CacheIcons als Drawable</b> <br>
	 * <br>
	 * Index 0 =<img src="doc-files/big_0.gif" width=32 height=32> <br>
	 * Index 1 =<img src="doc-files/big_1.gif" width=32 height=32> <br>
	 * Index 2 =<img src="doc-files/big_2.gif" width=32 height=32> <br>
	 * Index 3 =<img src="doc-files/big_3.gif" width=32 height=32> <br>
	 * Index 4 =<img src="doc-files/big_4.gif" width=32 height=32> <br>
	 * Index 5 =<img src="doc-files/big_5.gif" width=32 height=32> <br>
	 * Index 6 =<img src="doc-files/big_6.gif" width=32 height=32> <br>
	 * Index 7 =<img src="doc-files/big_7.png" width=32 height=32> <br>
	 * Index 8 =<img src="doc-files/big_8.gif" width=32 height=32> <br>
	 * Index 9 =<img src="doc-files/big_9.gif" width=32 height=32> <br>
	 * Index 10 =<img src="doc-files/big_10.gif" width=32 height=32> <br>
	 * Index 11 =<img src="doc-files/big_11.png" width=32 height=32> <br>
	 * Index 12 =<img src="doc-files/big_12.png" width=32 height=32> <br>
	 * Index 13 =<img src="doc-files/big_13.png" width=32 height=32> <br>
	 * Index 14 =<img src="doc-files/big_14.png" width=32 height=32> <br>
	 * Index 15 =<img src="doc-files/big_15.png" width=32 height=32> <br>
	 * Index 16 =<img src="doc-files/big_16.png" width=32 height=32> <br>
	 * Index 17 =<img src="doc-files/big_17.png" width=32 height=32> <br>
	 * Index 18 =<img src="doc-files/big_18.png" width=32 height=32> <br>
	 * Index 19 =<img src="doc-files/big_19.gif" width=32 height=32>
	 */
	public static Drawable[] CacheIconsBig = null;
	public static Drawable[] BatteryIcons = null;

	/**
	 * <b>Ein Array mit LogIcons als Drawable</b> <br>
	 * <br>
	 * Index 0 =<img src="doc-files/log0.gif" width=32 height=32> <br>
	 * Index 1 =<img src="doc-files/log1.png" width=32 height=32> <br>
	 * Index 2 =<img src="doc-files/log2.png" width=32 height=32> <br>
	 * Index 3 =<img src="doc-files/log3.png" width=32 height=32> <br>
	 * Index 4 =<img src="doc-files/log4.png" width=32 height=32> <br>
	 * Index 5 =<img src="doc-files/log5.png" width=32 height=32> <br>
	 * Index 6 =<img src="doc-files/log6.png" width=32 height=32> <br>
	 * Index 7 =<img src="doc-files/log7.png" width=32 height=32> <br>
	 * Index 8 =<img src="doc-files/log8.png" width=32 height=32> <br>
	 * Index 9 =<img src="doc-files/log9.png" width=32 height=32> <br>
	 * Index 10 =<img src="doc-files/log10.png" width=32 height=32> <br>
	 * Index 11 =<img src="doc-files/log11.jpg" width=32 height=32> <br>
	 * Index 12 =<img src="doc-files/log12.jpg" width=32 height=32> <br>
	 * Index 13 =<img src="doc-files/log13.png" width=32 height=32>
	 */
	public static Drawable[] LogIcons = null;
	public static Drawable[] Arrows = null;
	public static Drawable[] ChkIcons = null;
	public static Drawable[] BtnIcons = null;

	// New Map Icons
	public static ArrayList<ArrayList<Drawable>> NewMapIcons = null;
	public static ArrayList<ArrayList<Drawable>> NewMapOverlay = null;

	// / <summary>
	// / Letzte bekannte Position
	// / </summary>
	// ausgelagert in den Core
	// public static Coordinate LastValidPosition = new Coordinate();
	// / <summary>
	// / Instanz des GPS-Parsers
	// / </summary>
	public static Locator Locator = null;

	public static class Paints
	{
		public static Paint mesurePaint;
		public static Paint ListBackground;

		public static class Day
		{

			public static Paint selectedBack;
			public static Paint ListBackground;
			public static Paint ListBackground_second;

		}

		public static class Night
		{

			public static Paint selectedBack;
			public static Paint ListBackground;
			public static Paint ListBackground_second;

		}

		public static void init(Context context)
		{
			Resources res = context.getResources();

			// calc sizes

			mesurePaint = new Paint();
			mesurePaint.setTextSize(Sizes.getScaledFontSize_normal());

			ListBackground = new Paint();
			Night.ListBackground_second = new Paint();

			Night.ListBackground = new Paint();
			Night.ListBackground.setColor(res
					.getColor(R.color.Night_ListBackground));

			Night.selectedBack = new Paint();
			Night.selectedBack.setColor(res
					.getColor(R.color.Night_SelectedBackground));

			Day.ListBackground = new Paint();
			Day.ListBackground.setColor(res
					.getColor(R.color.Day_ListBackground));
			Day.ListBackground_second = new Paint();

			Day.selectedBack = new Paint();
			Day.selectedBack.setColor(res
					.getColor(R.color.Day_SelectedBackground));

			invertPaint.setColorFilter(new ColorMatrixColorFilter(Global.cm));

		}

	}

	public static void setDebugMsg(String msg)
	{
		((main) main.mainActivity).setDebugMsg(msg);
	}

	// / <summary>
	// / SDBM-Hash algorithm for storing hash values into the database. This is
	// neccessary to be compatible to the CacheBox@Home project. Because the
	// / standard .net Hash algorithm differs from compact edition to the normal
	// edition.
	// / </summary>
	// / <param name="str"></param>
	// / <returns></returns>
	public static long sdbm(String str)
	{
		if (str == null || str.equals("")) return 0;

		long hash = 0;
		// set mask to 2^32!!!???!!!
		long mask = 42949672;
		mask = mask * 100 + 95;

		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			hash = (c + (hash << 6) + (hash << 16) - hash) & mask;
		}

		return hash;
	}

	public static String GetDateTimeString()
	{
		Date now = new Date();
		SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
		String sDate = datFormat.format(now);
		datFormat = new SimpleDateFormat("HHmmss");
		sDate += " " + datFormat.format(now);
		return sDate;
	}

	public static String GetTrackDateTimeString()
	{
		Date now = new Date();
		SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
		String sDate = datFormat.format(now);
		datFormat = new SimpleDateFormat("HH:mm:ss");
		sDate += "T" + datFormat.format(now) + "Z";
		return sDate;
	}

	private static Drawable getDrawable(int ResId, Resources res)
	{
		return getDrawable(ResId, -1, res);
	}

	private static Drawable getDrawable(int ResId, int NightResId, Resources res)
	{
		Drawable ret = null;

		if (NightResId == -1 || !main.N)
		{
			ret = res.getDrawable(ResId);

			// im Nacht Mode wird das Drawable mit einem Filter belegt, um es
			// ein wenig abzudunkeln
			if (main.N)
			{
				ret.setColorFilter(Color.argb(255, 100, 100, 100),
						Mode.MULTIPLY);
			}

		}
		else
		{
			ret = res.getDrawable(NightResId);
		}

		if (!main.N)
		{
			ret.clearColorFilter();
		}

		return ret;

	}

	// N = Nachtmodus! Wenn true werden andere Icons geladen!
	public static void InitIcons(Context context)
	{
		Resources res = context.getResources();

		NewMapIcons = new ArrayList<ArrayList<Drawable>>();
		NewMapOverlay = new ArrayList<ArrayList<Drawable>>();

		// NewMapIcons[0] contains the 8x8 Bitmaps
		NewMapIcons.add(new ArrayList<Drawable>());
		NewMapIcons.get(0).add(getDrawable(R.drawable.map_8x8_green, res));
		NewMapIcons.get(0).add(getDrawable(R.drawable.map_8x8_yellow, res));
		NewMapIcons.get(0).add(getDrawable(R.drawable.map_8x8_red, res));
		NewMapIcons.get(0).add(getDrawable(R.drawable.map_8x8_white, res));
		NewMapIcons.get(0).add(getDrawable(R.drawable.map_8x8_blue, res));
		NewMapIcons.get(0).add(getDrawable(R.drawable.map_8x8_violet, res));
		NewMapIcons.get(0).add(getDrawable(R.drawable.map_8x8_found, res));
		NewMapIcons.get(0).add(getDrawable(R.drawable.map_8x8_own, res));

		// NewMapIcons[1] contains the 13x13 Bitmaps
		NewMapIcons.add(new ArrayList<Drawable>());
		NewMapIcons.get(1).add(getDrawable(R.drawable.map_13x13_green, res));
		NewMapIcons.get(1).add(getDrawable(R.drawable.map_13x13_yellow, res));
		NewMapIcons.get(1).add(getDrawable(R.drawable.map_13x13_red, res));
		NewMapIcons.get(1).add(getDrawable(R.drawable.map_13x13_white, res));
		NewMapIcons.get(1).add(getDrawable(R.drawable.map_13x13_blue, res));
		NewMapIcons.get(1).add(getDrawable(R.drawable.map_13x13_violet, res));
		NewMapIcons.get(1).add(getDrawable(R.drawable.map_13x13_found, res));
		NewMapIcons.get(1).add(getDrawable(R.drawable.map_13x13_own, res));

		// NewMapIcons[2] contains the normal 20x20 Bitmaps
		NewMapIcons.add(new ArrayList<Drawable>());
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_0, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_1, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_2, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_3, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_4, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_5, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_6, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_7, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_8, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_9, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_10, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_11, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_12, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_13, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_14, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_15, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_16, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_17, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_18, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_19, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_20, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_21, res));
		NewMapIcons.get(2).add(getDrawable(R.drawable.map_20x20_22, res));

		// Overlays for Icons
		NewMapOverlay.add(new ArrayList<Drawable>()); // 8x8
		NewMapOverlay.get(0).add(
				getDrawable(R.drawable.map_8x8_shaddowrect, res));
		NewMapOverlay.get(0).add(
				getDrawable(R.drawable.map_8x8_shaddowround, res));
		NewMapOverlay.get(0).add(
				getDrawable(R.drawable.map_8x8_shaddowstar, res));
		NewMapOverlay.get(0)
				.add(getDrawable(R.drawable.map_8x8_strikeout, res));

		NewMapOverlay.add(new ArrayList<Drawable>()); // 13x13
		NewMapOverlay.get(1).add(
				getDrawable(R.drawable.map_13x13_shaddowrect, res));
		NewMapOverlay.get(1).add(
				getDrawable(R.drawable.map_13x13_shaddowround, res));
		NewMapOverlay.get(1).add(
				getDrawable(R.drawable.map_13x13_shaddowstar, res));
		NewMapOverlay.get(1).add(
				getDrawable(R.drawable.map_13x13_strikeout, res));

		NewMapOverlay.add(new ArrayList<Drawable>()); // 20x20
		NewMapOverlay.get(2).add(
				getDrawable(R.drawable.map_20x20_shaddowrect, res));
		NewMapOverlay.get(2).add(
				getDrawable(R.drawable.map_20x20_selected, res));
		NewMapOverlay.get(2).add(
				getDrawable(R.drawable.map_20x20_shaddowrect_deact, res));
		NewMapOverlay.get(2).add(
				getDrawable(R.drawable.map_20x20_selected_deact, res));

		Icons = new Drawable[]
			{
					getDrawable(R.drawable.day_tb, R.drawable.night_tb, res),
					getDrawable(R.drawable.addwaypoint, res),
					getDrawable(R.drawable.smilie_gross, res),
					getDrawable(R.drawable.download, res),
					getDrawable(R.drawable.log1, res),
					getDrawable(R.drawable.maintenance, res),
					getDrawable(R.drawable.checkbox_checked, res),
					getDrawable(R.drawable.checkbox_unchecked, res),
					getDrawable(R.drawable.sonne, res),
					getDrawable(R.drawable.mond, res),
					getDrawable(R.drawable.travelbug, res),
					getDrawable(R.drawable.collapse, res),
					getDrawable(R.drawable.expand, res),
					getDrawable(R.drawable.enabled, res),
					getDrawable(R.drawable.disabled, res),
					getDrawable(R.drawable.retrieve_tb, res),
					getDrawable(R.drawable.drop_tb, res),
					getDrawable(R.drawable.star, res),
					getDrawable(R.drawable.earth, res),
					getDrawable(R.drawable.favorit, res),
					getDrawable(R.drawable.file, res),
					getDrawable(R.drawable.userdata, res),
					getDrawable(R.drawable.delete, res), // 22
					getDrawable(R.drawable.archiv, res), // 23
					getDrawable(R.drawable.not_available, res), // 24
					getDrawable(R.drawable.checkbox_crossed, res), // 25
					getDrawable(R.drawable.map22, res), // 26
					getDrawable(R.drawable.chk_icon, res), // 27
					getDrawable(R.drawable.delete_icon, res), // 28
					getDrawable(R.drawable.voice_rec_icon, res), // 29
					getDrawable(R.drawable.satellite, res), // 30
					getDrawable(R.drawable.close_icon, res), // 31
					getDrawable(R.drawable.info_icon, res), // 32
					getDrawable(R.drawable.warning_icon, res), // 33
					getDrawable(R.drawable.help_icon, res), // 34
					getDrawable(R.drawable.power_gc_live, res), // 35
					getDrawable(R.drawable.day_gc_live_icon,
							R.drawable.night_gc_live_icon, res), // 36
					getDrawable(R.drawable.pin_icon, res), // 37
					getDrawable(R.drawable.pin_icon_disable, res), // 38
					getDrawable(R.drawable.chk_icon_disable, res), // 39
					getDrawable(R.drawable.day_slider_down,
							R.drawable.night_slider_down, res), // 40
					getDrawable(R.drawable.day_slider_up_down,
							R.drawable.night_slider_up_down, res), // 41
			};

		Arrows = new Drawable[]
			{
					getDrawable(R.drawable.arrow, R.drawable.night_arrow, res),
					getDrawable(R.drawable.arrow_small,
							R.drawable.night_arrow_small, res), };

		SmallStarIcons = new Drawable[]
			{
					getDrawable(R.drawable.smallstars_0,
							R.drawable.night_smallstars_0, res),
					getDrawable(R.drawable.smallstars_0_5,
							R.drawable.night_smallstars_0_5, res),
					getDrawable(R.drawable.smallstars_1,
							R.drawable.night_smallstars_1, res),
					getDrawable(R.drawable.smallstars_1_5,
							R.drawable.night_smallstars_1_5, res),
					getDrawable(R.drawable.smallstars_2,
							R.drawable.night_smallstars_2, res),
					getDrawable(R.drawable.smallstars_2_5,
							R.drawable.night_smallstars_2_5, res),
					getDrawable(R.drawable.smallstars_3,
							R.drawable.night_smallstars_3, res),
					getDrawable(R.drawable.smallstars_3_5,
							R.drawable.night_smallstars_3_5, res),
					getDrawable(R.drawable.smallstars_4,
							R.drawable.night_smallstars_4, res),
					getDrawable(R.drawable.smallstars_4_5,
							R.drawable.night_smallstars_4_5, res),
					getDrawable(R.drawable.smallstars_5,
							R.drawable.night_smallstars_5, res) };

		StarIcons = new Drawable[]
			{ getDrawable(R.drawable.stars0, res),
					getDrawable(R.drawable.stars0_5, res),
					getDrawable(R.drawable.stars1, res),
					getDrawable(R.drawable.stars1_5, res),
					getDrawable(R.drawable.stars2, res),
					getDrawable(R.drawable.stars2_5, res),
					getDrawable(R.drawable.stars3, res),
					getDrawable(R.drawable.stars3_5, res),
					getDrawable(R.drawable.stars4, res),
					getDrawable(R.drawable.stars4_5, res),
					getDrawable(R.drawable.stars5, res) };

		SizeIcons = new Drawable[]
			{ getDrawable(R.drawable.other, res),
					getDrawable(R.drawable.micro, res),
					getDrawable(R.drawable.small, res),
					getDrawable(R.drawable.regular, res),
					getDrawable(R.drawable.large, res) };

		// BatteryIcons = new Drawable[] {
		// getDrawable(R.drawable.bat0),
		// getDrawable(R.drawable.bat1),
		// getDrawable(R.drawable.bat2),
		// getDrawable(R.drawable.bat3),
		// };

		CacheIconsBig = new Drawable[]
			{ getDrawable(R.drawable.big_0, res),
					getDrawable(R.drawable.big_1, res),
					getDrawable(R.drawable.big_2, res),
					getDrawable(R.drawable.big_3, res),
					getDrawable(R.drawable.big_4, res),
					getDrawable(R.drawable.big_5, res),
					getDrawable(R.drawable.big_6, res),
					getDrawable(R.drawable.big_7, res),
					getDrawable(R.drawable.big_8, res),
					getDrawable(R.drawable.big_9, res),
					getDrawable(R.drawable.big_10, res),
					getDrawable(R.drawable.big_11, res),
					getDrawable(R.drawable.big_12, res),
					getDrawable(R.drawable.big_13, res),
					getDrawable(R.drawable.big_14, res),
					getDrawable(R.drawable.big_15, res),
					getDrawable(R.drawable.big_16, res),
					getDrawable(R.drawable.big_17, res),
					getDrawable(R.drawable.big_18, res),
					getDrawable(R.drawable.big_19, res), };

		LogIcons = new Drawable[]
			{ getDrawable(R.drawable.log0, res),
					getDrawable(R.drawable.log1, res),
					getDrawable(R.drawable.log2, res),
					getDrawable(R.drawable.log3, res),
					getDrawable(R.drawable.log4, res),
					getDrawable(R.drawable.log5, res),
					getDrawable(R.drawable.log6, res),
					getDrawable(R.drawable.log7, res),
					getDrawable(R.drawable.log8, res),
					getDrawable(R.drawable.log9, res),
					getDrawable(R.drawable.log10, res),
					getDrawable(R.drawable.log11, res),
					getDrawable(R.drawable.log12, res),
					getDrawable(R.drawable.log13, res),

			};

		ChkIcons = new Drawable[]
			{
					getDrawable(R.drawable.day_btn_check_off,
							R.drawable.night_btn_check_off, res),
					getDrawable(R.drawable.day_btn_check_on,
							R.drawable.night_btn_check_on, res), };

		BtnIcons = new Drawable[]
			{
					getDrawable(R.drawable.day_btn_default_normal,
							R.drawable.night_btn_default_normal, res),
					getDrawable(R.drawable.button, R.drawable.night_button, res),
					getDrawable(R.drawable.doc_icon, res),
					getDrawable(R.drawable.big_16, res),
					getDrawable(R.drawable.list_icon, res), // LogView braucht
															// noch ein Icon
					getDrawable(R.drawable.map, res),
					getDrawable(R.drawable.compass, res),
					getDrawable(R.drawable.cache_list_icon, res),
					getDrawable(R.drawable.list_icon, res), // TrackListView
															// braucht noch ein
															// Icon
					getDrawable(R.drawable.log10, res),
					getDrawable(R.drawable.video_icon, res),
					getDrawable(R.drawable.voice_rec_icon, res),
					getDrawable(R.drawable.star, res), // Live search braucht
														// noch ein Icon
					getDrawable(R.drawable.filter, res),
					getDrawable(R.drawable.lock_icon, res),
					getDrawable(R.drawable.auto_sort_on_icon, res), // 15
					getDrawable(R.drawable.auto_sort_off_icon, res), // 16
					getDrawable(R.drawable.solver_icon, res), // 17
					getDrawable(R.drawable.images_icon, res), // 18
			};

	}

	static String FormatDM(double coord, String positiveDirection,
			String negativeDirection)
	{
		int deg = (int) coord;
		double frac = coord - deg;
		double min = frac * 60;

		String result = Math.abs(deg) + "° "
				+ String.format("%.3f", Math.abs(min));

		if (coord < 0) result += negativeDirection;
		else
			result += positiveDirection;

		return result;
	}

	public static String FormatLatitudeDM(double latitude)
	{
		return FormatDM(latitude, "N", "S");
	}

	public static String FormatLongitudeDM(double longitude)
	{
		return FormatDM(longitude, "E", "W");
	}

	public static String Rot13(String message)
	{
		String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lookup = "nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM";

		String result = "";

		for (int i = 0; i < message.length(); i++)
		{
			String curChar = message.substring(i, i + 1);
			int idx = alphabet.indexOf(curChar);

			if (idx < 0) result += curChar;
			else
				result += lookup.substring(idx, idx + 1);
		}

		return result;
	}

	static TypedArray themeStyles;
	private static int[] colorAttrs = new int[]
		{ R.attr.ListBackground, R.attr.ListBackground_secend,
				R.attr.ListBackground_select, R.attr.myBackground,
				R.attr.ListSeparator, R.attr.TextColor,
				R.attr.TextColor_disable, R.attr.EmptyBackground,
				R.attr.DropDownBackground, R.attr.ToggleBtColor_off,
				R.attr.ToggleBtColor_on, R.attr.TitleBarBackColor,
				R.attr.SlideDownBackColor, R.attr.LinkLabelColor,
				R.attr.Compass_rimColorFilter, R.attr.Compass_faceColorFilter,
				R.attr.Compass_TextColor, R.attr.Compass_N_TextColor,
				R.attr.Map_Compass_TextColor, R.attr.Map_ColorCompassPanel };

	public static void initTheme(Context context)
	{

		colorAttrs = new int[]
			{ R.attr.ListBackground, R.attr.ListBackground_secend,
					R.attr.ListBackground_select, R.attr.myBackground,
					R.attr.ListSeparator, R.attr.TextColor,
					R.attr.TextColor_disable, R.attr.EmptyBackground,
					R.attr.DropDownBackground, R.attr.ToggleBtColor_off,
					R.attr.ToggleBtColor_on, R.attr.TitleBarBackColor,
					R.attr.SlideDownBackColor, R.attr.LinkLabelColor,
					R.attr.Compass_rimColorFilter,
					R.attr.Compass_faceColorFilter, R.attr.Compass_TextColor,
					R.attr.Compass_N_TextColor, R.attr.Map_Compass_TextColor,
					R.attr.Map_ColorCompassPanel };

		Theme t = context.getTheme();
		Arrays.sort(colorAttrs);
		themeStyles = t.obtainStyledAttributes(colorAttrs);
	}

	public static int getColor(int attrResid)
	{
		return (int) themeStyles.getColor(
				Arrays.binarySearch(colorAttrs, attrResid), 0);
	}

	public static void PlaySound(String soundFile)
	{
		if (!Config.GetBool("PlaySounds")) return;
		MediaPlayer mp = new MediaPlayer();
		mp.setOnPreparedListener(new OnPreparedListener()
		{
			@Override
			public void onPrepared(MediaPlayer mp)
			{
				mp.start();
			}
		});
		try
		{
			mp.setDataSource(Config.WorkPath + "/data/sound/" + soundFile);
			mp.prepare();
		}
		catch (Exception e)
		{
			Logger.Error("Global.PlaySound()", Config.WorkPath + "/data/sound/"
					+ soundFile, e);
			e.printStackTrace();
		}
	}

	public static String getVersionString()
	{
		final String ret = "Version: " + CurrentVersion
				+ String.valueOf(CurrentRevision) + "  "
				+ (VersionPrefix.equals("") ? "" : "(" + VersionPrefix + ")");
		return ret;
	}

	public static MenuItem TranslateMenuItem(Menu menu, int id, String StringId)
	{
		return TranslateMenuItem(menu, id, StringId, "");
	}

	public static MenuItem TranslateMenuItem(Menu menu, int id,
			String StringId, String zusatz)
	{
		MenuItem mi = menu.findItem(id);
		try
		{
			if (mi != null)
			{
				String trans = Global.Translations.Get(StringId) + zusatz;
				mi.setTitle(trans);
			}
		}
		catch (Exception e)
		{
			Logger.Error("Global.TranslateMenuItem()", "", e);
		}
		return mi;
	}

	/**
	 * isOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet
	 * zuzugreifen
	 */
	public static boolean isOnline()
	{
		ConnectivityManager cm = (ConnectivityManager) main.mainActivity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting())
		{
			return true;
		}
		return false;
	}

	/**
	 * APIisOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet
	 * zuzugreifen und ein API Access Token vorhanden ist.
	 */
	public static boolean APIisOnline()
	{
		if (Config.GetString("GcAPI").length() == 0)
		{
			Logger.General("global.APIisOnline() -Invalid AccessToken");
			return false;
		}
		if (isOnline())
		{
			return true;
		}
		return false;
	}

	/**
	 * JokerisOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet
	 * zuzugreifen und ein Passwort für gcJoker.de vorhanden ist.
	 */
	public static boolean JokerisOnline()
	{
		if (Config.GetString("GcJoker").length() == 0)
		{
			Logger.General("global.APIisOnline() -Invalid Joker");
			return false;
		}
		if (isOnline())
		{
			return true;
		}
		return false;
	}

}