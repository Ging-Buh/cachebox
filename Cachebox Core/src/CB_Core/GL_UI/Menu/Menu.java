package CB_Core.GL_UI.Menu;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class Menu extends ButtonDialog
{
	protected Menu that;

	public float ItemHeight = -1f;

	private static final int ANIMATION_DURATION = 1200;
	private static float mMoreMenuToggleButtonWidth = -1;

	private ArrayList<MenuItemBase> mItems = new ArrayList<MenuItemBase>();
	private V_ListView mListView;

	private static CB_RectF MENU_REC = null;
	private static boolean MENU_REC_IsInitial = false;
	private Menu mMoreMenu = null;
	private boolean mMoreMenuVisible = false;
	private Button mMoreMenuToggleButton;
	private Label mMoreMenuLabel;

	/**
	 * -1=not initial<br>
	 * 0=left<br>
	 * 1=to left<br>
	 * 2= to right<br>
	 * 3=right
	 */
	private int mAnimationState = -1;// -1=not initial 0=left 1=to left 2= to right 3=right
	private int itemsCount = -1;
	private float animateStartTime;
	private boolean isMoreMenu = false;
	private String mMoreMenuTextRight = "";
	private String mMoreMenuTextLeft = "";
	private Menu mParentMenu;
	private boolean mMoreMenuIsInitial = false;
	private int Level = 0;

	public static CB_RectF getMenuRec()
	{
		if (!MENU_REC_IsInitial)
		{
			float sollWidth = GL_UISizes.UI_Left.getWidth();

			if (!GlobalCore.isTab) sollWidth /= 1.2f;
			sollWidth *= 0.83;
			MENU_REC = new CB_RectF(new SizeF(sollWidth, 50));
			MENU_REC_IsInitial = true;
		}
		return MENU_REC;
	}

	public Menu(String Name)
	{
		super(getMenuRec(), Name);
		that = this;

		if (ItemHeight == -1f) ItemHeight = UI_Size_Base.that.getButtonHeight();
		mListView = new V_ListView(this, "MenuList");
		mListView.setSize(this.getContentSize());
		mListView.setZeroPos();
		this.addChild(mMoreMenu);
		initialDialog();
	}

	protected OnClickListener MenuItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			GL.that.closeDialog(that);
			if (isMoreMenu) GL.that.closeDialog(mParentMenu);
			if (mOnItemClickListner != null)
			{
				for (OnClickListener tmp : mOnItemClickListner)
				{
					if (tmp.onClick(v, x, y, pointer, button)) break;
				}
			}

			return true;
		}
	};

	public void addMoreMenu(Menu menu, String TextLeft, String TextRight)
	{
		if (menu == null)
		{
			mMoreMenuTextRight = "";
			mMoreMenuTextLeft = "";
			mMoreMenu = null;
			return;
		}
		mMoreMenuTextRight = TextRight;
		mMoreMenuTextLeft = TextLeft;
		mMoreMenu = menu;
		mMoreMenu.isMoreMenu = true;
		mMoreMenu.setParrentMenu(this);
		mMoreMenu.setVisible(false);
		mMoreMenu.Level = this.Level + 1;
	}

	public Menu getMoreMenu()
	{
		return mMoreMenu;
	}

	public String getTextLeftMoreMenu()
	{
		return mMoreMenuTextLeft;
	}

	public String getTextRightMoreMenu()
	{
		return mMoreMenuTextRight;
	}

	private void setParrentMenu(Menu menu)
	{
		mParentMenu = menu;
	}

	private void toggleMoreMenu()
	{
		mMoreMenuVisible = !mMoreMenuVisible;
		if (mMoreMenuVisible)
		{
			showMoreMenu();
		}
		else
		{
			hideMoreMenu();
		}
		animateStartTime = GL.that.getStateTime();
	}

	private void showMoreMenu()
	{
		mMoreMenu.setVisible(true);
		mAnimationState = 1;
		mMoreMenu.setWidth(0);
		layout();
		int index = GL.that.getDialogLayer().getchilds().indexOf(mMoreMenuToggleButton);
		GL.that.getDialogLayer().getchilds().MoveItemLast(index);
	}

	private void hideMoreMenu()
	{
		mAnimationState = 2;
		mMoreMenu.setWidth(this.width);
		layout();
		int index = GL.that.getDialogLayer().getchilds().indexOf(mMoreMenuToggleButton);
		GL.that.getDialogLayer().getchilds().MoveItemLast(index);
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if (mMoreMenuToggleButton != null)
		{
			if (visible) layout();
			mMoreMenuToggleButton.setVisible(visible);
		}
	}

	@Override
	protected void Initial()
	{
		super.Initial();

		if (!isMoreMenu)
		{
			// Menu level 1

			if (mItems.size() != itemsCount)
			{
				// new Hight calculation
				itemsCount = mItems.size();
				float higherValue = mTitleHeight + mHeaderHight + getFooterHeight() + (margin * 2);
				for (MenuItemBase item : mItems)
				{
					higherValue += item.getHeight() + mListView.getDividerHeight();
				}
				higherValue = Math.min(higherValue, UI_Size_Base.that.getWindowHeight() * 0.95f);
				if (higherValue > UI_Size_Base.that.getWindowHeight() * 0.95f)
				{
					higherValue = UI_Size_Base.that.getWindowHeight() * 0.95f;
				}
				float MenuWidth = GL_UISizes.UI_Left.getWidth();
				if (!GlobalCore.isTab) MenuWidth /= 1.2f;
				this.setSize(MenuWidth, higherValue);

				// initial more menus
				if (mMoreMenu != null) mMoreMenu.Initial();

			}
		}
		else
		{
			this.setSize(mParentMenu.getWidth(), mParentMenu.getHeight());
		}

		if (mMoreMenuToggleButtonWidth == -1)
		{
			float mesuredLblHeigt = Fonts.MeasureSmall("T").height;
			mMoreMenuToggleButtonWidth = SpriteCache.btn.getLeftWidth() + SpriteCache.btn.getRightWidth() + (mesuredLblHeigt * 1.5f);
		}

		mListView.setSize(this.getContentSize());

		this.addChild(mListView);
		mListView.setBaseAdapter(new CustomAdapter());

		if (mMoreMenu != null && !mMoreMenuIsInitial)
		{
			mMoreMenu.Initial();
			mMoreMenu.setVisible(false);
			mMoreMenu.setZeroPos();
			mMoreMenu.setHeight(this.height);
			mMoreMenu.setWidth(0);
			mMoreMenu.setY(0 - this.getFooterHeight());
			this.addChild(mMoreMenu);

			mMoreMenuToggleButton = new Button("");
			mMoreMenuToggleButton.setWidth(mMoreMenuToggleButtonWidth);
			mMoreMenuToggleButton.setHeight(this.getContentSize().height);
			float MenuY = mParentMenu != null ? mParentMenu.getY() : this.getY();
			mMoreMenuToggleButton.setY(MenuY + this.getFooterHeight());
			GL.that.getDialogLayer().addChild(mMoreMenuToggleButton);
			mMoreMenuToggleButton.setOnClickListener(new OnClickListener()
			{

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					toggleMoreMenu();
					return true;
				}
			});

			mMoreMenuLabel = new Label(mMoreMenuToggleButton, "MoreLabel");
			mMoreMenuLabel.setText(mMoreMenuTextLeft, Fonts.getSmall(), Fonts.getFontColor(), HAlignment.CENTER);

			mMoreMenuLabel.setWidth(mMoreMenuToggleButton.getHeight());
			mMoreMenuLabel.setHeight(mMoreMenuToggleButton.getWidth());
			mMoreMenuLabel.setX(mMoreMenuToggleButton.getWidth());
			mMoreMenuLabel.setY(0);
			mMoreMenuLabel.setOrigin(0, 0);
			mMoreMenuLabel.setRotate(90);
			mMoreMenuLabel.setText(mMoreMenuTextRight);
			mMoreMenuLabel.withoutScissor = true;
			mMoreMenuToggleButton.addChild(mMoreMenuLabel);
			mMoreMenuIsInitial = true;
		}

		// set display center pos
		float cx = (UI_Size_Base.that.getWindowWidth() / 2) - this.getHalfWidth();
		float cy = (UI_Size_Base.that.getWindowHeight() / 2) - this.getHalfHeight();
		this.setPos(cx, cy);

		layout();
	}

	@Override
	public void render(SpriteBatch batch)
	{
		super.render(batch);

		if (mAnimationState > -1) mMoreMenuToggleButton.setY(this.getWorldRec().getY() + this.getFooterHeight());

		// Animation calculation
		if (mAnimationState == 1 || mAnimationState == 2)
		{
			float targetValue = this.getWidth() * 1.5f;

			float animateValue = (1 + ((int) ((GL.that.getStateTime() - animateStartTime) * 1000) % ANIMATION_DURATION)
					/ (ANIMATION_DURATION / targetValue));

			if (mAnimationState == 1)
			{
				if (animateValue >= this.getWidth() - 10)
				{
					animateValue = this.getWidth();
					mAnimationState = 0;
				}
				mMoreMenu.setSize(animateValue, this.getHeight());
			}
			else
			{
				if (animateValue >= this.getWidth() - 10)
				{
					animateValue = this.getWidth();
					mMoreMenu.setVisible(false);
					mAnimationState = 3;
				}
				mMoreMenu.setSize(this.getWidth() - animateValue, this.getHeight());
			}

			layout();
			GL.that.renderOnce("MoreMenuAnimation");
		}
		else if (mAnimationState == -1)
		{
			if (mMoreMenu != null)
			{
				mMoreMenuToggleButton.setHeight(this.getContentSize().height);
				mAnimationState = 3;
				layout();
			}

		}

	}

	public class CustomAdapter implements Adapter
	{

		public ListViewItemBase getView(int position)
		{
			ListViewItemBase v = mItems.get(position);

			v.setWidth(mListView.getWidth());

			v.resetInitial();
			return v;
		}

		@Override
		public int getCount()
		{
			return mItems.size();
		}

		@Override
		public float getItemSize(int position)
		{
			if (mItems == null || mItems.size() == 0 || mItems.size() < position) return 0;
			return mItems.get(position).getHeight();
		}
	}

	public void addItem(MenuItemBase menuItem)
	{
		menuItem.setOnClickListener(MenuItemClickListner);
		mItems.add(menuItem);
		mListView.notifyDataSetChanged();
		resetInitial();
	}

	public MenuItem addItem(int ID, String StringId)
	{
		return addItem(ID, StringId, "", false);
	}

	public MenuItem addItem(int ID, String StringId, boolean withoutTranslation)
	{
		return addItem(ID, StringId, "", withoutTranslation);
	}

	public MenuItem addItem(int ID, String StringId, String anhang, Sprite icon)
	{
		MenuItem item = addItem(ID, StringId, anhang);
		if (icon != null) item.setIcon(new SpriteDrawable(icon));
		return item;
	}

	public MenuItem addItem(int ID, String StringId, String anhang, Drawable icon)
	{
		MenuItem item = addItem(ID, StringId, anhang);
		if (icon != null) item.setIcon(icon);
		return item;
	}

	public MenuItem addItem(int ID, String StringId, String anhang)
	{
		return addItem(ID, StringId, anhang, false);
	}

	public MenuItem addItem(int index, String text, Drawable drawable, boolean withoutTranslation)
	{
		MenuItem item = addItem(index, text, "", withoutTranslation);
		if (drawable != null) item.setIcon(drawable);
		return item;
	}

	public MenuItem addItem(int ID, String StringId, String anhang, boolean withoutTranslation)
	{
		String trans;
		if (StringId == null || StringId.equals(""))
		{
			trans = anhang;
		}
		else
		{
			trans = Translation.Get(StringId) + anhang;
		}

		if (withoutTranslation) trans = StringId;

		// layout();
		MenuItem item = new MenuItem(new SizeF(mListView.getWidth(), ItemHeight), mItems.size(), ID, "Menu Item@" + ID);

		item.setTitle(trans);
		addItem(item);

		return item;
	}

	private void layout()
	{

		float WithOffset = isMoreMenu ? mMoreMenuToggleButtonWidth / 2 : 0;
		if (isMoreMenu && mMoreMenu != null) WithOffset = mMoreMenuToggleButtonWidth;
		if (!isMoreMenu && mMoreMenu != null) WithOffset = mMoreMenuToggleButtonWidth / 2;
		if (mListView != null)
		{
			mListView.setSize(this.getContentSize().width - WithOffset, this.getContentSize().height);
			mListView.setZeroPos();
			if (isMoreMenu && mMoreMenu != null) WithOffset /= 2;
			if (!isMoreMenu && mMoreMenu != null) WithOffset = 0;
			mListView.setX(WithOffset);
		}

		// Alle Items in der Breite anpassen
		float w = mListView.getWidth();
		for (MenuItemBase item : mItems)
		{
			item.setWidth(w);
			item.resetInitial();
		}
		mListView.notifyDataSetChanged();

		if (mMoreMenuToggleButton != null)
		{

			switch (mAnimationState)
			{
			case 0:
				this.setWidth(getLeve0_Width());
				mMoreMenu.setWidth(getLeve0_Width());
				mMoreMenu.setX(-this.getLeftWidth() - this.getRightWidth() - 2.5f);
				// TODO die -2,5f müssen auf meinem S3 sein,
				// damit die linke Position passt auf dem desktop sind es 0 auf anderen?
				// ich habe hier den zusammen hang noch nicht finden können
				mMoreMenuToggleButton.setX(getLevel0_x() - mMoreMenuToggleButton.getHalfWidth() + (margin * 2));

				mMoreMenuLabel.setText(mMoreMenuTextLeft, Fonts.getSmall(), Fonts.getFontColor(), HAlignment.CENTER);
				break;
			case 1:
				mMoreMenu.setX(this.width - mMoreMenu.width - this.getLeftWidth());
				mMoreMenuToggleButton.setX(getLevel0_x() + mMoreMenu.getX() - this.getLeftWidth());
				break;
			case 2:
				mMoreMenu.setX(this.width - mMoreMenu.width - this.getLeftWidth());
				mMoreMenuToggleButton.setX(getLevel0_x() + mMoreMenu.getX() - mMoreMenuToggleButton.getHalfWidth());
				break;
			case 3:
				mMoreMenu.setWidth(0);
				mMoreMenuToggleButton.setX(getLevel0_maxX() - mMoreMenuToggleButton.getHalfWidth() - (margin * 2));

				break;
			}
		}
	}

	@Override
	public void Show()
	{
		layout();
		super.Show();

	}

	public MenuItem addItem(int ID, String StringId, Sprite icon)
	{
		MenuItem item = addItem(ID, StringId);
		if (icon != null) item.setIcon(new SpriteDrawable(icon));
		return item;
	}

	protected ArrayList<OnClickListener> mOnItemClickListner;

	public void addItemClickListner(OnClickListener onItemClickListner)
	{
		if (this.mOnItemClickListner == null) this.mOnItemClickListner = new ArrayList<GL_View_Base.OnClickListener>();
		this.mOnItemClickListner.add(onItemClickListner);
	}

	public void addItemClickListner(ArrayList<OnClickListener> onItemClickListner)
	{
		if (this.mOnItemClickListner == null) this.mOnItemClickListner = new ArrayList<GL_View_Base.OnClickListener>();
		this.mOnItemClickListner.addAll(onItemClickListner);
	}

	public ArrayList<OnClickListener> getItemClickListner()
	{
		return this.mOnItemClickListner;
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public void setPrompt(String Prompt)
	{
		// set Title with full width, add many blanks
		this.setTitle(Prompt + "                                                       ");
		layout();
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
		layout();
	}

	public ArrayList<MenuItemBase> getItems()
	{
		return mItems;
	}

	public void addItems(ArrayList<MenuItemBase> items)
	{
		for (MenuItemBase menuItem : items)
		{
			menuItem.setOnClickListener(MenuItemClickListner);
			mItems.add(menuItem);
			mListView.notifyDataSetChanged();
		}
	}

	public void addDivider()
	{
		MenuItemDivider item = new MenuItemDivider(new SizeF(mListView.getWidth(), ItemHeight / 5), mItems.size(), "Menu Devider");
		item.setEnabled(false);
		addItem(item);
	}

	/**
	 * Die indexes der Items werden neu erstellt.
	 */
	public int reorganizeIndexes()
	{
		int Index = 0;
		for (MenuItemBase item : mItems)
		{
			item.setIndex(Index++);
		}
		return Index;
	}

	private float getLevel0_x()
	{
		if (mParentMenu == null) return this.getX();
		return mParentMenu.getLevel0_x();
	}

	private float getLevel0_maxX()
	{
		if (mParentMenu == null) return this.getMaxX();
		return mParentMenu.getLevel0_maxX();
	}

	private float getLeve0_Width()
	{
		if (mParentMenu == null) return this.getWidth();
		return mParentMenu.getLeve0_Width();
	}

}
