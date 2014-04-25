package CB_UI.GL_UI.Activitys;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Solver.Solver;
import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Functions.Function;
import CB_Core.Solver.Functions.Functions;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.CollapseBox.animatetHeightChangedListner;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.LinearCollapseBox;
import CB_UI_Base.GL_UI.Controls.Linearlayout;
import CB_UI_Base.GL_UI.Controls.Linearlayout.LayoutChanged;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.Color;

public class SelectSolverFunction extends ButtonDialog
{
	private Label desc;
	private IFunctionResult mResultListner;
	private ScrollBox scrollBox;
	private Linearlayout mLinearLayout;
	private CB_RectF categoryBtnRec, itemBtnRec;
	private Function selectedFunction;
	private DataType dataType;

	public interface IFunctionResult
	{
		public void selectedFunction(Function function);
	}

	public SelectSolverFunction(DataType dataType, IFunctionResult resultListner)
	{
		super(ActivityRec(), "SelectSolverFunctionActivity", "", "", MessageBoxButtons.OKCancel, MessageBoxIcon.None, null);
		mResultListner = resultListner;
		this.dataType = dataType;

		// Gr�ssen f�r die CategoryButtons und ItemButtons berechnen!
		categoryBtnRec = new CB_RectF(leftBorder, 0, innerWidth - mCenter9patch.getLeftWidth() - mCenter9patch.getRightWidth(),
				UI_Size_Base.that.getButtonHeight());

		itemBtnRec = new CB_RectF(leftBorder, 0, categoryBtnRec.getWidth() - leftBorder - rightBorder, UI_Size_Base.that.getButtonHeight());

		// Initialisiert die unteren Buttons f�r Ok/Cancel
		iniOkCancel();

		// �ber den Buttons liegt ein Wrapped Label, welches die Beschreibeung der Selectierten Function anzeigt
		iniDescLabel();

		// Initialisieren der Controls f�r die Function List
		iniFunctionList();

		// jetzt sind alle Controls initialisiert und wir k�nnen Die Liste mit den Funktionen F�llen
		fillContent();

	}

	public static CB_RectF ActivityRec()
	{
		float w = Math.min(UI_Size_Base.that.getSmallestWidth(), UI_Size_Base.that.getWindowHeight() * 0.66f);

		return new CB_RectF(0, 0, w, (int) (UI_Size_Base.that.getWindowHeight() * 0.95));
	}

