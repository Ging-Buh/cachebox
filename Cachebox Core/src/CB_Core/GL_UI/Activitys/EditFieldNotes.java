package CB_Core.GL_UI.Activitys;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Events.KeyboardFocusChangedEvent;
import CB_Core.Events.KeyboardFocusChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.FilterSettings.FilterSetListView;
import CB_Core.GL_UI.Activitys.FilterSettings.FilterSetListView.FilterSetEntry;
import CB_Core.GL_UI.Activitys.FilterSettings.FilterSetListViewItem;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.GL_UI.Controls.EditTextFieldBase.DefaultOnscreenKeyboard;
import CB_Core.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_Core.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.FieldNoteEntry;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class EditFieldNotes extends ActivityBase implements KeyboardFocusChangedEvent
{
	private FieldNoteEntry altfieldNote;
	private FieldNoteEntry fieldNote;
	private Button bOK = null;
	private Button bCancel = null;
	private Label tvCacheName = null;
	private EditWrapedTextField etComment = null;
	private Image ivTyp = null;
	private Label tvFounds = null;
	private EditWrapedTextField tvDate = null;
	private EditWrapedTextField tvTime = null;
	private Label lblDate = null;
	private Label lblTime = null;
	private Box scrollBox = null;
	FilterSetListViewItem GcVote;

	public interface ReturnListner
	{
		public void returnedFieldNote(FieldNoteEntry fn);
	}

	private ReturnListner mReturnListner;

	public EditFieldNotes(FieldNoteEntry note, ReturnListner listner)
	{
		super(ActivityBase.ActivityRec(), "");

		mReturnListner = listner;
		fieldNote = note;
		altfieldNote = note.copy();
		scrollBox = new Box(ActivityBase.ActivityRec(), "");
		this.addChild(scrollBox);
		iniOkCancel();
		iniNameLabel();
		iniImage();
		iniFoundLabel();
		iniDate();
		iniTime();
		iniGC_VoteItem();
		iniCommentTextField();

		iniTextfieldFocus();

		setDefaultValues();

	}

	private void setDefaultValues()
	{
		tvCacheName.setText(fieldNote.CacheName);

		tvFounds.setText("Founds: #" + fieldNote.foundNumber);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
		String sDate = iso8601Format.format(fieldNote.timestamp);
		tvDate.setText(sDate);
		iso8601Format = new SimpleDateFormat("HH:mm");
		String sTime = iso8601Format.format(fieldNote.timestamp);
		tvTime.setText(sTime);

		ivTyp.setDrawable(new SpriteDrawable(SpriteCache.LogIcons.get(fieldNote.typeIcon)));
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
				if (mReturnListner != null)
				{
					fieldNote.comment = etComment.getText();
					if (GcVote != null)
					{
						fieldNote.gc_Vote = (int) (GcVote.getValue() * 100);
					}

					// parse Date and Time
					String date = tvDate.getText();
					String time = tvTime.getText();

					date = date.replace("-", ".");
					time = time.replace(":", ".");

					try
					{
						Date timestamp;
						DateFormat formatter;

						formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
						timestamp = (Date) formatter.parse(date + "." + time + ".00");

						fieldNote.timestamp = timestamp;
					}
					catch (ParseException e)
					{
						final GL_MsgBox msg = GL_MsgBox.Show(GlobalCore.Translations.Get("wrongDate"),
								GlobalCore.Translations.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error,
								new OnMsgBoxClickListener()
								{

									@Override
									public boolean onClick(int which)
									{
										Timer runTimer = new Timer();
										TimerTask task = new TimerTask()
										{

											@Override
											public void run()
											{
												that.show();
											}
										};

										runTimer.schedule(task, 200);

										return true;
									}
								});

						Timer runTimer = new Timer();
						TimerTask task = new TimerTask()
						{

							@Override
							public void run()
							{
								GL.that.showDialog(msg);
							}
						};

						runTimer.schedule(task, 200);
						return true;
					}

					// check of changes
					if (!altfieldNote.equals(fieldNote))
					{
						fieldNote.uploaded = false;
						fieldNote.UpdateDatabase();
					}

					mReturnListner.returnedFieldNote(fieldNote);
				}
				finish();
				return true;
			}
		});

		bCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mReturnListner != null) mReturnListner.returnedFieldNote(null);
				finish();
				return true;
			}
		});

	}

	private void iniNameLabel()
	{
		tvCacheName = new Label(Left + margin, height - Top - MeasuredLabelHeight, width - Left - Right - margin, MeasuredLabelHeight,
				"CacheNameLabel");
		tvCacheName.setFont(Fonts.getBig());
		tvCacheName.setText(fieldNote.CacheName);
		scrollBox.addChild(tvCacheName);
	}

	private float secondTab = 0;

	private void iniImage()
	{
		ivTyp = new Image(Left + margin, tvCacheName.getY() - margin - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(),
				UiSizes.getButtonHeight(), "");
		scrollBox.addChild(ivTyp);

		secondTab = ivTyp.getMaxX() + (margin * 3);
	}

	private void iniFoundLabel()
	{
		tvFounds = new Label(secondTab, ivTyp.getMaxY() - UiSizes.getButtonHeight(), width - secondTab - Right - margin,
				UiSizes.getButtonHeight(), "CacheNameLabel");
		tvFounds.setFont(Fonts.getBig());
		scrollBox.addChild(tvFounds);
	}

	private float LabelWidth = 0;

	private void iniDate()
	{
		LabelWidth = Math.max(Fonts.Measure(GlobalCore.Translations.Get("date")).width,
				Fonts.Measure(GlobalCore.Translations.Get("time")).width);
		LabelWidth *= 1.3;// use Big Font

		lblDate = new Label(secondTab, tvFounds.getY() - UiSizes.getButtonHeight() - (margin * 3), LabelWidth, UiSizes.getButtonHeight(),
				"");
		lblDate.setFont(Fonts.getBig());
		lblDate.setText(GlobalCore.Translations.Get("date") + ":");
		scrollBox.addChild(lblDate);
		CB_RectF rec = new CB_RectF(lblDate.getMaxX() + margin, lblDate.getY() - margin, width - lblDate.getMaxX() - margin - Right,
				UiSizes.getButtonHeight());

		tvDate = new EditWrapedTextField(this, rec, "");
		scrollBox.addChild(tvDate);
	}

	private void iniTime()
	{

		lblTime = new Label(secondTab, lblDate.getY() - UiSizes.getButtonHeight() - (margin * 3), LabelWidth, UiSizes.getButtonHeight(), "");
		lblTime.setFont(Fonts.getBig());
		lblTime.setText(GlobalCore.Translations.Get("time") + ":");
		scrollBox.addChild(lblTime);
		CB_RectF rec = new CB_RectF(lblTime.getMaxX() + margin, lblTime.getY() - margin, width - lblTime.getMaxX() - margin - Right,
				UiSizes.getButtonHeight());

		tvTime = new EditWrapedTextField(this, rec, "");
		scrollBox.addChild(tvTime);
	}

	private void iniGC_VoteItem()
	{

		if (!Config.settings.GcVotePassword.getEncryptedValue().equalsIgnoreCase(""))
		{
			float itemHeight = UiSizes.getButtonHeight() * 1.1f;

			FilterSetEntry tmp = new FilterSetEntry(GlobalCore.Translations.Get("maxRating"), SpriteCache.Stars.toArray(),
					FilterSetListView.NUMERICK_ITEM, 0, 5, fieldNote.gc_Vote / 100.0, 0.5f);
			GcVote = new FilterSetListViewItem(new CB_RectF(Left, lblTime.getY() - itemHeight - margin, this.width - Left - Right,
					itemHeight), 0, tmp);
			scrollBox.addChild(GcVote);
		}

	}

	private void iniCommentTextField()
	{
		CB_RectF rec;
		if (GcVote != null)
		{
			rec = new CB_RectF(Left, GcVote.getY() - UiSizes.getButtonHeight(), width - Left - Right, UiSizes.getButtonHeight());
		}
		else
		{
			rec = new CB_RectF(Left, lblTime.getY() - UiSizes.getButtonHeight() - margin, width - Left - Right, UiSizes.getButtonHeight());
		}

		etComment = new EditWrapedTextField(this, rec, TextFieldType.MultiLineWraped, "DescTextField");
		etComment.setText(fieldNote.comment);

		// set Size to linecount
		float maxTextFieldHeight = this.height / 2.3f;
		float rand = etComment.getStyle().background.getBottomHeight() + etComment.getStyle().background.getTopHeight();
		float descriptionHeight = Math.min(maxTextFieldHeight, etComment.getMeasuredHeight() + rand);
		descriptionHeight = Math.max(descriptionHeight, UiSizes.getButtonHeight());
		etComment.setHeight(descriptionHeight);
		if (GcVote != null)
		{
			etComment.setY(GcVote.getY() - descriptionHeight);
		}
		else
		{
			etComment.setY(lblTime.getY() - descriptionHeight - margin);
		}

		etComment.setTextFieldListener(new TextFieldListener()
		{

			@Override
			public void keyTyped(EditTextFieldBase textField, char key)
			{

			}

			@Override
			public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight)
			{
				layoutTextFields();
			}
		});

		scrollBox.addChild(etComment);

	}

	private void iniTextfieldFocus()
	{
		registerTextField(etComment);
		registerTextField(tvDate);
		registerTextField(tvTime);
	}

	private ArrayList<EditWrapedTextField> allTextFields = new ArrayList<EditWrapedTextField>();

	private OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();

	public void registerTextField(final EditWrapedTextField textField)
	{
		textField.setOnscreenKeyboard(new OnscreenKeyboard()
		{
			@Override
			public void show(boolean arg0)
			{
				textField.setFocus(true);
				scrollToY(textField.getY(), textField.getMaxY());
			}
		});

		allTextFields.add(textField);
	}

	private void scrollToY(float y, float maxY)
	{
		if (y < this.halfHeight)// wird von softKeyboard verdeckt
		{
			scrollBox.setY(this.height - maxY - MeasuredLabelHeight);
		}
		else
		{
			scrollBox.setY(0);
		}
	}

	private void layoutTextFields()
	{
		float maxTextFieldHeight = this.height / 2.3f;
		float rand = etComment.getStyle().background.getBottomHeight() + etComment.getStyle().background.getTopHeight();
		float descriptionHeight = Math.min(maxTextFieldHeight, etComment.getMeasuredHeight() + rand);

		descriptionHeight = Math.max(descriptionHeight, UiSizes.getButtonHeight());

		etComment.setHeight(descriptionHeight);

		if (GcVote != null)
		{
			etComment.setY(GcVote.getY() - descriptionHeight);
		}
		else
		{
			etComment.setY(lblTime.getY() - descriptionHeight - margin);
		}

		scrollToY(etComment.getY(), etComment.getMaxY());

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		super.onTouchDown(x, y, pointer, button);

		if (etComment.contains(x, y))
		{
			// TODO close SoftKeyboard
			scrollBox.setY(0);
		}

		// for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		for (Iterator<GL_View_Base> iterator = scrollBox.getchilds().reverseIterator(); iterator.hasNext();)
		{
			// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
			GL_View_Base view = iterator.next();

			if (view instanceof FilterSetListViewItem)
			{
				if (view.contains(x, y))
				{
					((FilterSetListViewItem) view).lastItemTouchPos = new Vector2(x - view.getPos().x, y - view.getPos().y);
				}
			}
		}
		return true;
	}

	@Override
	public void dispose()
	{
		super.dispose();
		mReturnListner = null;
		fieldNote = null;
		bOK = null;
		bCancel = null;
		tvCacheName = null;
		etComment = null;
		ivTyp = null;
		tvFounds = null;
		tvDate = null;
		tvTime = null;
		lblDate = null;
		lblTime = null;
		scrollBox = null;
		GcVote = null;
	}

	@Override
	public void onShow()
	{
		KeyboardFocusChangedEventList.Add(this);
	}

	@Override
	public void onHide()
	{
		KeyboardFocusChangedEventList.Remove(this);
	}

	@Override
	public void KeyboardFocusChanged(EditTextFieldBase focus)
	{
		if (focus == null)
		{
			scrollBox.setY(0);
		}
	}
}
