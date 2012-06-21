package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Spinner extends Button
{
	private NinePatch triangle;
	private String[] mItems;
	private int mSelectedIndex = -1;
	private Spinner that;

	public interface selectionChangedListner
	{
		public void selectionChanged(int index);
	}

	private selectionChangedListner mListner;

	public Spinner(float X, float Y, float Width, float Height, String Name, String[] items, selectionChangedListner listner)
	{
		super(X, Y, Width, Height, Name);
		mItems = items;
		that = this;
		mListner = listner;
	}

	public Spinner(CB_RectF rec, String Name, String[] items, selectionChangedListner listner)
	{
		super(rec, Name);
		mItems = items;
		that = this;
		mListner = listner;
	}

	@Override
	protected void Initial()
	{
		super.Initial();

		if (triangle == null)
		{
			Sprite tr = SpriteCache.getThemedSprite("spinner_triangle");
			int patch = (int) tr.getWidth() / 2;
			triangle = new NinePatch(tr, 0, patch, patch, 0);
		}

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// show Menu to select
				Menu icm = new Menu("SpinnerSelection" + that.name);
				icm.setItemClickListner(new OnClickListener()
				{
					@Override
					public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
					{
						int sel = ((MenuItem) v).getIndex();
						setSelection(sel);
						if (mListner != null) mListner.selectionChanged(sel);
						return false;
					}
				});

				int index = 0;
				for (String tmp : mItems)
				{
					icm.addItem(index++, tmp, true);
				}

				icm.show();
				return true;
			}
		});

	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);
		triangle.draw(batch, 0, 0, width, height);
	}

	@Override
	protected void SkinIsChanged()
	{
		triangle = null;
		resetInitial();
	}

	public void setSelection(int i)
	{
		if (mItems.length >= i)
		{
			String Text = mItems[i];
			mSelectedIndex = i;
			this.setText(Text);
			lblTxt.setHAlignment(HAlignment.LEFT);
			lblTxt.setText(Text);

		}

	}

}
