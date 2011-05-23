package de.droidcachebox.Geocaching;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Map.Layer;
import de.droidcachebox.Views.MapView;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.text.StaticLayout;
import android.text.Layout.Alignment;
import android.text.TextPaint;


public class Cache implements Comparable<Cache> {
    public enum CacheTypes
    {
        Traditional, // = 0,
        Multi, // = 1,
        Mystery, // = 2,
        Camera, // = 3,
        Earth, // = 4,
        Event, // = 5,
        MegaEvent, // = 6,
        CITO, // = 7,
        Virtual, // = 8,
        Letterbox, // = 9,
        Wherigo, // = 10,
        ReferencePoint, // = 11,
        Wikipedia, // = 12,
        Undefined, // = 13,
        MultiStage, // = 14,
        MultiQuestion, // = 15,
        Trailhead, // = 16,
        ParkingArea, // = 17,
        Final // = 18,
    };
	
    @Override
    public int compareTo(Cache c2) {
    	float dist1 = this.CachedDistance();
    	float dist2 = c2.CachedDistance();
        return (dist1 < dist2 ? -1 : (dist1 == dist2 ? 0 : 1));
    }
    
    // Koordinaten des Caches auf der Karte gelten in diesem Zoom
    public static final int MapZoomLevel = 18;
    /// Koordinaten des Caches auf der Karte
    public double MapX;
    /// Koordinaten des Caches auf der Karte
    public double MapY;
    /// Id des Caches bei geocaching.com. Wird zumm Loggen benötigt und von
    /// geotoad nicht exportiert
    public String GcId;
    /// Id des Caches in der Datenbank von geocaching.com
    public long Id;
    /// Waypoint Code des Caches
    public String GcCode;
    /// Name des Caches
    public String Name;
    public Coordinate Coordinate = new Coordinate(); 
    /// Breitengrad
    public double Latitude() { return Coordinate.Latitude; }
    /// Längengrad
    public double Longitude() { return Coordinate.Longitude; }
    /// Durchschnittliche Bewertung des Caches von GcVote
    public float Rating;
    /// Größe des Caches. Bei Wikipediaeinträgen enthält dieses Feld den Radius in m
    public int Size;
    /// Schwierigkeit des Caches
    public float Difficulty;
    /// Geländebewertung
    public float Terrain;
    /// Wurde der Cache archiviert?
    public boolean Archived;
    /// Ist der Cache derzeit auffindbar?
    public boolean Available;
    /// Ist der Cache einer der Favoriten
    protected boolean favorit;
    
    public boolean Favorit()
    {
    	return favorit;
    }
    
    public void Favorit(boolean value)
    {
		favorit = value;
/*		SqlCeCommand command = new SqlCeCommand("update Caches set Favorit=@favorit where Id=@id", Database.Data.Connection);
		command.Parameters.Add("@favorit", DbType.Boolean).Value = value;
		command.Parameters.Add("@id", DbType.Int64).Value = Id;
		command.ExecuteNonQuery();
		command.Dispose();*/
    }
    /// hat der Cache Clues oder Notizen erfasst
    protected boolean hasUserData;
/*    public bool HasUserData
    {
        get
        {
            return hasUserData;
        }
        set
        {
            hasUserData = value;
            SqlCeCommand command = new SqlCeCommand("update Caches set HasUserData=@hasUserData where Id=@id", Database.Data.Connection);
            command.Parameters.Add("@hasUserData", DbType.Boolean).Value = value;
            command.Parameters.Add("@id", DbType.Int64).Value = Id;
            command.ExecuteNonQuery();
            command.Dispose();
        }
    }*/
    
    public boolean CorrectedCoordinates;
    
    /// <summary>
    ///  wenn ein Wegpunkt "Final" existiert, ist das mystery-Rätsel gelöst.
    /// </summary>
    public boolean MysterySolved()
    {
        if (this.CorrectedCoordinates)
          return true;

        if (this.Type != CacheTypes.Mystery)
          return false;

        boolean x;
        x = false;

        ArrayList<Waypoint> wps = waypoints;
        for (Waypoint wp : wps)
        {
          if (wp.Type == CacheTypes.Final)
          {
            x = true;
          }
        };
        return x;
    }
    
