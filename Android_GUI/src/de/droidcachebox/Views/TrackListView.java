package de.droidcachebox.Views;




import CB_Core.Types.Coordinate;
import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Map.Descriptor.PointD;
import de.droidcachebox.Map.RouteOverlay;
import de.droidcachebox.Map.RouteOverlay.Route;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.AllContextMenuCallHandler;
import de.droidcachebox.Views.Forms.EditCoordinate;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;
import de.droidcachebox.Views.Forms.projectionCoordinate;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.File;

import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



/**
 * Liste zur darstellung und verwaltung der Routes in RouteOverlay.Routes
 * <br><br><br>
 * <img src="doc-files/TrackListView.png" width=250 height=400> 
 * @author Longri
 *
 */
public class TrackListView extends ListView implements ViewOptionsMenu {
	
	CustomAdapter lvAdapter;
	Activity parentActivity;
	protected static float lastTouchX;
	protected static float lastTouchY;
	TrackListViewItem selectedItem;
	int[] ColorField = new int[] { Color.RED, Color.YELLOW, Color.BLACK, Color.GREEN, Color.GRAY };
	final int projectionZoomLevel = 15;
	final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY=98765;
	
	
	//Schrit-ablauf Member
	/**
	 * Aktiver Schritt einer Schritt-ablaufkette
	 * </br> wird benötigt, um zu erkennen, welches ActivitieResult behandelt werden soll
	 * 
	 * <h1>Point to Point ablaufkette:</h1>
	 *  Schritt1: get from Coords [value=P2P_GET_FIRST_POINT]
	 * </br> Schritt2: get to Coords   [value=P2P_GET_SECEND_POINT]
	 * </br><h1>Point to Point ablaufkette:</h1>
	 *  Schritt1: get from Coords [value=PROJECT_GET_FIRST_POINT]
	 * </br> Schritt2: get projection values   [value=PROJECT_GET_PROJECT_VALUES]
	 * </br><h1>Point to Point ablaufkette:</h1>
	 *  Schritt1: get from Coords [value=CIRCLE_GET_FIRST_POINT]
	 * </br> Schritt2: get circ radius   [value=CIRCLE_GET_PROJECT_VALUE]
	 */
	private static int nextStep=-1;
	private static final int P2P_GET_FIRST_POINT=1;
	private static final int P2P_GET_SECEND_POINT=2;
	private static final int PROJECT_GET_FIRST_POINT=3;
	private static final int PROJECT_GET_PROJECT_VALUES=4;
	private static final int CIRCLE_GET_FIRST_POINT=5;
	private static final int CIRCLE_GET_PROJECT_VALUE=6;
	
