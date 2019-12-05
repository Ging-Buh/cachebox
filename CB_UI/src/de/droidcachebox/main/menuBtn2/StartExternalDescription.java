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
package de.droidcachebox.main.menuBtn2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.ex_import.DescriptionImageGrabber;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.utils.log.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * @author Longri
 */
public class StartExternalDescription extends AbstractAction {

    private static final String log = "StartExternalDescription";
    private static StartExternalDescription that;
    private final String TEMP_CACHE_HTML_FILE = "temp.html";
    private final LinkedList<String> NonLocalImages = new LinkedList<>();
    private final LinkedList<String> NonLocalImagesUrl = new LinkedList<>();

    private StartExternalDescription() {
        super("descExt", MenuID.AID_SHOW_DescExt);
    }

    public static StartExternalDescription getInstance() {
        if (that == null) that = new StartExternalDescription();
        return that;
    }

    /**
     * execute
     */
    @Override
    public void execute() {
        if (getEnabled()) {

            //save desc Html local and show ext
            Cache cache = GlobalCore.getSelectedCache();
            NonLocalImages.clear();
            NonLocalImagesUrl.clear();
            String cachehtml = Database.getShortDescription(cache) + Database.getDescription(cache);
            String html = DescriptionImageGrabber.resolveImages(cache, cachehtml, false, NonLocalImages, NonLocalImagesUrl);
            String header = "<!DOCTYPE html><html><head><meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" /></head><body>";
            html = header + html;

            // add 2 empty lines so that the last line of description can be selected with the markers
            // add trailer
            html += "</br></br>" + "</body></html>";

            String filePath = CB_UI_Base_Settings.ImageCacheFolder.getValue() + "/" + TEMP_CACHE_HTML_FILE;

            try {

                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filePath), Charset.forName("utf-8"));
                out.write(html);
                out.close();

                PlatformUIBase.callUrl("file://" + filePath);

            } catch (IOException ex) {
                Log.err(log, "Write Temp HTML", ex);
            }
        }
    }

    @Override
    public boolean getEnabled() {
        // liefert true zurück wenn ein Cache gewählt ist und dieser einen Hint hat
        if (GlobalCore.getSelectedCache() == null)
            return false;

        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.docIcon.name());
    }
}
