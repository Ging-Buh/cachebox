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
package CB_Core.Types;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import CB_Core.DB.Database;
import CB_Core.Enums.LogTypes;
import CB_Core.Settings.CB_Core_Settings;
import CB_Utils.DB.CoreCursor;
import CB_Utils.Util.iChanged;

public class FieldNoteList extends ArrayList<FieldNoteEntry>
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(FieldNoteList.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum LoadingType
	{
		Loadall, LoadNew, loadMore, loadNewLastLength
	}

	private boolean cropedList = false;
	private int actCropedLength = -1;

	public FieldNoteList()
	{
		CB_Core_Settings.FieldNotesLoadAll.addChangedEventListner(settingsChangedListner);
		CB_Core_Settings.FieldNotesLoadLength.addChangedEventListner(settingsChangedListner);
	}

	iChanged settingsChangedListner = new iChanged()
	{

		@Override
		public void isChanged()
		{
			synchronized (FieldNoteList.this)
			{
				FieldNoteList.this.clear();
				cropedList = false;
				actCropedLength = -1;
			}
		}
	};

	public boolean isCropped()
	{
		return cropedList;
	}

	public void LoadFieldNotes(String where, LoadingType loadingType)
	{
		synchronized (this)
		{
			LoadFieldNotes(where, "", loadingType);
		}
	}

	public void LoadFieldNotes(String where, String order, LoadingType loadingType)
	{
		synchronized (this)
		{
			// List clear?
			if (loadingType == LoadingType.Loadall || loadingType == LoadingType.LoadNew || loadingType == LoadingType.loadNewLastLength)
			{
				this.clear();
			}

			String sql = "select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog from FieldNotes";
			if (!where.equals(""))
			{
				sql += " where " + where;
			}
			if (order == "")
			{
				sql += " order by FoundNumber DESC, Timestamp DESC";
			}
			else
			{
				sql += " order by " + order;
			}

			// SQLite Limit ?
			boolean maybeCropped = !CB_Core_Settings.FieldNotesLoadAll.getValue() && loadingType != LoadingType.Loadall;

			if (maybeCropped)
			{
				switch (loadingType)
				{
				case Loadall:
					// do nothing
					break;
				case LoadNew:
					actCropedLength = CB_Core_Settings.FieldNotesLoadLength.getValue();
					sql += " LIMIT " + String.valueOf(actCropedLength + 1);
					break;
				case loadNewLastLength:
					if (actCropedLength == -1) actCropedLength = CB_Core_Settings.FieldNotesLoadLength.getValue();
					sql += " LIMIT " + String.valueOf(actCropedLength + 1);
					break;
				case loadMore:
					int Offset = actCropedLength;
					actCropedLength += CB_Core_Settings.FieldNotesLoadLength.getValue();
					sql += " LIMIT " + String.valueOf(CB_Core_Settings.FieldNotesLoadLength.getValue() + 1);
					sql += " OFFSET " + String.valueOf(Offset);
				}
			}

			CoreCursor reader = null;
			try
			{
				reader = Database.FieldNotes.rawQuery(sql, null);
			}
			catch (Exception exc)
			{
				log.error("FieldNoteList", "LoadFieldNotes", exc);
			}
			reader.moveToFirst();
			while (reader.isAfterLast() == false)
			{
				FieldNoteEntry fne = new FieldNoteEntry(reader);
				if (!this.contains(fne))
				{
					this.add(fne);
				}

				reader.moveToNext();
			}
			reader.close();

			// check Cropped
			if (maybeCropped)
			{
				if (this.size() > actCropedLength)
				{
					cropedList = true;
					// remove last item
					this.remove(this.size() - 1);
				}
				else
				{
					cropedList = false;
				}
			}
		}
	}

	/**
	 * @param dirFileName
	 *            Config.settings.FieldNotesGarminPath.getValue()
	 */
	public static void CreateVisitsTxt(String dirFileName)
	{
		FieldNoteList lFieldNotes = new FieldNoteList();
		lFieldNotes.LoadFieldNotes("", "Timestamp ASC", LoadingType.Loadall);

		File txtFile = new File(dirFileName);
		FileWriter writer;
		try
		{
			writer = new FileWriter(txtFile);

			for (FieldNoteEntry fieldNote : lFieldNotes)
			{
				String log = fieldNote.gcCode + "," + fieldNote.GetDateTimeString() + "," + fieldNote.type.toString() + ",\"" + fieldNote.comment + "\"\n";
				writer.write(log + "\n");

			}
			writer.flush();
			writer.close();
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}
		;
	}

	public void DeleteFieldNoteByCacheId(long cacheId, LogTypes type)
	{
		synchronized (this)
		{
			int foundNumber = 0;
			FieldNoteEntry fne = null;
			// l�scht eine evtl. vorhandene FieldNote vom type f�r den Cache cacheId
			for (FieldNoteEntry fn : this)
			{
				if ((fn.CacheId == cacheId) && (fn.type == type))
				{
					fne = fn;
				}
			}
			if (fne != null)
			{
				if (fne.type == LogTypes.found) foundNumber = fne.foundNumber;
				this.remove(fne);
				fne.DeleteFromDatabase();
			}
			decreaseFoundNumber(foundNumber);
		}
	}

	public void DeleteFieldNote(long id, LogTypes type)
	{
		synchronized (this)
		{
			int foundNumber = 0;
			FieldNoteEntry fne = null;
			// l�scht eine evtl. vorhandene FieldNote vom type f�r den Cache cacheId
			for (FieldNoteEntry fn : this)
			{
				if (fn.Id == id)
				{
					fne = fn;
				}
			}
			if (fne != null)
			{
				if (fne.type == LogTypes.found) foundNumber = fne.foundNumber;
				this.remove(fne);
				fne.DeleteFromDatabase();
			}
			decreaseFoundNumber(foundNumber);
		}
	}

	public void decreaseFoundNumber(int deletedFoundNumber)
	{
		if (deletedFoundNumber > 0)
		{
			// alle FoundNumbers anpassen, die gr��er sind
			for (FieldNoteEntry fn : this)
			{
				if ((fn.type == LogTypes.found) && (fn.foundNumber > deletedFoundNumber))
				{
					int oldFoundNumber = fn.foundNumber;
					fn.foundNumber--;
					String s = fn.comment;
					s = fn.comment.replaceAll("#" + oldFoundNumber, "#" + fn.foundNumber);
					fn.comment = s;
					fn.fillType();
					fn.UpdateDatabase();
				}
			}
		}
	}

	public boolean contains(FieldNoteEntry fne)
	{
		synchronized (this)
		{
			for (FieldNoteEntry item : this)
			{
				if (fne.equals(item)) return true;
			}
			return false;
		}
	}
}
