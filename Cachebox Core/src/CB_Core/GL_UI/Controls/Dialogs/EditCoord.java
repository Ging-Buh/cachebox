package CB_Core.GL_UI.Controls.Dialogs;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.Converter.UTMConvert;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_Core.GL_UI.Controls.NumPad;
import CB_Core.GL_UI.Controls.NumPad.keyEventListner;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.TextField;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.OnscreenKeyboard;

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

	TextField tbSLatDeg;
	TextField tbSLatMin;
	TextField tbSLatSec;

	TextField tbSLonDeg;
	TextField tbSLonMin;
	TextField tbSLonSec;
	// Utm
	TextField tbUX;
	TextField tbUY;
	TextField tbUZone;
	Label lUtmO;
	Label lUtmN;
	Label lUtmZ;

	NumPad numPad;

	TextField focusedTextField = null;

	public interface ReturnListner
	{
		public void returnCoord(Coordinate coord);
	}

	public EditCoord(CB_RectF rec, String Name, Coordinate Coord, ReturnListner returnListner)
	{
		super(rec, Name);
		coord = Coord;
		mReturnListner = returnListner;

		this.setBackground(new NinePatch(SpriteCache.getThemedSprite("activity_back"), 16, 16, 16, 16));

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
		bDLat = new Button(left, bDec.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(), UiSizes.getButtonHeight(), "BDLat");
		bDLon = new Button(left, bDLat.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(), UiSizes.getButtonHeight(), "bDLon");
		CB_RectF EditTextBoxRec = new CB_RectF(bDLon.getMaxX() + margin, bDLon.getY(), this.width - bDLon.getMaxX() - margin,
				bDLat.getMaxY() - bDLon.getY());

		trDec = new Box(EditTextBoxRec, "trDec");
		trMin = new Box(EditTextBoxRec, "trMin");
		trSec = new Box(EditTextBoxRec, "trSec");

		EditTextBoxRec.setHeight((bDLat.getMaxY() - bDLon.getY()) * 1.5f);
		EditTextBoxRec.setY(bDLon.getY() - bDLon.getHeight());
		EditTextBoxRec.setX(EditTextBoxRec.getX() + (margin * 3f));
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
				if (mReturnListner != null)
				{
					GL_Listener.glListener.closeDialog();
					mReturnListner.returnCoord(null);
				}
				else
					GL_Listener.glListener.closeDialog();
				return true;
			}
		});

		bDec.setOnStateChangedListner(new OnStateChangeListener()
		{
			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				if (State == 1) showPage(0);
			}
		});

		bMin.setState(1);
		bMin.setOnStateChangedListner(new OnStateChangeListener()
		{
			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				if (State == 1) showPage(1);
			}
		});

		bSec.setOnStateChangedListner(new OnStateChangeListener()
		{
			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				if (State == 1) showPage(2);
			}
		});
		bUtm.setOnStateChangedListner(new OnStateChangeListener()
		{
			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				if (State == 1) showPage(3);
			}
		});

	}

	@Override
	protected void Initial()
	{

		lUtmO = new Label(nineBackground.getLeftWidth(), bDec.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonWidthWide(),
				UiSizes.getButtonHeight(), "lUtmO");
		lUtmO.setText("Ostwert");
		this.addChild(lUtmO);

		lUtmN = new Label(nineBackground.getLeftWidth(), bDLat.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonWidthWide(),
				UiSizes.getButtonHeight(), "lUtmN");
		lUtmN.setText("Nordwert");
		this.addChild(lUtmN);

		lUtmZ = new Label(nineBackground.getLeftWidth(), bDLat.getY() - UiSizes.getButtonHeight() - UiSizes.getButtonHeight(),
				UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight(), "lUtmZ");
		lUtmZ.setText("Zone");
		this.addChild(lUtmZ);

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
		createTrMin();

		showPage(1);

		createTrDec();
		createTrSec();
		createTrUtn();

		GL_Listener.glListener.addRenderView(this, GL_Listener.FRAME_RATE_IDLE); // Cursor blink

	}

	private void createTrUtn()
	{
		CB_RectF editRec = new CB_RectF(0, 0, (trMin.getWidth() - (margin * 3)), UiSizes.getButtonHeight());
		editRec.setWidth(editRec.getWidth() - (margin * 2));

		tbUZone = new TextField(editRec, "tbUZone");
		setKeyboardHandling(tbUZone);

		editRec.setY(tbUZone.getMaxY());

		tbUY = new TextField(editRec, "tbUY");
		setKeyboardHandling(tbUY);

		editRec.setY(tbUY.getMaxY());

		tbUX = new TextField(editRec, "tbUX");
		setKeyboardHandling(tbUX);

		trUtm.addChild(tbUX);
		trUtm.addChild(tbUY);
		trUtm.addChild(tbUZone);
	}

	private void createTrSec()
	{
		CB_RectF editRec = new CB_RectF(0, 0, (trMin.getWidth() - (margin * 3)) / 3, UiSizes.getButtonHeight());
		editRec.setWidth(editRec.getWidth() - (margin * 2));

		CB_RectF labelRec = new CB_RectF(new SizeF(margin, UiSizes.getButtonHeight()));

		tbSLonDeg = new TextField(editRec, "tbSLonDeg");
		setKeyboardHandling(tbSLonDeg);

		labelRec.setX(tbSLonDeg.getMaxX());
		Label l1 = new Label(labelRec, "l1");
		l1.setText("�");
		trSec.addChild(l1);

		editRec.setX(l1.getMaxX() + margin);
		tbSLonMin = new TextField(editRec, "tbSLonMin");
		setKeyboardHandling(tbSLonMin);

		labelRec.setX(tbSLonMin.getMaxX());
		Label l3 = new Label(labelRec, "l3");
		l3.setText("'");
		trSec.addChild(l3);

		editRec.setX(l3.getMaxX() + margin);
		tbSLonSec = new TextField(editRec, "tbSLonSec");
		setKeyboardHandling(tbSLonSec);

		labelRec.setX(tbSLonSec.getMaxX());
		Label l5 = new Label(labelRec, "l5");
		l5.setText("\"");
		trSec.addChild(l5);

		editRec.setX(tbSLonDeg.getX());
		editRec.setY(tbSLonDeg.getMaxY());

		tbSLatDeg = new TextField(editRec, "tbSLatDeg");
		setKeyboardHandling(tbSLatDeg);

		labelRec.setX(tbSLonDeg.getMaxX());
		labelRec.setY(tbSLonDeg.getMaxY());
		Label l2 = new Label(labelRec, "l2");
		l2.setText("�");
		trSec.addChild(l2);

		editRec.setX(l2.getMaxX() + margin);
		tbSLatMin = new TextField(editRec, "tbSLatMin");
		setKeyboardHandling(tbSLatMin);

		labelRec.setX(tbSLatMin.getMaxX());
		Label l4 = new Label(labelRec, "l4");
		l4.setText("'");
		trSec.addChild(l4);

		editRec.setX(l4.getMaxX() + margin);
		tbSLatSec = new TextField(editRec, "tbSLatSec");
		setKeyboardHandling(tbSLatSec);

		labelRec.setX(tbSLatSec.getMaxX());
		Label l6 = new Label(labelRec, "l6");
		l6.setText("\"");
		trSec.addChild(l6);

		trSec.addChild(tbSLatDeg);
		trSec.addChild(tbSLatMin);
		trSec.addChild(tbSLatSec);
		trSec.addChild(tbSLonDeg);
		trSec.addChild(tbSLonMin);
		trSec.addChild(tbSLonSec);

	}

	private void createTrDec()
	{
		CB_RectF editRec = new CB_RectF(0, 0, (trMin.getWidth() - (margin * 3)), UiSizes.getButtonHeight());
		editRec.setWidth(editRec.getWidth() - (margin * 2));

		CB_RectF labelRec = new CB_RectF(new SizeF(margin, UiSizes.getButtonHeight()));

		tbDLon = new TextField(editRec, "tbDLon");
		setKeyboardHandling(tbDLon);

		labelRec.setX(tbDLon.getMaxX());
		Label l1 = new Label(labelRec, "l1");
		l1.setText("�");
		trDec.addChild(l1);

		editRec.setY(tbDLon.getMaxY());

		tbDLat = new TextField(editRec, "tbDLat");
		setKeyboardHandling(tbDLat);

		labelRec.setX(tbDLon.getMaxX());
		labelRec.setY(tbDLon.getMaxY());
		Label l2 = new Label(labelRec, "l2");
		l2.setText("�");
		trDec.addChild(l2);

		trDec.addChild(tbDLat);
		trDec.addChild(tbDLon);
	}

	private void createTrMin()
	{
		CB_RectF editRec = new CB_RectF(0, 0, (trMin.getWidth() - (margin * 3)) / 2, UiSizes.getButtonHeight());
		editRec.setWidth(editRec.getWidth() - (margin * 2));

		CB_RectF labelRec = new CB_RectF(new SizeF(margin, UiSizes.getButtonHeight()));

		tbMLonDeg = new TextField(editRec, "tbMLonDeg");
		setKeyboardHandling(tbMLonDeg);

		labelRec.setX(tbMLonDeg.getMaxX());
		Label l1 = new Label(labelRec, "l1");
		l1.setText("�");
		trMin.addChild(l1);

		editRec.setX(l1.getMaxX() + margin);
		tbMLonMin = new TextField(editRec, "tbMLonMin");
		setKeyboardHandling(tbMLonMin);

		labelRec.setX(tbMLonMin.getMaxX());
		Label l3 = new Label(labelRec, "l3");
		l3.setText("'");
		trMin.addChild(l3);

		editRec.setX(tbMLonDeg.getX());
		editRec.setY(tbMLonDeg.getMaxY());
		tbMLatDeg = new TextField(editRec, "tbMLatDeg");
		setKeyboardHandling(tbMLatDeg);

		labelRec.setX(tbMLatDeg.getMaxX());
		labelRec.setY(tbMLonDeg.getMaxY());
		Label l2 = new Label(labelRec, "l2");
		l2.setText("�");
		trMin.addChild(l2);

		editRec.setX(l2.getMaxX() + margin);
		tbMLatMin = new TextField(editRec, "tbMLatMin");
		setKeyboardHandling(tbMLatMin);

		labelRec.setX(tbMLatMin.getMaxX());
		Label l4 = new Label(labelRec, "l4");
		l4.setText("'");
		trMin.addChild(l4);

		trMin.addChild(tbMLatDeg);
		trMin.addChild(tbMLatMin);
		trMin.addChild(tbMLonDeg);
		trMin.addChild(tbMLonMin);
	}

	private ArrayList<TextField> allTextFields = new ArrayList<TextField>();

	private void setKeyboardHandling(final TextField textField)
	{
		textField.setOnscreenKeyboard(new OnscreenKeyboard()
		{
			@Override
			public void show(boolean arg0)
			{
				boolean showKeayboard = false;

				for (TextField tmp : allTextFields)
				{
					tmp.resetFocus();
				}

				textField.setFocus(true);
				focusedTextField = textField;

				if (tbUZone != null && textField.equals(tbUZone)) showKeayboard = true;

				if (showKeayboard) Gdx.input.setOnscreenKeyboardVisible(showKeayboard);
			}
		});

		allTextFields.add(textField);
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
			lUtmO.setVisibility(CB_View_Base.INVISIBLE);
			lUtmN.setVisibility(CB_View_Base.INVISIBLE);
			lUtmZ.setVisibility(CB_View_Base.INVISIBLE);
			bDLat.setVisibility(CB_View_Base.VISIBLE);
			bDLon.setVisibility(CB_View_Base.VISIBLE);
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
			tbDLat.setFocus();
			tbDLon.setText(String.format("%.5f", coord.Longitude).replace(",", "."));

			showNumPad(NumPad.Type.withDot);

			break;
		case 1:
			// show Degree - Minute
			lUtmO.setVisibility(CB_View_Base.INVISIBLE);
			lUtmN.setVisibility(CB_View_Base.INVISIBLE);
			lUtmZ.setVisibility(CB_View_Base.INVISIBLE);
			bDLat.setVisibility(CB_View_Base.VISIBLE);
			bDLon.setVisibility(CB_View_Base.VISIBLE);
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

			tbMLonDeg.setFocus();

			showNumPad(NumPad.Type.withDot);

			break;
		case 2:
			// show Degree - Minute - Second
			lUtmO.setVisibility(CB_View_Base.INVISIBLE);
			lUtmN.setVisibility(CB_View_Base.INVISIBLE);
			lUtmZ.setVisibility(CB_View_Base.INVISIBLE);
			bDLat.setVisibility(CB_View_Base.VISIBLE);
			bDLon.setVisibility(CB_View_Base.VISIBLE);
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

			if (coord.Latitude >= 0) bDLat.setText("N");
			else
				bDLat.setText("S");
			if (coord.Longitude >= 0) bDLon.setText("E");
			else
				bDLon.setText("W");

			tbSLatDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbSLatMin.setText(String.valueOf(imin).replace(",", "."));
			tbSLatSec.setText(String.format("%.2f", sec).replace(",", "."));

			deg = Math.abs((int) coord.Longitude);
			frac = Math.abs(coord.Longitude) - deg;
			min = frac * 60;
			imin = (int) min;
			frac = min - imin;
			sec = frac * 60;

			tbSLonDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbSLonMin.setText(String.valueOf(imin).replace(",", "."));
			tbSLonSec.setText(String.format("%.2f", sec).replace(",", "."));

			tbSLonDeg.setFocus();

			showNumPad(NumPad.Type.withDot);

			break;
		case 3:
			// show UTM
			lUtmO.setVisibility(CB_View_Base.VISIBLE);
			lUtmN.setVisibility(CB_View_Base.VISIBLE);
			lUtmZ.setVisibility(CB_View_Base.VISIBLE);
			bDLat.setVisibility(CB_View_Base.INVISIBLE);
			bDLon.setVisibility(CB_View_Base.INVISIBLE);
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

			if (coord.Latitude >= 0) bDLat.setText("N");
			else
				bDLat.setText("S");
			if (coord.Longitude >= 0) bDLon.setText("E");
			else
				bDLon.setText("W");

			tbUY.setFocus();

			showNumPad(NumPad.Type.withDot);

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
			scoord += bDLat.getText() + " " + tbSLatDeg.getText() + "\u00B0 " + tbSLatMin.getText() + "\u0027 " + tbSLatSec.getText()
					+ "\\u0022";
			scoord += " " + bDLon.getText() + " " + tbSLonDeg.getText() + "\u00B0 " + tbSLonMin.getText() + "\u0027 " + tbSLonSec.getText()
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

	private void showNumPad(NumPad.Type type)
	{
		if (numPad != null) return;
		float numWidth = this.width - nineBackground.getLeftWidth() - nineBackground.getRightWidth();
		float numHeight = this.height - nineBackground.getBottomHeight() - nineBackground.getTopHeight() - (UiSizes.getButtonHeight() * 2)
				- (margin * 2);

		numHeight -= trUtm.getHeight();

		CB_RectF numRec = new CB_RectF(nineBackground.getLeftWidth(), UiSizes.getButtonHeight() + (margin * 3), numWidth, numHeight);

		numPad = new NumPad(numRec, "numPad", type, keyListner);

		this.addChild(numPad);
	}

	keyEventListner keyListner = new keyEventListner()
	{

		@Override
		public void KeyPressed(String value)
		{
			if (focusedTextField == null) return;

			int cursorPos = focusedTextField.getCursorPosition();

			if (value.equals("O"))
			{
				// sollte nicht passieren, da der Button nicht sichtbar ist
			}
			else if (value.equals("C"))
			{
				// sollte nicht passieren, da der Button nicht sichtbar ist
			}
			else if (value.equals("<"))
			{
				if (cursorPos == 0) cursorPos = 1; // cursorPos darf nicht 0 sein
				focusedTextField.setCursorPosition(cursorPos - 1);
			}
			else if (value.equals(">"))
			{
				focusedTextField.setCursorPosition(cursorPos + 1);
			}
			else if (value.equals("D"))
			{
				if (cursorPos > 0)
				{
					String text2 = focusedTextField.getText().substring(cursorPos);
					String text1 = focusedTextField.getText().substring(0, cursorPos - 1);

					focusedTextField.setText(text1 + text2);
					focusedTextField.setCursorPosition(cursorPos + -1);
				}
			}
			else
			{
				String text2 = focusedTextField.getText().substring(cursorPos);
				String text1 = focusedTextField.getText().substring(0, cursorPos);

				focusedTextField.setText(text1 + value + text2);
				focusedTextField.setCursorPosition(cursorPos + value.length());
			}

		}
	};
}
