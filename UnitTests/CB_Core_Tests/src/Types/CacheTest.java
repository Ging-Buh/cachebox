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
package Types;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Core.DAO.CacheDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Locator.CoordinateGPS;
import CB_Locator.Location;
import CB_Locator.Location.ProviderType;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Lists.CB_List;
import __Static.InitTestDBs;

public class CacheTest extends TestCase
{

	private static Cache mCache;
	private static final String BR = "\n";
	private static final String LONG_DESC = "<font face=\"tahoma\" size=\"3\" color=\"#330033\"><br />"
			+ BR
			+ "<br />"
			+ BR
			+ "T5 Klettercache<br />"
			+ BR
			+ "Ihr benötigt ein 30 m Seil und Eurer Klettergeraffel<br />"
			+ BR
			+ "Bigshot wäre von Vorteil!<br />"
			+ BR
			+ "BITTE NUR KLETTERN, wenn Klettererfahrungen und geeignetes Wissen"
			+ BR
			+ "vorhanden sind!! Klettern natürlich auf eigene Gefahr!<br />"
			+ BR
			+ "BITTE:<br />"
			+ BR
			+ "NICHT alleine Klettern!! Denkt daran, auch ein Fall aus wenigen"
			+ BR
			+ "Metern Höhe kann böse enden!!<br /></font><br /><hr /><br /><br />"
			+ BR
			+ "<center><img src="
			+ BR
			+ "\"http://img.geocaching.com/cache/9b0334c7-c419-41c8-b883-8bb0adf20ac3.jpg\" /><br />"
			+ BR
			+ ""
			+ BR
			+ "<br />"
			+ BR
			+ "<font face=\"tahoma\" size=\"3\" color=\"#330033\"><br />"
			+ BR
			+ "<br />"
			+ BR
			+ "Der Hampir, so sagt man, optisch ein liebes zartes Wesen<br />"
			+ BR
			+ "im dunklen Hardtwald treibt er seine Spesen.<br />"
			+ BR
			+ "So süß, so flauschig sogleich<br />"
			+ BR
			+ "auch sein Fell so samtig und weich!<br />"
			+ BR
			+ "Deshalb lass dich blos nicht blenden,<br />"
			+ BR
			+ "sonst könnte es sehr böse für dich enden!<br />"
			+ BR
			+ "<br />"
			+ BR
			+ "Aaaaaber wenn du ihn entdeckst,<br />"
			+ BR
			+ "so achte dich vor ihm, die Gefahr besteht dass du vergisst<br />"
			+ BR
			+ "und vor lauter Kummer und Sorgen ihm tief in die Augen"
			+ BR
			+ "erblickst!!<br />"
			+ BR
			+ "<br />"
			+ BR
			+ "Es ist dann zu spät!<br />"
			+ BR
			+ "Dann hat dich der Hampir bereits erspäht!!<br />"
			+ BR
			+ "Der Hampir, so sagt man erschallt sein Gelächter<br />"
			+ BR
			+ "wenn es Beute vor sich hat, so schaurig so grell,<br />"
			+ BR
			+ "rette dich wenn du kannst schneller als schnell!<br />"
			+ BR
			+ "<br />"
			+ BR
			+ "Und wage dich nicht in den Wald<br />"
			+ BR
			+ "in der Nacht beim Vollmond ist es dort bitterkalt!<br />"
			+ BR
			+ "Nebelschwaden dort, aber die schaurige Gestalten<br />"
			+ BR
			+ "verstecken sich im dunkeln mit dem Gedanken,<br />"
			+ BR
			+ "ihre Beute noch schneller zu jagen als der Hampir!<br />"
			+ BR
			+ "Dennoch willst du in den Wald?! Überlege es dir!!<br />"
			+ BR
			+ "<br />"
			+ BR
			+ "Du meinst, ach was... Hampire... die gibt es doch nicht?!<br />"
			+ BR
			+ "Die Hasen die warnen: HIER wartet er auf dich!!!<br />"
			+ BR
			+ "<br /></font></center>"
			+ BR
			+ "<font face=\"tahoma\" size=\"3\" color=\"#330033\"><br />"
			+ BR
			+ "<br />"
			+ BR
			+ "Fotos dürft Ihr gerne machen <img src="
			+ BR
			+ "'http://www.geocaching.com/images/icons/icon_smile_big.gif' border="
			+ BR
			+ "\"0\" align=\"middle\" /><br />"
			+ BR
			+ "<br />"
			+ BR
			+ "<br />"
			+ BR
			+ "ein besonderer Dank an Monas Cacherteam, für die handwerkliche"
			+ BR
			+ "Meisterleistung!!<br />"
			+ BR
			+ "Es ist genau so geworden, wie es sich die Hasen vorgestellt"
			+ BR
			+ "haben!!<br />"
			+ BR
			+ "<br /></font><br />"
			+ BR
			+ "<a href=\"http://www.andyhoppe.com/\" title="
			+ BR
			+ "\"Counter/Zähler\"><img src=\"http://c.andyhoppe.com/1302990447\""
			+ BR
			+ "style=\"border:none\" alt=\"Counter/Zähler\" /></a><p>Additional Hidden Waypoints</p>PK2T9RW - GC2T9RW Parking<br />N 49° 21.077 E 008° 37.840<br />Raststätte Hardtwald West."
			+ BR + "Und für Ortskundige: einfach Richtung ADAC Übungsgelände. Dann müsst Ihr nicht auf die Autobahn.<br />";

