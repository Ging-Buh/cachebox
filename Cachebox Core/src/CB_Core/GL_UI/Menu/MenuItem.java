package CB_Core.GL_UI.Menu;

import java.awt.Menu;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class MenuItem extends MenuItemBase
{
	private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

	private String mTitle;
	private boolean mIsEnabled = true;
	private Drawable mIcon;
	private Label mLabel;

	protected boolean mIsCheckable = false;
	protected boolean mIsChecked = false;
	protected boolean mLeft = false;

	private Image checkImage;

	private int mID;

	protected boolean isPressed = false;

	public MenuItem(SizeF size, int Index, int ID, String Name)
	{
		super(new CB_RectF(size), Index, Name);
		mID = ID;

		mIndex = Index;
	}

	public int getMenuItemId()
	{
		return mID;
	}

	public void toggleCheck()
	{
		if (isCheckable())
		{
			mIsChecked = !mIsChecked;

			Drawable drawable = null;
			if (mIsChecked)
			{
				drawable = new SpriteDrawable(SpriteCache.ChkIcons.get(1));
			}
			else
			{
				drawable = new SpriteDrawable(SpriteCache.ChkIcons.get(0));
			}

			checkImage.setDrawable(drawable);
		}
	}

	@Override
	protected void Initial()
	{
		super.Initial();
		this.removeChilds();

		boolean hasIcon = (mIcon != null);

		float left = (mIsCheckable || mLeft) ? this.height * 0.97f : this.height * 0.2f;
		float right = hasIcon ? this.height : 0;
		float labelWidth = (this.width - right - left) * 0.97f;

		mLabel = new Label(this.ScaleCenter(0.97f), "MenuItemLabel");

		mLabel.setWidth(labelWidth);

		// float x = ((this.width - mLabel.getWidth()) / 2) + left;
		float x = left;
		float y = (this.height - mLabel.getHeight()) / 2;

		if (hasIcon)
		{
			CB_RectF rec = new CB_RectF(this.width - this.height, 0, this.height, this.height).ScaleCenter(0.75f);

			Image iconImage = new Image(rec, "MenuItemImage");

			iconImage.setDrawable(mIcon);
			if (!mIsEnabled)
			{
				iconImage.setColor(DISABLE_COLOR);
			}

			this.addChild(iconImage);
		}

		if (mIsCheckable)
		{
			CB_RectF rec = new CB_RectF(0, 0, this.height, this.height).ScaleCenter(0.75f);

			rec.setHeight(rec.getWidth());

			checkImage = new Image(rec, "MenuItemCheckImage");

			Drawable drawable = null;
			if (mIsChecked)
			{
				drawable = new SpriteDrawable(SpriteCache.ChkIcons.get(1));
			}
			else
			{
				drawable = new SpriteDrawable(SpriteCache.ChkIcons.get(0));
			}

			checkImage.setDrawable(drawable);
			this.addChild(checkImage);
		}

		mLabel.setPos(x, y);
		if (mTitle != null) mLabel.setText(mTitle);
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
	public String getTitle()
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
	public MenuItem setIcon(Drawable icon)
	{
		mIcon = icon;
		this.resetInitial();
		return this;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		isPressed = true;

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		isPressed = false;

		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		isPressed = false;

		return false;
	}

	public void setEnabled(boolean enabled)
	{
		mIsEnabled = enabled;

		if (!mIsEnabled)
		{
			// lösche ClickListner
			setOnClickListener(null);
			setOnLongClickListener(null);
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

	@Override
	protected void SkinIsChanged()
	{
	}

}
