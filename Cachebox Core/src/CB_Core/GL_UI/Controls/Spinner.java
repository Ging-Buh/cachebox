package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Spinner extends Button
{
	private NinePatch triangle;
	private int mSelectedIndex = -1;
	private Spinner that;
	private String prompt;
	private Image icon;

	private SpinnerAdapter mAdapter;

	public interface selectionChangedListner
	{
		public void selectionChanged(int index);
	}

	private selectionChangedListner mListner;

	public Spinner(String Name, SpinnerAdapter adapter, selectionChangedListner listner)
	{
		super(new CB_RectF(0, 0, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()), Name);
		mAdapter = adapter;
		that = this;
		mListner = listner;
	}

	public Spinner(float X, float Y, float Width, float Height, String Name, SpinnerAdapter adapter, selectionChangedListner listner)
	{
		super(X, Y, Width, Height, Name);
		mAdapter = adapter;
		that = this;
		mListner = listner;
	}

	public Spinner(CB_RectF rec, String Name, SpinnerAdapter adapter, selectionChangedListner listner)
	{
		super(rec, Name);
		mAdapter = adapter;
		that = this;
		mListner = listner;
	}

	@Override
	protected void Initial()
	{
		super.Initial();

		if (triangle == null)
		{
			Sprite tr = SpriteCache.getThemedSprite("spinner-triangle");
			int patch = (int) tr.getWidth() / 2;
			triangle = new NinePatch(tr, 0, patch, patch, 0);
		}

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mAdapter == null) return true; // kann nix anzeigen

				// show Menu to select
				Menu icm = new Menu("SpinnerSelection" + that.name);
				icm.addItemClickListner(new OnClickListener()
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

				for (int index = 0; index < mAdapter.getCount(); index++)
				{
					String text = mAdapter.getText(index);
					Drawable drawable = mAdapter.getIcon(index);

					icm.addItem(index, text, drawable, true);
				}

				if (prompt != null && !prompt.equalsIgnoreCase(""))
				{
					icm.setPrompt(prompt);
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
		if (mAdapter != null && mAdapter.getCount() >= i && i > -1)
		{
			String Text = mAdapter.getText(i);
			mSelectedIndex = i;
			this.setText(Text);

			Drawable drw = mAdapter.getIcon(i);

			if (drw != null)
			{
				lblTxt.setHAlignment(HAlignment.LEFT);
				if (icon == null)
				{
					CB_RectF rec = (new CB_RectF(0, 0, this.height, this.height)).ScaleCenter(0.7f);

					icon = new Image(rec, "");
					icon.setY(this.halfHeight - icon.getHalfHeight());

					float margin = UiSizes.getMargin();

					icon.setX(margin * 2);

					this.addChild(icon);

					lblTxt.setX(icon.getMaxX() + margin);
				}
				float margin = UiSizes.getMargin();

				icon.setX(margin * 2);

				this.addChild(icon);

				lblTxt.setX(icon.getMaxX() + margin);
				icon.setDrawable(drw);
			}
			else
			{
				lblTxt.setHAlignment(HAlignment.CENTER);
			}
			lblTxt.setText(Text);
		}

	}

	public int getSelectedItem()
	{
		return mSelectedIndex;
	}

	public void setPrompt(String Prompt)
	{
		prompt = Prompt;
	}

	public SpinnerAdapter getAdapter()
	{
		return mAdapter;
	}

	public void setAdapter(SpinnerAdapter adapter)
	{
		mAdapter = adapter;
	}

	public void setSelectionChangedListner(selectionChangedListner selectionChangedListner)
	{
		mListner = selectionChangedListner;
	}

}
