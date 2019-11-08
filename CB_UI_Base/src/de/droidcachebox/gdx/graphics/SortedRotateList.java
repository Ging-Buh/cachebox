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
package de.droidcachebox.gdx.graphics;

import de.droidcachebox.utils.CB_List;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * A sorted list of symbols and texts! <br>
 * Sorts first the symbols according to the textures then the texts for fonts!
 *
 * @author Longri
 */
public class SortedRotateList implements Iterable<MatrixDrawable> {
    CB_List<MatrixDrawable> symbols = new CB_List<MatrixDrawable>();
    CB_List<MatrixDrawable> Text = new CB_List<MatrixDrawable>();

    public SortedRotateList() {

    }

    public void add(MatrixDrawable drws) {
        if (drws.drawable instanceof SymbolDrawable) {
            symbols.add(drws);
        } else {
            Text.add(drws);
        }
    }

    @Override
    public Iterator<MatrixDrawable> iterator() {
        return new Itr();
    }

    public boolean isEmpty() {
        if (symbols.isEmpty() && Text.isEmpty())
            return true;
        else
            return false;
    }

    public void clear() {
        symbols.clear();
        Text.clear();
    }

    public void remove(CB_List<MatrixDrawable> clearList) {
        for (int i = 0, n = clearList.size(); i < n; i++) {
            MatrixDrawable drw = clearList.get(i);
            if (drw.drawable instanceof SymbolDrawable) {
                symbols.remove(drw);
            } else {
                Text.remove(drw);
            }
        }

    }

    /**
     * An optimized version of AbstractList.Itr
     */
    private class Itr implements Iterator<MatrixDrawable> {

        int cursor = 0; // index of next element to return

        @Override
        public boolean hasNext() {
            return cursor != symbols.size() + Text.size();
        }

        @Override
        public MatrixDrawable next() {
            if (cursor >= symbols.size() + Text.size())
                throw new ConcurrentModificationException();

            if (cursor >= symbols.size()) {
                return Text.get(cursor++ - symbols.size());
            } else {
                return symbols.get(cursor++);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove is not supported by this Iterator");

        }

    }
}