    ///  true, if a this mystery cache has a final waypoint
    public boolean HasFinalWaypoint() { return GetFinalWaypoint() != null; }

    ///  search the final waypoint for a mystery cache
    public Waypoint GetFinalWaypoint()
    {
        if (this.Type != CacheTypes.Mystery)
            return null;

        for (Waypoint wp : waypoints)
        {
            if (wp.Type == CacheTypes.Final)
            {
                return wp;
            }
        };

        return null;
    }

    protected boolean found;

    /// Wurde der Cache bereits gefunden?
    public boolean Found()
    {
    	return found;
    }
    public void Found(boolean value)
    {
        found = value;
        
        ContentValues args = new ContentValues();
        args.put("found", value);
        Database.Data.myDB.update("Caches", args, "Id=" + Id, null);
/*        SqlCeCommand command = new SqlCeCommand("update Caches set Found=@found where Id=@id", Database.Data.Connection);
        command.Parameters.Add("@found", DbType.Boolean).Value = value;
        command.Parameters.Add("@id", DbType.Int64).Value = Id;
        command.ExecuteNonQuery();
        command.Dispose();
        Replication.FoundChanged(Id, found);*/
    }

/*
        public int Vote
        {
            get
            {
                SqlCeCommand command = new SqlCeCommand("select Vote from Caches where Id=@id", Database.Data.Connection);
                command.Parameters.Add("@id", DbType.Int64).Value = Id;
                String resultString = command.ExecuteScalar().ToString();
                int result = int.Parse(resultString);
                command.Dispose();
                return result;
            }

            set
            {
                SqlCeCommand command = new SqlCeCommand("update Caches set Vote=@vote, VotePending=@votepending where Id=@id", Database.Data.Connection);
                command.Parameters.Add("@vote", DbType.Int16).Value = (short)value;
                command.Parameters.Add("@votepending", DbType.Boolean).Value = true;
                command.Parameters.Add("@id", DbType.Int64).Value = Id;
                command.ExecuteNonQuery();
                command.Dispose();
            }
        }
*/
        private int noteCheckSum = 0;   // for Replication
        public String GetNote()
        {
        	String resultString = "";
            Cursor c = Database.Data.myDB.rawQuery("select Notes from Caches where Id=?", new String[] { String.valueOf(Id) });
            c.moveToFirst();
            while(c.isAfterLast() == false)
            {
                resultString = c.getString(0);
                break;
            };
            noteCheckSum = (int)Global.sdbm(resultString);
            return resultString;
        }
        
        public void SetNote(String value)
        {
            int newNoteCheckSum = (int)Global.sdbm(value);
            
//        	Replication.NoteChanged(this.Id, noteCheckSum, newNoteCheckSum);
          if (newNoteCheckSum != noteCheckSum)
          {
              ContentValues args = new ContentValues();
              args.put("Notes", value);
              args.put("HasUserData", true);
              
              Database.Data.myDB.update("Caches", args, "id=" + Id, null);
              noteCheckSum = newNoteCheckSum;
          }
        }

        private int solverCheckSum = 0;   // for Replication
        public String GetSolver()
        {
        	String resultString = "";
            Cursor c = Database.Data.myDB.rawQuery("select Solver from Caches where Id=?", new String[] { String.valueOf(Id) });
            c.moveToFirst();
            while(c.isAfterLast() == false)
            {
                resultString = c.getString(0);
                break;
            };
            noteCheckSum = (int)Global.sdbm(resultString);
            return resultString;
        }
        public void SetSolver(String value)
        {
            int newSolverCheckSum = (int)Global.sdbm(value);
            
//            Replication.SolverChanged(this.Id, solverCheckSum, newSolverCheckSum);
            if (newSolverCheckSum != solverCheckSum)
            {
                ContentValues args = new ContentValues();
                args.put("Solver", value);
                args.put("HasUserData", true);
                
                Database.Data.myDB.update("Caches", args, "id=" + Id, null);
                solverCheckSum = newSolverCheckSum;
            }
        }
        

 
    // Name der Tour, wenn die GPX-Datei aus GCTour importiert wurde
    public String TourName;