	@Override
	public void setUp() throws Exception
	{

		super.setUp();
		mCache = new Cache(false);
	}

	@Override
	protected void tearDown() throws Exception
	{

		super.tearDown();
		mCache = null;
	}

	@Test
	public static void testConstructor()
	{
		assertTrue("Objekt muss konstruierbar sein", mCache != null);
	}

	@Test
	public static void testDistance()
	{
		CoordinateGPS coordinate1 = new CoordinateGPS(49.428333, 6.203333);
		assertTrue("Objekt muss konstruierbar sein", coordinate1 != null);

		// Vorsicht - hier wird nur die Objekt-Referenz gesetzt...
		mCache.Pos = coordinate1;

		// Initial Locator
		new CB_Locator.Locator(Location.NULL_LOCATION);

		// Set Location
		CB_Locator.Locator.setNewLocation(new CB_Locator.Location(49.427700, 6.204300, 100, false, 0, false, 0, 0, ProviderType.GPS));

		// Distanzberechnung
		float distance = mCache.Distance(CalculationType.ACCURATE, true);
		assertTrue("Entfernung muss 100m sein", (distance > 99.38390) && (distance < 99.38392));

		distance = mCache.Distance(CalculationType.FAST, true);
		assertTrue("Entfernung muss 100m sein", (distance > 99) && (distance < 101));

	}

