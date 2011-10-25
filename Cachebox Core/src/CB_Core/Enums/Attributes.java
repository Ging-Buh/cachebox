package CB_Core.Enums;

import java.util.ArrayList;
import java.util.HashMap;

public enum Attributes
{
	Default, Dogs, Fee, ClimbingGear, Boat, Scuba, Kids, TakesLess, ScenicView, SignificantHike, Climbing, Wading, Swimming, Anytime, Night, Winter, PoisonPlants, Snakes, Ticks, AbandonedMines, Cliff, Hunting, Dangerous, WheelchairAccessible, Parking, PublicTransportation, Drinking, Restrooms, Telephone, Picnic, Camping, Bicycles, Motorcycles, Quads, Offroad, Snowmobiles, Horses, Campfires, Thorns, Stealth, Stroller, NeedsMaintenance, Livestock, Flashlight, TruckDriver, FieldPuzzle, UVLight, Snowshoes, CrossCountrySkiis, SpecialTool, NightCache, ParkAndGrab, AbandonedStructure, ShortHike, MediumHike, LongHike, FuelNearby, FoodNearby, WirelessBeacon, ForTourists;

	public static long GetAttributeIndex(Attributes attrib)
	{
		return ((long) 1) << (attrib.ordinal());
	}

	
	private boolean negative=false;
	
	
	
	public static Attributes getAttributeEnumByGcComId(int id)
	{
		switch (id)
		{
		case 1:
			return CB_Core.Enums.Attributes.Dogs;
		case 2:
			return CB_Core.Enums.Attributes.Fee;
		case 3:
			return CB_Core.Enums.Attributes.ClimbingGear;
		case 4:
			return CB_Core.Enums.Attributes.Boat;
		case 5:
			return CB_Core.Enums.Attributes.Scuba;
		case 6:
			return CB_Core.Enums.Attributes.Kids;
		case 7:
			return CB_Core.Enums.Attributes.TakesLess;
		case 8:
			return CB_Core.Enums.Attributes.ScenicView;
		case 9:
			return CB_Core.Enums.Attributes.SignificantHike;
		case 10:
			return CB_Core.Enums.Attributes.Climbing;
		case 11:
			return CB_Core.Enums.Attributes.Wading;
		case 12:
			return CB_Core.Enums.Attributes.Swimming;
		case 13:
			return CB_Core.Enums.Attributes.Anytime;
		case 14:
			return CB_Core.Enums.Attributes.Night;
		case 15:
			return CB_Core.Enums.Attributes.Winter;
		case 17:
			return CB_Core.Enums.Attributes.PoisonPlants;
		case 18:
			return CB_Core.Enums.Attributes.Snakes;
		case 19:
			return CB_Core.Enums.Attributes.Ticks;
		case 20:
			return CB_Core.Enums.Attributes.AbandonedMines;
		case 21:
			return CB_Core.Enums.Attributes.Cliff;
		case 22:
			return CB_Core.Enums.Attributes.Hunting;
		case 23:
			return CB_Core.Enums.Attributes.Dangerous;
		case 24:
			return CB_Core.Enums.Attributes.WheelchairAccessible;
		case 25:
			return CB_Core.Enums.Attributes.Parking;
		case 26:
			return CB_Core.Enums.Attributes.PublicTransportation;
		case 27:
			return CB_Core.Enums.Attributes.Drinking;
		case 28:
			return CB_Core.Enums.Attributes.Restrooms;
		case 29:
			return CB_Core.Enums.Attributes.Telephone;
		case 30:
			return CB_Core.Enums.Attributes.Picnic;
		case 31:
			return CB_Core.Enums.Attributes.Camping;
		case 32:
			return CB_Core.Enums.Attributes.Bicycles;
		case 33:
			return CB_Core.Enums.Attributes.Motorcycles;
		case 34:
			return CB_Core.Enums.Attributes.Quads;
		case 35:
			return CB_Core.Enums.Attributes.Offroad;
		case 36:
			return CB_Core.Enums.Attributes.Snowmobiles;
		case 37:
			return CB_Core.Enums.Attributes.Horses;
		case 38:
			return CB_Core.Enums.Attributes.Campfires;
		case 39:
			return CB_Core.Enums.Attributes.Thorns;
		case 40:
			return CB_Core.Enums.Attributes.Stealth;
		case 41:
			return CB_Core.Enums.Attributes.Stroller;
		case 42:
			return CB_Core.Enums.Attributes.NeedsMaintenance;
		case 43:
			return CB_Core.Enums.Attributes.Livestock;
		case 44:
			return CB_Core.Enums.Attributes.Flashlight;
		case 46:
			return CB_Core.Enums.Attributes.TruckDriver;
		case 47:
			return CB_Core.Enums.Attributes.FieldPuzzle;
		case 48:
			return CB_Core.Enums.Attributes.UVLight;
		case 49:
			return CB_Core.Enums.Attributes.Snowshoes;
		case 50:
			return CB_Core.Enums.Attributes.CrossCountrySkiis;
		case 51:
			return CB_Core.Enums.Attributes.SpecialTool;
		case 52:
			return CB_Core.Enums.Attributes.NightCache;
		case 53:
			return CB_Core.Enums.Attributes.ParkAndGrab;
		case 54:
			return CB_Core.Enums.Attributes.AbandonedStructure;
		case 55:
			return CB_Core.Enums.Attributes.ShortHike;
		case 56:
			return CB_Core.Enums.Attributes.MediumHike;
		case 57:
			return CB_Core.Enums.Attributes.LongHike;
		case 58:
			return CB_Core.Enums.Attributes.FuelNearby;
		case 59:
			return CB_Core.Enums.Attributes.FoodNearby;
		case 60:
			return CB_Core.Enums.Attributes.WirelessBeacon;
		case 63:
			return CB_Core.Enums.Attributes.ForTourists;
		}

			
		return CB_Core.Enums.Attributes.Default;
	}

