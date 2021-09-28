package de.droidcachebox.components;

import static de.droidcachebox.gdx.math.UiSizes.getInstance;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.droidcachebox.Global;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.R;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.LogEntry;
import de.droidcachebox.database.LogType;
import de.droidcachebox.gdx.math.CB_Rect;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.utils.ActivityUtils;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

public class CacheDraw {

    public static CB_Rect BearingRec;
    private static int voteWidth = 0;
    private static int rightBorder = 0;
    private static int nameLayoutWidthRightBorder = 0;
    private static int nameLayoutWidth = 0;
    private static Paint dtPaint;
    private static TextPaint namePaint;

    public static void drawInfo(Cache cache, Canvas canvas, int width, int height, int BackgroundColor, DrawStyle drawStyle) {
        int x = 0;
        int y = 0;
        CB_Rect DrawChangedRect = new CB_Rect(x, y, width, height);
        drawInfo(cache, canvas, DrawChangedRect, BackgroundColor, drawStyle, false);
    }

    /*
    public static void drawInfo(Cache cache, int Width, int Height, DrawStyle drawStyle) {
        if (cachedBitmap == null || !(cachedBitmapId == cache.Id)) {
            cachedBitmap = Bitmap.createBitmap(Width, Height, Bitmap.Config.ARGB_8888);
            CB_Rect newRec = new CB_Rect(0, 0, Width, Height);
            Canvas scaledCanvas = new Canvas(cachedBitmap);
            scaledCanvas.drawColor(Color.TRANSPARENT);
            drawInfo(cache, scaledCanvas, newRec, Color.TRANSPARENT, Color.TRANSPARENT, drawStyle, false);
            cachedBitmapId = cache.Id;
        }

    }
     */

    /*
    public static void drawInfo(Cache cache, Canvas canvas, CB_Rect rec, int BackgroundColor, DrawStyle drawStyle, float scale) {
        if (cachedBitmap == null || !(cachedBitmapId == cache.Id)) {
            cachedBitmap = Bitmap.createBitmap(rec.getWidth(), rec.getHeight(), Bitmap.Config.ARGB_8888);
            CB_Rect newRec = new CB_Rect(0, 0, rec.getWidth(), rec.getHeight());
            Canvas scaledCanvas = new Canvas(cachedBitmap);
            scaledCanvas.drawColor(Color.TRANSPARENT);
            drawInfo(cache, scaledCanvas, newRec, BackgroundColor, Color.RED, drawStyle, false);
            cachedBitmapId = cache.Id;
        }

        if (cachedBitmapPaitnt == null) {
            cachedBitmapPaitnt = new Paint();
            cachedBitmapPaitnt.setAntiAlias(true);
            cachedBitmapPaitnt.setFilterBitmap(true);
            cachedBitmapPaitnt.setDither(true);
        }

        canvas.save();
        canvas.scale(scale, scale);
        canvas.drawBitmap(cachedBitmap, rec.getPos().x / scale, rec.getCrossPos().y / scale, cachedBitmapPaitnt);
        canvas.restore();

    }
     */

    public static void drawInfo(Cache cache, Canvas canvas, CB_Rect rec, int BackgroundColor, DrawStyle drawStyle, boolean withoutBearing) {
        drawInfo(cache, canvas, rec, BackgroundColor, -1, drawStyle, withoutBearing);
    }

