package CB_Core.Enums;

public enum Attributes
{
	Default,
	Dogs,
	Fee,
	ClimbingGear ,
	Boat ,
	Scuba,
	Kids,
	TakesLess,
	ScenicView ,
	SignificantHike ,
	Climbing,
	Wading,
	Swimming,
	Anytime,
	Night ,
	Winter ,
	PoisonPlants,
	Snakes ,
	Ticks ,
	AbandonedMines,
	Cliff ,
	Hunting ,
	Dangerous ,
	WheelchairAccessible ,
	Parking,
	PublicTransportation ,
	Drinking ,
	Restrooms ,
	Telephone ,
	Picnic ,
	Camping ,
	Bicycles ,
	Motorcycles ,
	Quads ,
	Offroad ,
	Snowmobiles ,
	Horses ,
	Campfires ,
	Thorns ,
	Stealth ,
	Stroller ,
	NeedsMaintenance,
	Livestock ,
	Flashlight ,
	TruckDriver ,
	FieldPuzzle ,
	UVLight ,
	Snowshoes ,
	CrossCountrySkiis,
	SpecialTool ,
	NightCache ,
	ParkAndGrab ,
	AbandonedStructure ,
	ShortHike ,
	MediumHike ,
	LongHike ,
	FuelNearby ,
	FoodNearby ;
	
	
	
	public static long GetAttributeIndex(Attributes attrib)
    {
    	return ((long)1) << (attrib.ordinal());
    }
	
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
        }

        return CB_Core.Enums.Attributes.Default;
    }
	

	
}