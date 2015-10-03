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
package CB_Core.DAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.LoggerFactory;

import CB_Core.DB.Database;
import CB_Core.Types.Trackable;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

public class TrackableDAO
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(TrackableDAO.class);

	private Trackable ReadFromCursor(CoreCursor reader)
	{
		try
		{
			Trackable trackable = new Trackable(reader);

			return trackable;
		}
		catch (Exception exc)
		{
			log.error("Read Trackable", "", exc);
			return null;
		}
	}

	public void WriteToDatabase(Trackable trackable)
	{
		Parameters args = createArgs(trackable);

		try
		{
			Database.FieldNotes.insert("Trackable", args);
		}
		catch (Exception exc)
		{
			log.error("Write Trackable", "", exc);

		}
	}

	public void UpdateDatabase(Trackable trackable)
	{
		Parameters args = createArgs(trackable);

		try
		{
			Database.FieldNotes.update("Trackable", args, "GcCode='" + trackable.getGcCode() + "'", null);
		}
		catch (Exception exc)
		{
			log.error("Ubdate Trackable", "", exc);

		}

	}

	private Parameters createArgs(Trackable trackable)
	{
		String stimestampCreated = "";
		String stimestampLastVisit = "";

		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			stimestampCreated = iso8601Format.format(trackable.getDateCreated());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			String lastVisit = trackable.getLastVisit();
			if (!lastVisit.isEmpty()) stimestampLastVisit = iso8601Format.format(lastVisit);
			else
				stimestampLastVisit = "";
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Parameters args = new Parameters();
		args.put("Archived", trackable.getArchived() ? 1 : 0);
		args.put("GcCode", trackable.getGcCode());
		args.put("CacheID", trackable.getCurrentGeocacheCode());
		args.put("CurrentGoal", trackable.getCurrentGoal());
		args.put("CurrentOwnerName", trackable.getCurrentOwner());
		args.put("DateCreated", stimestampCreated);
		args.put("Description", trackable.getDescription());
		args.put("IconUrl", trackable.getIconUrl());
		args.put("ImageUrl", trackable.getImageUrl());
		args.put("name", trackable.getName());
		args.put("OwnerName", trackable.getOwner());
		args.put("Url", trackable.getUrl());
		args.put("TypeName", trackable.getTypeName());
		args.put("Url", trackable.getUrl());
		args.put("LastVisit", stimestampLastVisit);
		args.put("Home", trackable.getHome());
		args.put("TravelDistance", trackable.getTravelDistance());
		return args;
	}

	public Trackable getFromDbByGcCode(String GcCode)
	{
		String where = "GcCode = \"" + GcCode + "\"";
		String query = "select Id ,Archived ,GcCode ,CacheId ,CurrentGoal ,CurrentOwnerName ,DateCreated ,Description ,IconUrl ,ImageUrl ,Name ,OwnerName ,Url,TypeName, Home,TravelDistance   from Trackable WHERE " + where;
		CoreCursor reader = Database.FieldNotes.rawQuery(query, null);

		try
		{
			if (reader != null && reader.getCount() > 0)
			{
				reader.moveToFirst();
				Trackable ret = ReadFromCursor(reader);

				reader.close();
				return ret;
			}
			else
			{
				if (reader != null) reader.close();
				return null;
			}
		}
		catch (Exception e)
		{
			if (reader != null) reader.close();
			e.printStackTrace();
			return null;
		}

	}

}
