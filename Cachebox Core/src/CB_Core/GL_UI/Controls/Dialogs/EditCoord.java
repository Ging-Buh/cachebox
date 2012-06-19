package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.Converter.UTMConvert;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.TextField;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.g2d.NinePatch;

public class EditCoord extends Dialog
{
	private int aktPage = -1; // Deg-Min
	UTMConvert convert = new UTMConvert();

	Coordinate coord;
	ReturnListner mReturnListner;
	Box trDec;
	Box trMin;
	Box trSec;
	Box trUtm;
	// Allgemein
	MultiToggleButton bDec;
	MultiToggleButton bMin;
	MultiToggleButton bSec;
	MultiToggleButton bUtm;
	// Deg
	Button bDLat;
	TextField tbDLat;
	Button bDLon;
	TextField tbDLon;
	// Deg - Min
	// Button bMLat;
	TextField tbMLatDeg;
	TextField tbMLatMin;
	// Button bMLon;
	TextField tbMLonDeg;
	TextField tbMLonMin;
	// Deg - Min - Sec
	Button bSLat;
	TextField tbSLatDeg;
	TextField tbSLatMin;
	TextField tbSLatSec;
	Button bSLon;
	TextField tbSLonDeg;
	TextField tbSLonMin;
	TextField tbSLonSec;
	// Utm
	TextField tbUX;
	TextField tbUY;
	TextField tbUZone;
	Button bUX;
	Button bUY;

	public interface ReturnListner
	{
		public void returnCoord(Coordinate coord);
	}