	private void iniOkCancel()
	{

		button1.setText(Translation.Get("ok"));
		button1.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mResultListner != null)
				{
					try
					{
						mResultListner.selectedFunction(selectedFunction);
					}
					catch (NullPointerException e)
					{
						throw new IllegalArgumentException("Der Returnlistner kann hier die R�ckgabe von NULL nicht verarbeiten!");
					}
				}
				GL.that.closeDialog(SelectSolverFunction.this);
				return true;
			}
		});
		button3.setText(Translation.Get("cancel"));
		button3.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mResultListner != null) try
				{
					mResultListner.selectedFunction(null);
				}
				catch (NullPointerException e)
				{
					throw new IllegalArgumentException("Der Returnlistner kann hier die R�ckgabe von NULL nicht verarbeiten!");
				}
				GL.that.closeDialog(SelectSolverFunction.this);
				return true;
			}
		});

		// CB_RectF btnRec = new CB_RectF(Left, Bottom, (width - Left - Right) / 2, UiSizes.getButtonHeight());
		// bOK = new Button(btnRec, "OkButton");
		//
		// btnRec.setX(bOK.getMaxX());
		// bCancel = new Button(btnRec, "CancelButton");
		//
		// bOK.setText(Translation.Get("ok"));
		// bCancel.setText(Translation.Get("cancel"));
		//
		// this.addChild(bOK);
		// this.addChild(bCancel);
		//
		// bOK.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		// {
		// if (mResultListner != null)
		// {
		// try
		// {
		// mResultListner.selectedFunction(selectedFunction);
		// }
		// catch (NullPointerException e)
		// {
		// throw new IllegalArgumentException("Der Returnlistner kann hier die R�ckgabe von NULL nicht verarbeiten!");
		// }
		// }
		// GL.that.closeDialog(SelectSolverFunction.this);
		// return true;
		// }
		// });
		//
		// bCancel.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		// {
		// if (mResultListner != null) try
		// {
		// mResultListner.selectedFunction(null);
		// }
		// catch (NullPointerException e)
		// {
		// throw new IllegalArgumentException("Der Returnlistner kann hier die R�ckgabe von NULL nicht verarbeiten!");
		// }
		// GL.that.closeDialog(SelectSolverFunction.this);
		// return true;
		// }
		// });

	}

	private void iniDescLabel()
	{
		// rechteck f�r Label erstellen
		CB_RectF rec = new CB_RectF(0, this.getBottomHeight(), this.getWidth(), UI_Size_Base.that.getButtonHeight() * 1.5f);

		desc = new Label(rec, "description");

		// das Beschreibungs Label erh�llt auch den BackGround der Activity.
		// Damit haben alle Bereiche der Activity den Selben Rahmen, dies Wirkt aufger�umter
		desc.setBackground(this.getBackground());

		this.addChild(desc);
	}

	private void iniFunctionList()
	{
		// rechteck f�r die List erstellen.
		// diese ergibt sich aus dem Platzangebot oberhalb des desc Labels
		CB_RectF rec = new CB_RectF(0, desc.getMaxY(), desc.getWidth(), this.getHeight() - desc.getMaxY() - mFooterHeight);

		// Die Eintr�ge der Function List werden aber nicht in einer ListView dargestellt, sondern werden in ein LinearLayout von oben nach
		// unten geschrieben.
		//
		// Dieses LinearLayout wird dann in eine ScrollBox verpackt, damit dies Scrollbar ist, wenn die L�nge den Anzeige Bereich
		// �berschreitet!
		scrollBox = new ScrollBox(rec);

		// damit die Scrollbox auch Events erh�llt
		scrollBox.setClickable(true);

		// die ScrollBox erh�lt den Selben Hintergrund wie die Activity und wird damit ein wenig abgegrenzt von den Restlichen Controls
		scrollBox.setBackground(this.getBackground());

		// Initial LinearLayout
		// Dieses wird nur mit der Breite Initialisiert, die H�he ergibt sich aus dem Inhalt
		mLinearLayout = new Linearlayout(categoryBtnRec.getWidth(), "SelectSolverFunction-LinearLayout");

		// damit das LinearLayout auch Events erh�llt
		mLinearLayout.setClickable(true);

		mLinearLayout.setZeroPos();

		// hier setzen wir ein LayoutChanged Listner, um die innere H�he der ScrollBox bei einer ver�nderung der H�he zu setzen!
		mLinearLayout.setLayoutChangedListner(new LayoutChanged()
		{
			@Override
			public void LayoutIsChanged(Linearlayout linearLayout, float newHeight)
			{
				mLinearLayout.setZeroPos();
				scrollBox.setVirtualHeight(newHeight);
			}
		});

		// add LinearLayout zu ScrollBox und diese zu der Activity
		scrollBox.addChild(mLinearLayout);
		this.addChild(scrollBox);

	}

	private void fillContent()
	{

		/**
		 * in dieser liste sind alle Function Buttons enthalten! diese wird ben�tigt, um hier den Zustand der Buttons �ndern zu k�nnen. wenn
		 * ein Button selectiert wurde m�ssen alle anderen deselectiert werden.
		 */
		final ArrayList<Button> functBtnList = new ArrayList<Button>();

		Iterator<Functions> iteratorCat = Solver.functions.values().iterator();

		if (iteratorCat != null && iteratorCat.hasNext())
		{
			do
			{
				Functions cat = iteratorCat.next();

				// erstelle Category Button
				final Button categoryButton = new Button(categoryBtnRec, "Btn-" + cat.getName());
				categoryButton.setText(Translation.Get(cat.getName()));

				// alle Buttons m�ssen das Atribut Dragable habe, da sie sich in einer Dragable View befinden.
				categoryButton.setDrageble();

				// Category Button Gelb einf�rben, damit sie sich von den Function Buttons unterscheiden
				categoryButton.setColorFilter(new Color(1f, 0.8f, 0.0f, 1));

				// erstelle Category Box
				final LinearCollapseBox lay = new LinearCollapseBox(categoryBtnRec, "CollabsBox-" + cat.getName());

				// die CollapseBox mit einem Rahmen versehen
				lay.setBackground(this.getBackground());

				lay.setClickable(true);

				// Z�hler f�r die Anzahl der Funktionen, die zu dieser CollabsBox hinzugef�gt wurden.
				// Dies wird dazu benutzt, um zu entscheiden, ob die Category vielleicht keine Eintr�ge hat und garnicht in der Liste
				// erscheinen soll.
				int EntryCount = 0;

				Iterator<Function> iteratorFunctions = cat.iterator();
				if (iteratorFunctions != null && iteratorFunctions.hasNext())
				{
					do
					{
						// erstelle einzelnen Funktions Button

						final Function fct = iteratorFunctions.next();
						if (!fct.returnsDataType(dataType))
						{
							continue;
						}
						final Button btnFct = new Button(itemBtnRec, "FunctionBtn-" + fct.getName());

						// den Function Button der algemeinen Liste hinzuf�grn
						functBtnList.add(btnFct);

						// setze Button Text
						btnFct.setText(fct.getName());

						// alle Buttons m�ssen das Atribut Dragable habe, da sie sich in einer Dragable View befinden.
						btnFct.setDrageble();

						// Wenn Der Button geclickt wurd, wird dieser als Selecktiert Markiert
						btnFct.setOnClickListener(new OnClickListener()
						{

							@Override
							public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
							{
								// ColorFilter aller Buttons zur�ck setzen
								Iterator<Button> btnIterator = functBtnList.iterator();
								do
								{
									btnIterator.next().clearColorFilter();
								}
								while (btnIterator.hasNext());

								// setze f�r diesen Button den ColorFilter als selected Markierung
								btnFct.setColorFilter(new Color(1f, 0.5f, 0.5f, 1));

								// Schreibe die Funktions Beschreibung in das Desc Label
								desc.setWrappedText(fct.getDescription());

								selectedFunction = fct;

								// hier muss einmal gerendert werden, damit die �nderungen �bernommen werden
								GL.that.renderOnce();

								return false;
							}
						});

						// den Function Button der Collapse Box hinzuf�gen
						lay.addChild(btnFct);
						EntryCount++; // Den Function Z�hler erh�hen;

					}
					while (iteratorFunctions.hasNext());
				}

				// Nur wenn die Anzahl der Eintr�ge gr��er 0 ist, erscheinen Category Buttn und CollbaseBox in dem LinearLayout
				if (EntryCount > 0)
				{

					// CategoryButton und CollabsBox werden beide auf Position 0,0 gesetzt, da,it sie richtig angeordnet werden k�nnen
					categoryButton.setZeroPos();
					lay.setZeroPos();

					// Den Category Button zum LinearLayout hinzuf�gen mit einem normalen Abstand zum dar�berliegendem Control
					mLinearLayout.addChild(categoryButton, margin);

					// die Collapse noch Schliessen, bevor sie zum LinearLayout hinzugef�gt wird.
					// Da wir hier aber keine Animation haben wollen, setzen wir die AnimationsH�he auf null und rufen nicht die Methode
					// Collapse() auf, da diese eine Animation starten w�rde.
					lay.setAnimationHeight(0f);

					// Die mit den Functions Buttons gef�llte CollapseBox zum LinearLayout hinzuf�gen mit keinem Abstand zum
					// dar�berliegendem
					// Category Button!
					mLinearLayout.addChild(lay, margin);

					// Wenn die CollapseBox ihre gr��e ver�ndert, muss dies noch dem LinearLayout mitgeteilt werden und auch der ScrollBox,
					// dass sich die innere H�he ge�ndert hat!
					lay.setAnimationListner(new animatetHeightChangedListner()
					{
						@Override
						public void animatedHeightChanged(float Height)
						{
							mLinearLayout.layout();

							// mLinearLayout.setZeroPos();
							// scrollBox.setVirtualHeight(mLinearLayout.getHeight());
						}
					});

					// Bei einem Click auf dem Category Button wird die darunterliegende CollabsBox ge�fnet oder geschlossen
					categoryButton.setOnClickListener(new OnClickListener()
					{
						@Override
						public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
						{
							lay.Toggle();
							return false;
						}
					});
				}

			}
			while (iteratorCat.hasNext());

		}
	}

}
