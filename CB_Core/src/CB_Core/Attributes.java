package CB_Core;

import CB_Core.Types.DLong;

import java.util.ArrayList;
import java.util.HashMap;

public enum Attributes {
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
    GeoTour // 67
    ;

    private static HashMap<Attributes, Integer> attributeLookup;
    private boolean negative = false;

    public static DLong GetAttributeDlong(Attributes attrib) {
        return DLong.shift(GetAttributeID(attrib));
    }

    public static int GetAttributeID(Attributes attrib) {
        if (attributeLookup == null)
            ini();
        return attributeLookup.get(attrib);
    }

    public static Attributes getAttributeEnumByGcComId(int id) {
        switch (id) {
            case 1:
                return CB_Core.Attributes.Dogs;
            case 2:
                return CB_Core.Attributes.Access_or_parking_fee;
            case 3:
                return CB_Core.Attributes.Climbing_gear;
            case 4:
                return CB_Core.Attributes.Boat;
            case 5:
                return CB_Core.Attributes.Scuba_gear;
            case 6:
                return CB_Core.Attributes.Recommended_for_kids;
            case 7:
                return CB_Core.Attributes.Takes_less_than_an_hour;
            case 8:
                return CB_Core.Attributes.Scenic_view;
            case 9:
                return CB_Core.Attributes.Significant_Hike;
            case 10:
                return CB_Core.Attributes.Difficult_climbing;
            case 11:
                return CB_Core.Attributes.May_require_wading;
            case 12:
                return CB_Core.Attributes.May_require_swimming;
            case 13:
                return CB_Core.Attributes.Available_at_all_times;
            case 14:
                return CB_Core.Attributes.Recommended_at_night;
            case 15:
                return CB_Core.Attributes.Available_during_winter;
            case 16:
                return CB_Core.Attributes.Cactus;
            case 17:
                return CB_Core.Attributes.Poison_plants;
            case 18:
                return CB_Core.Attributes.Dangerous_Animals;
            case 19:
                return CB_Core.Attributes.Ticks;
            case 20:
                return CB_Core.Attributes.Abandoned_mines;
            case 21:
                return CB_Core.Attributes.Cliff_falling_rocks;
            case 22:
                return CB_Core.Attributes.Hunting;
            case 23:
                return CB_Core.Attributes.Dangerous_area;
            case 24:
                return CB_Core.Attributes.Wheelchair_accessible;
            case 25:
                return CB_Core.Attributes.Parking_available;
            case 26:
                return CB_Core.Attributes.Public_transportation;
            case 27:
                return CB_Core.Attributes.Drinking_water_nearby;
            case 28:
                return CB_Core.Attributes.Public_restrooms_nearby;
            case 29:
                return CB_Core.Attributes.Telephone_nearby;
            case 30:
                return CB_Core.Attributes.Picnic_tables_nearby;
            case 31:
                return CB_Core.Attributes.Camping_available;
            case 32:
                return CB_Core.Attributes.Bicycles;
            case 33:
                return CB_Core.Attributes.Motorcycles;
            case 34:
                return CB_Core.Attributes.Quads;
            case 35:
                return CB_Core.Attributes.Off_road_vehicles;
            case 36:
                return CB_Core.Attributes.Snowmobiles;
            case 37:
                return CB_Core.Attributes.Horses;
            case 38:
                return CB_Core.Attributes.Campfires;
            case 39:
                return CB_Core.Attributes.Thorns;
            case 40:
                return CB_Core.Attributes.Stealth_required;
            case 41:
                return CB_Core.Attributes.Stroller_accessible;
            case 42:
                return CB_Core.Attributes.Needs_maintenance;
            case 43:
                return CB_Core.Attributes.Watch_for_livestock;
            case 44:
                return CB_Core.Attributes.Flashlight_required;
            case 45:
                return CB_Core.Attributes.Lost_And_Found_Tour;
            case 46:
                return CB_Core.Attributes.Truck_Driver;
            case 47:
                return CB_Core.Attributes.Field_Puzzle;
            case 48:
                return CB_Core.Attributes.UV_Light_Required;
            case 49:
                return CB_Core.Attributes.Snowshoes;
            case 50:
                return CB_Core.Attributes.Cross_Country_Skis;
            case 51:
                return CB_Core.Attributes.Special_Tool_Required;
            case 52:
                return CB_Core.Attributes.Night_Cache;
            case 53:
                return CB_Core.Attributes.Park_and_Grab;
            case 54:
                return CB_Core.Attributes.Abandoned_Structure;
            case 55:
                return CB_Core.Attributes.Short_hike;
            case 56:
                return CB_Core.Attributes.Medium_hike;
            case 57:
                return CB_Core.Attributes.Long_Hike;
            case 58:
                return CB_Core.Attributes.Fuel_Nearby;
            case 59:
                return CB_Core.Attributes.Food_Nearby;
            case 60:
                return CB_Core.Attributes.Wireless_Beacon;
            case 61:
                return CB_Core.Attributes.Partnership_Cache;
            case 62:
                return CB_Core.Attributes.Seasonal_Access;
            case 63:
                return CB_Core.Attributes.Tourist_Friendly;
            case 64:
                return CB_Core.Attributes.Tree_Climbing;
            case 65:
                return CB_Core.Attributes.Front_Yard;
            case 66:
                return CB_Core.Attributes.Teamwork_Required;
            case 67:
                return CB_Core.Attributes.GeoTour;
        }

        return CB_Core.Attributes.Default;
    }

    private static void ini() {
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
        attributeLookup.put(Attributes.GeoTour, 67);
    }

    public static ArrayList<Attributes> getAttributes(DLong attributesPositive, DLong attributesNegative) {
        ArrayList<Attributes> ret = new ArrayList<Attributes>();
        if (attributeLookup == null)
            ini();
        for (Attributes attribute : attributeLookup.keySet()) {
            DLong att = Attributes.GetAttributeDlong(attribute);
            if ((att.BitAndBiggerNull(attributesPositive))) {
                attribute.negative = false;
                ret.add(attribute);
            }
        }
        for (Attributes attribute : attributeLookup.keySet()) {
            DLong att = Attributes.GetAttributeDlong(attribute);
            if ((att.BitAndBiggerNull(attributesNegative))) {
                attribute.negative = true;
                ret.add(attribute);
            }
        }

        return ret;
    }

    public String getImageName() {
        if (attributeLookup == null)
            ini();
        String ret = "att_" + String.valueOf(attributeLookup.get(this));

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
            case Bicycles:
                break;
            case Boat:
                break;
            case Cactus:
                break;
            case Campfires:
                break;
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
            case Default:
                break;
            case Difficult_climbing:
                return "Difficult climbing";
            case Dogs:
                break;
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
            case GeoTour:
                break;
            case Horses:
                break;
            case Hunting:
                break;
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
            case Motorcycles:
                break;
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
            case Quads:
                break;
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
            case Snowmobiles:
                break;
            case Snowshoes:
                break;
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
            case Thorns:
                break;
            case Ticks:
                break;
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
            default:
                break;

        }

        return super.toString();
    }

}