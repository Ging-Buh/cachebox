package de.droidcachebox.dataclasses;

import java.util.HashMap;

import de.droidcachebox.utils.DLong;

public enum Attribute {
    Default, // 0
    Dogs, // 1
    Access_or_parking_fee, // 2
    Climbing_gear, // 3
    Boat, // 4
    Scuba_gear, // 5
    Recommended_for_kids, // 6
    Takes_less_than_an_hour, // 7
    Scenic_view, // 8
    Significant_Hike, // 9
    Difficult_climbing, // 10
    May_require_wading, // 11
    May_require_swimming, // 12
    Available_at_all_times, // 13
    Recommended_at_night, // 14
    Available_during_winter, // 15
    Cactus, // 16
    Poison_plants, // 17
    Dangerous_Animals, // 18
    Ticks, // 19
    Abandoned_mines, // 20
    Cliff_falling_rocks, // 21
    Hunting, // 22
    Dangerous_area, //
    Wheelchair_accessible, //
    Parking_available, //
    Public_transportation, //
    Drinking_water_nearby, //
    Public_restrooms_nearby, //
    Telephone_nearby, //
    Picnic_tables_nearby, //
    Camping_available, //
    Bicycles, //
    Motorcycles, //
    Quads, //
    Off_road_vehicles, //
    Snowmobiles, //
    Horses, //
    Campfires, //
    Thorns, //
    Stealth_required, //
    Stroller_accessible, //
    Needs_maintenance, //
    Watch_for_livestock, //
    Flashlight_required, //
    Lost_And_Found_Tour, //
    Truck_Driver, //
    Field_Puzzle, //
    UV_Light_Required, //
    Snowshoes, //
    Cross_Country_Skis, //
    Special_Tool_Required, //
    Night_Cache, //
    Park_and_Grab, //
    Abandoned_Structure, //
    Short_hike, //
    Medium_hike, //
    Long_Hike, //
    Fuel_Nearby, //
    Food_Nearby, //
    Wireless_Beacon, // 60
    Partnership_Cache, // 61
    Seasonal_Access, // 62
    Tourist_Friendly, // 63
    Tree_Climbing, // 64
    Front_Yard, // 65
    Teamwork_Required, // 66
    GeoTour, // 67
    Challenge,
    Bonus,
    Powertrail,
    Solutionchecker,
    ;

    private static HashMap<Attribute, Integer> attributeLookup;
    private boolean negative = false;

    public static DLong GetAttributeDlong(Attribute attrib) {
        return DLong.shift(GetAttributeID(attrib));
    }

    public static int GetAttributeID(Attribute attrib) {
        if (attributeLookup == null)
            ini();
        return attributeLookup.get(attrib);
    }

    public static Attribute getAttributeEnumByGcComId(int id) {
        switch (id) {
            case 1:
                return Dogs;
            case 2:
                return Access_or_parking_fee;
            case 3:
                return Climbing_gear;
            case 4:
                return Boat;
            case 5:
                return Scuba_gear;
            case 6:
                return Recommended_for_kids;
            case 7:
                return Takes_less_than_an_hour;
            case 8:
                return Scenic_view;
            case 9:
                return Significant_Hike;
            case 10:
                return Difficult_climbing;
            case 11:
                return May_require_wading;
            case 12:
                return May_require_swimming;
            case 13:
                return Available_at_all_times;
            case 14:
                return Recommended_at_night;
            case 15:
                return Available_during_winter;
            case 16:
                return Cactus;
            case 17:
                return Poison_plants;
            case 18:
                return Dangerous_Animals;
            case 19:
                return Ticks;
            case 20:
                return Abandoned_mines;
            case 21:
                return Cliff_falling_rocks;
            case 22:
                return Hunting;
            case 23:
                return Dangerous_area;
            case 24:
                return Wheelchair_accessible;
            case 25:
                return Parking_available;
            case 26:
                return Public_transportation;
            case 27:
                return Drinking_water_nearby;
            case 28:
                return Public_restrooms_nearby;
            case 29:
                return Telephone_nearby;
            case 30:
                return Picnic_tables_nearby;
            case 31:
                return Camping_available;
            case 32:
                return Bicycles;
            case 33:
                return Motorcycles;
            case 34:
                return Quads;
            case 35:
                return Off_road_vehicles;
            case 36:
                return Snowmobiles;
            case 37:
                return Horses;
            case 38:
                return Campfires;
            case 39:
                return Thorns;
            case 40:
                return Stealth_required;
            case 41:
                return Stroller_accessible;
            case 42:
                return Needs_maintenance;
            case 43:
                return Watch_for_livestock;
            case 44:
                return Flashlight_required;
            case 45:
                return Lost_And_Found_Tour;
            case 46:
                return Truck_Driver;
            case 47:
                return Field_Puzzle;
            case 48:
                return UV_Light_Required;
            case 49:
                return Snowshoes;
            case 50:
                return Cross_Country_Skis;
            case 51:
                return Special_Tool_Required;
            case 52:
                return Night_Cache;
            case 53:
                return Park_and_Grab;
            case 54:
                return Abandoned_Structure;
            case 55:
                return Short_hike;
            case 56:
                return Medium_hike;
            case 57:
                return Long_Hike;
            case 58:
                return Fuel_Nearby;
            case 59:
                return Food_Nearby;
            case 60:
                return Wireless_Beacon;
            case 61:
                return Partnership_Cache;
            case 62:
                return Seasonal_Access;
            case 63:
                return Tourist_Friendly;
            case 64:
                return Tree_Climbing;
            case 65:
                return Front_Yard;
            case 66:
                return Teamwork_Required;
            case 67:
                return GeoTour;
            case 68:
                return Default; // nicht bekannt
            case 69:
                return Bonus;
            case 70:
                return Powertrail;
            case 71:
                return Challenge;
            case 72:
                return Solutionchecker;
        }

        return Default;
    }