    // Name der GPX-Datei aus der importiert wurde
    public int GPXFilename_ID;

    /// <summary>
    /// Art des Caches
    /// </summary>
    public CacheTypes Type;

    /// <summary>
    /// Erschaffer des Caches
    /// </summary>
    public String PlacedBy;

    /// <summary>
    /// Verantwortlicher
    /// </summary>
    public String Owner;

    /// <summary>
    /// Datum, an dem der Cache versteckt wurde
    /// </summary>
    public Date DateHidden;

    /// <summary>
    /// URL des Caches
    /// </summary>
    public String Url;
    
    public ArrayList<LogEntry> Logs()
    {
        ArrayList<LogEntry> result = new ArrayList<LogEntry>();

        Cursor reader = Database.Data.myDB.rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId=@cacheid order by Timestamp desc", new String[] { Long.toString(this.Id) });

    	reader.moveToFirst();
        while(reader.isAfterLast() == false)
        {
        	LogEntry logent = new LogEntry(reader, true); 
            result.add(logent);
            reader.moveToNext();
        }
        reader.close();
        /*
        System.Windows.Forms.Cursor.Current = System.Windows.Forms.Cursors.WaitCursor;

        SqlCeCommand command = new SqlCeCommand("select CacheId, Timestamp, Finder, Type, Comment from Logs where CacheId=@cacheid order by Timestamp desc", Database.Data.Connection);
        command.Parameters.Add("@cacheid", DbType.Int64).Value = this.Id;
        SqlCeDataReader reader = command.ExecuteReader();

        while (reader.Read())
            result.Add(new LogEntry(reader, true));

        reader.Dispose();
        command.Dispose();

        System.Windows.Forms.Cursor.Current = System.Windows.Forms.Cursors.Default;
*/
        return result;
    }


    /// Entfernung von der letzten gültigen Position
    public float Distance()
    {
        Coordinate fromPos = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
    	Waypoint waypoint = this.GetFinalWaypoint();
        // Wenn ein Mystery-Cache einen Final-Waypoint hat, soll die Diszanzberechnung vom Final aus gemacht werden
        // If a mystery has a final waypoint, the distance will be calculated to the final not the the cache coordinates
    	Coordinate toPos = Coordinate;
        if (waypoint != null)
        	toPos = new Coordinate(waypoint.Coordinate.Latitude, waypoint.Coordinate.Longitude);
        float[] dist = new float[4];
        Location.distanceBetween(fromPos.Latitude, fromPos.Longitude, toPos.Latitude, toPos.Longitude, dist);
        cachedDistance = dist[0];
        return (float)cachedDistance;
    }

    /// <summary>
    /// Falls keine erneute Distanzberechnung nötig ist nehmen wir diese Distanz
    /// </summary>
    protected float cachedDistance = 0;
    public float CachedDistance()
    {
        if (cachedDistance != 0)
            return cachedDistance;
        else
            return Distance();
    }

    /// <summary>
    /// Anzahl der Travelbugs und Coins, die sich in diesem Cache befinden
    /// </summary>
    public int NumTravelbugs;

    public String GetDescription()
    {
    	String description = "";
        Cursor reader = Database.Data.myDB.rawQuery("select Description from Caches where Id=?", new String[] { Long.toString(this.Id) } );
    	reader.moveToFirst();
        while(reader.isAfterLast() == false)
        {
        	description = reader.getString(0);
            reader.moveToNext();
        }
        reader.close();

        return description;
}

