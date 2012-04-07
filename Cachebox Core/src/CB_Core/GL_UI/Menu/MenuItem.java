package CB_Core.GL_UI.Menu;

import java.awt.Menu;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class MenuItem extends ListViewItemBase
{
	private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

	private String mTitle;
	private boolean mIsEnabled = true;
	private Sprite mIcon;
	private Label mLabel;
	private boolean mIsCheckable = false;
	private boolean mIsChecked = false;
	private boolean mLeft = false;
	private Object data = null;

	private Image mImage;
	private int mID;

	protected boolean isPressed = false;

	public MenuItem(SizeF size, int Index, int ID, CharSequence Name)
	{
		super(new CB_RectF(size), Index, Name);
		mID = ID;
	}

	public int getMenuItemId()
	{
		return mID;
	}

	@Override
	protected void Initial()
	{
		this.removeChilds();

		boolean hasIcon = (mIcon != null);

		float left = (mIsCheckable || mLeft) ? this.height * 0.75f : 0;
		float right = hasIcon ? this.height : 0;
		float labelWidth = (this.width - right - left) * 0.97f;

		setBackground();
		mLabel = new Label(this.ScaleCenter(0.97f), "MenuItemLabel");

		mLabel.setWidth(labelWidth);

		float x = ((this.width - mLabel.getWidth()) / 2) + left;
		float y = (this.height - mLabel.getHeight()) / 2;

		if (hasIcon)
		{
			CB_RectF rec = new CB_RectF(this.width - this.height, 0, this.height, this.height).ScaleCenter(0.75f);

			Image iconImage = new Image(rec, "MenuItemImage");

			Sprite sprite = null;
			if (mIsEnabled)
			{
				sprite = mIcon;
			}
			else
			{
				sprite = new Sprite(mIcon);
				sprite.setColor(DISABLE_COLOR);
			}

			iconImage.setSprite(sprite);
			this.addChild(iconImage);
		}

		if (mIsCheckable)
		{
			CB_RectF rec = new CB_RectF(0, 0, this.height, this.height).ScaleCenter(0.75f);

			rec.setHeight(rec.getWidth());

			Image checkImage = new Image(rec, "MenuItemCheckImage");

			Sprite sprite = null;
			if (mIsChecked)
			{
				sprite = SpriteCache.ChkIcons.get(1);
			}
			else
			{
				sprite = SpriteCache.ChkIcons.get(0);
			}

			checkImage.setSprite(sprite);
			this.addChild(checkImage);
		}

		mLabel.setPos(x, y);
		mLabel.setText(mTitle);
		if (!mIsEnabled)
		{
			mLabel.setTextColor(DISABLE_COLOR);
		}

		this.addChild(mLabel);
	}

	/**
	 * Change the title associated with this item.
	 * 
	 * @param title
	 *            The new text to be displayed.
	 * @return This Item so additional setters can be called.
	 */
	public MenuItem setTitle(String title)
	{
		mTitle = title;
		if (mLabel == null) mLabel = new Label(this.ScaleCenter(0.97f), "MenuItemLabel");
		mLabel.setText(title);
		return this;
	}

	/**
	 * Retrieve the current title of the item.
	 * 
	 * @return The title.
	 */
	public CharSequence getTitle()
	{
		return mTitle;
	}

	/**
	 * Change the icon associated with this item. This icon will not always be shown, so the title should be sufficient in describing this
	 * item. See {@link Menu} for the menu types that support icons.
	 * 
	 * @param icon
	 *            The new icon (as a Sprite) to be displayed.
	 * @return This Item so additional setters can be called.
	 */
	public MenuItem setIcon(Sprite icon)
	{
		mIcon = icon;
		this.resetInitial();
		return this;
	}

	private void setBackground()
	{

		Boolean BackGroundChanger = ((this.getIndex() % 2) == 1);

		if (isPressed)
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_pressed"), 8, 8, 8, 8));
		}
		else if (BackGroundChanger)
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_first"), 8, 8, 8, 8));
		}
		else
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_secend"), 8, 8, 8, 8));
		}

		GL_Listener.glListener.renderOnce(this);
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		isPressed = true;
		setBackground();
		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		isPressed = false;
		setBackground();
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		isPressed = false;
		setBackground();
		return true;
	}

	public void setEnabled(boolean enabled)
	{
		mIsEnabled = enabled;

		if (!mIsEnabled)
		{
			// lösche ClickListner
			mOnClickListener = null;
			mOnLongClickListener = null;
		}

		this.resetInitial();
	}

	public void setCheckable(boolean isCheckable)
	{
		mIsCheckable = isCheckable;
		this.resetInitial();
	}

	public void setChecked(boolean checked)
	{
		mIsChecked = checked;
		this.resetInitial();
	}

	public boolean isCheckable()
	{
		return mIsCheckable;
	}

	public void setLeft(boolean value)
	{
		mLeft = value;
		this.resetInitial();
	}

	public void setData(Object data)
	{
		this.data = data;
	}

	public Object getData()
	{
		return this.data;
	}

}
