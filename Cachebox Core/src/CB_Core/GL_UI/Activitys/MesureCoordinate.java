package CB_Core.GL_UI.Activitys;

import CB_Core.GlobalCore;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.SatBarChart;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Locator.Locator;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Coordinate;
import CB_Core.Types.MeasuredCoord;
import CB_Core.Types.MeasuredCoordList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MesureCoordinate extends ActivityBase implements PositionChangedEvent
{
	private Button bOK = null;
	private Button bCancel = null;
	private MesureCoordinate that;
	private MeasuredCoordList mMeasureList = new MeasuredCoordList();
	private Label lblMeasureCount;
	private Label lblMeasureCoord;
	private Label lblDescMeasureCount;
	private Label lblDescMeasureCoord;
	private int MeasureCount = 0;
	private Sprite drawing;
	private SatBarChart chart;

	private final int projectionZoom = 16;// 18;
	// Erdradius / anzahl Kacheln = Meter pro Kachel
	private final double metersPerTile = 6378137.0 / Math.pow(2, projectionZoom);

	private ReturnListner mReturnListner;

	public interface ReturnListner
	{
		public void returnCoord(Coordinate coord);
	}

	public MesureCoordinate(CB_RectF rec, String Name, ReturnListner listner)
	{
		super(rec, Name);
		mReturnListner = listner;

		that = this;

		MeasuredCoord.Referenz = GlobalCore.LastValidPosition;

		if (MeasuredCoord.Referenz == null)
		{
			MeasuredCoord.Referenz = new Coordinate();
		}

		iniOkCancel();
		iniLabels();
		lblDescMeasureCoord.setText(GlobalCore.Translations.Get("MeasureCoord"));
		lblDescMeasureCount.setText(GlobalCore.Translations.Get("MeasureCount"));

		CB_Core.Events.PositionChangedEventList.Add(this);

		iniChart();

	}

	private void iniOkCancel()
	{
		CB_RectF btnRec = new CB_RectF(Left, Bottom, (width - Left - Right) / 2, UiSizes.getButtonHeight());
		bOK = new Button(btnRec, "OkButton");

		btnRec.setX(bOK.getMaxX());
		bCancel = new Button(btnRec, "CancelButton");

		bOK.setText(GlobalCore.Translations.Get("ok"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));

		this.addChild(bOK);
		this.addChild(bCancel);

		bOK.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mReturnListner != null) mReturnListner.returnCoord(mMeasureList.getAccuWeightedAverageCoord());
				finish();
				return true;
			}
		});

		bCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mReturnListner != null) mReturnListner.returnCoord(null);
				finish();
				return true;
			}
		});

	}

	private void iniLabels()
	{
		float y = bOK.getMaxY() + (that.width - Left - Right) + (margin * 3);
		float w = Math.max(Fonts.Mesure(GlobalCore.Translations.Get("MeasureCoord")).width,
				Fonts.Mesure(GlobalCore.Translations.Get("MeasureCount")).width);
		CB_RectF rec = new CB_RectF(Left + margin, y, w, MesuredLabelHeight);
		CB_RectF rec2 = new CB_RectF(rec.getMaxX() + margin, y, width - Left - Right - w - margin, MesuredLabelHeight);

		lblDescMeasureCount = new Label(rec, "");

		lblMeasureCount = new Label(rec2, "");

		rec2.setY(lblMeasureCount.getMaxY() + margin);
		rec.setY(lblMeasureCount.getMaxY() + margin);

		lblDescMeasureCoord = new Label(rec, "");

		lblMeasureCoord = new Label(rec2, "");

		this.addChild(lblDescMeasureCount);
		this.addChild(lblMeasureCount);
		this.addChild(lblDescMeasureCoord);
		this.addChild(lblMeasureCoord);
	}

	private void iniChart()
	{
		float w = this.width - Left - Right - margin - margin;
		float h = this.height - lblDescMeasureCoord.getMaxY() - Top - margin;

		CB_RectF rec = new CB_RectF(Left + margin, lblDescMeasureCoord.getMaxY() + margin, w, h);
		chart = new SatBarChart(rec, "");
		this.addChild(chart);
	}

	@Override
	protected void finish()
	{
		chart.dispose();
		chart = null;
		drawing = null;
		GL.that.removeRenderView(this);
		super.finish();
	}

	private boolean inRepaint = false;

	@Override
	protected void Initial()
	{
		repaintPreview();
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		if (drawing != null) drawing.draw(batch);

		if (redraw) repaintPreview();

	}

	private boolean redraw = true;

	private void repaintPreview()
	{
		if (inRepaint) return;
		inRepaint = true;

		float innerWidth = that.width - Left - Right;

		CB_RectF panelRec = new CB_RectF(Left, bOK.getMaxY(), innerWidth, innerWidth);

		int w = getNextHighestPO2((int) panelRec.getWidth());
		int h = getNextHighestPO2((int) panelRec.getHeight());
		Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);

		p.setColor(Color.LIGHT_GRAY);
		p.fillRectangle(0, 0, (int) panelRec.getWidth(), (int) panelRec.getHeight());

		int centerX = (int) panelRec.getHalfWidth();
		int centerY = (int) panelRec.getHalfHeight();

		float minPix = Math.min(panelRec.getWidth(), panelRec.getHeight());

		if (mMeasureList.size() > 0)
		{
			// Gemittelter Punkt der GPS-Messungen
			double medianLat = MeasuredCoord.Referenz.Latitude;
			double medianLon = MeasuredCoord.Referenz.Longitude;

			MeasuredCoordList sortetdList = (MeasuredCoordList) mMeasureList.clone();
			sortetdList.sort();

			double peakLat = Math.max(Math.abs(sortetdList.get(0).getLatitude() - medianLat),
					Math.abs(sortetdList.get(sortetdList.size() - 1).getLatitude() - medianLat));
			double peakLon = Math.max(Math.abs(sortetdList.get(0).getLongitude() - medianLon),
					Math.abs(sortetdList.get(sortetdList.size() - 1).getLongitude() - medianLon));

			// Umrechnung in XY
			double medianX = Descriptor.LongitudeToTileX(projectionZoom, medianLon);
			double medianY = Descriptor.LatitudeToTileY(projectionZoom, medianLat);

			double extremeX = Descriptor.LongitudeToTileX(projectionZoom, peakLon + medianLon);
			double extremeY = Descriptor.LatitudeToTileY(projectionZoom, peakLat + medianLat);

			double peakX = Math.abs(extremeX - medianX);
			double peakY = Math.abs(extremeY - medianY);

			double maxPeak = Math.max(peakX, peakY);

			double factor = (maxPeak > 0) ? (double) minPix / maxPeak : 1;

			int x = (int) centerX;
			int y = (int) centerY;

			// Track zeichnen

			for (int i = 1; i < mMeasureList.size(); i++)
			{

				PointD lastDrawEntry = Descriptor.projectCoordinate(mMeasureList.get(i - 1).getLatitude(), mMeasureList.get(i - 1)
						.getLongitude(), projectionZoom);

				int lastX = (int) (centerX + (lastDrawEntry.X - medianX) * factor);
				int lastY = (int) (centerY - (lastDrawEntry.Y - medianY) * factor);

				PointD thisDrawEntry = Descriptor.projectCoordinate(mMeasureList.get(i).getLatitude(), mMeasureList.get(i).getLongitude(),
						projectionZoom);

				x = (int) (centerX + (thisDrawEntry.X - medianX) * factor);
				y = (int) (centerY - (thisDrawEntry.Y - medianY) * factor);

				p.setColor(Color.RED);
				p.drawLine(lastX, lastY, x, y);

			}

			p.setColor(Color.BLUE);
			p.drawCircle(x, y, 4);
		}

		//
		int m2 = (int) ((4 * minPix) / metersPerTile);
		int m4 = m2 * 2;

		p.setColor(Color.BLACK);
		p.drawCircle(centerX, centerY, m2);
		p.drawCircle(centerX, centerY, m4);

		p.drawLine(centerX, 0, centerX, (int) panelRec.getHeight());
		p.drawLine(0, centerY, (int) panelRec.getWidth(), centerY);

		Texture tex = new Texture(p);

		drawing = new Sprite(tex, (int) panelRec.getWidth(), (int) panelRec.getHeight());
		drawing.setX(Left);
		drawing.setY(bOK.getMaxY() + Bottom);
		p.dispose();

		inRepaint = false;

		redraw = false;

		GL.that.renderOnce("MesureCoord");
	}

	@Override
	public void PositionChanged(Locator locator)
	{
		if (mMeasureList == null)
		{
			GL.that.Toast("MeasureList = null");
			return;
		}

		Coordinate coord = new Coordinate(locator.getLocation());

		if (MeasureCount == 0) lblMeasureCoord.setText("");

		MeasureCount++;
		mMeasureList.add(new MeasuredCoord(locator.getLocation().getLatitude(), locator.getLocation().getLongitude(), locator.getLocation()
				.getAccuracy()));

		lblMeasureCount.setText(String.valueOf(MeasureCount) + "/" + String.valueOf(mMeasureList.size()));

		// nach jeder 10. Messung die Liste Aufräumen
		if (mMeasureList.size() % 10 == 0)
		{
			mMeasureList.setAverage();
			mMeasureList.clearDiscordantValue();
			lblMeasureCoord.setText(mMeasureList.toString());
		}

		redraw = true;

	}

	@Override
	public void OrientationChanged(float heading)
	{
		// interesiert nicht
	}

	@Override
	public String getReceiverName()
	{
		return "MesureCoordinate";
	}

}