	public EditCoord(CB_RectF rec, String Name, Coordinate Coord, ReturnListner returnListner)
	{
		super(rec, Name);
		coord = Coord;
		mReturnListner = returnListner;

		this.setBackground(new NinePatch(SpriteCache.getThemedSprite("text_field_back"), 16, 16, 16, 16));

		float left = nineBackground.getLeftWidth();
		float innerWidth = this.width - left - left;

		CB_RectF MTBRec = new CB_RectF(left, this.height - left - UiSizes.getButtonHeight(), innerWidth / 4, UiSizes.getButtonHeight());

		bDec = new MultiToggleButton(MTBRec, "bDec");
		bMin = new MultiToggleButton(MTBRec, "bDec");
		bSec = new MultiToggleButton(MTBRec, "bDec");
		bUtm = new MultiToggleButton(MTBRec, "bDec");

		bDec.setX(left);
		bMin.setX(bDec.getMaxX());
		bSec.setX(bMin.getMaxX());
		bUtm.setX(bSec.getMaxX());

		this.addChild(bDec);
		this.addChild(bMin);
		this.addChild(bSec);
		this.addChild(bUtm);

		MultiToggleButton.initialOn_Off_ToggleStates(bDec, "Dec", "Dec");
		MultiToggleButton.initialOn_Off_ToggleStates(bMin, "Min", "Min");
		MultiToggleButton.initialOn_Off_ToggleStates(bSec, "Sec", "Sec");
		MultiToggleButton.initialOn_Off_ToggleStates(bUtm, "UTM", "UTM");

		Button bOK = new Button(left, left, innerWidth / 2, UiSizes.getButtonHeight(), "OK Button");
		Button bCancel = new Button(bOK.getMaxX(), left, innerWidth / 2, UiSizes.getButtonHeight(), "Cancel Button");
		bDLat = new Button(left, bDec.getX() - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(), UiSizes.getButtonHeight(), "BDLat");
		bDLon = new Button(left, bDLat.getX() - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(), UiSizes.getButtonHeight(), "BDLat");
		CB_RectF EditTextBoxRec = new CB_RectF(bDLon.getMaxX() + margin, bDLon.getY(), this.width - bDLon.getMaxX() - margin, this.height
				- bDLon.getX() - bDec.getHeight() - left);

		trDec = new Box(EditTextBoxRec, "trDec");
		trMin = new Box(EditTextBoxRec, "trMin");
		trSec = new Box(EditTextBoxRec, "trSec");
		trUtm = new Box(EditTextBoxRec, "trUtm");

		this.addChild(trDec);
		this.addChild(trMin);
		this.addChild(trSec);
		this.addChild(trUtm);

		// Translations
		bOK.setText(GlobalCore.Translations.Get("ok"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));

		this.addChild(bOK);
		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (!parseView())
				{
					GL_Listener.glListener.Toast("Invalid COORD", 2000);
					return false;
				}

				if (mReturnListner != null)
				{
					GL_Listener.glListener.closeDialog();
					mReturnListner.returnCoord(coord);
				}
				else
					GL_Listener.glListener.closeDialog();
				return true;
			}
		});

		this.addChild(bCancel);
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				GL_Listener.glListener.closeDialog();
				return true;
			}
		});

	}

	@Override
	protected void Initial()
	{

		bDec.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showPage(0);
				return true;
			}
		});

		bMin.setState(1);
		bMin.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showPage(1);
				return true;
			}
		});

		bSec.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showPage(2);
				return true;
			}
		});
		bUtm.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showPage(3);
				return true;
			}
		});

		this.addChild(bDLat);
		bDLat.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (bDLat.getText().equals("N")) bDLat.setText("S");
				else
					bDLat.setText("N");
				return true;
			}
		});

		this.addChild(bDLon);
		bDLon.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (bDLon.getText().equals("E")) bDLon.setText("W");
				else
					bDLon.setText("E");
				return true;
			}
		});

		// trMin
		CB_RectF editRec = new CB_RectF(0, 0, (trMin.getWidth() - (margin * 3)) / 2, UiSizes.getButtonHeight());

		tbMLatDeg = new TextField(editRec, "tbMLatDeg");
		tbMLatMin = new TextField(editRec, "tbMLatMin");
		tbMLonDeg = new TextField(editRec, "tbMLonDeg");
		tbMLonMin = new TextField(editRec, "tbMLonMin");

		trMin.addChild(tbMLatDeg);
		trMin.addChild(tbMLatMin);
		trMin.addChild(tbMLonDeg);
		trMin.addChild(tbMLonMin);

		showPage(1);

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	private void showPage(int newPage)
	{
		if (aktPage >= 0) parseView();
		switch (newPage)
		{
		case 0:
			// show Degrees
			trDec.setVisibility(CB_View_Base.VISIBLE);
			trMin.setVisibility(CB_View_Base.INVISIBLE);
			trSec.setVisibility(CB_View_Base.INVISIBLE);
			trUtm.setVisibility(CB_View_Base.INVISIBLE);
			bDec.setState(1);
			bMin.setState(0);
			bSec.setState(0);
			bUtm.setState(0);
			if (coord.Latitude > 0) bDLat.setText("N");
			else
				bDLat.setText("S");
			if (coord.Longitude > 0) bDLon.setText("E");
			else
				bDLon.setText("W");
			tbDLat.setText(String.format("%.5f", coord.Latitude).replace(",", "."));
			tbDLon.setText(String.format("%.5f", coord.Longitude).replace(",", "."));
			break;
		case 1:
			// show Degree - Minute
			trDec.setVisibility(CB_View_Base.INVISIBLE);
			trMin.setVisibility(CB_View_Base.VISIBLE);
			trSec.setVisibility(CB_View_Base.INVISIBLE);
			trUtm.setVisibility(CB_View_Base.INVISIBLE);
			bDec.setState(0);
			bMin.setState(1);
			bSec.setState(0);
			bUtm.setState(0);
			if (coord.Latitude >= 0) bDLat.setText("N");
			else
				bDLat.setText("S");
			if (coord.Longitude >= 0) bDLon.setText("E");
			else
				bDLon.setText("W");

			double deg = (int) Math.abs(coord.Latitude);
			double frac = Math.abs(coord.Latitude) - deg;
			double min = frac * 60;
			tbMLatDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbMLatMin.setText(String.format("%.3f", min).replace(",", "."));

			deg = (int) Math.abs(coord.Longitude);
			frac = Math.abs(coord.Longitude) - deg;
			min = frac * 60;
			tbMLonDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbMLonMin.setText(String.format("%.3f", min).replace(",", "."));

			break;
		case 2:
			// show Degree - Minute - Second
			trMin.setVisibility(CB_View_Base.INVISIBLE);
			trDec.setVisibility(CB_View_Base.INVISIBLE);
			trSec.setVisibility(CB_View_Base.VISIBLE);
			trUtm.setVisibility(CB_View_Base.INVISIBLE);
			bDec.setState(0);
			bMin.setState(0);
			bSec.setState(1);
			bUtm.setState(0);

			deg = Math.abs((int) coord.Latitude);
			frac = Math.abs(coord.Latitude) - deg;
			min = frac * 60;
			int imin = (int) min;
			frac = min - imin;
			double sec = frac * 60;

			if (coord.Latitude > 0) bSLat.setText("N");
			else
				bSLat.setText("S");
			tbSLatDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbSLatMin.setText(String.valueOf(imin).replace(",", "."));
			tbSLatSec.setText(String.format("%.2f", sec).replace(",", "."));

			deg = Math.abs((int) coord.Longitude);
			frac = Math.abs(coord.Longitude) - deg;
			min = frac * 60;
			imin = (int) min;
			frac = min - imin;
			sec = frac * 60;

			if (coord.Longitude > 0) bSLon.setText("E");
			else
				bSLon.setText("W");
			tbSLonDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbSLonMin.setText(String.valueOf(imin).replace(",", "."));
			tbSLonSec.setText(String.format("%.2f", sec).replace(",", "."));

			break;
		case 3:
			// show UTM
			trMin.setVisibility(CB_View_Base.INVISIBLE);
			trDec.setVisibility(CB_View_Base.INVISIBLE);
			trSec.setVisibility(CB_View_Base.INVISIBLE);
			trUtm.setVisibility(CB_View_Base.VISIBLE);
			bDec.setState(0);
			bMin.setState(0);
			bSec.setState(0);
			bUtm.setState(1);

			double nording = 0;
			double easting = 0;
			String zone = "";
			convert.iLatLon2UTM(coord.Latitude, coord.Longitude);
			nording = convert.UTMNorthing;
			easting = convert.UTMEasting;
			zone = convert.sUtmZone;
			// tbUY.setText(String.Format(NumberFormatInfo.InvariantInfo, "{0:0}", Math.Floor(nording)));
			// tbUX.setText(String.Format(NumberFormatInfo.InvariantInfo, "{0:0}", Math.Floor(easting)));
			tbUY.setText(String.format("%.1f", nording).replace(",", "."));
			tbUX.setText(String.format("%.1f", easting).replace(",", "."));
			tbUZone.setText(zone);
			if (coord.Latitude > 0) bUY.setText("N");
			else if (coord.Latitude < 0) bUY.setText("S");
			if (coord.Longitude > 0) bUX.setText("E");
			else if (coord.Longitude < 0) bUX.setText("W");
			break;
		}
		aktPage = newPage;
	}

	private boolean parseView()
	{
		String scoord = "";
		switch (aktPage)
		{
		case 0:
			// show Degrees
			scoord += bDLat.getText() + " " + tbDLat.getText() + "\u00B0";
			scoord += " " + bDLon.getText() + " " + tbDLon.getText() + "\u00B0";
			break;
		case 1:
			// show Degree - Minute
			scoord += bDLat.getText() + " " + tbMLatDeg.getText() + "\u00B0 " + tbMLatMin.getText() + "\u0027";
			scoord += " " + bDLon.getText() + " " + tbMLonDeg.getText() + "\u00B0 " + tbMLonMin.getText() + "\u0027";
			break;
		case 2:
			// show Degree - Minute - Second
			scoord += bSLat.getText() + " " + tbSLatDeg.getText() + "\u00B0 " + tbSLatMin.getText() + "\u0027 " + tbSLatSec.getText()
					+ "\\u0022";
			scoord += " " + bSLon.getText() + " " + tbSLonDeg.getText() + "\u00B0 " + tbSLonMin.getText() + "\u0027 " + tbSLonSec.getText()
					+ "\\u0022";
			break;
		case 3:
			// show UTM
			scoord += tbUZone.getText() + " " + tbUX.getText() + " " + tbUY.getText();
			break;
		}

		// replace , with .
		scoord = scoord.replace(",", ".");

		Coordinate newCoord = new Coordinate(scoord);
		if (newCoord.Valid)
		{
			coord = newCoord;
			return true;
		}
		else
			return false;
	}

	@Override
	public GL_View_Base addChild(GL_View_Base view)
	{
		// die Childs in die Box umleiten
		this.addChildDirekt(view);

		return view;
	}

	@Override
	public void removeChilds()
	{
		this.removeChildsDirekt();
	}

}
