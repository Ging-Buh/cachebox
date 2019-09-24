/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.Map;

import CB_Locator.Map.*;
import CB_Locator.Map.TileGL.TileState;
import CB_UI_Base.graphics.extendedInterfaces.ext_GraphicFactory;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.HSV_Color;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.graphics.BitmapFactory;
import com.badlogic.gdx.graphics.Pixmap.Format;
import org.mapsforge.map.android.graphics.ext_AndroidGraphicFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author ging-buh
 * @author Longri
 */
public class AndroidManager extends ManagerBase {
    private static final String log = "AndroidManager";

    public AndroidManager() {
    }

    @Override
    public PackBase getMapPack(String file) throws IOException {
        return new AndroidPack(file);
    }


    @Override
    protected TileGL getTileGL(Layer layer, Descriptor desc, int ThreadIndex) {

        if (layer.isMapsForge()) {
            return getMapsforgeTileGL_Bmp(layer, desc, ThreadIndex);
        }

        Format format = layer.isOverlay() ? Format.RGBA4444 : Format.RGB565;
        try {
            // Schauen, ob Tile im Cache liegt
            String cachedTileFilename = layer.GetLocalFilename(desc);

            long cachedTileAge = 0;

            if (FileIO.fileExists(cachedTileFilename)) {
                File info = FileFactory.createFile(cachedTileFilename);
                cachedTileAge = info.lastModified();
            }

            // Kachel im Pack suchen
            for (int i = 0; i < mapPacks.size(); i++) {
                PackBase mapPack = mapPacks.get(i);
                if ((mapPack.layer.Name.equalsIgnoreCase(layer.Name)) && (mapPack.MaxAge >= cachedTileAge)) {
                    BoundingBox bbox = mapPacks.get(i).contains(desc);

                    if (bbox != null) {
                        byte[] b = mapPacks.get(i).LoadFromBoundingBoxByteArray(bbox, desc);

                        if (CB_UI_Base_Settings.nightMode.getValue()) {
                            ImageData imgData = getImagePixel(b);
                            imgData = getImageDataWithColormatrixManipulation(HSV_Color.NIGHT_COLOR_MATRIX, imgData);
                            b = getImageFromData(imgData);
                        }

                        TileGL_Bmp bmpTile = new TileGL_Bmp(desc, b, TileState.Present, format);
                        return bmpTile;
                    }
                }
            }
            // Kein Map Pack am Start!
            // Falls Kachel im Cache liegt, diese von dort laden!
            if (cachedTileAge != 0) {
                android.graphics.Bitmap result = BitmapFactory.decodeFile(cachedTileFilename);
                if (result != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    result.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] b = stream.toByteArray();

                    if (CB_UI_Base_Settings.nightMode.getValue()) {
                        ImageData imgData = getImagePixel(b);
                        imgData = getImageDataWithColormatrixManipulation(HSV_Color.NIGHT_COLOR_MATRIX, imgData);
                        b = getImageFromData(imgData);
                    }

                    TileGL_Bmp bmpTile = new TileGL_Bmp(desc, b, TileState.Present, format);
                    return bmpTile;
                }
            }
        } catch (Exception exc) {
            Log.err(log, "Exception", exc);
        }
        return null;
    }

    /*
    public android.graphics.Bitmap LoadLocalBitmap(Layer layer, Descriptor desc) {
        try {
            // Schauen, ob Tile im Cache liegt
            String cachedTileFilename = layer.GetLocalFilename(desc);

            long cachedTileAge = 0;

            if (FileIO.fileExists(cachedTileFilename)) {
                File info = FileFactory.createFile(cachedTileFilename);
                cachedTileAge = info.lastModified();
            }

            // Kachel im Pack suchen
            for (int i = 0; i < mapPacks.size(); i++) {
                AndroidPack mapPack = (AndroidPack) mapPacks.get(i);
                if ((mapPack.layer.Name.equalsIgnoreCase(layer.Name)) && (mapPack.MaxAge >= cachedTileAge)) {
                    BoundingBox bbox = mapPacks.get(i).contains(desc);

                    if (bbox != null)
                        return ((AndroidPack) (mapPacks.get(i))).LoadFromBoundingBox(bbox, desc);
                }
            }
            // Kein Map Pack am Start!
            // Falls Kachel im Cache liegt, diese von dort laden!
            if (cachedTileAge != 0) {
                return BitmapFactory.decodeFile(cachedTileFilename);
            }
        } catch (Exception exc) {

        }
        return null;
    }
     */

    private ImageData getImagePixel(byte[] img) {
        android.graphics.Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
        // Buffer dst = null;
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        // bitmap.getPixels(pixels, 0, 0, 0, 0, bitmap.getWidth(), bitmap.getHeight());

        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        ImageData imgData = new ImageData();
        imgData.width = bitmap.getWidth();
        imgData.height = bitmap.getHeight();
        imgData.PixelColorArray = pixels;

        return imgData;
    }

    private byte[] getImageFromData(ImageData imgData) {
        android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(imgData.PixelColorArray, imgData.width, imgData.height, android.graphics.Bitmap.Config.RGB_565);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return b;
    }

    @Override
    public ext_GraphicFactory getGraphicFactory(float Scalefactor) {
        return ext_AndroidGraphicFactory.getInstance(Scalefactor);
    }
}