	private static HashMap<Attributes, Integer> attributeLookup;

	private static void ini()
	{
		attributeLookup = new HashMap<Attributes, Integer>();
		attributeLookup.put(Attributes.Default, 0);
		attributeLookup.put(Attributes.Dogs, 1);
		attributeLookup.put(Attributes.Fee, 2);
		attributeLookup.put(Attributes.ClimbingGear, 3);
		attributeLookup.put(Attributes.Boat, 4);
		attributeLookup.put(Attributes.Scuba, 5);
		attributeLookup.put(Attributes.Kids, 6);
		attributeLookup.put(Attributes.TakesLess, 7);
		attributeLookup.put(Attributes.ScenicView, 8);
		attributeLookup.put(Attributes.SignificantHike, 9);
		attributeLookup.put(Attributes.Climbing, 10);
		attributeLookup.put(Attributes.Wading, 11);
		attributeLookup.put(Attributes.Swimming, 12);
		attributeLookup.put(Attributes.Anytime, 13);
		attributeLookup.put(Attributes.Night, 14);
		attributeLookup.put(Attributes.Winter, 15);
		attributeLookup.put(Attributes.PoisonPlants, 17);
		attributeLookup.put(Attributes.Snakes, 18);
		attributeLookup.put(Attributes.Ticks, 19);
		attributeLookup.put(Attributes.AbandonedMines, 20);
		attributeLookup.put(Attributes.Cliff, 21);
		attributeLookup.put(Attributes.Hunting, 22);
		attributeLookup.put(Attributes.Dangerous, 23);
		attributeLookup.put(Attributes.WheelchairAccessible, 24);
		attributeLookup.put(Attributes.Parking, 25);
		attributeLookup.put(Attributes.PublicTransportation, 26);
		attributeLookup.put(Attributes.Drinking, 27);
		attributeLookup.put(Attributes.Restrooms, 28);
		attributeLookup.put(Attributes.Telephone, 29);
		attributeLookup.put(Attributes.Picnic, 30);
		attributeLookup.put(Attributes.Camping, 31);
		attributeLookup.put(Attributes.Bicycles, 32);
		attributeLookup.put(Attributes.Motorcycles, 33);
		attributeLookup.put(Attributes.Quads, 34);
		attributeLookup.put(Attributes.Offroad, 35);
		attributeLookup.put(Attributes.Snowmobiles, 36);
		attributeLookup.put(Attributes.Horses, 37);
		attributeLookup.put(Attributes.Campfires, 38);
		attributeLookup.put(Attributes.Thorns, 39);
		attributeLookup.put(Attributes.Stealth, 40);
		attributeLookup.put(Attributes.Stroller, 41);
		attributeLookup.put(Attributes.NeedsMaintenance, 42);
		attributeLookup.put(Attributes.Livestock, 43);
		attributeLookup.put(Attributes.Flashlight, 44);
		attributeLookup.put(Attributes.TruckDriver, 46);
		attributeLookup.put(Attributes.FieldPuzzle, 47);
		attributeLookup.put(Attributes.UVLight, 48);
		attributeLookup.put(Attributes.Snowshoes, 49);
		attributeLookup.put(Attributes.CrossCountrySkiis, 50);
		attributeLookup.put(Attributes.SpecialTool, 51);
		attributeLookup.put(Attributes.NightCache, 52);
		attributeLookup.put(Attributes.ParkAndGrab, 53);
		attributeLookup.put(Attributes.AbandonedStructure, 54);
		attributeLookup.put(Attributes.ShortHike, 55);
		attributeLookup.put(Attributes.MediumHike, 56);
		attributeLookup.put(Attributes.LongHike, 57);
		attributeLookup.put(Attributes.FuelNearby, 58);
		attributeLookup.put(Attributes.FoodNearby, 59);
		attributeLookup.put(Attributes.WirelessBeacon, 60);
		attributeLookup.put(Attributes.ForTourists, 63);
	}

	public static ArrayList<Attributes> getAttributes(long attributesPositive, long attributesNegative)
	{
		ArrayList<Attributes> ret = new ArrayList<Attributes>();

		if (attributeLookup == null) ini();

		for (Attributes attribute : attributeLookup.keySet())
		{
			long att = Attributes.GetAttributeIndex(attribute);
			if ((att & attributesPositive) > 0)
			{
				ret.add(attribute);
			}
		}
		for (Attributes attribute : attributeLookup.keySet())
		{
			long att = Attributes.GetAttributeIndex(attribute);
			if ((att & attributesNegative) > 0)
			{
				attribute.negative=true;
				ret.add(attribute);
			}
		}
			
		return ret;
	}

	
	public String getImageName()
	{
		if (attributeLookup == null) ini();
		String ret = "att_"+ String.valueOf(attributeLookup.get(this));
		
		if (negative)
		{
			ret+="_0";
		}
		else
		{
			ret+="_1";
		}
		
		return ret; 
	}
	
}