package de.droidcachebox.gdx.graphics;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

import de.droidcachebox.gdx.GL;

public class PixmapDrawable extends EmptyDrawable {
    private Texture tex;

    public PixmapDrawable(final Pixmap pixmap) {
        // must create on GL Thread
        GL.that.runOnGL(() -> {
            tex = new Texture(pixmap);
            // tex.bind();
            pixmap.dispose();
        });

    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        if (tex != null)
            batch.draw(tex, x, y, width, height);
    }

    public void dispose() {
        if (tex == null)
            return;
        tex.dispose();
        tex = null;
    }
}