	private static double Lon1;
	private static double Lat1;
	private static double Lon2;
	private static double Lat2;

	
	/**
	 * Constructor
	 */
	public TrackListView(final Context context, final Activity parentActivity) {
		super(context);
		this.parentActivity = parentActivity;
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext());
		this.setAdapter(lvAdapter);
		this.setOnItemClickListener(new OnItemClickListener() 
		{
			
	       		
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) 
			{
				
				
				
				Rect HitRec = new Rect();
				arg1.getHitRect(HitRec);
				Rect colorChangeHitRec = new Rect(HitRec.left,HitRec.top,HitRec.left+HitRec.height(),HitRec.bottom);
				Rect chkBoxHitRec = new Rect(HitRec.width()-HitRec.height(),HitRec.top,HitRec.right,HitRec.bottom);
				
				if(chkBoxHitRec.contains((int)TrackListView.lastTouchX, (int)TrackListView.lastTouchY))
				{
					((TrackListViewItem)arg1).switchCheked();
					lvAdapter.notifyDataSetInvalidated();
					invalidate();
				}else if(colorChangeHitRec.contains((int)TrackListView.lastTouchX, (int)TrackListView.lastTouchY))
				{
					// open ColorChanger Dialog
					((TrackListViewItem)arg1).changeColor();
				}else
				{
					((TrackListViewItem)arg1).setSelected(true);
					selectedItem=((TrackListViewItem)arg1);
				}
					
			
				
				return;
			}
			
			
			
		});
		
		this.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				lastTouchX = arg1.getX();
				lastTouchY = arg1.getY();
				return false;
			}
		});

	
		ActivityUtils.setListViewPropertys(this);
		
	}
	
	static public int windowW=0;
    static public int windowH=0 ;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
    {
    // we overriding onMeasure because this is where the application gets its right size.
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    windowW = getMeasuredWidth();
    windowH = getMeasuredHeight();
    }

	
	public class CustomAdapter extends BaseAdapter /*implements OnClickListener*/ {
		 
		
	 
	    private Context context;
	    
	 
	    public CustomAdapter(Context context ) 
	    {
	        this.context = context;
	        
	    }
	 
	    
	    
	 
	   
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	    	
	    		 Boolean BackGroundChanger = ((position % 2) == 1);
			     TrackListViewItem v = new TrackListViewItem(context,RouteOverlay.Routes.get(position), BackGroundChanger);
			     return v;
	    }





		@Override
		public int getCount() 
		{
			return RouteOverlay.Routes.size();
		}





		@Override
	    public Object getItem(int position) {
	    	if (RouteOverlay.Routes!= null)
	    	{
    			return RouteOverlay.Routes.get(position);
	    	} else
	    		return null;
	    }	 
	    	 
	}

	

	/**
	 * Lädt einen Track in die RouteList
	 */
	private void HandleLoad() 
	{
		String fileName = Config.GetString("TrackFolder");
		
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		
		// Construct URI from file name.
		File file = new File(fileName);
		intent.setData(Uri.fromFile(file));
		
		// Set fancy title and button (optional)
		intent.putExtra(FileManagerIntents.EXTRA_TITLE, "Select file to open");
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, "Open");
		
		try {
			main.mainActivity.startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(main.mainActivity, "No compatible file manager found", 
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * erstellt einen neuen Track aus Point und Radius und
	 * fügt diesen der Routes List hinzu
	 */
	private void HandleGenerate_Circle() 
	{
		
		showEditCoord(CIRCLE_GET_FIRST_POINT);
		
	}

	/**
	 * erstellt einen neuen Track aus Point und Point und
	 * fügt diesen der Routes List hinzu
	 */
	private void HandleGenerate_Point2Point() 
	{
		showEditCoord(P2P_GET_FIRST_POINT);
	}

	/**
	 * erstellt einen Track aus Point,Richtung und Entfernung  und
	 * fügt diesen der Routes List hinzu
	 */
	private void HandleGenerate_Projection() 
	{
		showEditCoord(PROJECT_GET_FIRST_POINT);
	}

	/**
	 * Löscht den selectierten Track aus der Routes List
	 */
	private void HandleTrackDelete() 
	{
//		MessageBox.Show("Handle Track Delete");
		if(selectedItem==null)
		{
			MessageBox.Show("Kein Track zum Löschen gewählt!");
			return;
		}
		
		if (selectedItem.getRoute().IsActualTrack)
		{
			MessageBox.Show("Aktueller Track darf nicht gelöscht werden!");
			return;
		}
		
		RouteOverlay.Routes.remove(selectedItem.getRoute());
		selectedItem=null;
		lvAdapter.notifyDataSetChanged();
	}
	
	
	/**
	 * Öffnet den Edit-Coord Dialog
	 * 
	 * @param NextStep Schritt zur behandlung des ActivityResults
	 */
	private void showEditCoord(int NextStep)
	{
		nextStep=NextStep;
		String Title ="";
		switch (nextStep)
		{
			case P2P_GET_FIRST_POINT:Title="set From Pos";break;
			case P2P_GET_SECEND_POINT:Title="set To Pos";break;
			case PROJECT_GET_FIRST_POINT:Title="set From Pos";break;
			case CIRCLE_GET_FIRST_POINT:Title="set Center Pos";break;
		}
		
		Coordinate coord = GlobalCore.LastValidPosition;
		if ((coord == null) || (!coord.Valid))
			if(GlobalCore.SelectedCache()!=null)
			{
				coord = GlobalCore.SelectedCache().Pos;
			}
			else
			{
				coord=null;
			}
			
		if (coord==null)
		{
			coord=new Coordinate(0.0,0.0);
		}
				
		// Koordinaten Dialog öffnen
		Intent coordIntent = new Intent().setClass(getContext(), EditCoordinate.class);
        Bundle b = new Bundle();
        b.putSerializable("Coord", coord);
        b.putSerializable("Title", Title);
        coordIntent.putExtras(b);
        parentActivity.startActivityForResult(coordIntent, 0);
		
	}
	
	private void showProjectCoord(int NextStep)
	{
		Coordinate coord = GlobalCore.LastValidPosition;

		nextStep=NextStep;
		String Title ="";
		switch (nextStep)
		{
			
			case PROJECT_GET_PROJECT_VALUES:
				Title="get Projection";
				coord.Latitude=Lat1;
				coord.Longitude=Lon1;
				break;
		}
		
		if ((coord == null) || (!coord.Valid))
			if(GlobalCore.SelectedCache()!=null)
			{
				coord = GlobalCore.SelectedCache().Pos;
			}
			else
			{
				coord=null;
			}
			
		if (coord==null)
		{
			coord=new Coordinate(0.0,0.0);
		}
				
		// Projection Dialog öffnen
		Intent coordIntent = new Intent().setClass(getContext(), projectionCoordinate.class);
        Bundle b = new Bundle();
        b.putSerializable("Coord", coord);
        b.putSerializable("Title", Global.Translations.Get("Projection"));
        b.putSerializable("Radius", false);
        coordIntent.putExtras(b);
        parentActivity.startActivityForResult(coordIntent, 0);
	}
	
	private void showCircle(int NextStep)
	{
		Coordinate coord = GlobalCore.LastValidPosition;

		nextStep=NextStep;
		String Title ="";
		switch (nextStep)
		{
			
			case CIRCLE_GET_PROJECT_VALUE:
				Title="get Radius";
				coord.Latitude=Lat1;
				coord.Longitude=Lon1;
				break;
		}
		
		if ((coord == null) || (!coord.Valid))
			if(GlobalCore.SelectedCache()!=null)
			{
				coord = GlobalCore.SelectedCache().Pos;
			}
			else
			{
				coord=null;
			}
			
		if (coord==null)
		{
			coord=new Coordinate(0.0,0.0);
		}
				
		// Projection Dialog öffnen
		Intent coordIntent = new Intent().setClass(getContext(), projectionCoordinate.class);
        Bundle b = new Bundle();
        b.putSerializable("Coord", coord);
        b.putSerializable("Title", "Circle");
        b.putSerializable("Radius", true);
        coordIntent.putExtras(b);
        parentActivity.startActivityForResult(coordIntent, 0);
	}
		
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
		
		
		switch (requestCode) {
		case REQUEST_CODE_PICK_FILE_OR_DIRECTORY:
			if (resultCode == android.app.Activity.RESULT_OK  && data != null) {
				// obtain the filename
				Uri fileUri = data.getData();
				if (fileUri != null) {
					String filePath = fileUri.getPath();
					if (filePath != null) {
						((main)main.mainActivity).mapView.LoadTrack(filePath);
					}
				}
			}
			break;
		}
		
		
		if (data == null)
			return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			Coordinate coord = (Coordinate)bundle.getSerializable("CoordResult");
			if (coord != null)
			{
				if (nextStep==P2P_GET_FIRST_POINT)
				{
					Lon1= coord.Longitude;
					Lat1= coord.Latitude;
					showEditCoord(P2P_GET_SECEND_POINT);
					
				}else if (nextStep==P2P_GET_SECEND_POINT)
				{
					Lon2= coord.Longitude;
					Lat2= coord.Latitude;
					
					int TrackColor = ColorField[(RouteOverlay.Routes.size()) % ColorField.length];
					Paint paint = new Paint();
					paint.setColor(TrackColor);
					paint.setStrokeWidth(3);
					RouteOverlay.Routes.add(GenP2PRoute(Lat1, Lon1, Lat2, Lon2, paint));
					lvAdapter.notifyDataSetChanged();
					resetStep();
				}else if (nextStep==PROJECT_GET_FIRST_POINT)
				{
					Lon1= coord.Longitude;
					Lat1= coord.Latitude;
					showProjectCoord(PROJECT_GET_PROJECT_VALUES);
					
				}else if (nextStep==PROJECT_GET_PROJECT_VALUES)
				{
					Lon2= coord.Longitude;
					Lat2= coord.Latitude;
					
					Coordinate FromCoord = new Coordinate(Lat1,Lon1);
					
					double distance = coord.Distance(FromCoord);
					double bearing = coord.bearingTo(FromCoord) + 180; //vieleicht noch um 180° drehen?
					
					int TrackColor = ColorField[(RouteOverlay.Routes.size()) % ColorField.length];
					Paint paint = new Paint();
					paint.setColor(TrackColor);
					paint.setStrokeWidth(3);
					RouteOverlay.Routes.add(GenProjectRoute(Lat1, Lon1, distance, bearing, paint));
					lvAdapter.notifyDataSetChanged();
					resetStep();
				
				}else if (nextStep==CIRCLE_GET_FIRST_POINT)
				{
					Lon1= coord.Longitude;
					Lat1= coord.Latitude;
					showCircle(CIRCLE_GET_PROJECT_VALUE);
					
				}else if (nextStep==CIRCLE_GET_PROJECT_VALUE)
				{
					Lon2= coord.Longitude;
					Lat2= coord.Latitude;
					
					Coordinate FromCoord = new Coordinate(Lat1,Lon1);
					
					double distance = coord.Distance(FromCoord);
					
					int TrackColor = ColorField[(RouteOverlay.Routes.size()) % ColorField.length];
					Paint paint = new Paint();
					paint.setColor(TrackColor);
					paint.setStrokeWidth(3);
					RouteOverlay.Routes.add(GenCircleRoute(Lat1, Lon1, distance, paint));
					lvAdapter.notifyDataSetChanged();
					resetStep();
				}
				
			}
		}
	}

	
	/**
	 * Setzt alle Schritt Variabeln zurück
	 */
	private void resetStep() 
	{
		nextStep=-1;
		Lat1=0;
		Lat2=0;
		Lon1=0;
		Lon2=0;
	}
	
	

	/**
	 * Generiert eine Route Point to Point
	 * @param FromLat
	 * @param FromLon
	 * @param ToLat
	 * @param ToLon
	 * @param paint Das Paint, mit der die Route in die Karte gezeichnet wird.
	 * @return Generierte Route
	 */
    public Route GenP2PRoute(double FromLat, double FromLon, double ToLat, double ToLon, Paint paint)
    {
        Route route = new Route(paint, null);

        route.Name = "Point 2 Point Route";

        PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, FromLon),
            Descriptor.LatitudeToTileY(projectionZoomLevel, FromLat));
        route.Points.add(projectedPoint);
        projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, ToLon),
            Descriptor.LatitudeToTileY(projectionZoomLevel, ToLat));
        route.Points.add(projectedPoint);

        route.ShowRoute = true;

        return route;
    }

    
    /**
     * Generiert eine Route, Point and radius
     * @param FromLat
     * @param FromLon
     * @param Distance
     * @param paint Das Paint, mit der die Route in die Karte gezeichnet wird.
     * @return Generierte Route
     */
    public Route GenCircleRoute(double FromLat, double FromLon, double Distance, Paint paint)
    {
        Route route = new Route(paint, null);

        route.Name = "Circle Route";

        Coordinate GEOPosition = new Coordinate();
        GEOPosition.Latitude = FromLat;
        GEOPosition.Longitude = FromLon;

        Coordinate Projektion = new Coordinate();

        for (int i = 0; i <= 360; i++)
        {
            Projektion = Coordinate.Project(GEOPosition.Latitude, GEOPosition.Longitude, (double)i, Distance);

            PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, Projektion.Longitude),
                                        Descriptor.LatitudeToTileY(projectionZoomLevel, Projektion.Latitude));
            route.Points.add(projectedPoint);

        }

        route.ShowRoute = true;

        return route;
    }

    
    /**
     * Generiert eine Route, Point to Distance and angle
     * @param FromLat
     * @param FromLon
     * @param Distance
     * @param Bearing
     * @param paint Das Paint, mit der die Route in die Karte gezeichnet wird.
     * @return Generierte Route
     */
    public Route GenProjectRoute(double FromLat, double FromLon, double Distance, double Bearing, Paint paint)
    {
        Route route = new Route(paint, null);

        route.Name = "Projected Route";

        Coordinate GEOPosition = new Coordinate();
        GEOPosition.Latitude = FromLat;
        GEOPosition.Longitude = FromLon;
        PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, GEOPosition.Longitude),
                                    Descriptor.LatitudeToTileY(projectionZoomLevel, GEOPosition.Latitude));
        route.Points.add(projectedPoint);

        Coordinate Projektion = new Coordinate();

        Projektion = Coordinate.Project(GEOPosition.Latitude, GEOPosition.Longitude, Bearing, Distance);

        projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, Projektion.Longitude),
                                    Descriptor.LatitudeToTileY(projectionZoomLevel, Projektion.Latitude));
        route.Points.add(projectedPoint);
        route.ShowRoute = true;

        return route;
    }

    
    /**
     * Read track from gpx file
	 * attention it is possible that a gpx file contains more than 1 <trk> segments
	 * in this case all segments was connectet to one track
     * @param file
     * @param pen
     * @param minDistanceMeters
     * @return geladene Route
     */
    public Route LoadRoute(String file, Paint pen, double minDistanceMeters)
    {
    		Route route = new Route(pen, null);
            route.FileName = FileIO.GetFileName(file);

        try
        {
           /* BinaryReader reader = new BinaryReader(File.Open(file, FileMode.Open));
            
                      
            long length = reader.BaseStream.Length;
            
            String line = null;
            bool inBody = false;
            bool inTrk = false;
            bool ReadName = false;

            Coordinate lastAcceptedCoordinate = null;

            StringBuilder sb = new StringBuilder();
            while (reader.BaseStream.Position < length)
            {

                char nextChar = reader.ReadChar();
                sb.Append(nextChar);

                if (nextChar == '>')
                {
                    line = sb.ToString().Trim().ToLower();
                    sb = new StringBuilder();

                    // Read Routename form gpx file
                    // attention it is possible that a gpx file contains more than 1 <trk> segments
                    // In this case the first name was used
                    if (ReadName && (route.Name == null))
                    {
                        route.Name = line.Substring(0,line.IndexOf("</name>"));
                        ReadName = false;
                        continue;
                    }

                    if (!inTrk)
                    {
                        // Begin of the Track detected?
                        if (line.IndexOf("<trk>") > -1)
                            inTrk = true;

                        continue;
                    }
                    else
                    {
                        // found <name>?
                        if (line.IndexOf("<name>") > -1)
                        {
                            ReadName = true;
                            continue;
                        }
                    }


                    if (!inBody)
                    {
                        // Anfang der Trackpoints gefunden?
                        if (line.IndexOf("<trkseg>") > -1)
                            inBody = true;

                        continue;
                    }

                    // Ende gefunden?
                    if (line.IndexOf("</trkseg>") > 0)
                        break;

                    if (line.IndexOf("<trkpt") > -1)
                    {
                        // Trackpoint lesen
                        int lonIdx = line.IndexOf("lon=\"") + 5;
                        int latIdx = line.IndexOf("lat=\"") + 5;

                        int lonEndIdx = line.IndexOf("\"", lonIdx);
                        int latEndIdx = line.IndexOf("\"", latIdx);

                        String latStr = line.Substring(latIdx, latEndIdx - latIdx);
                        String lonStr = line.Substring(lonIdx, lonEndIdx - lonIdx);

                        double lat = double.Parse(latStr, NumberFormatInfo.InvariantInfo);
                        double lon = double.Parse(lonStr, NumberFormatInfo.InvariantInfo);

                        if (lastAcceptedCoordinate != null)
                            if (Datum.WGS84.Distance(lat, lon, lastAcceptedCoordinate.Latitude, lastAcceptedCoordinate.Longitude) < minDistanceMeters)
                                continue;

                        lastAcceptedCoordinate = new Coordinate(lat, lon);

                        PointD projectedPoint = new PointD(Descriptor.LongitudeToTileX(projectionZoomLevel, lon),
                            Descriptor.LatitudeToTileY(projectionZoomLevel, lat));

                        route.Points.Add(projectedPoint);
                    }
                }
            }

            reader.Close();
            */
            
            if (route.Points.size() < 2)
                route.Name = "no Route segment found";

            route.ShowRoute = true;

            return route;
        }

        catch (Exception exc)
        {
            MessageBox.Show(exc.toString(), "Error", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
            return null;
        }

    }



	@Override
	public void OnShow() 
	{
		ActivityUtils.setListViewPropertys(this);
		selectedItem=null;
		lvAdapter.notifyDataSetInvalidated();
		invalidate();
	}

	@Override
	public void OnHide() 
	{
		
		
	}

	@Override
	public void OnFree() {
		
	}

	
	
	@Override public int GetMenuId() {return 0;}
	@Override public int GetContextMenuId() {return 0;}
	@Override public void BeforeShowContextMenu(Menu menu) {}
	@Override public boolean ContextMenuItemSelected(MenuItem item) {return false;}
	
	
	public boolean ItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.menu_tracklistview_delete:
			HandleTrackDelete();
			break;
			
		case R.id.menu_tracklistview_projection:
			HandleGenerate_Projection();
			break;
			
		case R.id.menu_tracklistview_point2point:
			HandleGenerate_Point2Point();
			break;
			
		case R.id.menu_tracklistview_circle:
			HandleGenerate_Circle();
			break;
			
		case R.id.menu_tracklistview_load:
			HandleLoad();
			break;
			
	
		}
		return true;
	}
	
	@Override
		public void BeforeShowMenu(Menu menu) 
		{
			AllContextMenuCallHandler.showTrackListViewContextMenu();
		}
	
}

