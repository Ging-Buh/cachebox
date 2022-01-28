package de.droidcachebox.dataclasses;

import de.droidcachebox.utils.DLong;

public enum Attribute {
    Default("Default"), // 0
    Dogs("Dogs"), // 1
    Access_or_parking_fee("Access or parking fee"), // 2
    Climbing_gear("Climbing gear"), // 3
    Boat("Boat"), // 4
    Scuba_gear("Scuba gear"), // 5
    Recommended_for_kids("Recommended for kids"), // 6
    Takes_less_than_an_hour("Takes less than an hour"), // 7
    Scenic_view("Scenic view"), // 8
    Significant_Hike("Significant Hike"), // 9
    Difficult_climbing("Difficult climbing"), // 10
    May_require_wading("May require wading"), // 11
    May_require_swimming("May require swimming"), // 12
    Available_at_all_times("Available at all times"), // 13
    Recommended_at_night("Recommended at night"), // 14
    Available_during_winter("Available during winter"), // 15
    Cactus("Cactus"), // 16
    Poison_plants("Poison plants"), // 17
    Dangerous_Animals("Dangerous Animals"), // 18
    Ticks("Ticks"), // 19
    Abandoned_mines("Abandoned mines"), // 20
    Cliff_falling_rocks("Cliff falling rocks"), // 21
    Hunting("Hunting"), // 22
    Dangerous_area("Dangerous area"), //
    Wheelchair_accessible("Wheelchair accessible"), //
    Parking_available("Parking available"), //
    Public_transportation("Public transportation"), //
    Drinking_water_nearby("Drinking water nearby"), //
    Public_restrooms_nearby("Public restrooms nearby"), //
    Telephone_nearby("Telephone nearby"), //
    Picnic_tables_nearby("Picnic tables nearby"), //
    Camping_available("Camping available"), //
    Bicycles("Bicycles"), //
    Motorcycles("Motorcycles"), //
    Quads("Quads"), //
    Off_road_vehicles("Off road vehicles"), //
    Snowmobiles("Snowmobiles"), //
    Horses("Horses"), //
    Campfires("Campfires"), //
    Thorns("Thorns"), //
    Stealth_required("Stealth required"), //
    Stroller_accessible("Stroller accessible"), //
    Needs_maintenance("Needs maintenance"), //
    Watch_for_livestock("Watch for livestock"), //
    Flashlight_required("Flashlight required"), //
    Lost_And_Found_Tour("Lost And Found Tour"), //
    Truck_Driver("Truck Driver"), //
    Field_Puzzle("Field Puzzle"), //
    UV_Light_Required("UV Light Required"), //
    Snowshoes("Snowshoes"), //
    Cross_Country_Skis("Cross Country Skis"), //
    Special_Tool_Required("Special Tool Required"), //
    Night_Cache("Night Cache"), //
    Park_and_Grab("Park and Grab"), //
    Abandoned_Structure("Abandoned Structure"), //
    Short_hike("Short hike"), //
    Medium_hike("Medium hike"), //
    Long_Hike("Long Hike"), //
    Fuel_Nearby("Fuel Nearby"), //
    Food_Nearby("Food Nearby"), //
    Wireless_Beacon("Wireless Beacon"), // 60
    Partnership_Cache("Partnership Cache"), // 61
    Seasonal_Access("Seasonal Access"), // 62
    Tourist_Friendly("Tourist Friendly"), // 63
    Tree_Climbing("Tree Climbing"), // 64
    Front_Yard("Front Yard"), // 65
    Teamwork_Required("Teamwork Required"), // 66
    GeoTour("GeoTour"), // 67
    Challenge("Challenge cache"),
    Bonus("Bonus cache"),
    Powertrail("Power trail"),
    Solutionchecker("Geocaching.com solution checker"),
    ;

    public String string;
    private boolean negative = false;

    Attribute(String string) {
        this.string = string;
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

    public DLong getDLong() {
        return DLong.shift(ordinal());
    }

    public String getImageName() {
        String ret = "att_" + ordinal();
        if (negative) {
            ret += "_0";
        } else {
            ret += "_1";
        }
        return ret;
    }

    @Override
    public String toString() {
        return string;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

}