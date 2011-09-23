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

package de.droidcachebox.Views.Forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.zip.Inflater;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Custom_Controls.CanvasDrawControl;
import de.droidcachebox.Custom_Controls.MultiToggleButton;
import de.droidcachebox.Locator.GPS;
import de.droidcachebox.Locator.Locator;

import de.droidcachebox.UTM.UTMConvert;
import de.droidcachebox.Ui.ActivityUtils;
import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Types.Coordinate;
import CB_Core.Types.MeasuredCoord;
import CB_Core.Types.MeasuredCoordList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MeasureCoordinateActivity extends Activity implements
		LocationListener
{
	private Intent aktIntent;
	public static LinearLayout strengthLayout;

	private MeasuredCoordList mMeasureList = new MeasuredCoordList();
	private TextView lblMeasureCount;
	private TextView lblMeasureCoord;
	private int MeasureCount = 0;

	CanvasDrawControl panelPreview;

	/**
	 * Die MeasureCoordinate Activity hat ihren eigenen locationmanager, mit
	 * einem Höheren Aktualisierungs Intewall
	 */
	public static LocationManager locationManager;

	public void onCreate(Bundle savedInstanceState)
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.measure_coordinate);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// übergebene Koordinate auslesen
		Bundle bundle = getIntent().getExtras();
		MeasuredCoord.Referenz = (Coordinate) bundle.getSerializable("Coord");

		if (MeasuredCoord.Referenz == null)
		{
			MeasuredCoord.Referenz = GlobalCore.LastValidPosition;
		}

		if (MeasuredCoord.Referenz == null)
		{
			MeasuredCoord.Referenz = new Coordinate();
		}

		aktIntent = getIntent();

		findViewById();

		Button bOK = (Button) findViewById(R.id.edco_ok);
		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				aktIntent.putExtra("SOMETHING", "EXTRAS");
				Bundle extras = new Bundle();
				extras.putSerializable("CoordResult",
						mMeasureList.getAccuWeightedAverageCoord());
				aktIntent.putExtras(extras);
				setResult(RESULT_OK, aktIntent);
				finish();
			}
		});
		Button bCancel = (Button) findViewById(R.id.edco_cancel);
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				aktIntent.putExtra("SOMETHING", "EXTRAS");
				Bundle extras = new Bundle();
				extras.putSerializable("CoordResult", null);
				aktIntent.putExtras(extras);
				setResult(RESULT_OK, aktIntent);
				finish();
			}
		});

		initialLocationManager();
		setSatStrength();

		// Translations
		bOK.setText(Global.Translations.Get("ok"));
		bCancel.setText(Global.Translations.Get("cancel"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.menu_editcoordinate, menu);
		return false;
	}

	private void findViewById()
	{

		panelPreview = (CanvasDrawControl) findViewById(R.id.meco_panel);
		strengthLayout = (LinearLayout) findViewById(R.id.strength_control);
		lblMeasureCount = (TextView) findViewById(R.id.textView2);
		lblMeasureCoord = (TextView) findViewById(R.id.textView4);
		// CachesFoundLabel=(TextView)findViewById(R.id.about_CachesFoundLabel);
		// descTextView=(TextView)findViewById(R.id.splash_textViewDesc);
		// versionTextView=(TextView)findViewById(R.id.splash_textViewVersion);

	}

	private static View[] balken = null;

	public static void setSatStrength()
	{

		de.droidcachebox.Locator.GPS.setSatStrength(strengthLayout, balken);

	}

	@Override
	public void onLocationChanged(Location location)
	{
		Locator_LocationDataReceived(location);

	}

	@Override
	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// TODO Auto-generated method stub

	}

	private Locator locator;

	private void initialLocationManager()
	{

		try
		{
			if (locationManager != null)
			{
				// ist schon initialisiert
				return;
			}

			// GPS
			// Get the location manager
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			// Define the criteria how to select the locatioin provider -> use
			// default
			Criteria criteria = new Criteria(); // noch nötig ???
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_HIGH);

			locator = new Locator();

			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);

		}
		catch (Exception e)
		{
			Logger.Error("MesureCoordinate.initialLocationManager()", "", e);
			e.printStackTrace();
		}

	}

	/*
	 * ######### Portierung c# ####################
	 */

	final int projectionZoom = 20;

	Canvas graphics;

	void Locator_LocationDataReceived(Location location)
	{

		Coordinate coord = new Coordinate(location.getLatitude(),
				location.getLongitude());

		if (MeasureCount == 0) lblMeasureCoord
				.setText(coord.FormatCoordinate());

		MeasureCount++;
		mMeasureList.add(new MeasuredCoord(location.getLatitude(), location
				.getLongitude(), location.getAccuracy()));

		lblMeasureCount.setText(String.valueOf(MeasureCount) + "/"
				+ String.valueOf(mMeasureList.size()));

		// nach jeder 10. Messung die Liste Aufräumen
		if (mMeasureList.size() % 10 == 0)
		{
			mMeasureList.sort();
			mMeasureList.clearDiscordantValue();
			lblMeasureCoord.setText(mMeasureList.getAccuWeightedAverageCoord()
					.FormatCoordinate());
		}

		setSatStrength();
		repaintPreview();
	}

	// Brush controlBrush;
	Paint redPen = new Paint(Color.RED);
	Paint blackPen = new Paint(Color.BLACK);

	// Brush blueBrush = new SolidBrush(Color.Blue);

	void repaintPreview()
	{

		Canvas graphics = panelPreview.getCanvas();
		graphics.drawColor(Color.CYAN);

		// graphics.Clip = new Region(new Rectangle(panelPreview.Left,
		// panelPreview.Top, panelPreview.Width, panelPreview.Height));

		// panelPreview.Visible = false;
		// labelNumMeasurements.Text = projectedTrack.Count.ToString();

		// graphics.FillRectangle(controlBrush, panelPreview.Left,
		// panelPreview.Top, panelPreview.Width, panelPreview.Height);

		int centerX = panelPreview.getWidth() / 2;
		int centerY = panelPreview.getHeight() / 2;

		// lock (projectedTrack)
		// {
		if (mMeasureList.size() > 0)
		{

			// Erdradius / anzahl Kacheln = Meter pro Kachel
			double metersPerTile = 6378137.0 / Math.pow(2, projectionZoom);

			// Gemittelter Punkt der GPS-Messungen
			double medianLat = MeasuredCoord.Referenz.Latitude;
			double medianLon = MeasuredCoord.Referenz.Longitude;

			double peakLat = Math.max(
					Math.abs(mMeasureList.get(0).getLatitude() - medianLat),
					Math.abs(mMeasureList.get(mMeasureList.size() - 1)
							.getLatitude() - medianLat));
			double peakLon = Math.max(
					Math.abs(mMeasureList.get(0).getLongitude() - medianLon),
					Math.abs(mMeasureList.get(mMeasureList.size() - 1)
							.getLongitude() - medianLon));

			// Umrechnung in XY
			double medianX = Descriptor.LongitudeToTileX(projectionZoom,
					medianLon);
			double medianY = Descriptor.LatitudeToTileY(projectionZoom,
					medianLat);

			double extremeX = Descriptor.LongitudeToTileX(projectionZoom,
					peakLon + medianLon);
			double extremeY = Descriptor.LatitudeToTileY(projectionZoom,
					peakLat + medianLat);

			double peakX = Math.abs(extremeX - medianX);
			double peakY = Math.abs(extremeY - medianY);

			double maxPeak = Math.max(peakX, peakY);
			int minPix = Math.min(panelPreview.getWidth(),
					panelPreview.getHeight());

			double factor = (maxPeak > 0) ? (double) minPix / maxPeak : 1;

			// In Ausgabe-Variablen umkopieren
			// MedianPosition.Latitude = medianLat;
			// MedianPosition.Longitude = medianLon;

			// lblCoordinates.Text = MedianPosition.FormatCoordinate();

			int x = centerX;
			int y = centerY;

			// Track zeichnen

			for (int i = 1; i < mMeasureList.size(); i++)
			{

				PointD lastDrawEntry = Descriptor.projectCoordinate(
						mMeasureList.get(i - 1).getLatitude(), mMeasureList
								.get(i - 1).getLongitude(), projectionZoom);

				int lastX = (int) (centerX + (lastDrawEntry.X - medianX)
						* factor);
				int lastY = (int) (centerY - (lastDrawEntry.Y - medianY)
						* factor);

				PointD thisDrawEntry = Descriptor.projectCoordinate(
						mMeasureList.get(i).getLatitude(), mMeasureList.get(i)
								.getLongitude(), projectionZoom);

				x = (int) (centerX + (thisDrawEntry.X - medianX) * factor);
				y = (int) (centerY - (thisDrawEntry.Y - medianY) * factor);

				graphics.drawLine(lastX, lastY, x, y, redPen);
			}

			// graphics.FillEllipse(blueBrush, x - 2, y - 2, 4, 4);
		}
		// }

		graphics.drawLine(centerX, 0, centerX, panelPreview.getHeight(),
				blackPen);
		graphics.drawLine(0, centerY, panelPreview.getWidth(), centerY,
				blackPen);

		panelPreview.invalidate();
	}

}
