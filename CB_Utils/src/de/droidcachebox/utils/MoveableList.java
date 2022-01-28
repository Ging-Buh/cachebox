/*
 * Copyright (C) 2011 team-cachebox.de
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

package de.droidcachebox.utils;

import java.util.Iterator;

/// <summary>
/// Diese klasse ist von DynamicList(Of T) abgeleitet und implementiert Methoden zum bewegen der elemente in einer DynamicList(Of T)
/// </summary>
/// 
/// 
///  <example>
///  <para><b>Der Aufruf folgender Methoden lösen ein Changed Event aus:</b></para>
/// 
/// <para>
///  Add / 
///  AddRange / 
///  Clear / 
///  Insert / 
///  InsertRange / 
///  Remove / 
///  RemoveAll / 
///  RemoveAt / 
///  RemoveRange / (geerbt von DynamicList(Of T)
///  </para>
/// 
/// 
/// <para><b>Neu implementierte Methoden, welche ein Changed Event aus lösen:</b></para>
/// <para >
///  MoveItem /
///  moveItemFirst /
///  MoveItemLast /
///  MoveItemsLeft /
///  MoveItemsRight /
/// 
/// </para>
/// 
/// <code lang="VB" title=" Kompletter Code dieser Klasse: " source="C:\@Work\Longri_Sammlung\coc_VB01\MoveableDynamicList.vb"/>
/// 
/// 
/// </example>
/// 
/// 
/// <typeparam name="T">Der Typ der Elemente in der Liste. </typeparam>
/// <remarks>
/// 
/// Auf Elemente in dieser Auflistung kann mithilfe eines ganzzahligen Index zugegriffen werden. Diese Auflistung verwendet nullbasierte Indizes. 
/// 
///
///</remarks>

/**
 * @author Longri
 */
public class MoveableList<T> extends CB_List<T> {

    private static final long serialVersionUID = -3030926604332765746L;
    protected CB_List<IChanged> ChangedEventList = new CB_List<>();
    private boolean dontFireEvent = false;
    private int moveResultIndex;

    public MoveableList() {
        super();
    }

    public MoveableList(MoveableList<T> list) {
        super(list);
    }

    protected void fire() {
        if (dontFireEvent)
            return;
        synchronized (ChangedEventList) {
            for (int i = 0, n = ChangedEventList.size(); i < n; i++) {
                IChanged listener = ChangedEventList.get(i);
                listener.handleChange();
            }
        }
    }

    public void addChangedEventListener(IChanged listener) {
        synchronized (ChangedEventList) {
            if (!ChangedEventList.contains(listener))
                ChangedEventList.add(listener);
        }
    }

    public void removeChangedEventListener(IChanged listener) {
        synchronized (ChangedEventList) {
            ChangedEventList.remove(listener);
        }
    }

    private void privateMoveItem(int CutItem, int InsertItem) {
        T CutItemInfo = this.get(CutItem);

        this.remove(CutItem);
        this.add(InsertItem, CutItemInfo);

        fire();

    }

    public void beginnUpdate() {
        dontFireEvent = true;
    }

    public void endUpdate() {
        dontFireEvent = false;
        fire();
    }

    @Override
    public int add(T t) {
        int ID = super.add(t);
        fire();
        return ID;
    }

    @Override
    public void add(int index, T t) {
        super.add(index, t);
        fire();
    }

    @Override
    public void addAll(CB_List<T> t) {
        super.addAll(t);
        fire();
    }

    @Override
    public void addAll(int index, CB_List<T> t) {
        super.addAll(index, t);
        fire();
    }

    @Override
    public void clear() {
        super.clear();
        fire();
    }

    @Override
    public T remove(int index) {
        T t = super.remove(index);
        fire();
        return t;
    }

    public void moveItemsLeft() {
        privateMoveItem(0, this.size() - 1);
        moveResultIndex = -1;
    }

    public void moveItemsRight() {
        privateMoveItem(this.size() - 1, 0);
        moveResultIndex = -1;
    }

    public void moveItemFirst(int index) {
        privateMoveItem(index, 0);
        moveResultIndex = 0;

    }

    public void MoveItemLast(int index) {
        privateMoveItem(index, this.size() - 1);
        moveResultIndex = this.size() - 1;
    }

    public int moveItem(int index, int Step) {
        moveResultIndex = index;
        if (index < 0)
            throw new IndexOutOfBoundsException();
        int Insert = 0;
        if (Step == 0) {
            return moveResultIndex;
        } else if (Step > 0) {
            Insert = chkNewPos(index + Step);
        } else {
            Insert = chkNewPos(index + Step, true);
        }

        if (Insert == index)
            return moveResultIndex;

        privateMoveItem(index, Insert);
        moveResultIndex = Insert;
        return moveResultIndex;
    }

    public void moveItem(int index) {
        this.moveItem(index, 1);
    }

    private int chkNewPos(int Pos, boolean Negative) {
        if (((Pos < this.size()) & (Pos >= 0)))
            return Pos;

        if (Negative) {
            Pos += this.size();
            Pos = chkNewPos(Pos, true);
        } else {
            Pos -= this.size();
            Pos = chkNewPos(Pos);
        }
        return Pos;
    }

    private int chkNewPos(int Pos) {
        return this.chkNewPos(Pos, false);
    }

    // / <summary>
    // / returns the position of the last move.
    // / </summary>
    // / <returns>zero-based index, of the last move</returns>
    // / <remarks>Bei den Methoden <see cref="MoveItemsLeft">[MoveItemsLeft]</see> und <see cref="MoveItemsRight">[MoveItemsRight]</see>
    // / wird die Eigenschaft <see cref="MoveResultIndex">[MoveResultIndex]</see> auf <b>-1</b> gesetzt, <b>da alle Items bewegt wurden.</b>
    // </remarks>
    public int MoveResultIndex() {
        return moveResultIndex;
    }

    public Iterator<T> reverseIterator() {

        Iterator<T> iterator = new Iterator<T>() {
            int aktItem = size() - 1;

            @Override
            public boolean hasNext() {
                if (aktItem >= 0)
                    return true;
                return false;
            }

            @Override
            public T next() {
                if (size() == 0 || size() < aktItem) {
                    aktItem = -1;
                    return null;
                }

                T ret = get(aktItem);
                aktItem--;
                return ret;
            }

            @Override
            public void remove() {

            }
        };

        return iterator;
    }

    public void remove(MoveableList<T> items) {
        super.removeAll(items);
    }

}
