package de.droidcachebox.locator.map;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;

public class Track {
    private final ArrayList<TrackPoint> trackPoints;
    private String name;
    private Color color;
    private String fileName;
    private boolean isVisible;
    private boolean isActualTrack;
    private double trackLength;
    private double altitudeDifference;

    public Track(String name) {
        trackPoints = new ArrayList<>();
        this.name = name;
        color = Color.MAGENTA; // or do config?
        fileName = "";
        trackLength = 0;
        isVisible = false;
        isActualTrack = false;
        altitudeDifference = 0;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isActualTrack() {
        return isActualTrack;
    }

    public void setActualTrack(boolean actualTrack) {
        isActualTrack = actualTrack;
    }

    public double getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(double trackLength) {
        this.trackLength = trackLength;
    }

    public double getAltitudeDifference() {
        return altitudeDifference;
    }

    public void setAltitudeDifference(double altitudeDifference) {
        this.altitudeDifference = altitudeDifference;
    }
}