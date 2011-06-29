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
	
}