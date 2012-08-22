package CB_Core.GL_UI.Activitys.settings;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.QuickButtonList;
import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.GL_UI.Controls.SpinnerAdapter;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Main.Actions.QuickButton.QuickActions;
import CB_Core.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class SettingsItem_QuickButton extends CB_View_Base
{

	chkBox chkOnOff;
	Label lblChkOnOff;
	Spinner invisibleSelectSpinner;
	SpinnerAdapter selectAdapter;

	ImageButton up, down, del, add;
	V_ListView listView;

	public SettingsItem_QuickButton(CB_RectF rec, String Name)
	{
		super(rec, Name);

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showSelect();
				return true;
			}
		});

		initialButtons();

		layout();

	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	private void showSelect()
	{
		// erstelle Menu mit allen Actions, die noch nicht in der QuickButton List enthalten sind.

		final ArrayList<QuickActions> AllActionList = new ArrayList<QuickActions>();
		QuickActions[] tmp = QuickActions.values();

		for (QuickActions item : tmp)
		{
			boolean exist = false;
			for (Iterator<QuickButtonItem> it = QuickButtonList.quickButtonList.iterator(); it.hasNext();)
			{
				QuickButtonItem listItem = it.next();
				if (listItem.getAction() == item) exist = true;
			}
			if (!exist) AllActionList.add(item);
		}

		Menu icm = new Menu("Select QuickButtonItem");
		icm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				int selected = (((MenuItem) v).getMenuItemId());
				QuickActions addItem = AllActionList.get(selected);
				return true;
			}
		});

		int menuIndex = 0;
		for (QuickActions item : AllActionList)
		{
			if (item == QuickActions.empty) continue;

			icm.addItem(menuIndex++, QuickActions.getName(item.ordinal()), new SpriteDrawable(QuickActions
					.getActionEnumById(item.ordinal()).getIcon()), true);
		}

		icm.setPrompt(GlobalCore.Translations.Get("selectQuickButtemItem"));

		icm.show();

	}

	private void initialButtons()
	{

		up = new ImageButton("up");
		down = new ImageButton("down");
		del = new ImageButton("del");
		add = new ImageButton("add");

		up.setHeight(up.getWidth());
		down.setHeight(up.getWidth());
		del.setHeight(up.getWidth());
		add.setHeight(up.getWidth());

		up.setImage(new SpriteDrawable(SpriteCache.Arrows.get(11)));
		down.setImage(new SpriteDrawable(SpriteCache.Arrows.get(11)));
		del.setImage(new SpriteDrawable(SpriteCache.Arrows.get(11)));
		add.setImage(new SpriteDrawable(SpriteCache.Arrows.get(11)));

		up.setImageScale(0.7f);
		down.setImageScale(0.7f);
		del.setImageScale(0.7f);
		add.setImageScale(0.7f);

		up.setImageRotation(90f);
		down.setImageRotation(-90f);

		this.addChild(up);
		this.addChild(down);
		this.addChild(del);
		this.addChild(add);

		add.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showSelect();
				return true;
			}
		});

	}

	private void layout()
	{
		float btnLeft = this.width - Right - up.getWidth();
		float margin = up.getHalfHeight() / 2;

		add.setX(btnLeft);
		add.setY(Bottom + margin);

		del.setX(btnLeft);
		del.setY(add.getMaxY() + margin);

		down.setX(btnLeft);
		down.setY(del.getMaxY() + margin);

		up.setX(btnLeft);
		up.setY(down.getMaxY() + margin);

		this.setHeight(up.getMaxY() + margin);

	}
}
