/*
 * Copyright (C) 2011-2014 team-cachebox.de
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
package cb_server;

import CB_Locator.LocatorSettings;
import CB_Utils.Util.IChanged;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import de.Map.DesktopManager;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.layer.renderer.MF_DatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.rule.RenderThemeFuture;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MapServlet extends HttpServlet {
    private static final long serialVersionUID = 2094731483963312861L;
    private MF_DatabaseRenderer databaseRenderer;
    private File mapFile;
    private ExternalRenderTheme renderTheme;
    MapFile MF_mapDatabase;
    private final DisplayModel model;
    private static Object syncObject = new Object();
    private RenderThemeFuture renderThemeFuture;

    public MapServlet() {
        model = new DisplayModel();
        new DesktopManager(model);

    }

    IChanged MapsettingChangedListner = new IChanged() {

        @Override
        public void handleChange() {
            setMapSetting();
        }
    };

    private void setMapSetting() {
        GraphicFactory Mapsforge_Factory = AwtGraphicFactory.INSTANCE;
        mapFile = FileFactory.createFile(CBS_Settings.CBS_Mapsforge_Map.getValue());
        File RenderThemeFile = FileFactory.createFile(LocatorSettings.MapsforgeDayTheme.getValue());
        renderTheme = null;
        try {
            renderTheme = new ExternalRenderTheme(RenderThemeFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        renderThemeFuture = new RenderThemeFuture(Mapsforge_Factory, renderTheme, DesktopManager.Manager.DISPLAY_MODEL);
        new Thread(renderThemeFuture).start();


        MF_mapDatabase = new MapFile(mapFile.getAbsolutePath());

        setMapSetting();

        CBS_Settings.CBS_Mapsforge_Map.addSettingChangedListener(MapsettingChangedListner);
        LocatorSettings.MapsforgeDayTheme.addSettingChangedListener(MapsettingChangedListner);


//		MapDataStore mapDataStore, GraphicFactory graphicFactory, TileCache tileCache, TileBasedLabelStore labelStore, boolean renderLabels, boolean cacheLabels
//		databaseRenderer = new CachedDatabaseRenderer(MF_mapDatabase, Mapsforge_Factory);
        databaseRenderer = new MF_DatabaseRenderer(MF_mapDatabase, Mapsforge_Factory, MF_DatabaseRenderer.firstLevelTileCache, null, true, true);
    }

    public MapServlet(String greeting) {
        this();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        synchronized (syncObject) {

            String sQuery = request.getPathInfo().replace(".png", "");
            String[] query = sQuery.split("/");
            int z = Integer.valueOf(query[1]);
            int x = Integer.valueOf(query[2]);
            int y = Integer.valueOf(query[3]);

            response.setContentType("image/png");
            response.setStatus(HttpServletResponse.SC_OK);

            Tile ti = new Tile(x, y, (byte) z, 256);
            RendererJob job = new RendererJob(ti, MF_mapDatabase, renderThemeFuture, model, 1, false, false);
            TileBitmap tile = databaseRenderer.executeJob(job);

            try {
                tile.compress(response.getOutputStream());
                //			tile.compress(os);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
