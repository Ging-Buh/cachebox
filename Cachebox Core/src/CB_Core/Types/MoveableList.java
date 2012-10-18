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

package CB_Core.Types;

import java.util.ArrayList;
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
///  MoveItemFirst /
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
public class MoveableList<T> extends ArrayList<T>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private void PrivateMoveItem(int CutItem, int InsertItem)
	{
		T CutItemInfo = this.get(CutItem);

		this.remove(CutItem);
		this.add(InsertItem, CutItemInfo);

	}

	// / <summary>
	// / Rotiert alle Item der List(of T) über den Anfang hinaus.
	// / </summary>
	// / <remarks>
	// / <code lang="none" source="C:\@Work\Longri_Sammlung\coc_VB01\MoveableDynamicList_XmlDocu.vb" region="Docu Move Left" />
	// / <para>Die Eigenschaft <see cref="MoveResultIndex">[MoveResultIndex]</see> wird auf <b>-1</b> gesetzt.</para>
	// / <para>Das Event <see cref="Changed">[Changed]</see> wird ausgelösst.</para>
	// /</remarks>
	public void MoveItemsLeft()
	{
		PrivateMoveItem(0, this.size() - 1);
		_MoveResultIndex = -1;
	}

	// / <summary>
	// / Rotiert alle Item der List(of T) über das Ende hinaus.
	// / </summary>
	// / <remarks>
	// / <code lang="none" source="C:\@Work\Longri_Sammlung\coc_VB01\MoveableDynamicList_XmlDocu.vb" region="Docu Move Right" />
	// / <para>Die Eigenschaft <see cref="MoveResultIndex">[MoveResultIndex]</see> wird auf <b>-1</b> gesetzt.</para>
	// / <para>Das Event <see cref="Changed">[Changed]</see> wird ausgelösst.</para>
	// /</remarks>
	public void MoveItemsRight()
	{
		PrivateMoveItem(this.size() - 1, 0);
		_MoveResultIndex = -1;
	}

	// / <summary>
	// / Verschiebt das über den Index angegebenen Item an den Anfang der List(of T)
	// / </summary>
	// / <param name="index">Der nullbasierte Index, des Items, welches verschoben werden soll.</param>
	// / <remarks>
	// / <code lang="none" source="C:\@Work\Longri_Sammlung\coc_VB01\MoveableDynamicList_XmlDocu.vb" region="Docu Move First" />
	// / <para>Die Eigenschaft <see cref="MoveResultIndex">[MoveResultIndex]</see> wird auf 0 gesetzt.</para>
	// / <para>Das Event <see cref="Changed">[Changed]</see> wird ausgelösst.</para>
	// /</remarks>
	public void MoveItemFirst(int index)
	{
		PrivateMoveItem(index, 0);
		_MoveResultIndex = 0;

	}

	// / <summary>
	// / Verschiebt das über den Index angegebenen Item an das Ende der List(of T)
	// / </summary>
	// / <param name="index">Der nullbasierte Index, des Items, welches verschoben werden soll.</param>
	// / <remarks>
	// / <code lang="none" source="C:\@Work\Longri_Sammlung\coc_VB01\MoveableDynamicList_XmlDocu.vb" region="Docu Move Last" />
	// / <para>Die Eigenschaft <see cref="MoveResultIndex">[MoveResultIndex]</see> wird auf Count-1 gesetzt.</para>
	// / <para>Das Event <see cref="Changed">[Changed]</see> wird ausgelösst.</para>
	// / </remarks>
	public void MoveItemLast(int index)
	{
		PrivateMoveItem(index, this.size() - 1);
		_MoveResultIndex = this.size() - 1;

	}

	// / <summary>
	// / Verschiebt das über den Index angegebenen Item an die Position, welche sich aus der Summe von Index und [Step] ergibt.
	// / </summary>
	// / <param name="index">Der nullbasierte Index, des Items, welches verschoben werden soll.</param>
	// / <param name="Step">Die Anzahl an schritten um die das Item verschoben werden soll.</param>
	// / <example>
	// / <code lang="none" source="C:\@Work\Longri_Sammlung\Release\Doku@Work\LinkedTxtDocu\MoveableDynamicList(Of T).MoveItem.txt" />
	// / </example>
	// / <remarks>
	// / <para>Die Eigenschaft <see cref="MoveResultIndex">[MoveResultIndex]</see> wird auf den Index des ergebnisses gesetzt.</para>
	// / <para>Das Event <see cref="Changed">[Changed]</see> wird ausgelösst.</para>
	// / <para><b>Das Event <see cref="Changed">[Changed]</see> wird NICHT ausgelösst, wenn Step = 0 ist oder das Ergebniss dem Index
	// entspricht.
	// / (Wenn sich die List(Of T) nicht geändert hat.)</b></para>
	// / </remarks>
	public int MoveItem(int index, int Step)
	{
		_MoveResultIndex = index;
		if (index < 0) throw new IndexOutOfBoundsException();
		int Insert = 0;
		if (Step == 0)
		{
			return _MoveResultIndex;
		}
		else if (Step > 0)
		{
			Insert = ChkNewPos(index + Step);
		}
		else
		{
			Insert = ChkNewPos(index + Step, true);
		}

		if (Insert == index) return _MoveResultIndex;

		PrivateMoveItem(index, Insert);
		_MoveResultIndex = Insert;
		return _MoveResultIndex;
	}

	// / <summary>
	// / Verschiebt das über den Index angegebenen Item an die Position, welche sich aus der Summe von Index und [Step] ergibt.
	// / </summary>
	// / <param name="index">Der nullbasierte Index, des Items, welches verschoben werden soll.</param>
	// / <param name="Step">Die Anzahl an schritten um die das Item verschoben werden soll.</param>
	// / <example>
	// / <code lang="none" source="C:\@Work\Longri_Sammlung\Release\Doku@Work\LinkedTxtDocu\MoveableDynamicList(Of T).MoveItem.txt" />
	// / </example>
	// / <remarks>
	// / <para>Die Eigenschaft <see cref="MoveResultIndex">[MoveResultIndex]</see> wird auf den Index des ergebnisses gesetzt.</para>
	// / <para>Das Event <see cref="Changed">[Changed]</see> wird ausgelösst.</para>
	// / <para><b>Das Event <see cref="Changed">[Changed]</see> wird NICHT ausgelösst, wenn Step = 0 ist oder das Ergebniss dem Index
	// entspricht.
	// / (Wenn sich die List(Of T) nicht geändert hat.)</b></para>
	// / </remarks>
	public void MoveItem(int index)
	{
		this.MoveItem(index, 1);
	}

	private int ChkNewPos(int Pos, boolean Negative)
	{
		if (((Pos < this.size()) & (Pos >= 0))) return Pos;

		if (Negative)
		{
			Pos += this.size();
			Pos = ChkNewPos(Pos, true);
		}
		else
		{
			Pos -= this.size();
			Pos = ChkNewPos(Pos);
		}
		return Pos;
	}

	private int ChkNewPos(int Pos)
	{
		return this.ChkNewPos(Pos, false);
	}

	private int _MoveResultIndex;

	// / <summary>
	// / Gigbt die Position der letzten Move Methode zurück.
	// / </summary>
	// / <returns>Null-Basierender Index, des Ergebnisses der letzten Move Nethode</returns>
	// / <remarks>Bei den Methoden <see cref="MoveItemsLeft">[MoveItemsLeft]</see> und <see cref="MoveItemsRight">[MoveItemsRight]</see>
	// / wird die Eigenschaft <see cref="MoveResultIndex">[MoveResultIndex]</see> auf <b>-1</b> gesetzt, <b>da alle Items bewegt wurden.</b>
	// </remarks>
	public int MoveResultIndex()
	{
		return _MoveResultIndex;
	}

	public Iterator<T> reverseIterator()
	{
		final MoveableList<T> that = this;

		Iterator<T> iterator = new Iterator<T>()
		{
			int aktItem = that.size() - 1;

			@Override
			public boolean hasNext()
			{
				if (aktItem >= 0) return true;
				return false;
			}

			@Override
			public T next()
			{

				T ret = that.get(aktItem);
				aktItem--;
				return ret;
			}

			@Override
			public void remove()
			{

			}
		};

		return iterator;
	}

	@SuppressWarnings("unchecked")
	public MoveableList<T> clone()
	{
		return (MoveableList<T>) super.clone();
	}

}
