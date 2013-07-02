package CB_Core.Enums;

import java.util.ArrayList;
import java.util.HashMap;

import CB_Core.Types.DLong;

public enum Attributes
{
	Default,
	Dogs,
	Access_or_parking_fee,
	Climbing_gear,
	Boat,
	Scuba_gear,
	Recommended_for_kids,
	Takes_less_than_an_hour,
	Scenic_view,
	Significant_Hike,
	Difficult_climbing,
	May_require_wading,
	May_require_swimming,
	Available_at_all_times,
	Recommended_at_night,
	Available_during_winter,
	Cactus,
	Poison_plants,
	Dangerous_Animals,
	Ticks,
	Abandoned_mines,
	Cliff_falling_rocks,
	Hunting,
	Dangerous_area,
	Wheelchair_accessible,
	Parking_available,
	Public_transportation,
	Drinking_water_nearby,
	Public_restrooms_nearby,
	Telephone_nearby,
	Picnic_tables_nearby,
	Camping_available,
	Bicycles,
	Motorcycles,
	Quads,
	Off_road_vehicles,
	Snowmobiles,
	Horses,
	Campfires,
	Thorns,
	Stealth_required,
	Stroller_accessible,
	Needs_maintenance,
	Watch_for_livestock,
	Flashlight_required,
	Lost_And_Found_Tour,
	Truck_Driver,
	Field_Puzzle,
	UV_Light_Required,
	Snowshoes,
	Cross_Country_Skis,
	Special_Tool_Required,
	Night_Cache,
	Park_and_Grab,
	Abandoned_Structure,
	Short_hike,
	Medium_hike,
	Long_Hike,
	Fuel_Nearby,
	Food_Nearby,
	Wireless_Beacon,
	Partnership_Cache,
	Seasonal_Access,
	Tourist_Friendly,
	Tree_Climbing,
	Front_Yard,
	Teamwork_Required;

	public static DLong GetAttributeDlong(Attributes attrib)
	{

		if (attributeLookup == null) ini();

		int Id = attributeLookup.get(attrib);

		return DLong.shift(Id);
	}

	private boolean negative = false;