    protected String hint = "";
    public String Hint()
    {
    	if (hint.equals(""))
    	{
            Cursor reader = Database.Data.myDB.rawQuery("select Hint from Caches where Id=?", new String[] { Long.toString(this.Id) } );
        	reader.moveToFirst();
            while(reader.isAfterLast() == false)
            {
            	hint = reader.getString(0);
                reader.moveToNext();
            }
            reader.close();
    	}
    	return hint;
    }

    public ArrayList<Waypoint> waypoints = null;
/*
    public List<Waypoint> Waypoints
    {
        set
        {
            waypoints = value;
        }
        get
        {
            if (waypoints == null)
            {
                waypoints = new List<Waypoint>();

                SqlCeCommand command = new SqlCeCommand("select GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title from Waypoint where CacheId=@cacheid", Database.Data.Connection);
                command.Parameters.Add("@cacheid", DbType.Int64).Value = Id;
                SqlCeDataReader reader = command.ExecuteReader();

                while (reader.Read())
                    waypoints.Add(new Waypoint(reader));

                reader.Dispose();
                command.Dispose();
            }

            return waypoints;
        }
    }
*/
    protected boolean listingChanged;
 /*
    public bool ListingChanged
    {
        get
        {
            return listingChanged;
        }

        set
        {
            listingChanged = value;
            SqlCeCommand command = new SqlCeCommand("update Caches set ListingChanged=@ListingChanged where Id=@id", Database.Data.Connection);
            command.Parameters.Add("@ListingChanged", DbType.Boolean).Value = value;
            command.Parameters.Add("@id", DbType.Int64).Value = Id;
            command.ExecuteNonQuery();
            command.Dispose();

        }
    }

*/

/*
    public enum Attributes: ulong
    {
        Dogs = 1UL << 1,
        Fee = 1UL << 2,
        ClimbingGear = 1UL << 3,
        Boat = 1UL << 4,
        Scuba = 1UL << 5,
        Kids = 1UL << 6,
        TakesLess = 1UL << 7,
        ScenicView = 1UL << 8,
        SignificantHike = 1UL << 9,
        Climbing = 1UL << 10,
        Wading = 1UL << 11,
        Swimming = 1UL << 12,
        Anytime = 1UL << 13,
        Night = 1UL << 14,
        Winter = 1UL << 15,
        PoisonPlants = 1UL << 16,
        Snakes = 1UL << 17,
        Ticks = 1UL << 18,
        AbandonedMines = 1UL << 19,
        Cliff = 1UL << 20,
        Hunting = 1UL << 21,
        Dangerous = 1UL << 22,
        WheelchairAccessible = 1UL << 23,
        Parking = 1UL << 24,
        PublicTransportation = 1UL << 25,
        Drinking = 1UL << 26,
        Restrooms = 1UL << 27,
        Telephone = 1UL << 28,
        Picnic = 1UL << 29,
        Camping = 1UL << 30,
        Bicycles = 1UL << 31,
        Motorcycles = 1UL << 32,
        Quads = 1UL << 33,
        Offroad = 1UL << 34,
        Snowmobiles = 1UL << 35,
        Horses = 1UL << 36,
        Campfires = 1UL << 37,
        Thorns = 1UL << 38,
        Stealth = 1UL << 39,
        Stroller = 1UL << 40,
        NeedsMaintenance = 1UL << 41,
        Livestock = 1UL << 42,
        Flashlight = 1UL << 43,
        TruckDriver = 1UL << 44,
        FieldPuzzle = 1UL << 45,
        UVLight = 1UL << 46,
        Snowshoes = 1UL << 47,
        CrossCountrySkiis = 1UL << 48,
        SpecialTool = 1UL << 49,
        NightCache = 1UL << 50,
        ParkAndGrab = 1UL << 51,
        AbandonedStructure = 1UL << 52,
        ShortHike = 1UL << 53,
        MediumHike = 1UL << 54,
        LongHike = 1UL << 55,
        FuelNearby = 1UL << 56,
        FoodNearby = 1UL << 57,

        Default = 0,
    }

    protected ulong attributesPositive = 0;
    public ulong AttributesPositive
    {
        get
        {
            if (attributesPositive == 0)
            {
                SqlCeCommand command = new SqlCeCommand("select AttributesPositive from Caches where Id=@id", Database.Data.Connection);
                command.Parameters.Add("@id", DbType.Int64).Value = this.Id;
                String data = command.ExecuteScalar().ToString();
                attributesPositive = (data.Length > 0) ? ulong.Parse(data) : 0;
                command.Dispose();
            }
            return attributesPositive;
        }
    }

    protected ulong attributesNegative = 0;
    public ulong AttributesNegative
    {
        get
        {
            if (attributesNegative == 0)
            {
                SqlCeCommand command = new SqlCeCommand("select AttributesNegative from Caches where Id=@id", Database.Data.Connection);
                command.Parameters.Add("@id", DbType.Int64).Value = this.Id;
                String data = command.ExecuteScalar().ToString();
                attributesNegative = (data.Length > 0)? ulong.Parse(data) : 0;
                command.Dispose();
            }
            return attributesNegative;
        }
    }
*/
    public Cache(Cursor reader)
    {
        Id = reader.getLong(0);
        GcCode = reader.getString(1).trim();
        Coordinate = new Coordinate(reader.getDouble(2), reader.getDouble(3));
        Name = reader.getString(4).trim();
        Size = reader.getInt(5);
        Difficulty = ((float)reader.getShort(6)) / 2;
        Terrain = ((float)reader.getShort(7)) / 2;
        Archived = reader.getInt(8) != 0;
        Available = reader.getInt(9) != 0;
        int ifound = reader.getInt(10);
        found = reader.getInt(10) != 0;
        Type = CacheTypes.values()[reader.getShort(11)];
        PlacedBy = reader.getString(12).trim();
        Owner = reader.getString(13).trim();
        
        String sDate = reader.getString(14);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
        	DateHidden = iso8601Format.parse(sDate);
        } catch (ParseException e) {
		}

