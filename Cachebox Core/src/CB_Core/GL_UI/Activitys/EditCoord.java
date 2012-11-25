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
import CB_Core.Math.SizeF;
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
	private Box trDec;
	private Box trMin;
	private Box trSec;
	private Box trUtm;

	// Allgemein
	private MultiToggleButton bDec;
	private MultiToggleButton bMin;
	private MultiToggleButton bSec;
	private MultiToggleButton bUtm;

	// Deg
	private Button bDLat;
	private EditWrapedTextField tbDLat;
	private Button bDLon;
	private EditWrapedTextField tbDLon;

	// Deg - Min
	private EditWrapedTextField tbMLatDeg;
	private EditWrapedTextField tbMLatMin;

	// Button bMLon;
	private EditWrapedTextField tbMLonDeg;
	private EditWrapedTextField tbMLonMin;
	// Deg - Min - Sec

	private EditWrapedTextField tbSLatDeg;
	private EditWrapedTextField tbSLatMin;
	private EditWrapedTextField tbSLatSec;

	private EditWrapedTextField tbSLonDeg;
	private EditWrapedTextField tbSLonMin;
	private EditWrapedTextField tbSLonSec;
	// Utm
	private EditWrapedTextField tbUX;
	private EditWrapedTextField tbUY;
	private EditWrapedTextField tbUZone;
	private Label lUtmO;
	private Label lUtmN;
	private Label lUtmZ;

	// Deg - Min new
	private Box trMinNew;
	private Button[] btnLat;
	private Button[] btnLon;
	private BitmapFont font = Fonts.getCompass();
	private Button[] btnNumpad;
	private int focus;

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

		float innerWidth = this.width - this.getLeftWidth() - this.getLeftWidth();

		CB_RectF MTBRec = new CB_RectF(this.getLeftWidth(), this.height - this.getLeftWidth() - UiSizes.getButtonHeight(), innerWidth / 4,
				UiSizes.getButtonHeight());

		bDec = new MultiToggleButton(MTBRec, "bDec");
		bMin = new MultiToggleButton(MTBRec, "bMin");
		bSec = new MultiToggleButton(MTBRec, "bSec");
		bUtm = new MultiToggleButton(MTBRec, "bUtm");

		bDec.setX(this.getLeftWidth());
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

		Button bOK = new Button(this.getLeftWidth(), this.getLeftWidth(), innerWidth / 2, UiSizes.getButtonHeight(), "OK Button");
		Button bCancel = new Button(bOK.getMaxX(), this.getLeftWidth(), innerWidth / 2, UiSizes.getButtonHeight(), "Cancel Button");
		bDLat = new Button(this.getLeftWidth(), bDec.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(),
				UiSizes.getButtonHeight(), "BDLat");
		bDLon = new Button(this.getLeftWidth(), bDLat.getY() - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(),
				UiSizes.getButtonHeight(), "bDLon");
		CB_RectF EditTextBoxRec = new CB_RectF(bDLon.getMaxX() + margin, bDLon.getY(), this.width - bDLon.getMaxX() - margin,
				bDLat.getMaxY() - bDLon.getY());

		trDec = new Box(EditTextBoxRec, "trDec");
		trMin = new Box(EditTextBoxRec, "trMin");
		trSec = new Box(EditTextBoxRec, "trSec");
		trMinNew = new Box(new CB_RectF(this.getLeftWidth(), //
				this.getBottomHeight() + bOK.getHeight(), //
				this.width - this.getLeftWidth() - this.getRightWidth(), //
				this.height - this.getBottomHeight() - this.getTopHeight() - this.bMin.getHeight() - bOK.getHeight()), //
				"trMinNew");

		EditTextBoxRec.setHeight((bDLat.getMaxY() - bDLon.getY()) * 1.5f);
		EditTextBoxRec.setY(bDLon.getY() - bDLon.getHeight());
		EditTextBoxRec.setX(EditTextBoxRec.getX() + (margin * 3f));
		trUtm = new Box(EditTextBoxRec, "trUtm");

		this.addChild(trDec);
		this.addChild(trMin);
		this.addChild(trSec);
		this.addChild(trUtm);
		this.addChild(trMinNew);

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

		this.addChild(bCancel);
		bCancel.setOnClickListener(new OnClickListener()
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
		/*
		 * bDec.setOnStateChangedListner(new OnStateChangeListener() {
		 * 
		 * @Override public void onStateChange(GL_View_Base v, int State) { if (State == 1) showPage(0); } });
		 * 
		 * bMin.setState(1); bMin.setOnStateChangedListner(new OnStateChangeListener() {
		 * 
		 * @Override public void onStateChange(GL_View_Base v, int State) { if (State == 1) showPage(1); } });
		 * 
		 * bSec.setOnStateChangedListner(new OnStateChangeListener() {
		 * 
		 * @Override public void onStateChange(GL_View_Base v, int State) { if (State == 1) showPage(2); } });
		 * bUtm.setOnStateChangedListner(new OnStateChangeListener() {
		 * 
		 * @Override public void onStateChange(GL_View_Base v, int State) { if (State == 1) showPage(3); } });
		 */
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

		createTrMin();
		createTrDec();
		createTrSec();
		createTrUtn();
		createTrMinNew();
		showNumPad(NumPad.Type.withDot);
		showPage(1);

	}

	private void createTrUtn()
	{
		CB_RectF editRec = new CB_RectF(0, 0, (trMin.getWidth() - (margin * 3)), UiSizes.getButtonHeight());
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

	private void createTrSec()
	{
		CB_RectF editRec = new CB_RectF(0, 0, (trMin.getWidth() - (margin * 3)) / 3, UiSizes.getButtonHeight());
		editRec.setWidth(editRec.getWidth() - (margin * 2));

		CB_RectF labelRec = new CB_RectF(new SizeF(margin, UiSizes.getButtonHeight()));

		tbSLonDeg = new EditWrapedTextField(this, editRec, "tbSLonDeg");
		setKeyboardHandling(tbSLonDeg);

		labelRec.setX(tbSLonDeg.getMaxX());
		Label l1 = new Label(labelRec, "l1");
		l1.setText("°");
		trSec.addChild(l1);

		editRec.setX(l1.getMaxX() + margin);
		tbSLonMin = new EditWrapedTextField(this, editRec, "tbSLonMin");
		setKeyboardHandling(tbSLonMin);

		labelRec.setX(tbSLonMin.getMaxX());
		Label l3 = new Label(labelRec, "l3");
		l3.setText("'");
		trSec.addChild(l3);

		editRec.setX(l3.getMaxX() + margin);
		tbSLonSec = new EditWrapedTextField(this, editRec, "tbSLonSec");
		setKeyboardHandling(tbSLonSec);

		labelRec.setX(tbSLonSec.getMaxX());
		Label l5 = new Label(labelRec, "l5");
		l5.setText("\"");
		trSec.addChild(l5);

		editRec.setX(tbSLonDeg.getX());
		editRec.setY(tbSLonDeg.getMaxY());

		tbSLatDeg = new EditWrapedTextField(this, editRec, "tbSLatDeg");
		setKeyboardHandling(tbSLatDeg);

		labelRec.setX(tbSLonDeg.getMaxX());
		labelRec.setY(tbSLonDeg.getMaxY());
		Label l2 = new Label(labelRec, "l2");
		l2.setText("°");
		trSec.addChild(l2);

		editRec.setX(l2.getMaxX() + margin);
		tbSLatMin = new EditWrapedTextField(this, editRec, "tbSLatMin");
		setKeyboardHandling(tbSLatMin);

		labelRec.setX(tbSLatMin.getMaxX());
		Label l4 = new Label(labelRec, "l4");
		l4.setText("'");
		trSec.addChild(l4);

		editRec.setX(l4.getMaxX() + margin);
		tbSLatSec = new EditWrapedTextField(this, editRec, "tbSLatSec");
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

		tbDLon = new EditWrapedTextField(this, editRec, "tbDLon");
		setKeyboardHandling(tbDLon);

		labelRec.setX(tbDLon.getMaxX());
		Label l1 = new Label(labelRec, "l1");
		l1.setText("°");
		trDec.addChild(l1);

		editRec.setY(tbDLon.getMaxY());

		tbDLat = new EditWrapedTextField(this, editRec, "tbDLat");
		setKeyboardHandling(tbDLat);

		labelRec.setX(tbDLon.getMaxX());
		labelRec.setY(tbDLon.getMaxY());
		Label l2 = new Label(labelRec, "l2");
		l2.setText("°");
		trDec.addChild(l2);

		trDec.addChild(tbDLat);
		trDec.addChild(tbDLon);
	}

	private void createTrMin()
	{
		CB_RectF editRec = new CB_RectF(0, 0, (trMin.getWidth() - (margin * 3)) / 2, UiSizes.getButtonHeight());
		editRec.setWidth(editRec.getWidth() - (margin * 2));

		CB_RectF labelRec = new CB_RectF(new SizeF(margin, UiSizes.getButtonHeight()));

		tbMLonDeg = new EditWrapedTextField(this, editRec, "tbMLonDeg");
		setKeyboardHandling(tbMLonDeg);

		labelRec.setX(tbMLonDeg.getMaxX());
		Label l1 = new Label(labelRec, "l1");
		l1.setText("°");
		trMin.addChild(l1);

		editRec.setX(l1.getMaxX() + margin);
		tbMLonMin = new EditWrapedTextField(this, editRec, "tbMLonMin");
		setKeyboardHandling(tbMLonMin);

		labelRec.setX(tbMLonMin.getMaxX());
		Label l3 = new Label(labelRec, "l3");
		l3.setText("'");
		trMin.addChild(l3);

		editRec.setX(tbMLonDeg.getX());
		editRec.setY(tbMLonDeg.getMaxY());
		tbMLatDeg = new EditWrapedTextField(this, editRec, "tbMLatDeg");
		setKeyboardHandling(tbMLatDeg);

		labelRec.setX(tbMLatDeg.getMaxX());
		labelRec.setY(tbMLonDeg.getMaxY());
		Label l2 = new Label(labelRec, "l2");
		l2.setText("°");
		trMin.addChild(l2);

		editRec.setX(l2.getMaxX() + margin);
		tbMLatMin = new EditWrapedTextField(this, editRec, "tbMLatMin");
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

	private void createTrMinNew()
	{

		this.btnLat = new Button[9]; // N_48[°]29[.]369
		this.btnLon = new Button[9]; // E__9[°]15[.]807
		for (int i = 0; i < 9; i++)
		{
			this.btnLat[i] = new Button(this, "btnLat" + i);
			this.btnLon[i] = new Button(this, "btnLon" + i);
		}

		Button btn1 = new Button("btn1"); // oder Label for Degree Lat
		Button btn2 = new Button("btn2"); // oder Label for point Lat
		Button btn3 = new Button("btn3"); // oder Label for Degree Lon
		Button btn4 = new Button("btn4"); // oder Label for point Lon

		// Lat
		for (int i = 0; i < 4; i++)
		{
			this.trMinNew.addNext(this.btnLat[i]);
		}
		this.trMinNew.addNext(btn1);
		this.trMinNew.addNext(this.btnLat[4]);
		this.trMinNew.addNext(this.btnLat[5]);
		this.trMinNew.addNext(btn2);
		this.trMinNew.addNext(this.btnLat[6]);
		this.trMinNew.addNext(this.btnLat[7]);
		this.trMinNew.addLast(this.btnLat[8]);
		// Lon
		for (int i = 0; i < 4; i++)
		{
			this.trMinNew.addNext(this.btnLon[i]);
		}
		this.trMinNew.addNext(btn3);
		this.trMinNew.addNext(this.btnLon[4]);
		this.trMinNew.addNext(this.btnLon[5]);
		this.trMinNew.addNext(btn4);
		this.trMinNew.addNext(this.btnLon[6]);
		this.trMinNew.addNext(this.btnLon[7]);
		this.trMinNew.addLast(this.btnLon[8]);

		btn1.setText("°", font, Fonts.getFontColor());
		btn1.disable();
		btn2.setText(".", font, Fonts.getFontColor());
		btn2.disable();
		btn3.setText("°", font, Fonts.getFontColor());
		btn3.disable();
		btn4.setText(".", font, Fonts.getFontColor());
		btn4.disable();

		this.btnLat[0].setOnClickListener(new OnClickListener()
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

		this.btnLon[0].setOnClickListener(new OnClickListener()
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

		for (int i = 1; i < 9; i++)
		{
			this.btnLat[i].setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					Button btn = (Button) v;
					// Focus setzen;
					EditCoord parent = (EditCoord) btn.getParent();
					int l = btn.getName().length() - 1;
					int f = Integer.parseInt(btn.getName().substring(l));
					parent.setFocus(f);
					return true;
				}
			});
			this.btnLon[i].setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					Button btn = (Button) v;
					// Focus setzen;
					EditCoord parent = (EditCoord) btn.getParent();
					int l = btn.getName().length() - 1;
					int f = Integer.parseInt(btn.getName().substring(l));
					parent.setFocus(f + 9);
					return true;
				}
			});
		}

		// NumPad for the Buttons
		this.btnNumpad = new Button[10];
		Button dummy1 = new Button("dummy1");
		dummy1.setInvisible();
		Button dummy2 = new Button("dummy2");
		dummy2.setInvisible();
		for (int i = 0; i < 10; i++)
		{
			this.btnNumpad[i] = new Button(this, "btnNumpad" + i);
			btnNumpad[i].setText(String.format("%1d", i), font, Fonts.getFontColor());
			this.btnNumpad[i].setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					Button btn = (Button) v;
					EditCoord parent = (EditCoord) btn.getParent();
					if (parent.focus < 9)
					{
						parent.btnLat[parent.focus].setText(btn.getText(), font, Fonts.getHighLightFontColor());
					}
					else
					{
						parent.btnLon[parent.focus - 9].setText(btn.getText(), font, Fonts.getHighLightFontColor());
					}
					parent.setNextFocus();
					return true;
				}
			});

		}
		this.trMinNew.initRow(false);
		this.trMinNew.addNext(dummy1); // dummy links
		this.trMinNew.addNext(btnNumpad[0]);
		this.trMinNew.addLast(dummy2); // dummy rechts
		this.trMinNew.addNext(btnNumpad[7]);
		this.trMinNew.addNext(btnNumpad[8]);
		this.trMinNew.addLast(btnNumpad[9]);
		this.trMinNew.addNext(btnNumpad[4]);
		this.trMinNew.addNext(btnNumpad[5]);
		this.trMinNew.addLast(btnNumpad[6]);
		this.trMinNew.addNext(btnNumpad[1]);
		this.trMinNew.addNext(btnNumpad[2]);
		this.trMinNew.addLast(btnNumpad[3]);
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

		if (aktPage >= 0)
		{
			parseView(); // setting coord
		}

		if (!coord.Valid)
		{
			// oder aktuelle Position oder Cache Koordinaten
			coord.setLatitude(0d);
			coord.setLongitude(0d);
		}

		if (coord.getLatitude() >= 0) bDLat.setText("N");
		else
			bDLat.setText("S");
		if (coord.getLongitude() >= 0) bDLon.setText("E");
		else
			bDLon.setText("W");

		this.numPad.setVisible();
		this.trMinNew.setInvisible();

		switch (newPage)
		{
		case 0:
			// show Degrees
			lUtmO.setInvisible();
			lUtmN.setInvisible();
			lUtmZ.setInvisible();
			bDLat.setVisible();
			bDLon.setVisible();
			trDec.setVisible();
			trMin.setInvisible();
			trSec.setInvisible();
			trUtm.setInvisible();
			bDec.setState(1);
			bMin.setState(0);
			bSec.setState(0);
			bUtm.setState(0);

			tbDLat.setText(String.format("%.5f", Math.abs(coord.getLatitude())).replace(",", "."));
			tbDLat.setFocus();

			tbDLon.setText(String.format("%.5f", Math.abs(coord.getLongitude())).replace(",", "."));

			break;
		case 1:
			lUtmO.setInvisible();
			lUtmN.setInvisible();
			lUtmZ.setInvisible();
			bDLat.setInvisible();
			bDLon.setInvisible();

			trDec.setInvisible();
			trMin.setInvisible();
			trSec.setInvisible();
			trUtm.setInvisible();
			bDec.setState(0);
			bMin.setState(1);
			bSec.setState(0);
			bUtm.setState(0);

			String s;
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
				this.btnLat[i].setText(s.substring(i, (i + 1)), font, Fonts.getFontColor());
			}
			this.btnLat[1].setInvisible(); // nur 2 Stellen Grad
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
				this.btnLon[i].setText(s.substring(i, (i + 1)), font, Fonts.getFontColor());
			}

			this.setFocus(6); // erste Nachkommastelle N / S
			this.numPad.setInvisible();
			this.trMinNew.setVisible();

			break;
		case 4:
			// show Degree - Minute
			lUtmO.setInvisible();
			lUtmN.setInvisible();
			lUtmZ.setInvisible();
			bDLat.setVisible();
			bDLon.setVisible();
			trDec.setInvisible();
			trMin.setVisible();
			trSec.setInvisible();
			trUtm.setInvisible();
			bDec.setState(0);
			bMin.setState(1);
			bSec.setState(0);
			bUtm.setState(0);

			deg = (int) Math.abs(coord.getLatitude());
			frac = Math.abs(coord.getLatitude()) - deg;
			min = frac * 60;

			tbMLatDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbMLatMin.setText(String.format("%.3f", min).replace(",", "."));

			deg = (int) Math.abs(coord.getLongitude());
			frac = Math.abs(coord.getLongitude()) - deg;
			min = frac * 60;
			tbMLonDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbMLonMin.setText(String.format("%.3f", min).replace(",", "."));

			tbMLonDeg.setFocus();

			break;
		case 2:
			// show Degree - Minute - Second
			lUtmO.setInvisible();
			lUtmN.setInvisible();
			lUtmZ.setInvisible();
			bDLat.setVisible();
			bDLon.setVisible();
			trMin.setInvisible();
			trDec.setInvisible();
			trSec.setVisible();
			trUtm.setInvisible();
			bDec.setState(0);
			bMin.setState(0);
			bSec.setState(1);
			bUtm.setState(0);

			deg = Math.abs((int) coord.getLatitude());
			frac = Math.abs(coord.getLatitude()) - deg;
			min = frac * 60;
			int imin = (int) min;
			frac = min - imin;
			double sec = frac * 60;

			tbSLatDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbSLatMin.setText(String.valueOf(imin).replace(",", "."));
			tbSLatSec.setText(String.format("%.2f", sec).replace(",", "."));

			deg = Math.abs((int) coord.getLongitude());
			frac = Math.abs(coord.getLongitude()) - deg;
			min = frac * 60;
			imin = (int) min;
			frac = min - imin;
			sec = frac * 60;

			tbSLonDeg.setText(String.format("%.0f", deg).replace(",", "."));
			tbSLonMin.setText(String.valueOf(imin).replace(",", "."));
			tbSLonSec.setText(String.format("%.2f", sec).replace(",", "."));

			tbSLonDeg.setFocus();

			break;
		case 3:
			// show UTM
			lUtmO.setVisible();
			lUtmN.setVisible();
			lUtmZ.setVisible();
			bDLat.setInvisible();
			bDLon.setInvisible();
			trMin.setInvisible();
			trDec.setInvisible();
			trSec.setInvisible();
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

			break;
		}
		aktPage = newPage;
	}

	private void setFocus(int newFocus)
	{
		if (this.focus < 9)
		{
			this.btnLat[this.focus].setText(this.btnLat[this.focus].getText(), font, Fonts.getFontColor());
		}
		else
		{
			this.btnLon[this.focus - 9].setText(this.btnLon[this.focus - 9].getText(), font, Fonts.getFontColor());
		}
		if (newFocus < 9)
		{
			this.btnLat[newFocus].setText(this.btnLat[newFocus].getText(), font, Fonts.getHighLightFontColor());
		}
		else
		{
			this.btnLon[newFocus - 9].setText(this.btnLon[newFocus - 9].getText(), font, Fonts.getHighLightFontColor());
		}
		this.focus = newFocus;
	}

	private void setNextFocus()
	{
		int nextFocus = this.focus + 1;
		if (nextFocus == 9) nextFocus = 15; // erste Nachkommastelle E / W
		setFocus(nextFocus);
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
			scoord += this.btnLat[0].getText() + " ";
			scoord += this.btnLat[2].getText() + this.btnLat[3].getText() + "\u00B0 ";
			scoord += this.btnLat[4].getText() + this.btnLat[5].getText() + ".";
			scoord += this.btnLat[6].getText() + this.btnLat[7].getText() + this.btnLat[8].getText() + "\u0027 ";
			scoord += this.btnLon[0].getText() + " ";
			scoord += this.btnLon[1].getText() + this.btnLon[2].getText() + this.btnLon[3].getText() + "\u00B0 ";
			scoord += this.btnLon[4].getText() + this.btnLon[5].getText() + ".";
			scoord += this.btnLon[6].getText() + this.btnLon[7].getText() + this.btnLon[8].getText() + "\u0027";
			break;
		case 4:
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
