package de.droidcachebox.gdx.views;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.CB_UI_Settings;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.RouteOverlay;
import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.ColorPicker;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.map.MapViewBase;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.main.menuBtn3.ShowMap;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;

public class TrackListViewItem extends ListViewItemBackground {
    private static Sprite chkOff;
    private static Sprite chkOn;

    private static CB_RectF colorIcon;
    private static CB_RectF checkBoxIcon;
    private static CB_RectF scaledCheckBoxIcon;
    private Track track;
    private float left;
    private CB_Label trackName;
    private CB_Label trackLength;
    private Sprite colorReck;

    public TrackListViewItem(CB_RectF rec, int Index, Track track) {
        super(rec, Index, track.getName());
        this.track = track;
        setClickHandler((v, x, y, pointer, button) -> {
            TrackListViewItem clickedItem = (TrackListViewItem) v;
            TrackListView.getInstance().setSelection(clickedItem.getIndex());
            Vector2 clickedPosition = new Vector2(x, y);
            if (colorIcon.contains(clickedPosition)) {
                colorIconClicked();
            } else if (checkBoxIcon.contains(clickedPosition)) {
                checkBoxIconClicked();
            } else {
                Menu cm = new Menu("TrackRecordMenuTitle");
                cm.addMenuItem("ShowOnMap", Sprites.getSprite(Sprites.IconName.targetDay.name()), this::positionLatLon);
                // rename, save, delete darf nicht mit dem aktuellen Track gemacht werden....
                TrackListViewItem selectedTrackItem = TrackListView.getInstance().getSelectedItem();
                if (selectedTrackItem != null && !selectedTrackItem.getTrack().isActualTrack) {
                    cm.addMenuItem("rename", null, () -> {
                        StringInputBox.Show(WrapType.SINGLELINE, selectedTrackItem.getTrack().getName(), Translation.get("RenameTrack"), selectedTrackItem.getTrack().getName(), (which, data) -> {
                            String text = StringInputBox.editText.getText();
                            switch (which) {
                                case 1: // ok Clicket
                                    selectedTrackItem.getTrack().setName(text);
                                    TrackListView.getInstance().notifyDataSetChanged();
                                    break;
                                case 2: // cancel clicket
                                    break;
                                case 3:
                                    break;
                            }
                            return true;
                        });
                        TrackListView.getInstance().notifyDataSetChanged();
                    });
                    cm.addMenuItem("save", null, () -> PlatformUIBase.getFile(CB_UI_Settings.TrackFolder.getValue(),
                            "*.gpx",
                            Translation.get("SaveTrack"),
                            Translation.get("save"),
                            new PlatformUIBase.IgetFileReturnListener() {
                                TrackListViewItem selectedTrackItem = TrackListView.getInstance().getSelectedItem();

                                @Override
                                public void returnFile(String path) {
                                    if (path != null) {
                                        RouteOverlay.saveRoute(path, selectedTrackItem.getTrack());
                                        Log.debug("TrackListViewItem", "Load Track :" + path);
                                        TrackListView.getInstance().notifyDataSetChanged();
                                    }
                                }
                            }));
                    cm.addMenuItem("unload", null, () -> {
                        TrackListViewItem trackListViewItem = TrackListView.getInstance().getSelectedItem();
                        if (trackListViewItem == null) {
                            MessageBox.show(Translation.get("NoTrackSelected"), null, MessageBoxButton.OK, MessageBoxIcon.Warning, null);
                        } else if (trackListViewItem.getTrack().isActualTrack) {
                            MessageBox.show(Translation.get("IsActualTrack"), null, MessageBoxButton.OK, MessageBoxIcon.Warning, null);
                        } else {
                            RouteOverlay.remove(trackListViewItem.getTrack());
                            TrackListView.getInstance().notifyDataSetChanged();
                        }
                    });
                }
                cm.show();
            }
            return true;
        });
    }

