package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class CreditsView extends CB_View_Base
{
	private float ref;
	private Image logo;
	private ScrollBox scrollBox;

	public CreditsView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		this.setBackground(SpriteCache.AboutBack);

		ref = UiSizes.getWindowHeight() / 13;
		CB_RectF CB_LogoRec = new CB_RectF(this.getHalfWidth() - (ref * 2.5f), this.height - ((ref * 5) / 4.11f) - ref, ref * 5,
				(ref * 5) / 4.11f);

		logo = new Image(CB_LogoRec, "Logo");
		logo.setDrawable(SpriteCache.logo);
		this.addChild(logo);

		scrollBox = new ScrollBox(rec, 100, "ScrollBox");
		scrollBox.setHeight(logo.getY() - (ref / 2));
		scrollBox.setZeroPos();
		// scrollBox.setBackground(new ColorDrawable(Color.RED)); //Debug

		this.addChild(scrollBox);

	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void resize(float width, float height)
	{
		logo.setY(this.height - ((ref * 5) / 4.11f) - ref);
		scrollBox.setHeight(logo.getY() - (ref / 2));
	}

	public enum Job
	{
		developer, designer, contributer, tester, sponsor
	}

	public class person
	{

		public String name;
		public String nick;
		public Job job;
		public String email;
		public String desc;
		public Drawable image;

	}

}
