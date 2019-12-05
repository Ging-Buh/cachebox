package de.droidcachebox.locator.map;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;

public class Track {
    public final ArrayList<TrackPoint> trackPoints;
    public String name;
    public String fileName;
    public boolean showRoute = false;
    public boolean isActualTrack = false;
    public Color color;
    public double trackLength;
    public double altitudeDifference;

    public Track(String name, Color color) {
        trackPoints = new ArrayList<>();
        this.name = name;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}