    private void positionLatLon() {
        TrackListViewItem trackListViewItem = TrackListView.getInstance().getSelectedItem();
        ArrayList<TrackPoint> tracklist = trackListViewItem.getTrack().trackPoints;
        if (tracklist.size() > 0) {
            TrackPoint trackpoint = tracklist.get(0);
            double latitude = trackpoint.y;
            double longitude = trackpoint.x;
            ShowMap.getInstance().execute();
            ShowMap.getInstance().normalMapView.setMapStateFree(); // btn
            ShowMap.getInstance().normalMapView.setMapState(MapViewBase.MapState.FREE);
            ShowMap.getInstance().normalMapView.setCenter(new CoordinateGPS(latitude, longitude));
        }
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);
        left = getLeftWidth();
        drawColorRec(batch);
        if (trackName == null || trackLength == null) {
            createLabel();
        }
        drawRightChkBox(batch);
    }

    private void createLabel() {
        if (trackName == null) {
            CB_RectF rec = new CB_RectF(left, getHeight() / 2, getWidth() - left - getHeight() - 10, getHeight() / 2);
            trackName = new CB_Label(rec);
            trackName.setText(track.getName());
            addChild(trackName);
        }

        // draw Length
        if (trackLength == null) {
            CB_RectF rec = new CB_RectF(left, 0, getWidth() - left - getHeight() - 10, getHeight() / 2);
            trackLength = new CB_Label(name + " EntryLength", rec, "");
            trackLength.setText(Translation.get("length") + ": " + UnitFormatter.distanceString((float) track.trackLength) + " / " + UnitFormatter.distanceString((float) track.altitudeDifference));
            addChild(trackLength);
        }

        GL.that.renderOnce();
    }

    private void drawColorRec(Batch batch) {
        if (track == null)
            return;
        if (colorIcon == null) {
            colorIcon = new CB_RectF(0, 0, getHeight(), getHeight());
            colorIcon = colorIcon.scaleCenter(0.95f);
        }

        if (colorReck == null) {
            colorReck = Sprites.getSprite("text-field-back");
            colorReck.setBounds(colorIcon.getX(), colorIcon.getY(), colorIcon.getWidth(), colorIcon.getHeight());
            colorReck.setColor(track.getColor());
        }

        colorReck.draw(batch);

        left += colorIcon.getWidth() + UiSizes.getInstance().getMargin();

    }

    private void drawRightChkBox(Batch batch) {
        if (checkBoxIcon == null || scaledCheckBoxIcon == null) {
            checkBoxIcon = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);
            scaledCheckBoxIcon = checkBoxIcon.scaleCenter(0.8f);
        }

        if (chkOff == null) {
            chkOff = Sprites.getSprite("check-off");
            chkOff.setBounds(scaledCheckBoxIcon.getX(), scaledCheckBoxIcon.getY(), scaledCheckBoxIcon.getWidth(), scaledCheckBoxIcon.getHeight());
        }

        if (chkOn == null) {
            chkOn = Sprites.getSprite("check-on");
            chkOn.setBounds(scaledCheckBoxIcon.getX(), scaledCheckBoxIcon.getY(), scaledCheckBoxIcon.getWidth(), scaledCheckBoxIcon.getHeight());
        }

        if (track.isVisible) {
            chkOn.draw(batch);
        } else {
            chkOff.draw(batch);
        }

    }

    private void checkBoxIconClicked() {
        GL.that.RunOnGL(() -> {
            track.isVisible = !track.isVisible;
            RouteOverlay.trackListChanged();
        });
        GL.that.renderOnce();
    }

    private void colorIconClicked() {
        GL.that.RunOnGL(() -> {
            ColorPicker clrPick = new ColorPicker(ActivityBase.activityRec(), track.getColor(), color -> {
                if (color == null) return;
                track.setColor(color);
                colorReck = null;
            });
            clrPick.show();
        });
        GL.that.renderOnce();
    }

    public void notifyTrackChanged(Track track) {
        this.track = track;
        if (trackLength != null)
            trackLength.setText(Translation.get("length") + ": " + UnitFormatter.distanceString((float) track.trackLength) + " / " + UnitFormatter.distanceString((float) track.altitudeDifference));
    }

    public Track getTrack() {
        return track;
    }

}
