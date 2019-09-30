package CB_UI_Base.GL_UI.Menu;

import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.DialogElement;
import CB_UI_Base.Math.UiSizes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class MenuItemDivider extends MenuItem {

    Drawable Image;
    float spriteHeight = 0;
    float spriteWidth = 0;

    public MenuItemDivider() {
        super(0, "Separator");
        setHeight(UiSizes.getInstance().getButtonHeight() / 5);
        setEnabled(false);
    }

    public MenuItemDivider(int Index, String Name) {
        super(Index, Name);
    }

    @Override
    protected void render(Batch batch) {
        if (Image == null) {
            Sprite s = Sprites.Dialog.get(DialogElement.divider.ordinal());
            spriteHeight = s.getHeight();
            spriteWidth = s.getWidth();
            Image = new NinePatchDrawable(new NinePatch(s, 1, 1, 1, 1));
        }

        Image.draw(batch, 0, this.getHalfHeight() - (spriteHeight / 2), getWidth(), spriteHeight);
    }

}