        Url = reader.getString(15).trim();
        NumTravelbugs = reader.getInt(16);
        GcId = reader.getString(17).trim();
        Rating = ((float)reader.getShort(18)) / 100.0f;
        if (reader.getInt(19) > 0)
            favorit = true;
        else
            favorit = false;
        TourName = reader.getString(20).trim();

        if (reader.getString(21) != "")
            GPXFilename_ID = reader.getInt(21);
        else
            GPXFilename_ID = -1;

        if (reader.getInt(22) > 0)
            hasUserData = true;
        else
            hasUserData = false;

        if (reader.getInt(23) > 0)
            listingChanged = true;
        else
            listingChanged = false;

        if (reader.getInt(24) > 0)
            CorrectedCoordinates = true;
        else
            CorrectedCoordinates = false;

        MapX = 256.0 * Descriptor.LongitudeToTileX(MapZoomLevel, Longitude());
        MapY = 256.0 * Descriptor.LatitudeToTileY(MapZoomLevel, Latitude());
        
//        SpoilerExists();
    }
/*
    public override int GetHashCode()
    {
        return (int)Id;
    }

    public override bool Equals(object obj)
    {
        if (obj.GetType() != this.GetType())
            return false;

        return ((Cache)obj).Id == this.Id;
    }
*/

