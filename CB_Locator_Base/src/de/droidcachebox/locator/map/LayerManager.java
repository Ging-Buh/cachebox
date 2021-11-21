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
package de.droidcachebox.locator.map;

import static de.droidcachebox.settings.AllSettings.MapPackFolder;
import static de.droidcachebox.settings.AllSettings.MapPackFolderLocal;
import static de.droidcachebox.settings.AllSettings.UserMap1;
import static de.droidcachebox.settings.AllSettings.UserMap2;
import static de.droidcachebox.settings.AllSettings.currentMapLayer;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Collections;

import de.droidcachebox.locator.map.Layer.LayerUsage;
import de.droidcachebox.locator.map.Layer.MapType;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.log.Log;

/**
 * collects all possible layers into the  ArrayList<Layer> layers with the follwing Definitions:
 * LayerUsage are "normal" and "overlay" where both can be of MapType "ONLINE", "MAPSFORGE", "FREIZEITKARTE", "MapPack"
 * a better splitting may be Online (web, url) / Offline (file) and all do deliver a bitmap - tile
 * The possibilities to generate tiles of size 256x256 pixels are from:
 * + MapPack (Offline): tiles are assembled in a file as defined by CacheBox
 * + TileServer (Online): download the tiles as png (or jpg) by giving X, Y and Zoom in an url.
 * .... X goes from 0 (left edge is 180 °W) to 2**zoom − 1 (right edge is 180 °E)
 * .... Y goes from 0 (top edge is 85.0511 °N) to 2**zoom − 1 (bottom edge is 85.0511 °S)
 * .... see https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
 * + TMS (Online): a xml description for tile definition (url)
 * + BSH (Online): interpreting a java method getTileUrl(zoom,x,y) in definition file (.bsh) to get the special url for X,Y,Zoom
 * + MapsForge (Offline): getting tiles from a file in mapsforge format (one of these can be taken from Freizeitkarte)
 * <p>
 * not (directly) supported are tileservers with quadkey numbering system (BING), wms-servers (a wide range), ...
 */
public class LayerManager {

    private static final String log = "LayerManager";
    private static LayerManager manager;
    private final ArrayList<Layer> fixedLayers;
    private final ArrayList<Layer> layers;
    private final ArrayList<Layer> overlayLayers;

    private LayerManager() {
        layers = new ArrayList<>();
        fixedLayers = new ArrayList<>();
        initFixedLayers();
        overlayLayers = new ArrayList<>();
        initOverlayLayers();
    }

    public static LayerManager getInstance() {
        if (manager == null) manager = new LayerManager();
        return manager;
    }

    public Layer getOverlayLayer(String[] names) {
        // todo seems as if there is more than one overlay possible
        String name = names != null && names.length > 0 ? names[0] : "";
        for (Layer layer : overlayLayers) {
            if (layer.getName().equals(name)) return layer;
        }
        return null;
    }

    public Layer getLayer(String[] names) {

        fillLayerList();

        if (names[0].equals("OSM") || names[0].length() == 0)
            names[0] = "Mapnik";
        for (Layer layer : layers) {
            if (layer.getName().equalsIgnoreCase(names[0])) {
                // add aditional
                // todo : this adding is only necessary when setting from Config. Otherwise the adds are done directly to the layers additionalMapsforgeLayer.
                // therefore checked on additionalMapsforgeLayer.add for duplicates. A bit
                if (names.length > 1) {
                    for (int i = 1; i < names.length; i++) {
                        for (Layer additionalLayer : layers) {
                            if (additionalLayer.getName().equalsIgnoreCase(names[i])) {
                                layer.addAdditionalMap(additionalLayer);
                            }
                        }
                    }
                }
                return layer;
            }
        }
        // default
        Layer layer = getLayer(new String[]{"Mapnik"});
        currentMapLayer.setValue(layer.getAllLayerNames());
        return layer;
    }

    private void initOverlayLayers() {
        overlayLayers.add(new Layer(MapType.ONLINE, LayerUsage.overlay, Layer.StorageType.PNG, "hiking", "hiking", "https://tile.waymarkedtrails.org/hiking/{z}/{x}/{y}.png")); // k 8.10.2019
        overlayLayers.add(new Layer(MapType.ONLINE, LayerUsage.overlay, Layer.StorageType.PNG, "cycling", "cycling", "https://tile.waymarkedtrails.org/cycling/{z}/{x}/{y}.png"));  // k 8.10.2019
        overlayLayers.add(new Layer(MapType.ONLINE, LayerUsage.overlay, Layer.StorageType.PNG, "mtb", "mtb", "https://tile.waymarkedtrails.org/mtb/{z}/{x}/{y}.png")); // k 8.10.2019
        overlayLayers.add(new Layer(MapType.ONLINE, LayerUsage.overlay, Layer.StorageType.PNG, "riding", "riding", "https://tile.waymarkedtrails.org/riding/{z}/{x}/{y}.png")); // k 8.10.2019
        overlayLayers.add(new Layer(MapType.ONLINE, LayerUsage.overlay, Layer.StorageType.PNG, "skating", "skating", "https://tile.waymarkedtrails.org/skating/{z}/{x}/{y}.png")); // k 8.10.2019
        overlayLayers.add(new Layer(MapType.ONLINE, LayerUsage.overlay, Layer.StorageType.PNG, "slopemap", "slopemap", "https://tile.waymarkedtrails.org/slopes/{z}/{x}/{y}.png"));  // k 8.10.2019

        overlayLayers.add(new Layer(MapType.ONLINE, LayerUsage.overlay, Layer.StorageType.PNG, "public_transport", "public_transport", "http://tile.memomaps.de/tilegen/{z}/{x}/{y}.png")); // k 8.10.2019
        overlayLayers.add(new Layer(MapType.ONLINE, LayerUsage.overlay, Layer.StorageType.PNG, "railway", "railway", "https://a.tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png"));// k 8.10.2019
        overlayLayers.add(new Layer(MapType.ONLINE, LayerUsage.overlay, Layer.StorageType.PNG, "hillshading", "hillshading", "https://tiles.wmflabs.org/hillshading/{z}/{x}/{y}.png")); // k 8.10.2019
    }

