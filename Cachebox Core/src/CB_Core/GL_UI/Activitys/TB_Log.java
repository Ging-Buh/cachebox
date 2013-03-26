package CB_Core.GL_UI.Activitys;

import CB_Core.Config;
import CB_Core.TemplateFormatter;
import CB_Core.Enums.LogTypes;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Trackable;

public class TB_Log extends ActivityBase
{
	public static TB_Log that;
	private Trackable TB;
	private Button btnClose;
	private ImageButton btnAction;
	private Image icon;
	private Label lblName;
	private Box contentBox;
	private LogTypes LT;
	private EditWrapedTextField edit;

	public TB_Log()
	{
		super(ActivityRec(), "TB_Log_Activity");
		createControls();
		that = this;
	}

	public void Show(Trackable TB, LogTypes Type)
	{
		this.TB = TB;
		this.LT = Type;
		layout();
		GL.that.showActivity(this);
	}

	private void createControls()
	{

		btnClose = new Button("Close");
		btnClose.setText(Translation.Get("close"));
		btnClose.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				TB_Log.this.finish();
				return true;
			}
		});

		btnAction = new ImageButton("Action");
		btnAction.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				return true;
			}
		});

		contentBox = new Box(ActivityRec(), "ContentBox");
		contentBox.setHeight(this.height - (btnClose.getHeight() - margin) * 2.5f);
		contentBox.setBackground(SpriteCache.activityBackground);

		CB_RectF iconRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight());
		iconRec = iconRec.ScaleCenter(0.8f);

		icon = new Image(iconRec, "Icon");
		icon.setWeight(-1);
		lblName = new Label(iconRec, "Name");

		edit = new EditWrapedTextField("LogInput", TextFieldType.MultiLineWraped);
		edit.setWidth(contentBox.getWidth() - contentBox.getLeftWidth() - contentBox.getRightWidth());
		edit.setHeight(contentBox.getHeight());
	}

	private void layout()
	{
		this.removeChilds();
		this.initRow(false);
		this.addNext(btnAction);
		this.addLast(btnClose);
		this.addLast(contentBox);
		contentBox.addChild(edit);
		this.setMargins(margin * 2, 0);
		this.addNext(icon);
		icon.setImageURL(TB.getIconUrl());
		this.addLast(lblName);

		lblName.setWrappedText(TB.getName());
		switch (this.LT)
		{
		case discovered:
			btnAction.setImage(SpriteCache.Icons.get(58));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.DiscoverdTemplate.getValue(), TB));
			break;
		case visited:
			btnAction.setImage(SpriteCache.Icons.get(62));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.VisitedTemplate.getValue(), TB));
			break;
		case dropped_off:
			btnAction.setImage(SpriteCache.Icons.get(59));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.DroppedTemplate.getValue(), TB));
			break;
		case grab_it:
			btnAction.setImage(SpriteCache.Icons.get(60));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.GrabbedTemplate.getValue(), TB));
			break;
		case retrieve:
			btnAction.setImage(SpriteCache.Icons.get(61));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.PickedTemplate.getValue(), TB));
			break;
		}
	}
}