/*
    public static Cache GetCacheByCacheId(long cacheId)
    {
        foreach (Cache cache in Query)
        {
            if (cache.Id == cacheId)
                return cache;
        }
        return null;
    }
*/
/*
    public Waypoint GetWaypointByGcCode(string gcCode)
    {
        foreach (Waypoint wp in Waypoints)
            if (wp.GcCode == gcCode)
                return wp;

        return null;
    }

    /// <summary>
    /// Zum Sortieren von Caches nach Distanz
    /// </summary>
    /// <param name="obj">Cache, mit dem die Distanz verglichen werden soll</param>
    /// <returns>-1, falls obj näher ist als die Instanz, 1 falls sie weiter entfernt ist und sonst 0.</returns>
    public int CompareTo(object obj)
    {
        System.Diagnostics.Debug.Assert(obj is Cache, "Falscher Typ: " + obj.ToString() + " ist kein Cache!");

        double d1 = (obj as Cache).CachedDistance;

        if (d1 < CachedDistance)
            return 1;

        if (d1 > CachedDistance)
            return -1;

        return 0;
    }
*/
    ArrayList<String> spoilerRessources = null;
    public ArrayList<String> SpoilerRessources()
    {
        if (spoilerRessources == null)
        {
            ReloadSpoilerRessources();
        }

        return spoilerRessources;
    }
    public void SpoilerRessources(ArrayList<String> value)
    {
        spoilerRessources = value;
    }

    public void ReloadSpoilerRessources()
    {
        spoilerRessources = new ArrayList<String>();

        String path = Config.GetString("SpoilerFolder");
        String directory = path + "/" + GcCode.substring(0, 4);

        if (!Global.DirectoryExists(directory))
            return;

        
        File dir = new File(directory);
        FilenameFilter filter = new FilenameFilter() {			
			@Override
			public boolean accept(File dir, String filename) {
				
				filename = filename.toLowerCase();
				if (filename.indexOf(GcCode.toLowerCase()) == 0)
				{
		            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".png") || filename.endsWith(".gif"))
		            	return true;
				}
				return false;
			}
		};
        String[] files = dir.list(filter);

        for (String image : files)
        {
        	spoilerRessources.add(directory + "/" + image);
        }

        // Add own taken photo
        directory = Config.GetString("UserImageFolder");

        if (!Global.DirectoryExists(directory))
            return;

        dir = new File(directory);
        filter = new FilenameFilter() {			
			@Override
			public boolean accept(File dir, String filename) {
				
				filename = filename.toLowerCase();
				if (filename.indexOf(GcCode.toLowerCase()) >= 0)
					return true;
				return false;
			}
		};
        files = dir.list(filter);
        if (!(files == null))
        {
	        if (files.length>0)
	        {
		        for (String file : files)
			        {
		        		String ext = Global.GetFileExtension(file);
		        		if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif"))
		        		{
		        			spoilerRessources.add(directory + "/" + file);
		        		}
			        }
	        }
        }
    }

    public boolean SpoilerExists()
    {
        return SpoilerRessources().size() > 0;
    }
    
    public int GetMapIconId(String gcLogin)
    {
    	if (this.Owner.equalsIgnoreCase(gcLogin) && (gcLogin.length() > 0))
    		return 20;
    	if (this.found)
    		return 19;
    	if ((Type == CacheTypes.Mystery) && this.MysterySolved())
    		return 21;
    	
    	return Type.ordinal();
    }
    
    
    