	public static Attributes getAttributeEnumByGcComId(int id)
	{
		switch (id)
		{
		case 1:
			return CB_Core.Enums.Attributes.Dogs;
		case 2:
			return CB_Core.Enums.Attributes.Access_or_parking_fee;
		case 3:
			return CB_Core.Enums.Attributes.Climbing_gear;
		case 4:
			return CB_Core.Enums.Attributes.Boat;
		case 5:
			return CB_Core.Enums.Attributes.Scuba_gear;
		case 6:
			return CB_Core.Enums.Attributes.Recommended_for_kids;
		case 7:
			return CB_Core.Enums.Attributes.Takes_less_than_an_hour;
		case 8:
			return CB_Core.Enums.Attributes.Scenic_view;
		case 9:
			return CB_Core.Enums.Attributes.Significant_Hike;
		case 10:
			return CB_Core.Enums.Attributes.Difficult_climbing;
		case 11:
			return CB_Core.Enums.Attributes.May_require_wading;
		case 12:
			return CB_Core.Enums.Attributes.May_require_swimming;
		case 13:
			return CB_Core.Enums.Attributes.Available_at_all_times;
		case 14:
			return CB_Core.Enums.Attributes.Recommended_at_night;
		case 15:
			return CB_Core.Enums.Attributes.Available_during_winter;
		case 16:
			return CB_Core.Enums.Attributes.Cactus;
		case 17:
			return CB_Core.Enums.Attributes.Poison_plants;
		case 18:
			return CB_Core.Enums.Attributes.Dangerous_Animals;
		case 19:
			return CB_Core.Enums.Attributes.Ticks;
		case 20:
			return CB_Core.Enums.Attributes.Abandoned_mines;
		case 21:
			return CB_Core.Enums.Attributes.Cliff_falling_rocks;
		case 22:
			return CB_Core.Enums.Attributes.Hunting;
		case 23:
			return CB_Core.Enums.Attributes.Dangerous_area;
		case 24:
			return CB_Core.Enums.Attributes.Wheelchair_accessible;
		case 25:
			return CB_Core.Enums.Attributes.Parking_available;
		case 26:
			return CB_Core.Enums.Attributes.Public_transportation;
		case 27:
			return CB_Core.Enums.Attributes.Drinking_water_nearby;
		case 28:
			return CB_Core.Enums.Attributes.Public_restrooms_nearby;
		case 29:
			return CB_Core.Enums.Attributes.Telephone_nearby;
		case 30:
			return CB_Core.Enums.Attributes.Picnic_tables_nearby;
		case 31:
			return CB_Core.Enums.Attributes.Camping_available;
		case 32:
			return CB_Core.Enums.Attributes.Bicycles;
		case 33:
			return CB_Core.Enums.Attributes.Motorcycles;
		case 34:
			return CB_Core.Enums.Attributes.Quads;
		case 35:
			return CB_Core.Enums.Attributes.Off_road_vehicles;
		case 36:
			return CB_Core.Enums.Attributes.Snowmobiles;
		case 37:
			return CB_Core.Enums.Attributes.Horses;
		case 38:
			return CB_Core.Enums.Attributes.Campfires;
		case 39:
			return CB_Core.Enums.Attributes.Thorns;
		case 40:
			return CB_Core.Enums.Attributes.Stealth_required;
		case 41:
			return CB_Core.Enums.Attributes.Stroller_accessible;
		case 42:
			return CB_Core.Enums.Attributes.Needs_maintenance;
		case 43:
			return CB_Core.Enums.Attributes.Watch_for_livestock;
		case 44:
			return CB_Core.Enums.Attributes.Flashlight_required;
		case 45:
			return CB_Core.Enums.Attributes.Lost_And_Found_Tour;
		case 46:
			return CB_Core.Enums.Attributes.Truck_Driver;
		case 47:
			return CB_Core.Enums.Attributes.Field_Puzzle;
		case 48:
			return CB_Core.Enums.Attributes.UV_Light_Required;
		case 49:
			return CB_Core.Enums.Attributes.Snowshoes;
		case 50:
			return CB_Core.Enums.Attributes.Cross_Country_Skis;
		case 51:
			return CB_Core.Enums.Attributes.Special_Tool_Required;
		case 52:
			return CB_Core.Enums.Attributes.Night_Cache;
		case 53:
			return CB_Core.Enums.Attributes.Park_and_Grab;
		case 54:
			return CB_Core.Enums.Attributes.Abandoned_Structure;
		case 55:
			return CB_Core.Enums.Attributes.Short_hike;
		case 56:
			return CB_Core.Enums.Attributes.Medium_hike;
		case 57:
			return CB_Core.Enums.Attributes.Long_Hike;
		case 58:
			return CB_Core.Enums.Attributes.Fuel_Nearby;
		case 59:
			return CB_Core.Enums.Attributes.Food_Nearby;
		case 60:
			return CB_Core.Enums.Attributes.Wireless_Beacon;
		case 61:
			return CB_Core.Enums.Attributes.Partnership_Cache;
		case 62:
			return CB_Core.Enums.Attributes.Seasonal_Access;
		case 63:
			return CB_Core.Enums.Attributes.Tourist_Friendly;
		case 64:
			return CB_Core.Enums.Attributes.Tree_Climbing;
		case 65:
			return CB_Core.Enums.Attributes.Front_Yard;
		case 66:
			return CB_Core.Enums.Attributes.Teamwork_Required;
		}

		return CB_Core.Enums.Attributes.Default;
	}

	private static HashMap<Attributes, Integer> attributeLookup;