    public static void drawInfo(Cache geoCache, Canvas canvas, CB_Rect rec, int backgroundColor, int borderColor, DrawStyle drawStyle, boolean withoutBearing) {
        try {
            // init
            boolean notAvailable = (!geoCache.isAvailable() || geoCache.isArchived());
            boolean globalSelected = geoCache.generatedId == GlobalCore.getSelectedCache().generatedId;
            if (backgroundColor == -1)
                backgroundColor = globalSelected ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground);
            if (borderColor == -1)
                borderColor = Global.getColor(R.attr.ListSeparator);

            final int left = rec.getPos().x + getInstance().getHalfCornerSize();
            final int top = rec.getPos().y + getInstance().getHalfCornerSize();
            final int width = rec.getWidth() - getInstance().getHalfCornerSize();
            final int height = rec.getHeight() - getInstance().getHalfCornerSize();
            final int sdtImageTop = (int) (height - (getInstance().getScaledFontSize() / 0.9)) + rec.getPos().y;
            final int sdtLineTop = sdtImageTop + getInstance().getScaledFontSize();

            // Measure
            if (voteWidth == 0) // Grössen noch nicht berechnet
            {

                voteWidth = getInstance().getScaledIconSize() / 2;

                rightBorder = (int) (width * 0.15);
                nameLayoutWidthRightBorder = width - voteWidth - getInstance().getIconSize() - rightBorder - (getInstance().getScaledFontSize() / 2);
                nameLayoutWidth = width - voteWidth - getInstance().getIconSize() - (getInstance().getScaledFontSize() / 2);
                dtPaint = new Paint();
                dtPaint.setTextSize((float) (getInstance().getScaledFontSize() * 1.3));
                dtPaint.setAntiAlias(true);
            }
            if (namePaint == null) {
                namePaint = new TextPaint();
                namePaint.setTextSize((float) (getInstance().getScaledFontSize() * 1.3));
            }

            // reset namePaint attr
            namePaint.setColor(Global.getColor(R.attr.TextColor));
            namePaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
            namePaint.setAntiAlias(true);

            dtPaint.setColor(Global.getColor(R.attr.TextColor));

            // Draw roundetChangedRect
            ActivityUtils.drawFillRoundRecWithBorder(canvas, rec, 2, borderColor, backgroundColor);

            // Draw Vote
            if (geoCache.gcVoteRating > 0)
                ActivityUtils.putImageScale(canvas, Global.StarIcons[(int) (geoCache.gcVoteRating * 2)], -90, left, top, (double) getInstance().getScaledIconSize() / 160);

            int correctPos = (int) (getInstance().getScaledFontSize() * 1.3);

            // Draw Icon
            if (geoCache.hasCorrectedCoordinatesOrHasCorrectedFinal()) {
                ActivityUtils.putImageTargetHeight(canvas, Global.CacheIconsBig[19], left + voteWidth - correctPos, top - getInstance().getScaledFontSize() / 2, getInstance().getIconSize());
            } else {
                ActivityUtils.putImageTargetHeight(canvas, Global.CacheIconsBig[geoCache.getGeoCacheType().ordinal()], left + voteWidth - correctPos, top - getInstance().getScaledFontSize() / 2, getInstance().getIconSize());
            }

            // Draw Cache Name

            if (notAvailable) {
                namePaint.setColor(Color.RED);
                namePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                namePaint.setAntiAlias(true);
                dtPaint.setAntiAlias(true);
            }

            String dateString = "";

            try {
                SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
                dateString = postFormater.format(geoCache.getDateHidden());
            } catch (Exception ignored) {
            }

            String geoCacheName = (String) TextUtils.ellipsize(geoCache.getGeoCacheName(), namePaint, nameLayoutWidthRightBorder, TextUtils.TruncateAt.END);

            String drawName = (drawStyle == DrawStyle.withOwner) ? "by " + geoCache.getOwner() + ", " + dateString : geoCacheName;

            if (drawStyle == DrawStyle.all) {
                drawName += "\n";
                drawName += geoCache.getGeoCacheCode();
            } else if (drawStyle == DrawStyle.withOwner) {
                drawName = drawName + "\n" + geoCache.getCoordinate().formatCoordinate() + "\n" + geoCache.getGeoCacheCode();
            }

            StaticLayout layoutCacheName;
            if (drawStyle == DrawStyle.withOwnerAndName || drawStyle == DrawStyle.withOwner) {
                layoutCacheName = new StaticLayout(drawName, namePaint, nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            } else {
                layoutCacheName = new StaticLayout(drawName, namePaint, nameLayoutWidthRightBorder, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }

            int LayoutHeight = ActivityUtils.drawStaticLayout(canvas, layoutCacheName, left + voteWidth + getInstance().getIconSize() + 5, top);

            // over draw 3. Cache name line
            int VislinesHeight = LayoutHeight * 2 / layoutCacheName.getLineCount();
            if (layoutCacheName.getLineCount() > 2) {
                Paint backPaint = new Paint();
                backPaint.setColor(backgroundColor);
                // backPaint.setColor(Color.RED); //DEBUG

                canvas.drawRect(new Rect(left + voteWidth + getInstance().getIconSize() + 5, sdtImageTop, nameLayoutWidthRightBorder + left + voteWidth + getInstance().getIconSize() + 5, top + LayoutHeight + VislinesHeight - 7), backPaint);
            }

            // Draw owner and Last Found
            if (drawStyle == DrawStyle.withOwnerAndName && geoCache.getOwner() != null) {
                String drawText; //  = "by " + geoCache.getOwner() + ", " + dateString;

                // trim Owner Name length
                int counter = 0;
                do {
                    drawText = "by " + geoCache.getOwner().substring(0, geoCache.getOwner().length() - counter) + ", " + dateString;
                    counter++;
                } while (((int) namePaint.measureText(drawText)) >= nameLayoutWidth);

                drawText = drawText + "\n" + "\n" + geoCache.getCoordinate().formatCoordinate() + "\n" + geoCache.getGeoCacheCode() + "\n";

                String LastFound = getLastFoundLogDate(geoCache);
                if (!LastFound.equals("")) {
                    drawText += "\n";
                    drawText += "last found: " + LastFound;
                }
                StaticLayout layoutCacheOwner = new StaticLayout(drawText, namePaint, nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                // layoutCacheOwner= new StaticLayout(DrawText, 0, 30, namePaint,
                // nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false,
                // TextUtils.TruncateAt.START, nameLayoutWidth);
                ActivityUtils.drawStaticLayout(canvas, layoutCacheOwner, left + voteWidth + getInstance().getIconSize() + 5, top + (VislinesHeight / 2));
            }

            // Draw S/D/T
            int sdtleft = left + 2;
            String geoCacheSize;
            switch (geoCache.geoCacheSize) {
                case micro:
                    geoCacheSize = "M"; // micro;
                    break;
                case small:
                    geoCacheSize = "S"; // small;
                    break;
                case regular:
                    geoCacheSize = "R"; // regular;
                    break;
                case large:
                    geoCacheSize = "L"; // large;
                    break;
                default:
                    geoCacheSize = "O"; // other;
                    break;
            }
            canvas.drawText(geoCacheSize, sdtleft, sdtLineTop, dtPaint);
            sdtleft += getInstance().getSpaceWidth();
            sdtleft += ActivityUtils.putImageTargetHeight(canvas, Global.SizeIcons[(geoCache.geoCacheSize.ordinal())], sdtleft, sdtImageTop, getInstance().getScaledFontSize());
            sdtleft += getInstance().getTabWidth();
            canvas.drawText("D", sdtleft, sdtLineTop, dtPaint);
            sdtleft += getInstance().getSpaceWidth();
            sdtleft += ActivityUtils.putImageTargetHeight(canvas, Global.StarIcons[(int) (geoCache.getDifficulty() * 2)], sdtleft, sdtImageTop, getInstance().getScaledFontSize());
            sdtleft += getInstance().getTabWidth();
            canvas.drawText("T", sdtleft, sdtLineTop, dtPaint);
            sdtleft += getInstance().getSpaceWidth();
            sdtleft += ActivityUtils.putImageTargetHeight(canvas, Global.StarIcons[(int) (geoCache.getTerrain() * 2)], sdtleft, sdtImageTop, getInstance().getScaledFontSize());
            sdtleft += getInstance().getSpaceWidth();

            // Draw TB
            int numTb = geoCache.numTravelbugs;
            if (numTb > 0) {
                sdtleft += ActivityUtils.putImageScale(canvas, Global.Icons[0], -90, sdtleft, (int) (sdtImageTop - (getInstance().getScaledFontSize() / (getInstance().getTbIconSize() * 0.1))),
                        (double) getInstance().getScaledFontSize() / getInstance().getTbIconSize());
                // SDTleft += space;
                if (numTb > 1)
                    canvas.drawText("x" + numTb, sdtleft, sdtLineTop, dtPaint);
            }

            // Draw Bearing

            if (drawStyle != DrawStyle.withoutBearing && drawStyle != DrawStyle.withOwnerAndName && !withoutBearing) {

                int BearingHeight = (int) ((rec.getRight() - rightBorder < sdtleft) ? rec.getTop() - (getInstance().getScaledFontSize() * 2) : rec.getTop() - (getInstance().getScaledFontSize() * 0.8));

                if (BearingRec == null)
                    BearingRec = new CB_Rect(rec.getRight() - rightBorder, rec.getBottom(), rec.getRight(), BearingHeight);
                drawBearing(geoCache, canvas, BearingRec);
            }

            if (geoCache.isFound()) {

                ActivityUtils.putImageTargetHeight(canvas, Global.Icons[2], left + voteWidth - correctPos + getInstance().getIconSize() / 2, top - getInstance().getScaledFontSize() / 2 + getInstance().getIconSize() / 2,
                        getInstance().getIconSize() / 2);// Smile
            }

            if (geoCache.isFavorite()) {
                ActivityUtils.putImageTargetHeight(canvas, Global.Icons[19], left + voteWidth - correctPos + 2, top, getInstance().getIconSize() / 2);
            }

            if (geoCache.isArchived()) {
                ActivityUtils.putImageTargetHeight(canvas, Global.Icons[24], left + voteWidth - correctPos + 2, top, getInstance().getIconSize() / 2);
            } else if (!geoCache.isAvailable()) {
                ActivityUtils.putImageTargetHeight(canvas, Global.Icons[14], left + voteWidth - correctPos + 2, top, getInstance().getIconSize() / 2);
            }

            if (geoCache.iAmTheOwner()) {
                ActivityUtils.putImageTargetHeight(canvas, Global.Icons[17], left + voteWidth - correctPos + getInstance().getIconSize() / 2, top - getInstance().getScaledFontSize() / 2 + getInstance().getIconSize() / 2,
                        getInstance().getIconSize() / 2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void drawBearing(Cache cache, Canvas canvas, CB_Rect drawingRec) {
        if (Locator.getInstance().isValid()) {
            Coordinate position = Locator.getInstance().getMyPosition();
            double heading = Locator.getInstance().getHeading();
            double bearing = CoordinateGPS.Bearing(CalculationType.FAST, position.getLatitude(), position.getLongitude(), cache.getCoordinate().getLatitude(), cache.getCoordinate().getLongitude());
            double cacheBearing = bearing - heading;
            String cacheDistance = UnitFormatter.distanceString(cache.recalculateAndGetDistance(CalculationType.FAST, false, Locator.getInstance().getMyPosition()));
            drawBearing(canvas, drawingRec, cacheDistance, cacheBearing);
        }
    }

    private static void drawBearing(Canvas canvas, CB_Rect drawingRec, String Distance, double Bearing) {
        double scale = (double) getInstance().getScaledFontSize() / getInstance().getArrowScaleList();
        ActivityUtils.putImageScale(canvas, Global.Arrows[1], Bearing, drawingRec.getLeft(), drawingRec.getBottom(), scale);
        canvas.drawText(Distance, drawingRec.getLeft(), drawingRec.getTop(), dtPaint);
    }

    private static String getLastFoundLogDate(Cache cache) {
        String foundDate = "";
        CB_List<LogEntry> logs = Database.getLogs(cache);
        int n = logs.size();
        for (int i = 0; i < n; i++) {
            LogEntry l = logs.get(i);
            if (l.logType == LogType.found) {
                try {
                    SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
                    foundDate = postFormater.format(l.logDate);
                } catch (Exception ignored) {
                }
                break;
            }
        }
        Log.debug("CacheDraw", "ready getLastFoundLogDate");
        return foundDate;
    }

    // Draw Metods
    public enum DrawStyle {
        all, // alle infos
        withoutBearing, // ohne Richtungs-Pfeil
        withoutSeparator, // ohne unterster trennLinie
        withOwner, // mit Owner statt Name
        withOwnerAndName // mit Owner und Name
    }

}
