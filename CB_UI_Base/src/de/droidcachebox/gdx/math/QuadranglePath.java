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
package de.droidcachebox.gdx.math;

import com.badlogic.gdx.utils.Disposable;
import de.droidcachebox.gdx.graphics.GL_Paint;
import de.droidcachebox.utils.CB_List;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Longri
 */
public class QuadranglePath extends CB_List<Quadrangle> implements Disposable {
    private static final long serialVersionUID = 2368800461989756291L;
    protected AtomicBoolean isDisposed = new AtomicBoolean(false);

    public QuadranglePath(PathLine pathLine, GL_Paint paint) {
        for (int i = 0, n = pathLine.size(); i < n; i++) {
            this.add(new Quadrangle(pathLine.get(i), paint.getStrokeWidth()));
        }
    }

    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void dispose() {
        synchronized (isDisposed) {
            if (isDisposed.get())
                return;
            for (int i = 0, n = this.size(); i < n; i++) {
                this.get(i).dispose();
            }
            this.clear();
            isDisposed.set(true);
        }
    }
}
