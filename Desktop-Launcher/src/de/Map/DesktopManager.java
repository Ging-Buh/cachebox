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
package de.Map;

import CB_Locator.Map.*;
import CB_Locator.Map.TileGL.TileState;
import CB_UI_Base.graphics.extendedInterfaces.ext_GraphicFactory;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import org.mapsforge.map.awt.graphics.ext_AwtGraphicFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author ging-buh
 * @author Longri
 */
public class DesktopManager extends ManagerBase {

    public DesktopManager() {
    }

    @Override
    public ext_GraphicFactory getGraphicFactory(float Scalefactor) {
        return ext_AwtGraphicFactory.getInstance(Scalefactor);
    }

    @Override
    protected TileGL getTileGL(Layer layer, Descriptor desc, int ThreadIndex) {

        if (layer.isMapsForge()) {
            return getMapsforgeTileGL_Bmp(layer, desc, ThreadIndex);
        }
        // else
        com.badlogic.gdx.graphics.Pixmap.Format format = layer.isOverlay() ? com.badlogic.gdx.graphics.Pixmap.Format.RGBA4444 : com.badlogic.gdx.graphics.Pixmap.Format.RGB565;
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
                            imgData = getImageDataWithColorMatrixManipulation(imgData);
                            b = getImageFromData(imgData);
                        }

                        return new TileGL_Bmp(desc, b, TileState.Present, format);
                    }
                }
            }
            // Kein Map Pack am Start!
            // Falls Kachel im Cache liegt, diese von dort laden!
            if (cachedTileAge != 0) {
                File myImageFile = FileFactory.createFile(cachedTileFilename);
                BufferedImage img = ImageIO.read(myImageFile.getFileInputStream());
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                ImageIO.write(img, "png", bas);
                byte[] data = bas.toByteArray();

                if (CB_UI_Base_Settings.nightMode.getValue()) {
                    ImageData imgData = getImagePixel(data);
                    imgData = getImageDataWithColorMatrixManipulation(imgData);
                    data = getImageFromData(imgData);
                }

                return new TileGL_Bmp(desc, data, TileState.Present, format);
            }
        } catch (Exception exc) {
            /*
             * #if DEBUG Global.AddLog("Manager.LoadLocalBitmap: " + exc.ToString()); #endif
             */
        }
        return null;
    }

    private ImageData getImagePixel(byte[] img) {
        InputStream in = new ByteArrayInputStream(img);
        BufferedImage bImage;
        try {
            bImage = ImageIO.read(in);
        } catch (IOException e) {
            return null;
        }

        ImageData imgData = new ImageData();
        imgData.width = bImage.getWidth();
        imgData.height = bImage.getHeight();

        BufferedImage intimg = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(bImage, intimg);

        Raster ras = intimg.getData();
        DataBufferInt db = (DataBufferInt) ras.getDataBuffer();
        imgData.PixelColorArray = db.getData();

        return imgData;

    }

    private byte[] getImageFromData(ImageData imgData) {

        BufferedImage dstImage = new BufferedImage(imgData.width, imgData.height, BufferedImage.TYPE_INT_RGB);

        dstImage.getRaster().setDataElements(0, 0, imgData.width, imgData.height, imgData.PixelColorArray);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            ImageIO.write(dstImage, "png", bas);
        } catch (IOException e) {
            return null;
        }
        return bas.toByteArray();
    }

    @Override
    public PackBase getMapPack(String file) throws IOException {
        return new DesktopPack(file);
    }

}
