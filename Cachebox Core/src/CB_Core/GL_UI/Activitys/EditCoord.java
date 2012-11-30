package CB_Core.GL_UI.Activitys;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.Converter.UTMConvert;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.NumPad;
import CB_Core.GL_UI.Controls.NumPad.keyEventListner;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EditCoord extends ActivityBase
{
	private int aktPage = -1; // Deg-Min
	private UTMConvert convert = new UTMConvert();

	private Coordinate cancelCoord;
	private Coordinate coord;
	private ReturnListner mReturnListner;
	private Box trUtm;

	// Allgemein
	private BitmapFont font = Fonts.getCompass();

	private MultiToggleButton bDec;
	private MultiToggleButton bMin;
	private MultiToggleButton bSec;
	private MultiToggleButton bUtm;

	private int focus; // Nr of Button for next Input
	private int focusStartLon; // jump to this first input digit on Lon - input

	private Box pnlNumPad;

	// Deg : N_48.46270° E009.28468°
	private Box pnlD;
	private Button[] btnDLat;
	private Button[] btnDLon;

	// Deg - Min : N_48°27.762' E009°17.081'
	private Box pnlDM;
	private Button[] btnDMLat;
	private Button[] btnDMLon;

	// Deg - Min - Sec : N_48°28'56.16" E009°19'40.14"
	private Box pnlDMS;
	private Button[] btnDMSLat;
	private Button[] btnDMSLon;

	// Utm
	private Button bDLat;
	private Button bDLon;
	private EditWrapedTextField tbUX;
	private EditWrapedTextField tbUY;
	private EditWrapedTextField tbUZone;
	private Label lUtmO;
	private Label lUtmN;
	private Label lUtmZ;

	private NumPad numPad;

	private EditWrapedTextField focusedTextField = null;

	public interface ReturnListner
	{
		public void returnCoord(Coordinate coord);
	}

	public EditCoord(CB_RectF rec, String Name, Coordinate Coord, ReturnListner returnListner)
	{
		super(rec, Name);

		coord = Coord;
		cancelCoord = coord.copy();
		mReturnListner = returnListner;

		bDec = new MultiToggleButton("bDec");
		bMin = new MultiToggleButton("bMin");
		bSec = new MultiToggleButton("bSec");
		bUtm = new MultiToggleButton("bUtm");
		((Button) bDec).setFont(font);
		((Button) bMin).setFont(font);
		((Button) bSec).setFont(font);
		((Button) bUtm).setFont(font);
		this.addNext(bDec);
		this.addNext(bMin);
		this.addNext(bSec);
		this.addLast(bUtm);
		MultiToggleButton.initialOn_Off_ToggleStates(bDec, "Dec", "Dec");
		MultiToggleButton.initialOn_Off_ToggleStates(bMin, "Min", "Min");
		MultiToggleButton.initialOn_Off_ToggleStates(bSec, "Sec", "Sec");
		MultiToggleButton.initialOn_Off_ToggleStates(bUtm, "UTM", "UTM");

		Button btnOK = new Button("btnOK");
		Button btnCancel = new Button("btnCancel");
		this.initRow(false);
		this.addNext(btnOK);
		this.addLast(btnCancel);
		btnCancel.setText(GlobalCore.Translations.Get("cancel"), font, Fonts.getFontColor());
		btnOK.setText(GlobalCore.Translations.Get("ok"), font, Fonts.getFontColor());

		pnlNumPad = new Box(this.getAvailableWidth(), this.getAvailableHeight(), "pnlNumPad");
		this.createNumPad(pnlNumPad);
		this.addLast(pnlNumPad);

		this.pnlD = new Box(this.getAvailableWidth(), this.getAvailableHeight(), "pnlD");
		this.createD(this.pnlD);
		this.addLast(this.pnlD);

		pnlDM = new Box(pnlD.copy(), "pnlDM");
		this.createDM(this.pnlDM);
		this.addChild(pnlDM);

		pnlDMS = new Box(pnlD.copy(), "pnlDMS");
		this.createDMS(this.pnlDMS);
		this.addChild(pnlDMS);

		bDLat = new Button(this.getLeftWidth(), bDec.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(),
				UiSizes.getButtonHeight(), "BDLat");
		bDLon = new Button(this.getLeftWidth(), bDLat.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(),
				UiSizes.getButtonHeight(), "bDLon");
		CB_RectF EditTextBoxRec = new CB_RectF(bDLon.getMaxX() + margin, bDLon.getY(), this.width - bDLon.getMaxX() - margin,
				bDLat.getMaxY() - bDLon.getY());
		EditTextBoxRec.setHeight((bDLat.getMaxY() - bDLon.getY()) * 1.5f);
		EditTextBoxRec.setY(bDLon.getY() - bDLon.getHeight());
		EditTextBoxRec.setX(EditTextBoxRec.getX() + (margin * 3f));
		trUtm = new Box(EditTextBoxRec, "trUtm");
		this.addChild(trUtm);

		btnOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (!parseView())
				{
					GL.that.Toast("Invalid COORD", 2000);
					return true;
				}

				if (mReturnListner != null)
				{
					finish();
					mReturnListner.returnCoord(coord);
				}
				else
					finish();
				return true;
			}
		});
		btnCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mReturnListner != null)
				{
					GL.that.closeActivity();
					mReturnListner.returnCoord(cancelCoord);
				}
				else
					GL.that.closeActivity();
				return true;
			}
		});

	}

	@Override
	protected void Initial()
	{

		lUtmO = new Label(drawableBackground.getLeftWidth(), bDec.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonWidthWide(),
				UiSizes.getButtonHeight(), "lUtmO");
		lUtmO.setText("Ostwert");
		this.addChild(lUtmO);

		lUtmN = new Label(drawableBackground.getLeftWidth(), bDLat.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonWidthWide(),
				UiSizes.getButtonHeight(), "lUtmN");
		lUtmN.setText("Nordwert");
		this.addChild(lUtmN);

		lUtmZ = new Label(drawableBackground.getLeftWidth(), bDLat.getY() - UiSizes.getButtonHeight() - UiSizes.getButtonHeight(),
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

		this.addChild(bDLon);

		createTrUtn();

		showNumPad(NumPad.Type.withDot);
		showPage(1);

	}

	private void createTrUtn()
	{
		CB_RectF editRec = new CB_RectF(0, 0, (pnlDM.getWidth() - (margin * 3)), UiSizes.getButtonHeight());
		editRec.setWidth(editRec.getWidth() - (margin * 2));

		tbUZone = new EditWrapedTextField(this, editRec, "tbUZone");
		setKeyboardHandling(tbUZone);

		editRec.setY(tbUZone.getMaxY());

		tbUY = new EditWrapedTextField(this, editRec, "tbUY");
		setKeyboardHandling(tbUY);

		editRec.setY(tbUY.getMaxY());

		tbUX = new EditWrapedTextField(this, editRec, "tbUX");
		setKeyboardHandling(tbUX);

		trUtm.addChild(tbUX);
		trUtm.addChild(tbUY);
		trUtm.addChild(tbUZone);
	}

	private void createD(Box panel)
	{

		this.btnDLat = new Button[9]; // N_48[.]46270[°]
		this.btnDLon = new Button[9]; // E009[.]28468[°]
		for (int i = 0; i < 9; i++)
		{
			this.btnDLat[i] = new Button(this, "btnDLat" + i);
			this.btnDLon[i] = new Button(this, "btnDLon" + i);
		}

		Label lbl1 = new Label("lbl1");
		Label lbl2 = new Label("lbl2");
		Label lbl3 = new Label("lbl3");
		Label lbl4 = new Label("lbl4");

		// Lat
		for (int i = 0; i < 4; i++)
		{
			panel.addNext(this.btnDLat[i]);
		}
		panel.addNext(lbl2, 0.5f); // [.]
		for (int i = 4; i < 9; i++)
		{
			panel.addNext(this.btnDLat[i]);
		}
		panel.addLast(lbl1, 0.5f);
		// Lon
		for (int i = 0; i < 4; i++)
		{
			panel.addNext(this.btnDLon[i]);
		}
		panel.addNext(lbl4, 0.5f);
		for (int i = 4; i < 9; i++)
		{
			panel.addNext(this.btnDLon[i]);
		}
		panel.addLast(lbl3, 0.5f);

		lbl1.setText("°", font, Fonts.getFontColor());
		lbl2.setText(".", font, Fonts.getFontColor());
		lbl3.setText("°", font, Fonts.getFontColor());
		lbl4.setText(".", font, Fonts.getFontColor());
		this.setClickHandlers(this.btnDLat, this.btnDLon);
	}

	private void createDM(Box panel)
	{

		this.btnDMLat = new Button[9]; // N_48[°]29[.]369
		this.btnDMLon = new Button[9]; // E__9[°]15[.]807
		for (int i = 0; i < 9; i++)
		{
			this.btnDMLat[i] = new Button(this, "btnDMLat" + i);
			this.btnDMLon[i] = new Button(this, "btnDMLon" + i);
		}

		Label lbl1 = new Label("lbl1");
		Label lbl2 = new Label("lbl2");
		Label lbl3 = new Label("lbl3");
		Label lbl4 = new Label("lbl4");

		// Lat
		for (int i = 0; i < 4; i++)
		{
			panel.addNext(this.btnDMLat[i]);
		}
		panel.addNext(lbl1, 0.5f);
		panel.addNext(this.btnDMLat[4]);
		panel.addNext(this.btnDMLat[5]);
		panel.addNext(lbl2, 0.5f);
		panel.addNext(this.btnDMLat[6]);
		panel.addNext(this.btnDMLat[7]);
		panel.addLast(this.btnDMLat[8]);
		// Lon
		for (int i = 0; i < 4; i++)
		{
			panel.addNext(this.btnDMLon[i]);
		}
		panel.addNext(lbl3, 0.5f);
		panel.addNext(this.btnDMLon[4]);
		panel.addNext(this.btnDMLon[5]);
		panel.addNext(lbl4, 0.5f);
		panel.addNext(this.btnDMLon[6]);
		panel.addNext(this.btnDMLon[7]);
		panel.addLast(this.btnDMLon[8]);

		lbl1.setText("°", font, Fonts.getFontColor());
		lbl2.setText(".", font, Fonts.getFontColor());
		lbl3.setText("°", font, Fonts.getFontColor());
		lbl4.setText(".", font, Fonts.getFontColor());
		this.setClickHandlers(this.btnDMLat, this.btnDMLon);
	}

	private void createDMS(Box panel)
	{

		this.btnDMSLat = new Button[10]; // N_48[°]28[']56[.]16["]
		this.btnDMSLon = new Button[10]; // E__9[°]19[']40[.]14["]
		for (int i = 0; i < 10; i++)
		{
			this.btnDMSLat[i] = new Button(this, "btnDMSLat" + i);
			this.btnDMSLon[i] = new Button(this, "btnDMSLon" + i);
		}

		// Lat
		Label lbldeglat = new Label("lbldeglat");
		Label lblminlat = new Label("lblminlat");
		Label lblpntlat = new Label("lblpntlat");
		Label lblseclat = new Label("lblseclat");
		for (int i = 0; i < 4; i++)
		{
			panel.addNext(this.btnDMSLat[i]);
		}
		panel.addNext(lbldeglat, 0.5f);
		panel.addNext(this.btnDMSLat[4]);
		panel.addNext(this.btnDMSLat[5]);
		panel.addNext(lblminlat, 0.5f);
		panel.addNext(this.btnDMSLat[6]);
		panel.addNext(this.btnDMSLat[7]);
		panel.addNext(lblpntlat, 0.5f);
		panel.addNext(this.btnDMSLat[8]);
		panel.addLast(this.btnDMSLat[9]);
		// panel.addLast(lblseclat);// leave it because of small screen size

		lbldeglat.setText("°", font, Fonts.getFontColor());
		lblminlat.setText("'", font, Fonts.getFontColor());
		lblpntlat.setText(".", font, Fonts.getFontColor());
		lblseclat.setText("\"", font, Fonts.getFontColor());

		// Lon
		Label lbldeglon = new Label("lbldeglon");
		Label lblminlon = new Label("lblminlon");
		Label lblpntlon = new Label("lblpntlon");
		Label lblseclon = new Label("lblseclon");
		for (int i = 0; i < 4; i++)
		{
			panel.addNext(this.btnDMSLon[i]);
		}
		panel.addNext(lbldeglon, 0.5f);
		panel.addNext(this.btnDMSLon[4]);
		panel.addNext(this.btnDMSLon[5]);
		panel.addNext(lblminlon, 0.5f);
		panel.addNext(this.btnDMSLon[6]);
		panel.addNext(this.btnDMSLon[7]);
		panel.addNext(lblpntlon, 0.5f);
		panel.addNext(this.btnDMSLon[8]);
		panel.addLast(this.btnDMSLon[9]);
		// panel.addLast(lblseclon); // leave it because of small screen size

		lbldeglon.setText("°", font, Fonts.getFontColor());
		lblminlon.setText("'", font, Fonts.getFontColor());
		lblpntlon.setText(".", font, Fonts.getFontColor());
		lblseclon.setText("\"", font, Fonts.getFontColor());
		this.setClickHandlers(this.btnDMSLat, this.btnDMSLon);
	}

	private void setClickHandlers(Button[] bLat, Button[] bLon)
	{
		// N/S
		bLat[0].setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				Button btn = (Button) v;
				if (btn.getText().equals("N")) btn.setText("S");
				else
					btn.setText("N");
				return true;
			}
		});
		// E/W
		bLon[0].setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				Button btn = (Button) v;
				if (btn.getText().equals("E")) btn.setText("W");
				else
					btn.setText("E");
				return true;
			}
		});

		for (int i = 1; i < bLat.length; i++) // must have same length for Lat and Lon
		{
			bLat[i].setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					Button btn = (Button) v;
					// Focus setzen;
					EditCoord parent = (EditCoord) btn.getParent();
					// Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
					int l = btn.getName().length() - 1;
					int f = Integer.parseInt(btn.getName().substring(l));
					switch (parent.aktPage)
					{
					case 0:
						parent.focus = parent.setFocus(parent.btnDLat, parent.btnDLon, f);
						break;
					case 1:
						parent.focus = parent.setFocus(parent.btnDMLat, parent.btnDMLon, f);
						break;
					case 2:
						parent.focus = parent.setFocus(parent.btnDMSLat, parent.btnDMSLon, f);
						break;
					case 3:
						break;
					}
					return true;
				}
			});
			bLon[i].setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					Button btn = (Button) v;
					// Focus setzen;
					EditCoord parent = (EditCoord) btn.getParent();
					// Hilfskonstruktion: letztes Zeichen des Namens = Index des Buttonarrays
					int l = btn.getName().length() - 1;
					int f = Integer.parseInt(btn.getName().substring(l));
					switch (parent.aktPage)
					{
					case 0:
						parent.focus = parent.setFocus(parent.btnDLat, parent.btnDLon, f + 9);
						break;
					case 1:
						parent.focus = parent.setFocus(parent.btnDMLat, parent.btnDMLon, f + 9);
						break;
					case 2:
						parent.focus = parent.setFocus(parent.btnDMSLat, parent.btnDMSLon, f + 10);
						break;
					case 3:
						break;
					}
					return true;
				}
			});
		}
	}

	private void createNumPad(Box panel)
	{
		// NumPad for edit of the Lat- / Lon- Buttons
		Button[] btnNumpad;
		btnNumpad = new Button[10];
		Button dummy1 = new Button("dummy1");
		dummy1.setInvisible();
		Button dummy2 = new Button("dummy2");
		dummy2.setInvisible();
		for (int i = 0; i < 10; i++)
		{
			btnNumpad[i] = new Button(this, "btnNumpad" + i);
			btnNumpad[i].setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					Button btn = (Button) v;
					EditCoord parent = (EditCoord) btn.getParent();
					switch (parent.aktPage)
					{
					case 0:
						if (parent.focus < 9)
						{
							parent.btnDLat[parent.focus].setText(btn.getText(), font, Fonts.getHighLightFontColor());
						}
						else
						{
							parent.btnDLon[parent.focus - 9].setText(btn.getText(), font, Fonts.getHighLightFontColor());
						}
						parent.setNextFocus(parent.btnDLat, parent.btnDLon);
						break;
					case 1:
						if (parent.focus < 9)
						{
							parent.btnDMLat[parent.focus].setText(btn.getText(), font, Fonts.getHighLightFontColor());
						}
						else
						{
							parent.btnDMLon[parent.focus - 9].setText(btn.getText(), font, Fonts.getHighLightFontColor());
						}
						parent.setNextFocus(parent.btnDMLat, parent.btnDMLon);
						break;
					case 2:
						if (parent.focus < 10)
						{
							parent.btnDMSLat[parent.focus].setText(btn.getText(), font, Fonts.getHighLightFontColor());
						}
						else
						{
							parent.btnDMSLon[parent.focus - 10].setText(btn.getText(), font, Fonts.getHighLightFontColor());
						}
						parent.setNextFocus(parent.btnDMSLat, parent.btnDMSLon);
						break;
					case 3:
						break;
					}
					return true;
				}
			});

		}
		panel.initRow(false);
		panel.addNext(dummy1); // dummy links
		panel.addNext(btnNumpad[0]);
		panel.addLast(dummy2); // dummy rechts
		panel.addNext(btnNumpad[7]);
		panel.addNext(btnNumpad[8]);
		panel.addLast(btnNumpad[9]);
		panel.addNext(btnNumpad[4]);
		panel.addNext(btnNumpad[5]);
		panel.addLast(btnNumpad[6]);
		panel.addNext(btnNumpad[1]);
		panel.addNext(btnNumpad[2]);
		panel.addLast(btnNumpad[3]);
		for (int i = 0; i < 10; i++)
		{
			btnNumpad[i].setText(String.format("%1d", i), font, Fonts.getFontColor());
		}
		panel.adjustHeight();
	}

	private ArrayList<EditWrapedTextField> allTextFields = new ArrayList<EditWrapedTextField>();

	private void setKeyboardHandling(final EditWrapedTextField textField)
	{
		textField.setOnscreenKeyboard(new OnscreenKeyboard()
		{
			@Override
			public void show(boolean arg0)
			{

				for (EditWrapedTextField tmp : allTextFields)
				{
					tmp.resetFocus();
				}

				textField.setFocus(true);
				focusedTextField = textField;

				if (tbUZone != null && textField.equals(tbUZone))
				{
					Gdx.input.setOnscreenKeyboardVisible(true);
				}
				else
				{
					Gdx.input.setOnscreenKeyboardVisible(false);
				}

			}
		});

		allTextFields.add(textField);
	}

	private void showPage(int newPage)
	{
		String s;

		if (this.aktPage == newPage) return;

		if (aktPage >= 0)
		{
			parseView(); // setting coord
		}

		this.pnlD.setInvisible();
		this.pnlDM.setInvisible();
		this.pnlDMS.setInvisible();
		this.pnlNumPad.setVisible();
		this.numPad.setInvisible();

		switch (newPage)
		{
		case 0:
			// show Degrees
			lUtmO.setInvisible();
			lUtmN.setInvisible();
			lUtmZ.setInvisible();

			bDLat.setInvisible();
			bDLon.setInvisible();

			trUtm.setInvisible();

			bDec.setState(1);
			bMin.setState(0);
			bSec.setState(0);
			bUtm.setState(0);

			// Lat
			if (coord.getLatitude() >= 0) s = "N";
			else
				s = "S";
			s = s + String.format("%09.5f", Math.abs(coord.getLatitude())).replace(",", ".").replace(".", "");
			for (int i = 0; i < 9; i++)
			{
				this.btnDLat[i].setText(s.substring(i, (i + 1)), font, Fonts.getFontColor());
			}
			this.btnDLat[1].setInvisible(); // nur 2 Stellen Grad
			// Lon
			if (coord.getLongitude() >= 0) s = "E";
			else
				s = "W";
			s = s + String.format("%09.5f", Math.abs(coord.getLongitude())).replace(",", ".").replace(".", "");
			for (int i = 0; i < 9; i++)
			{
				this.btnDLon[i].setText(s.substring(i, (i + 1)), font, Fonts.getFontColor());
			}

			this.focus = this.setFocus(this.btnDLat, this.btnDLon, 4); // erste Nachkommastelle N / S
			this.focusStartLon = 13;

			this.pnlD.setVisible();

			break;
		case 1:
			lUtmO.setInvisible();
			lUtmN.setInvisible();
			lUtmZ.setInvisible();

			bDLat.setInvisible();
			bDLon.setInvisible();

			trUtm.setInvisible();

			bDec.setState(0);
			bMin.setState(1);
			bSec.setState(0);
			bUtm.setState(0);

			// Lat
			if (coord.getLatitude() >= 0) s = "N";
			else
				s = "S";
			double deg = (int) Math.abs(coord.getLatitude());
			double frac = Math.abs(coord.getLatitude()) - deg;
			double min = frac * 60;

			s = s + String.format("%03d", (int) deg);
			s = s + String.format("%02d", (int) min);
			s = s + String.format("%03d", (int) (0.5 + (min - (int) min) * 1000)); // gerundet
			for (int i = 0; i < 9; i++)
			{
				this.btnDMLat[i].setText(s.substring(i, (i + 1)), font, Fonts.getFontColor());
			}
			this.btnDMLat[1].setInvisible(); // nur 2 Stellen Grad
			// Lon
			if (coord.getLongitude() >= 0) s = "E";
			else
				s = "W";
			deg = (int) Math.abs(coord.getLongitude());
			frac = Math.abs(coord.getLongitude()) - deg;
			min = frac * 60;
			s = s + String.format("%03d", (int) deg);
			s = s + String.format("%02d", (int) min);
			s = s + String.format("%03d", (int) (0.5 + (min - (int) min) * 1000)); // gerundet
			for (int i = 0; i < 9; i++)
			{
				this.btnDMLon[i].setText(s.substring(i, (i + 1)), font, Fonts.getFontColor());
			}

			this.focus = this.setFocus(this.btnDMLat, this.btnDMLon, 6); // erste Nachkommastelle N / S
			this.focusStartLon = 15;

			this.pnlDM.setVisible();

			break;
		case 2:
			// show Degree - Minute - Second
			lUtmO.setInvisible();
			lUtmN.setInvisible();
			lUtmZ.setInvisible();

			bDLat.setInvisible();
			bDLon.setInvisible();

			trUtm.setInvisible();

			bDec.setState(0);
			bMin.setState(0);
			bSec.setState(1);
			bUtm.setState(0);

			// Lat
			if (coord.getLatitude() >= 0) s = "N";
			else
				s = "S";

			deg = (int) Math.abs(coord.getLatitude());
			frac = Math.abs(coord.getLatitude()) - deg;
			min = frac * 60;
			int imin = (int) min;
			frac = min - imin;
			double sec = frac * 60;

			s = s + String.format("%03d", (int) deg);
			s = s + String.format("%02d", imin);
			s = s + String.format("%02d", (int) sec);
			s = s + String.format("%02d", (int) (0.5 + (sec - (int) sec) * 100)); // gerundet
			for (int i = 0; i < 10; i++)
			{
				this.btnDMSLat[i].setText(s.substring(i, (i + 1)), font, Fonts.getFontColor());
			}
			this.btnDMSLat[1].setInvisible(); // nur 2 Stellen Grad

			// Lon
			if (coord.getLongitude() >= 0) s = "E";
			else
				s = "W";
			deg = (int) Math.abs(coord.getLongitude());
			frac = Math.abs(coord.getLongitude()) - deg;
			min = frac * 60;
			imin = (int) min;
			frac = min - imin;
			sec = frac * 60;
			s = s + String.format("%03d", (int) deg);
			s = s + String.format("%02d", imin);
			s = s + String.format("%02d", (int) sec);
			s = s + String.format("%02d", (int) (0.5 + (sec - (int) sec) * 100)); // gerundet
			for (int i = 0; i < 10; i++)
			{
				this.btnDMSLon[i].setText(s.substring(i, (i + 1)), font, Fonts.getFontColor());
			}

			this.focus = this.setFocus(this.btnDMSLat, this.btnDMSLon, 6); // erste Nachkommastelle N / S
			this.focusStartLon = 16;
			this.pnlDMS.setVisible();

			break;
		case 3:
			// show UTM
			lUtmO.setVisible();
			lUtmN.setVisible();
			lUtmZ.setVisible();

			bDLat.setInvisible();
			bDLon.setInvisible();

			trUtm.setVisible();

			bDec.setState(0);
			bMin.setState(0);
			bSec.setState(0);
			bUtm.setState(1);

			double nording = 0;
			double easting = 0;
			String zone = "";
			convert.iLatLon2UTM(coord.getLatitude(), coord.getLongitude());
			nording = convert.UTMNorthing;
			easting = convert.UTMEasting;
			zone = convert.sUtmZone;
			// tbUY.setText(String.Format(NumberFormatInfo.InvariantInfo, "{0:0}", Math.Floor(nording)));
			// tbUX.setText(String.Format(NumberFormatInfo.InvariantInfo, "{0:0}", Math.Floor(easting)));
			tbUY.setText(String.format("%.1f", nording).replace(",", "."));
			tbUX.setText(String.format("%.1f", easting).replace(",", "."));
			tbUZone.setText(zone);

			tbUY.setFocus();

			this.pnlNumPad.setInvisible();
			this.numPad.setVisible();

			break;
		}
		aktPage = newPage;
	}

	private int setFocus(Button[] bLat, Button[] bLon, int newFocus)
	{
		int nrOfButtons = bLat.length;
		// highlighted to normal
		if (this.focus < nrOfButtons)
		{
			bLat[this.focus].setText(bLat[this.focus].getText(), font, Fonts.getFontColor());
		}
		else
		{
			bLon[this.focus - nrOfButtons].setText(bLon[this.focus - nrOfButtons].getText(), font, Fonts.getFontColor());
		}
		// normal to highlighted showing next input change
		if (newFocus < nrOfButtons)
		{
			bLat[newFocus].setText(bLat[newFocus].getText(), font, Fonts.getHighLightFontColor());
		}
		else
		{
			bLon[newFocus - nrOfButtons].setText(bLon[newFocus - nrOfButtons].getText(), font, Fonts.getHighLightFontColor());
		}
		return newFocus;
	}

	private void setNextFocus(Button[] bLat, Button[] bLon)
	{
		int nextFocus = this.focus + 1;
		if (nextFocus == bLat.length) nextFocus = this.focusStartLon; // jump
		// TODO action if behind last : nothing (at the moment) or autosave or focusStartLat (discuss)
		this.focus = setFocus(bLat, bLon, nextFocus);
	}

	private boolean parseView()
	{
		String scoord = "";
		switch (aktPage)
		{
		case 0:
			scoord += this.btnDLat[0].getText() + " "; // N/S
			scoord += this.btnDLat[2].getText() + this.btnDLat[3].getText() + "."; // Deg 1
			for (int i = 4; i < 9; i++)
				scoord += this.btnDLat[i].getText(); // Deg 2
			scoord += "\u00B0";
			scoord += this.btnDLon[0].getText() + " "; // W/E
			scoord += this.btnDLon[1].getText() + this.btnDLon[2].getText() + this.btnDLon[3].getText() + "."; // Deg 1
			for (int i = 4; i < 9; i++)
				scoord += this.btnDLon[i].getText(); // Deg 2
			scoord += "\u00B0";
			break;
		case 1:
			scoord += this.btnDMLat[0].getText() + " "; // N/S
			scoord += this.btnDMLat[2].getText() + this.btnDMLat[3].getText() + "\u00B0 "; // Deg
			scoord += this.btnDMLat[4].getText() + this.btnDMLat[5].getText() + "."; // Min 1
			scoord += this.btnDMLat[6].getText() + this.btnDMLat[7].getText() + this.btnDMLat[8].getText() + "\u0027 "; // Min 2
			scoord += this.btnDMLon[0].getText() + " "; // W/E
			scoord += this.btnDMLon[1].getText() + this.btnDMLon[2].getText() + this.btnDMLon[3].getText() + "\u00B0 "; // Deg
			scoord += this.btnDMLon[4].getText() + this.btnDMLon[5].getText() + "."; // Min 1
			scoord += this.btnDMLon[6].getText() + this.btnDMLon[7].getText() + this.btnDMLon[8].getText() + "\u0027"; // Min 2
			break;
		case 2:
			scoord += this.btnDMSLat[0].getText() + " "; // N/S
			scoord += this.btnDMSLat[2].getText() + this.btnDMSLat[3].getText() + "\u00B0 "; // Deg
			scoord += this.btnDMSLat[4].getText() + this.btnDMSLat[5].getText() + "\u0027 "; // Min
			scoord += this.btnDMSLat[6].getText() + this.btnDMSLat[7].getText() + "."; // Sec 1
			scoord += this.btnDMSLat[8].getText() + this.btnDMSLat[9].getText() + "\\u0022 "; // Sec 2
			scoord += this.btnDMSLon[0].getText() + " "; // W/E
			scoord += this.btnDMSLon[1].getText() + this.btnDMSLon[2].getText() + this.btnDMSLon[3].getText() + "\u00B0 "; // Deg
			scoord += this.btnDMSLon[4].getText() + this.btnDMSLon[5].getText() + "\u0027 "; // Min
			scoord += this.btnDMSLon[6].getText() + this.btnDMSLon[7].getText() + "."; // Sec 1
			scoord += this.btnDMSLon[8].getText() + this.btnDMSLon[9].getText() + "\\u0022"; // Sec 2
			break;
		case 3:
			// show UTM
			scoord += tbUZone.getText() + " " + tbUX.getText() + " " + tbUY.getText();
			break;
		}

		Coordinate newCoord = new Coordinate(scoord);
		if (newCoord.Valid)
		{
			coord = newCoord;
			return true;
		}
		else
			return false;
	}

	private void showNumPad(NumPad.Type type)
	{
		if (numPad != null) return;
		float numWidth = this.width - this.getLeftWidth() - this.getRightWidth();
		float numHeight = this.height - this.getBottomHeight() - this.getTopHeight() - (UiSizes.getButtonHeight() * 2) - (margin * 2);

		numHeight -= trUtm.getHeight();

		CB_RectF numRec = new CB_RectF(this.getLeftWidth(), UiSizes.getButtonHeight() + (margin * 3), numWidth, numHeight);

		numPad = new NumPad(numRec, "numPad", type, keyListner);

		this.addChildAtLast(numPad);
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

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);

		// wenn utm Zone TextField kein Focus hat, SoftKeyBoard ausblenden

		if (GL.that.getKeyboardFocus() != tbUZone)
		{
			Gdx.input.setOnscreenKeyboardVisible(false);
		}

	}

}