    private static void ini() {
        attributeLookup = new HashMap<>();
        attributeLookup.put(Default, 0); // 0 == Default.ordinal() and so on
        attributeLookup.put(Dogs, 1);
        attributeLookup.put(Access_or_parking_fee, 2);
        attributeLookup.put(Climbing_gear, 3);
        attributeLookup.put(Boat, 4);
        attributeLookup.put(Scuba_gear, 5);
        attributeLookup.put(Recommended_for_kids, 6);
        attributeLookup.put(Takes_less_than_an_hour, 7);
        attributeLookup.put(Scenic_view, 8);
        attributeLookup.put(Significant_Hike, 9);
        attributeLookup.put(Difficult_climbing, 10);
        attributeLookup.put(May_require_wading, 11);
        attributeLookup.put(May_require_swimming, 12);
        attributeLookup.put(Available_at_all_times, 13);
        attributeLookup.put(Recommended_at_night, 14);
        attributeLookup.put(Available_during_winter, 15);
        attributeLookup.put(Cactus, 16);
        attributeLookup.put(Poison_plants, 17);
        attributeLookup.put(Dangerous_Animals, 18);
        attributeLookup.put(Ticks, 19);
        attributeLookup.put(Abandoned_mines, 20);
        attributeLookup.put(Cliff_falling_rocks, 21);
        attributeLookup.put(Hunting, 22);
        attributeLookup.put(Dangerous_area, 23);
        attributeLookup.put(Wheelchair_accessible, 24);
        attributeLookup.put(Parking_available, 25);
        attributeLookup.put(Public_transportation, 26);
        attributeLookup.put(Drinking_water_nearby, 27);
        attributeLookup.put(Public_restrooms_nearby, 28);
        attributeLookup.put(Telephone_nearby, 29);
        attributeLookup.put(Picnic_tables_nearby, 30);
        attributeLookup.put(Camping_available, 31);
        attributeLookup.put(Bicycles, 32);
        attributeLookup.put(Motorcycles, 33);
        attributeLookup.put(Quads, 34);
        attributeLookup.put(Off_road_vehicles, 35);
        attributeLookup.put(Snowmobiles, 36);
        attributeLookup.put(Horses, 37);
        attributeLookup.put(Campfires, 38);
        attributeLookup.put(Thorns, 39);
        attributeLookup.put(Stealth_required, 40);
        attributeLookup.put(Stroller_accessible, 41);
        attributeLookup.put(Needs_maintenance, 42);
        attributeLookup.put(Watch_for_livestock, 43);
        attributeLookup.put(Flashlight_required, 44);
        attributeLookup.put(Lost_And_Found_Tour, 45);
        attributeLookup.put(Truck_Driver, 46);
        attributeLookup.put(Field_Puzzle, 47);
        attributeLookup.put(UV_Light_Required, 48);
        attributeLookup.put(Snowshoes, 49);
        attributeLookup.put(Cross_Country_Skis, 50);
        attributeLookup.put(Special_Tool_Required, 51);
        attributeLookup.put(Night_Cache, 52);
        attributeLookup.put(Park_and_Grab, 53);
        attributeLookup.put(Abandoned_Structure, 54);
        attributeLookup.put(Short_hike, 55);
        attributeLookup.put(Medium_hike, 56);
        attributeLookup.put(Long_Hike, 57);
        attributeLookup.put(Fuel_Nearby, 58);
        attributeLookup.put(Food_Nearby, 59);
        attributeLookup.put(Wireless_Beacon, 60);
        attributeLookup.put(Partnership_Cache, 61);
        attributeLookup.put(Seasonal_Access, 62);
        attributeLookup.put(Tourist_Friendly, 63);
        attributeLookup.put(Tree_Climbing, 64);
        attributeLookup.put(Front_Yard, 65);
        attributeLookup.put(Teamwork_Required, 66);
        attributeLookup.put(GeoTour, 67);
        // no longer == geocaching.com
        attributeLookup.put(Challenge, 68);
        attributeLookup.put(Bonus, 69);
        attributeLookup.put(Powertrail, 70);
        attributeLookup.put(Solutionchecker, 71);
    }