//Draw Metods
    public enum DrawStyle
    {
    	all,		// alle infos
    	withoutBearing,	//ohne Richtungs-Pfeil
    	withoutSeparator; //ohne unterster trennLinie 
    };
    
    private StaticLayout layoutCacheName;
    
    public void DrawInfo(Canvas canvas, int width,int height, int BackgroundColor, DrawStyle drawStyle)
    {
      	int x=0;
      	int y=0;
      	Rect DrawRect = new Rect(x,y,width,height);
      	DrawInfo( canvas, DrawRect, BackgroundColor,  drawStyle);
    }
    
    
    // Static Mesured Member
    private static final int iconSize = (int) (Global.scaledFontSize_normal * 3.5);
    private static int VoteWidth = 0;
    private static int rightBorder = 0;
    private static int nameLayoutWidth = 0;
    private static Paint DTPaint;
    private final static int space = (int) (Global.scaledFontSize_normal*0.7);
    private final static int tab = (int) (Global.scaledFontSize_normal*0.6);
    public static Rect BearingRec; 
    private static TextPaint namePaint;
    public void DrawInfo(Canvas canvas,Rect rec, int BackgroundColor, DrawStyle drawStyle)
    {
    	// init
    		Boolean notAvailable = (!this.Available && !this.Archived);
            Boolean Night = Config.GetBool("nightMode");
            Boolean GlobalSelected = this == Global.SelectedCache();
            final int halfCornerSize = Global.scaledFontSize_normal/2;
            final int left = rec.left + halfCornerSize;
            final int top = rec.top + halfCornerSize;
            final int width = rec.width() - halfCornerSize;
            final int height = rec.height() - halfCornerSize;
            final int SDTImageTop = (int) (height-(Global.scaledFontSize_normal/1.5));
            final int SDTLineTop = SDTImageTop + Global.scaledFontSize_normal;
            	
    	// Mesure
    		if (VoteWidth == 0) // Grössen noch nicht berechnet
    		{
		    	
		      	VoteWidth = Global.scaledFontSize_normal/2;
		      	
		      	rightBorder = (int) (height * 0.8);
		      	nameLayoutWidth = width - VoteWidth - iconSize - rightBorder ;
		      	DTPaint = new Paint();
	    	    DTPaint.setTextSize(Global.scaledFontSize_normal);
	    	    DTPaint.setAntiAlias(true);
    		}
    		if (namePaint==null)
    		{
    			namePaint = new TextPaint();
    			namePaint.setTextSize(Global.scaledFontSize_normal);
    		}
    		
    	// reset namePaint attr
    		namePaint.setColor(Global.getColor(R.attr.TextColor));
    		namePaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
    		
    		DTPaint.setColor(Global.getColor(R.attr.TextColor));
    		
    	// Draw roundetRect
    		ActivityUtils.drawFillRoundRecWithBorder(canvas, rec, 2, Global.getColor(R.attr.ListSeparator), BackgroundColor);
    		
    	// Draw Vote
    		if (this.Rating > 0)
    			ActivityUtils.PutImageTargetHeight(canvas, Global.StarIcons[(int)(this.Rating * 2)],-90,left, top , (int) (Global.scaledFontSize_normal*0.8));
    	
    	// Draw Icon
    		 if (this.MysterySolved())
    	        {
    			 	ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[19],left + VoteWidth, top - (int) (Global.scaledFontSize_normal / 2) , iconSize); 
    	        }
    	        else
    	        {
    	        	ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[this.Type.ordinal()], left  + VoteWidth, top - (int) (Global.scaledFontSize_normal / 2) , iconSize); 
    	        }
    	
    	// Draw Cache Name
    		   
    	     if(notAvailable)
    	     {
    	    	 namePaint.setColor(Color.RED);
    	    	 namePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
    	     }
    	     layoutCacheName = new StaticLayout(this.Name, namePaint, nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    	     int LayoutHeight = ActivityUtils.drawStaticLayout(canvas, layoutCacheName, left + VoteWidth + iconSize + 5, top);
    	       
    	// over draw 3. Cache name line
    	     if(layoutCacheName.getLineCount()>2)
    	     {
    	    	 Paint backPaint = new Paint();
    	    	 backPaint.setColor(BackgroundColor); // Color.RED
    	    	 int VislinesHeight = LayoutHeight*2/layoutCacheName.getLineCount();
    	    	 canvas.drawRect(new Rect(left + VoteWidth + iconSize + 5,top + VislinesHeight,nameLayoutWidth+left + VoteWidth + iconSize + 5,top+LayoutHeight+VislinesHeight-4), backPaint);
    	     }
    	     
    	// Draw S/D/T
    	     int SDTleft = left+ 2;
    	     
    	     canvas.drawText("S",SDTleft,SDTLineTop , DTPaint);
    	     SDTleft += space;
    	     SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.SizeIcons[(int)(this.Size)], SDTleft, SDTImageTop, Global.scaledFontSize_normal);
    	     SDTleft += tab;	
    	     canvas.drawText("D",SDTleft,SDTLineTop , DTPaint);
    	     SDTleft += space;
    	     SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.StarIcons[(int)(this.Difficulty * 2)],SDTleft, SDTImageTop, Global.scaledFontSize_normal);
    	     SDTleft += tab;
    	     canvas.drawText("T", SDTleft,SDTLineTop , DTPaint);
    	     SDTleft += space;
    	     SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.StarIcons[(int)(this.Terrain * 2)], SDTleft, SDTImageTop, Global.scaledFontSize_normal);
    	     SDTleft += tab;
    	     
    	
    	     
    	// Draw TB
    	     int numTb = this.NumTravelbugs;
             if (numTb > 0)
              {
            	 SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[0],-90,SDTleft, (int) (SDTImageTop - (Global.scaledFontSize_normal/2.8)) , Global.scaledFontSize_normal*3);
                  //SDTleft += space;
                 if (numTb > 1)
                	 canvas.drawText("x" + String.valueOf(numTb),SDTleft, SDTLineTop , DTPaint);
              }
    	
             
        // Draw Bearing
    	     if (BearingRec==null) BearingRec = new Rect(rec.right-rightBorder,rec.top,rec.right,(int) (SDTImageTop*0.9));
    	     if (drawStyle != DrawStyle.withoutBearing) DrawBearing(canvas,BearingRec);
    	
    	  if (this.Found())
          {
        	  
    		  ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[2],left  + VoteWidth+iconSize/2,top - (int) (Global.scaledFontSize_normal / 2)+iconSize/2, iconSize/2);//Smile
          }
              

          if (this.Favorit())
         {
        	  ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[19],left + VoteWidth + 2, top , iconSize/2);
         }

         

          if (this.Archived)
          {
        	  ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[24], left + VoteWidth, top - (int) (Global.scaledFontSize_normal / 2), iconSize/2);
          }

         if (this.Owner.equals(Config.GetString("GcLogin")) && !(Config.GetString("GcLogin").equals("")))
           {
        	 ActivityUtils.PutImageTargetHeight(canvas,Global.Icons[17],left  + VoteWidth+iconSize/2,top - (int) (Global.scaledFontSize_normal / 2)+iconSize/2, iconSize/2);
           }
       	
    }
   
    
    
   
    private void DrawBearing(Canvas canvas,Rect drawingRec)
    {
    	
    	if (Global.LastValidPosition.Valid || Global.Marker.Valid)
        {
    		    Coordinate position = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
	            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
	            double bearing = de.droidcachebox.Geocaching.Coordinate.Bearing(position.Latitude, position.Longitude, this.Latitude(), this.Longitude());
	            double cacheBearing = bearing - heading;
	            String cacheDistance=UnitFormatter.DistanceString(this.Distance());
	            DrawBearing(canvas,drawingRec,cacheDistance,cacheBearing);   
			
       }
    }
    
    
    public void DrawBearing(Canvas canvas,Rect drawingRec, Waypoint waypoint)
    {
    	if (Global.LastValidPosition.Valid || Global.Marker.Valid)
        {
    		    Coordinate position = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
	            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
	            double bearing = de.droidcachebox.Geocaching.Coordinate.Bearing(position.Latitude, position.Longitude, waypoint.Latitude(), waypoint.Longitude());
	            double waypointBearing = bearing - heading;
	            String waypointDistance=UnitFormatter.DistanceString(this.Distance());
	            DrawBearing(canvas,drawingRec,waypointDistance,waypointBearing);
			   
    		
    		
       }
    }
    
    
    private void DrawBearing (Canvas canvas,Rect drawingRec, String Distance, double Bearing)
    {
    	ActivityUtils.PutImageTargetHeight(canvas, Global.Arrows[1],Bearing,drawingRec.left,drawingRec.top,(int) (drawingRec.height()*0.9));
	    canvas.drawText(Distance,drawingRec.left,drawingRec.bottom, DTPaint);
	       
    }
    
    
}