	@Test
	public static void test_assertCache_GC2T9RW_with_details(boolean withDescription) throws ClassNotFoundException
	{
		InitTestDBs.InitalConfig();

		// initialize Database
		String database = "./testdata/test.db3";
		InitTestDBs.InitTestDB(database);

		CacheDAO cacheDAO = new CacheDAO();

		Cache cache = cacheDAO.getFromDbByGcCode("GC2T9RW", true, withDescription);

		assertTrue("Cache muss zurückgegeben werden", cache != null);

		assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 49.349817);
		assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 8.62925);
		assertTrue("Pos ist ungültig", cache.Pos.isValid());

		assertEquals("GcCode falsch", "GC2T9RW", cache.getGcCode());
		assertEquals("DateHidden falsch", "Sat Apr 16 07:00:00 CEST 2011", cache.getDateHidden().toString());
		assertEquals("url falsch", "http://www.geocaching.com/seek/cache_details.aspx?guid=f26f18bd-9aaa-4499-944b-3e8cb62e41a7",
				cache.getUrl());
		assertTrue("Found ist falsch", cache.isFound());

		assertEquals("Id ist falsch", cache.getGcId(), "2190117");
		assertTrue("ist available ist falsch", cache.isAvailable());
		assertFalse("ist archived ist falsch", cache.isArchived());
		assertEquals("Name falsch", "der Hampir - T5 -", cache.getName());
		assertEquals("Placed by falsch", "Team Rabbits", cache.getPlacedBy());
		assertEquals("Owner falsch", "Team Rabbits", cache.getOwner());
		assertTrue("Typ ist falsch", cache.Type == CacheTypes.Traditional);
		assertTrue("Size ist falsch", cache.Size == CacheSizes.small);
		assertTrue("Difficulty ist falsch", cache.Difficulty == 2);
		assertTrue("Terrain ist falsch", cache.Terrain == 5);

		assertTrue("Attribut falsch", cache.isAttributePositiveSet(Attributes.Bicycles));
		assertFalse("Attribut falsch", cache.isAttributeNegativeSet(Attributes.Bicycles));
		assertFalse("Attribut falsch", cache.isAttributePositiveSet(Attributes.Boat));
		// Attribute Tests

		ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
		ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();

		PositvieList.add(Attributes.Bicycles);
		PositvieList.add(Attributes.Dogs);
		PositvieList.add(Attributes.Available_at_all_times);
		PositvieList.add(Attributes.Public_restrooms_nearby);
		PositvieList.add(Attributes.Parking_available);
		PositvieList.add(Attributes.Fuel_Nearby);
		PositvieList.add(Attributes.Short_hike);
		PositvieList.add(Attributes.Climbing_gear);
		PositvieList.add(Attributes.Ticks);
		PositvieList.add(Attributes.Hunting);

		Iterator<Attributes> positiveInterator = PositvieList.iterator();
		Iterator<Attributes> negativeInterator = NegativeList.iterator();

		while (positiveInterator.hasNext())
		{
			Attributes attr = (Attributes) positiveInterator.next();
			assertTrue(attr.toString() + " Attribut falsch", cache.isAttributePositiveSet(attr));
		}

		while (negativeInterator.hasNext())
		{
			Attributes attr = (Attributes) negativeInterator.next();
			assertTrue(attr.toString() + " Attribut falsch", cache.isAttributeNegativeSet(attr));
		}

		// fülle eine Liste mit allen Attributen
		ArrayList<Attributes> attributes = new ArrayList<Attributes>();
		Attributes[] tmp = Attributes.values();
		for (Attributes item : tmp)
		{
			attributes.add(item);
		}

		// Lösche die vergebenen Atribute aus der Kommplett Liste
		positiveInterator = PositvieList.iterator();
		negativeInterator = NegativeList.iterator();

		while (positiveInterator.hasNext())
		{
			attributes.remove(positiveInterator.next());
		}

		while (negativeInterator.hasNext())
		{
			attributes.remove(negativeInterator.next());
		}

		attributes.remove(Attributes.getAttributeEnumByGcComId(64));
		attributes.remove(Attributes.getAttributeEnumByGcComId(65));
		attributes.remove(Attributes.getAttributeEnumByGcComId(66));

		// Teste ob die Übrig gebliebenen Atributte auch nicht vergeben wurden.
		Iterator<Attributes> RestInterator = attributes.iterator();

		while (RestInterator.hasNext())
		{
			Attributes attr = (Attributes) RestInterator.next();
			assertFalse(attr.toString() + " Attribut falsch", cache.isAttributePositiveSet(attr));
			assertFalse(attr.toString() + " Attribut falsch", cache.isAttributeNegativeSet(attr));
		}

		assertEquals("Hint falsch", "wenn du ihn nicht findest, findet er dich!!", cache.getHint());

		CB_List<LogEntry> logs = new CB_List<LogEntry>();
		logs = Database.Logs(cache);

		LogEntry log = logs.get(0);

		assertEquals("CacheId ist falsch", log.CacheId, 24578729153020743L);
		assertEquals("Id ist falsch", log.Id, 170855167);
		assertEquals("Timestamp falsch", "Mon Jul 04 19:00:00 CEST 2011", log.Timestamp.toString());
		assertEquals("Finder falsch", "SaarFuchs", log.Finder);
		assertTrue("LogTyp falsch", log.Type == LogTypes.found);

		// TODO Beschreibungstexte überprüfen

		assertEquals("shortDescription must be NULL", null, cache.getShortDescription());
		if (!withDescription)
		{
			assertEquals("longDescription must be NULL", null, cache.getLongDescription());
		}
		else
		{
			assertEquals("longDescription must equals", LONG_DESC, cache.getLongDescription());
		}

		// assertEquals("Long description is wrong", LongDesc, cache.getLongDescription());

		// System.out.println( log.Comment );

		// Database.Data.Close();
	}

	@Test
	public static void test_assertCache_GC2T9RW_without_details() throws ClassNotFoundException
	{
		InitTestDBs.InitalConfig();

		// initialize Database
		String database = "./testdata/test.db3";
		InitTestDBs.InitTestDB(database);

		CacheDAO cacheDAO = new CacheDAO();

		Cache cache = cacheDAO.getFromDbByGcCode("GC2T9RW", false, false);

		assertTrue("Cache muss zurückgegeben werden", cache != null);

		assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 49.349817);
		assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 8.62925);
		assertTrue("Pos ist ungültig", cache.Pos.isValid());

		assertEquals("GcCode falsch", "GC2T9RW", cache.getGcCode());
		// assertEquals("DateHidden falsch", "Sat Apr 16 07:00:00 CEST 2011", cache.getDateHidden().toString());
		assertEquals("url must be EMPTY_STRING", Cache.EMPTY_STRING, cache.getUrl());
		assertTrue("Found ist falsch", cache.isFound());

		assertEquals("Id must be EMPTY_STRING", cache.getGcId(), Cache.EMPTY_STRING);
		assertTrue("ist available ist falsch", cache.isAvailable());
		assertFalse("ist archived ist falsch", cache.isArchived());
		assertEquals("Name falsch", "der Hampir - T5 -", cache.getName());
		assertEquals("Placed by must be EMPTY_STRING", Cache.EMPTY_STRING, cache.getPlacedBy());
		assertEquals("Owner falsch", "Team Rabbits", cache.getOwner());
		assertTrue("Typ ist falsch", cache.Type == CacheTypes.Traditional);
		assertTrue("Size ist falsch", cache.Size == CacheSizes.small);
		assertTrue("Difficulty ist falsch", cache.Difficulty == 2);
		assertTrue("Terrain ist falsch", cache.Terrain == 5);

		// fülle eine Liste mit allen Attributen
		ArrayList<Attributes> attributes = new ArrayList<Attributes>();
		Attributes[] tmp = Attributes.values();
		for (Attributes item : tmp)
		{
			attributes.add(item);
		}

		// Teste ob die Übrig gebliebenen Atributte auch nicht vergeben wurden.
		Iterator<Attributes> RestInterator = attributes.iterator();

		while (RestInterator.hasNext())
		{
			Attributes attr = (Attributes) RestInterator.next();
			assertFalse(attr.toString() + " Attribut falsch", cache.isAttributePositiveSet(attr));
			assertFalse(attr.toString() + " Attribut falsch", cache.isAttributeNegativeSet(attr));
		}

		assertEquals("shortDescription must be EMPTY_STRING", Cache.EMPTY_STRING, cache.getShortDescription());
		assertEquals("longDescription must be EMPTY_STRING", Cache.EMPTY_STRING, cache.getLongDescription());

		assertEquals("Hint must be EMPTY_STRING", Cache.EMPTY_STRING, cache.getHint());

	}

}
