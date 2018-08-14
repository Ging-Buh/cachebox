/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.graphics;

import CB_UI_Base.graphics.extendedInterfaces.ext_Paint;
import CB_Utils.MathUtils;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextOnPath;

/**
 * @author Longri
 */
public class TextDrawableFlipped extends TextDrawable {
    TextOnPath flippedCache;
    private boolean isFlipped = false;

    public TextDrawableFlipped(final String text, GL_Path path, float defaultWidth, float defaultHeight, final ext_Paint fill, final ext_Paint stroke, final boolean center) {
        super(text, path, defaultWidth, defaultHeight, fill, stroke, center);
    }

    @Override
    public boolean draw(Batch batch, float x, float y, float width, float height, float rotated) {
        if (isDisposed)
            return true;

        float direction = MathUtils.LegalizeDegreese(pathDirection + rotated);

        if (direction >= 180) {
            isFlipped = true;
        } else {
            isFlipped = false;
        }

        float scaleWidth = width / DEFAULT_WIDTH;
        float scaleHeight = height / DEFAULT_HEIGHT;

        transform.setToTranslation(x, y, 0);
        transform.scale(scaleWidth, scaleHeight, 1);

        if (isFlipped) {
            if (flippedCache == null) {
                workPath.revert();
                flippedCache = new TextOnPath(this.Text, workPath, fill, stroke, center);
            }
            if (flippedCache != null) {
                if (flippedCache.PathToClose())
                    return true;
                flippedCache.draw(batch, transform);
            } else {
                return true;
            }
        } else {
            if (Cache == null) {
                Cache = new TextOnPath(Text, workPath, fill, stroke, center);
            }
            if (Cache != null) {

                if (Cache.PathToClose())
                    return true;
                Cache.draw(batch, transform);
            } else {
                return true;
            }
        }

        if (workPath != null && Cache != null && flippedCache != null) {
            // all Caches created, can dispose Path
            workPath.dispose();
            workPath = null;
        }
        return false;
    }

    @Override
    public void dispose() {
        if (flippedCache != null)
            flippedCache.dispose();
        flippedCache = null;
        super.dispose();
    }

}
