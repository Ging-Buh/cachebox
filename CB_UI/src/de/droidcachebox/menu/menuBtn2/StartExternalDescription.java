/*
 * Copyright (C) 2015 team-cachebox.de
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
package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.ex_import.DescriptionImageGrabber;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.log.Log;

/**
 * @author Longri
 */
public class StartExternalDescription extends AbstractAction {

    private static final String sClass = "StartExternalDescription";
    private static StartExternalDescription instance;
    private final LinkedList<String> NonLocalImages = new LinkedList<>();
    private final LinkedList<String> NonLocalImagesUrl = new LinkedList<>();

    private StartExternalDescription() {
        super("descExt");
    }

    public static StartExternalDescription getInstance() {
        if (instance == null) instance = new StartExternalDescription();
        return instance;
    }

    /**
     * execute
     */
    @Override
    public void execute() {
        if (GlobalCore.isSetSelectedCache()) {
            CachesDAO cachesDAO = new CachesDAO();

            //save desc Html local and show ext
            Cache cache = GlobalCore.getSelectedCache();
            NonLocalImages.clear();
            NonLocalImagesUrl.clear();
            String cachehtml = cachesDAO.getShortDescription(cache) + cachesDAO.getDescription(cache);
            String html = DescriptionImageGrabber.resolveImages(cache, cachehtml, false, NonLocalImages, NonLocalImagesUrl);
            String header = "<!DOCTYPE html><html><head><meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" /></head><body>";
            html = header + html;

            // add 2 empty lines so that the last line of description can be selected with the markers
            // add trailer
            html += "</br></br>" + "</body></html>";

            String TEMP_CACHE_HTML_FILE = "temp.html";
            String filePath = Settings.imageCacheFolder.getValue() + "/" + TEMP_CACHE_HTML_FILE;

            try {

                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8);
                out.write(html);
                out.close();

                Platform.callUrl("file://" + filePath);

            } catch (IOException ex) {
                Log.err(sClass, "Write Temp HTML", ex);
            }
        }
    }

    @Override
    public boolean getEnabled() {
        return GlobalCore.isSetSelectedCache();
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.docIcon.name());
    }
}
