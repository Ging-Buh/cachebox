package de.droidcachebox.locator.map;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;

public class Track {
    public final ArrayList<TrackPoint> trackPoints;
    private String name;
    private Color color;
    public String fileName;
    public boolean isVisible = false;
    public boolean isActualTrack = false;
    public double trackLength;
    public double altitudeDifference;

    public Track(String name, Color color) {
        trackPoints = new ArrayList<>();
        this.name = name;
        this.color = color;
        trackLength = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}