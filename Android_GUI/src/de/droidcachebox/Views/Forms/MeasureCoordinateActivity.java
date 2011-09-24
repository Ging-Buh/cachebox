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

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Custom_Controls.CanvasDrawControl;
import de.droidcachebox.Ui.ActivityUtils;
import CB_Core.GlobalCore;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Types.Coordinate;
import CB_Core.Types.MeasuredCoord;
import CB_Core.Types.MeasuredCoordList;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

	@Override
	protected void onResume()
	{
		MeasureCount = 0;
		if (mMeasureList != null) mMeasureList.clear();
		super.onResume();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		mMeasureList = null;
		super.onDestroy();
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

		if (MeasureCount == 0) lblMeasureCoord.setText(coord.toString());

		MeasureCount++;
		mMeasureList.add(new MeasuredCoord(location.getLatitude(), location
				.getLongitude(), location.getAccuracy()));

		lblMeasureCount.setText(String.valueOf(MeasureCount) + "/"
				+ String.valueOf(mMeasureList.size()));

		// nach jeder 10. Messung die Liste Aufräumen
		if (mMeasureList.size() % 10 == 0)
		{
			mMeasureList.setAverage();
			mMeasureList.clearDiscordantValue();
			lblMeasureCoord.setText(Global.FormatLatitudeDM(mMeasureList
					.getAccuWeightedAverageCoord().Latitude)
					+ " "
					+ Global.FormatLongitudeDM(mMeasureList
							.getAccuWeightedAverageCoord().Longitude));
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
			// Gemittelter Punkt der GPS-Messungen
			double medianLat = MeasuredCoord.Referenz.Latitude;
			double medianLon = MeasuredCoord.Referenz.Longitude;

			MeasuredCoordList sortetdList = (MeasuredCoordList) mMeasureList
					.clone();
			sortetdList.sort();

			double peakLat = Math.max(
					Math.abs(sortetdList.get(0).getLatitude() - medianLat),
					Math.abs(sortetdList.get(sortetdList.size() - 1)
							.getLatitude() - medianLat));
			double peakLon = Math.max(
					Math.abs(sortetdList.get(0).getLongitude() - medianLon),
					Math.abs(sortetdList.get(sortetdList.size() - 1)
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