	private static void ini()
	{
		attributeLookup = new HashMap<Attributes, Integer>();
		attributeLookup.put(Attributes.Default, 0);
		attributeLookup.put(Attributes.Dogs, 1);
		attributeLookup.put(Attributes.Access_or_parking_fee, 2);
		attributeLookup.put(Attributes.Climbing_gear, 3);
		attributeLookup.put(Attributes.Boat, 4);
		attributeLookup.put(Attributes.Scuba_gear, 5);
		attributeLookup.put(Attributes.Recommended_for_kids, 6);
		attributeLookup.put(Attributes.Takes_less_than_an_hour, 7);
		attributeLookup.put(Attributes.Scenic_view, 8);
		attributeLookup.put(Attributes.Significant_Hike, 9);
		attributeLookup.put(Attributes.Difficult_climbing, 10);
		attributeLookup.put(Attributes.May_require_wading, 11);
		attributeLookup.put(Attributes.May_require_swimming, 12);
		attributeLookup.put(Attributes.Available_at_all_times, 13);
		attributeLookup.put(Attributes.Recommended_at_night, 14);
		attributeLookup.put(Attributes.Available_during_winter, 15);
		attributeLookup.put(Attributes.Cactus, 16);
		attributeLookup.put(Attributes.Poison_plants, 17);
		attributeLookup.put(Attributes.Dangerous_Animals, 18);
		attributeLookup.put(Attributes.Ticks, 19);
		attributeLookup.put(Attributes.Abandoned_mines, 20);
		attributeLookup.put(Attributes.Cliff_falling_rocks, 21);
		attributeLookup.put(Attributes.Hunting, 22);
		attributeLookup.put(Attributes.Dangerous_area, 23);
		attributeLookup.put(Attributes.Wheelchair_accessible, 24);
		attributeLookup.put(Attributes.Parking_available, 25);
		attributeLookup.put(Attributes.Public_transportation, 26);
		attributeLookup.put(Attributes.Drinking_water_nearby, 27);
		attributeLookup.put(Attributes.Public_restrooms_nearby, 28);
		attributeLookup.put(Attributes.Telephone_nearby, 29);
		attributeLookup.put(Attributes.Picnic_tables_nearby, 30);
		attributeLookup.put(Attributes.Camping_available, 31);
		attributeLookup.put(Attributes.Bicycles, 32);
		attributeLookup.put(Attributes.Motorcycles, 33);
		attributeLookup.put(Attributes.Quads, 34);
		attributeLookup.put(Attributes.Off_road_vehicles, 35);
		attributeLookup.put(Attributes.Snowmobiles, 36);
		attributeLookup.put(Attributes.Horses, 37);
		attributeLookup.put(Attributes.Campfires, 38);
		attributeLookup.put(Attributes.Thorns, 39);
		attributeLookup.put(Attributes.Stealth_required, 40);
		attributeLookup.put(Attributes.Stroller_accessible, 41);
		attributeLookup.put(Attributes.Needs_maintenance, 42);
		attributeLookup.put(Attributes.Watch_for_livestock, 43);
		attributeLookup.put(Attributes.Flashlight_required, 44);
		attributeLookup.put(Attributes.Lost_And_Found_Tour, 45);
		attributeLookup.put(Attributes.Truck_Driver, 46);
		attributeLookup.put(Attributes.Field_Puzzle, 47);
		attributeLookup.put(Attributes.UV_Light_Required, 48);
		attributeLookup.put(Attributes.Snowshoes, 49);
		attributeLookup.put(Attributes.Cross_Country_Skis, 50);
		attributeLookup.put(Attributes.Special_Tool_Required, 51);
		attributeLookup.put(Attributes.Night_Cache, 52);
		attributeLookup.put(Attributes.Park_and_Grab, 53);
		attributeLookup.put(Attributes.Abandoned_Structure, 54);
		attributeLookup.put(Attributes.Short_hike, 55);
		attributeLookup.put(Attributes.Medium_hike, 56);
		attributeLookup.put(Attributes.Long_Hike, 57);
		attributeLookup.put(Attributes.Fuel_Nearby, 58);
		attributeLookup.put(Attributes.Food_Nearby, 59);
		attributeLookup.put(Attributes.Wireless_Beacon, 60);
		attributeLookup.put(Attributes.Partnership_Cache, 61);
		attributeLookup.put(Attributes.Seasonal_Access, 62);
		attributeLookup.put(Attributes.Tourist_Friendly, 63);
		attributeLookup.put(Attributes.Tree_Climbing, 64);
		attributeLookup.put(Attributes.Front_Yard, 65);
		attributeLookup.put(Attributes.Teamwork_Required, 66);
	}

	public static ArrayList<Attributes> getAttributes(DLong attributesPositive, DLong attributesNegative)
	{
		ArrayList<Attributes> ret = new ArrayList<Attributes>();
		if (attributeLookup == null) ini();
		for (Attributes attribute : attributeLookup.keySet())
		{
			DLong att = Attributes.GetAttributeDlong(attribute);
			if ((att.BitAndBiggerNull(attributesPositive)))
			{
				ret.add(attribute);
			}
		}
		for (Attributes attribute : attributeLookup.keySet())
		{
			DLong att = Attributes.GetAttributeDlong(attribute);
			if ((att.BitAndBiggerNull(attributesNegative)))
			{
				attribute.negative = true;
				ret.add(attribute);
			}
		}

		return ret;
	}

	public String getImageName()
	{
		if (attributeLookup == null) ini();
		String ret = "att_" + String.valueOf(attributeLookup.get(this));

		if (negative)
		{
			ret += "_0";
		}
		else
		{
			ret += "_1";
		}
		return ret;
	}
}