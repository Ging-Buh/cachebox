package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.DialogElement;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class MenuItemDivider extends MenuItem {

    Drawable Image;
    float spriteHeight = 0;
    float spriteWidth = 0;

    public MenuItemDivider() {
        super(0, "Separator");
        setHeight(UiSizes.getInstance().getButtonHeight() / 5);
        setEnabled(false);
    }

    MenuItemDivider(int Index, String Name) {
        super(Index, Name);
    }

    @Override
    public void onResized(CB_RectF rec) {
        // setContentSize, called in onResized in MenuItem does't reflect that there is no mLabel for a divider
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