    private void initFixedLayers() {
        fixedLayers.add(new Layer(MapType.ONLINE, LayerUsage.normal, Layer.StorageType.PNG, "Mapnik", "Mapnik", "https://c.tile.openstreetmap.org/{z}/{x}/{y}.png"));
        fixedLayers.add(new Layer(MapType.ONLINE, LayerUsage.normal, Layer.StorageType.PNG, "OSM Cycle Map", "Open Cycle Map", "http://c.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png"));
        // fixedLayers.add(new Layer(MapType.ONLINE, Type.normal, "Esri", "", "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"));
        // fixedLayers.add(new Layer(MapType.ONLINE, Type.normal, "Google Hybrid", "", "http://mt0.google.com/vt/lyrs=y@142&x={x}&y={y}&z={z}"));
    }

    private void fillLayerList() {
        // after selection of Database
        layers.clear();
        layers.addAll(fixedLayers);

        try {
            String url = UserMap1.getValue();
            if (url.length() == 0) {
                url = UserMap1.getDefaultValue();
            }
            layers.add(getUserMap(url, "UserMap1"));
        } catch (Exception e) {
            Log.err(log, "Init UserMap1", e);
        }

        try {
            String url = UserMap2.getValue();
            if (url.length() > 0) {
                layers.add(getUserMap(url, "UserMap2"));
            }
        } catch (Exception e) {
            Log.err(log, "Init UserMap2", e);
        }

        Array<String> alreadyAdded = new Array<>(); // avoid same file in different directories
        Log.info(log, "dirOwnMaps = " + MapPackFolderLocal.getValue());
        addToLayers(MapPackFolderLocal.getValue(), alreadyAdded);
        Log.info(log, "dirGlobalMaps = " + MapPackFolder.getValue());
        addToLayers(MapPackFolder.getValue(), alreadyAdded);

        Collections.sort(layers, (layer1, layer2) -> layer1.getName().toLowerCase().compareTo(layer2.getName().toLowerCase()));

    }

    private void addToLayers(String directoryName, Array<String> alreadyAdded) {
        String[] fileNames = FileFactory.createFile(directoryName).list();
        if (fileNames != null && fileNames.length > 0) {
            for (String fileName : fileNames) {
                if (!alreadyAdded.contains(fileName, false)) {
                    try {
                        Layer layer;
                        String lowerCaseFileName = fileName.toLowerCase();
                        String pathAndName = directoryName + "/" + fileName;
                        if (lowerCaseFileName.endsWith("pack")) {
                            layer = new MapPackLayer(pathAndName);
                        } else if (lowerCaseFileName.endsWith("map")) {
                            layer = new MapsForgeLayer(pathAndName);
                        } else if (lowerCaseFileName.endsWith("xml")) {
                            layer = new TmsLayer(pathAndName);
                        } else if (lowerCaseFileName.endsWith("bsh")) {
                            layer = new BshLayer(pathAndName);
                        } else continue;
                        layers.add(layer);
                        alreadyAdded.add(fileName);
                    } catch (Exception ex) {
                        Log.err(log, "addToLayers: " + directoryName + "/" + fileName + ex.toString());
                    }
                } else {
                    Log.err(log, "addToLayers: " + directoryName + "/" + fileName + " already entered");
                }
            }
        }
    }

    private Layer getUserMap(String url, String name) {
        try {
            //Log.info(log, "getUserMap by url=" + url + " Name=" + name);
            Layer.StorageType storageType = Layer.StorageType.PNG;
            if (url.contains("{name:")) {
                //replace name
                int pos = url.indexOf("{name:");
                int endPos = url.indexOf("}", pos);
                String nameTag = url.substring(pos, endPos + 1);
                url = url.replace(nameTag, "");
                name = nameTag.replace("{name:", "").replace("}", "");
            }
            if (url.toLowerCase().contains("{jpg}")) {
                storageType = Layer.StorageType.JPG;
                url = url.replace("{JPG}", "").replace("{jpg}", "").trim();
            } else if (url.toLowerCase().contains("{png}")) {
                url = url.replace("{PNG}", "").replace("{png}", "").trim();
            }
            return new Layer(MapType.ONLINE, LayerUsage.normal, storageType, name, "", url);
        } catch (Exception e) {
            Log.err(log, "Err while getUserMap: url=" + url + " Name=" + name + " Err=" + e.getLocalizedMessage());
            return new Layer(MapType.ONLINE, LayerUsage.normal, Layer.StorageType.PNG, name, "", url);
        }
    }

    public ArrayList<Layer> getLayers() {
        return layers;
    }

    public ArrayList<Layer> getOverlayLayers() {
        return overlayLayers;
    }
}
