package de.droidcachebox.gdx.views;

import static de.droidcachebox.gdx.controls.messagebox.MsgBox.BTN_LEFT_POSITIVE;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.droidcachebox.TrackList;
import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.ColorPicker;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.menu.menuBtn3.executes.TrackListView;
import de.droidcachebox.settings.CB_UI_Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

public class TrackListViewItem extends ListViewItemBackground {
    private final static String log = "TrackListViewItem";
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

    public TrackListViewItem(CB_RectF rec, int index, Track track) {
        super(rec, index, track.getName());
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
                cm.addMenuItem("rename", null, this::setTrackName);
                cm.addMenuItem("save", Sprites.getSprite(Sprites.IconName.save.name()), this::saveAsFile);
                cm.addMenuItem("unload", null, this::unloadTrack);

                // (rename, save,) delete darf nicht mit dem aktuellen Track gemacht werden....
                if (!this.track.isActualTrack()) {
                    if (this.track.getFileName().length() > 0) {
                        if (!this.track.isActualTrack()) {
                            AbstractFile trackAbstractFile = FileFactory.createFile(this.track.getFileName());
                            if (trackAbstractFile.exists()) {
                                cm.addMenuItem("delete", Sprites.getSprite(Sprites.IconName.DELETE.name()), () -> MsgBox.show(Translation.get("DeleteTrack"),
                                        Translation.get("DeleteTrack"),
                                        MsgBoxButton.YesNo,
                                        MsgBoxIcon.Question,
                                        (which, data) -> {
                                            if (which == BTN_LEFT_POSITIVE) {
                                                try {
                                                    trackAbstractFile.delete();
                                                    TrackList.getInstance().removeTrack(this.track);
                                                    TrackListView.getInstance().notifyDataSetChanged();
                                                } catch (Exception ex) {
                                                    MsgBox.show(ex.toString(), Translation.get("Error"), MsgBoxButton.OK, MsgBoxIcon.Error, null);
                                                }
                                            }
                                            return true;
                                        }));
                            }
                        }
                    }
                }

                cm.show();
            }
            return true;
        });
    }

    private void positionLatLon() {
        if (track.getTrackPoints().size() > 0) {
            TrackPoint trackpoint = track.getTrackPoints().get(0);
            double latitude = trackpoint.y;
            double longitude = trackpoint.x;
            ShowMap.getInstance().execute();
            ShowMap.getInstance().normalMapView.setBtnMapStateToFree(); // btn
            // ShowMap.getInstance().normalMapView.setMapState(MapViewBase.MapState.FREE);
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
            trackLength.setText(Translation.get("length") + ": " + UnitFormatter.distanceString((float) track.getTrackLength()) + " / " + UnitFormatter.distanceString((float) track.getAltitudeDifference()));
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

        if (track.isVisible()) {
            chkOn.draw(batch);
        } else {
            chkOff.draw(batch);
        }

    }

    private void checkBoxIconClicked() {
        GL.that.RunOnGL(() -> {
            track.setVisible(!track.isVisible());
            TrackList.getInstance().trackListChanged();
        });
        GL.that.renderOnce();
    }

    private void colorIconClicked() {
        GL.that.RunOnGL(() -> {
            ColorPicker clrPick = new ColorPicker(track.getColor(), color -> {
                if (color == null) return;
                track.setColor(color);
                colorReck = null;
            });
            clrPick.show();
        });
        GL.that.renderOnce();
    }

    public void notifyTrackChanged() {
        if (trackLength != null)
            trackLength.setText(Translation.get("length") + ": " + UnitFormatter.distanceString((float) track.getTrackLength()) + " / " + UnitFormatter.distanceString((float) track.getAltitudeDifference()));
    }

    public Track getTrack() {
        return track;
    }

    private void setTrackName() {
        StringInputBox.show(WrapType.SINGLELINE, track.getName(), Translation.get("RenameTrack"), track.getName(), (which, data) -> {
            String text = StringInputBox.editText.getText();
            if (which == BTN_LEFT_POSITIVE) {
                track.setName(text);
                TrackListView.getInstance().notifyDataSetChanged();
            }
            return true;
        });
        TrackListView.getInstance().notifyDataSetChanged();
    }

    private void saveAsFile() {
        if (track.getName().length() > 0) {
            new FileOrFolderPicker(CB_UI_Settings.TrackFolder.getValue(),
                    Translation.get("SaveTrack"),
                    Translation.get("save"),
                    abstractFile -> {
                        if (abstractFile != null) {
                            String trackName = track.getName().replaceAll("[^a-zA-Z0-9_\\.\\-]", "_");
                            String extension = track.getName().toLowerCase().endsWith(".gpx") ? "" : ".gpx";
                            AbstractFile f = FileFactory.createFile(abstractFile, trackName + extension);
                            saveRoute(f, track);
                            if (f.exists()) {
                                track.setFileName(f.getAbsolutePath());
                                Log.info(log, f.getAbsolutePath() + " saved.");
                            } else {
                                Log.err(log, "Error saving " + abstractFile + "/" + track.getName() + extension);
                            }
                        }
                    }).show();
        } else {
            // existing gpx-file
            new FileOrFolderPicker(CB_UI_Settings.TrackFolder.getValue(),
                    "*.gpx",
                    Translation.get("SaveTrack"),
                    Translation.get("save"),
                    abstractFile -> {
                        if (abstractFile != null) {
                            saveRoute(abstractFile, track);
                            Log.debug("TrackListViewItem", "Load Track :" + abstractFile);
                        }
                    }).show();
        }
    }

    private void saveRoute(AbstractFile gpxAbstractFile, Track track) {
        FileWriter writer = null;
        try {
            writer = gpxAbstractFile.getFileWriter();
            try {
                writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.append(
                        "<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");

                Date now = new Date();
                SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                String sDate = datFormat.format(now);
                datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                sDate += "T" + datFormat.format(now) + "Z";
                writer.append("<time>").append(sDate).append("</time>\n");

                writer.append("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n");

                writer.append("<trk>\n");
                writer.append("<name>").append(track.getName()).append("</name>\n");
                writer.append("<extensions>\n<gpxx:TrackExtension>\n");
                writer.append("<gpxx:ColorRGB>").append(track.getColor().toString()).append("</gpxx:ColorRGB>\n");
                writer.append("</gpxx:TrackExtension>\n</extensions>\n");
                writer.append("<trkseg>\n");
                writer.flush();
            } catch (IOException e) {
                Log.err(log, "SaveTrack", e);
            }
        } catch (IOException e1) {
            Log.err(log, "SaveTrack", e1);
        }

        if (writer != null) {
            try {
                for (int i = 0; i < track.getTrackPoints().size(); i++) {
                    writer.append("<trkpt lat=\"").append(String.valueOf(track.getTrackPoints().get(i).y)).append("\" lon=\"").append(String.valueOf(track.getTrackPoints().get(i).x)).append("\">\n");

                    writer.append("   <ele>").append(String.valueOf(track.getTrackPoints().get(i).elevation)).append("</ele>\n");
                    Date dtmp = track.getTrackPoints().get(i).date;
                    if (dtmp != null) {
                        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        String sDate = datFormat.format(dtmp);
                        datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                        sDate += "T" + datFormat.format(track.getTrackPoints().get(i).date) + "Z";
                        writer.append("   <time>").append(sDate).append("</time>\n");
                    }
                    writer.append("</trkpt>\n");
                }
                writer.append("</trkseg>\n");
                writer.append("</trk>\n");
                writer.append("</gpx>\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                Log.err(log, "SaveTrack", e);
            }
        }
    }

    private void unloadTrack() {
        if (track.isActualTrack()) {
            MsgBox.show(Translation.get("IsActualTrack"), null, MsgBoxButton.OK, MsgBoxIcon.Warning, null);
        } else {
            TrackList.getInstance().removeTrack(track); // index passt nicht mehr
            TrackListView.getInstance().notifyDataSetChanged();
            dispose();
        }
    }
}
