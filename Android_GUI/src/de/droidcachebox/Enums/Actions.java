package de.droidcachebox.Enums;

import java.util.ArrayList;

import CB_Core.Enums.Attributes;
import CB_Core.Types.MoveableList;

/**
 * Enthält die Actions Möglichkeiten für die Quick Buttons
 * @author Longri
 *
 */
public enum Actions 
{
	DescriptionView,
	WaypointView,
	LogView,
	MapView,
	CacheListView,
	TrackListView,
	TakePicture,
	TakeVideo,
	VoiceRecord,
	LiveSearch,
	
	empty,;

	/**
	 * Gibt eine ArrayList von Actions zurück aus einem übergebenen String Array
	 * @param String[]
	 * @return ArrayList <Actions>
	 */
	public static MoveableList<Actions> getListFromConfig(String[] configList) 
	{
		MoveableList<Actions> retVel = new MoveableList<Actions>();
		if(configList==null)
		{
			return retVel;
		}
		for(String s:configList)
		{
			s=s.replace(",", ""); // Kommer entfernen
			int EnumId = Integer.parseInt(s);
			if(EnumId >-1)
			{
				retVel.add(Actions.getActionEnumById(EnumId));
			}
		}
		return retVel;
	}
	
	/**
	 * Gibt die ID des Übergebenen Enums zurück
	 * @param attrib
	 * @return long
	 */
	public static int GetIndex(Actions attrib)
    {
    	return attrib.ordinal();
    }
	
	
	public static Actions getActionEnumById(int id)
    {
        switch (id)
        {
            case 0:return Actions.DescriptionView;
    		case 1:return Actions.WaypointView;
        	case 2:return Actions.LogView;
        	case 3:return Actions.MapView;
        	case 4:return Actions.CacheListView;
        	case 5:return Actions.TrackListView;
        	case 6:return Actions.TakePicture;
        	case 7:return Actions.TakeVideo;
        	case 8:return Actions.VoiceRecord;
        	case 9:return Actions.LiveSearch;
        }
        return Actions.empty;
    }
}