    public static HashMap<Attribute, Integer> getAttributeLookup() {
        if (attributeLookup == null)
            ini();
        return attributeLookup;
    }

    public String getImageName() {
        if (attributeLookup == null)
            ini();
        String ret = "att_" + attributeLookup.get(this);

        if (negative) {
            ret += "_0";
        } else {
            ret += "_1";
        }
        return ret;
    }

    @Override
    public String toString() {
        switch (this) {
            case Abandoned_Structure:
                return "Abandoned Structure";
            case Abandoned_mines:
                return "Abandoned mines";
            case Access_or_parking_fee:
                return "Access or parking fee";
            case Available_at_all_times:
                return "Available at all times";
            case Available_during_winter:
                return "Available during winter";
            case Camping_available:
                return "Camping available";
            case Cliff_falling_rocks:
                return "Cliff falling rocks";
            case Climbing_gear:
                return "Climbing gear";
            case Cross_Country_Skis:
                return "Cross Country Skis";
            case Dangerous_Animals:
                return "Dangerous Animals";
            case Dangerous_area:
                return "Dangerous area";
            case Difficult_climbing:
                return "Difficult climbing";
            case Drinking_water_nearby:
                return "Drinking water nearby";
            case Field_Puzzle:
                return "Field Puzzle";
            case Flashlight_required:
                return "Flashlight required";
            case Food_Nearby:
                return "Food Nearby";
            case Front_Yard:
                return "Front Yard";
            case Fuel_Nearby:
                return "Fuel Nearby";
            case Long_Hike:
                return "Long Hike";
            case Lost_And_Found_Tour:
                return "Lost And Found Tour";
            case May_require_swimming:
                return "May require swimming";
            case May_require_wading:
                return "May require wading";
            case Medium_hike:
                return "Medium hike";
            case Needs_maintenance:
                return "Needs maintenance";
            case Night_Cache:
                return "Night Cache";
            case Off_road_vehicles:
                return "Off road vehicles";
            case Park_and_Grab:
                return "Park and Grab";
            case Parking_available:
                return "Parking available";
            case Partnership_Cache:
                return "Partnership Cache";
            case Picnic_tables_nearby:
                return "Picnic tables nearby";
            case Poison_plants:
                return "Poison plants";
            case Public_restrooms_nearby:
                return "Public restrooms nearby";
            case Public_transportation:
                return "Public transportation";
            case Recommended_at_night:
                return "Recommended at night";
            case Recommended_for_kids:
                return "Recommended for kids";
            case Scenic_view:
                return "Scenic view";
            case Scuba_gear:
                return "Scuba gear";
            case Seasonal_Access:
                return "Seasonal Access";
            case Short_hike:
                return "Short hike";
            case Significant_Hike:
                return "Significant Hike";
            case Special_Tool_Required:
                return "Special Tool Required";
            case Stealth_required:
                return "Stealth required";
            case Stroller_accessible:
                return "Stroller accessible";
            case Takes_less_than_an_hour:
                return "Takes less than an hour";
            case Teamwork_Required:
                return "Teamwork Required";
            case Telephone_nearby:
                return "Telephone nearby";
            case Tourist_Friendly:
                return "Tourist Friendly";
            case Tree_Climbing:
                return "Tree Climbing";
            case Truck_Driver:
                return "Truck Driver";
            case UV_Light_Required:
                return "UV Light Required";
            case Watch_for_livestock:
                return "Watch for livestock";
            case Wheelchair_accessible:
                return "Wheelchair accessible";
            case Wireless_Beacon:
                return "Wireless Beacon";
            case Challenge:
                return "Challenge cache";
            case Bonus:
                return "Bonus cache";
            case Powertrail:
                return "Power trail";
            case Solutionchecker:
                return "Geocaching.com solution checker";
            default:
                break;
        }
        return super.toString();
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }
}