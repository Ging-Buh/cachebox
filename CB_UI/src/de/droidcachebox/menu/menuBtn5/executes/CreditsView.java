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
package de.droidcachebox.menu.menuBtn5.executes;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;
import java.util.Collections;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.Linearlayout;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;

public class CreditsView extends CB_View_Base {
    private static CreditsView that;
    private final float ref;
    private final Image logo;
    private final ScrollBox scrollBox;
    private final Linearlayout layout;
    private float lineHeight;
    private float margin;

    private CreditsView() {
        super(ViewManager.leftTab.getContentRec(), "CreditsView");
        CB_RectF rec = this;
        this.setBackground(Sprites.aboutback);
        ref = UiSizes.getInstance().getWindowHeight() / 13f;
        CB_RectF CB_LogoRec = new CB_RectF(this.getHalfWidth() - (ref * 2.5f), this.getHeight() - ((ref * 5) / 4.11f) - ref, ref * 5, (ref * 5) / 4.11f);

        logo = new Image(CB_LogoRec, "Logo", false);
        logo.setDrawable(Sprites.logo);
        this.addChild(logo);

        scrollBox = new ScrollBox(rec);
        scrollBox.setHeight(logo.getY() - (ref / 2));
        scrollBox.setZeroPos();
        // scrollBox.setBackground(new ColorDrawable(Color.RED)); //Debug

        this.addChild(scrollBox);

        layout = new Linearlayout(rec.getWidth(), "LinearLayout");
        layout.setLayoutChangedListener((linearLayout, newHeight) -> scrollBox.setVirtualHeight(newHeight));

        scrollBox.addChild(layout);

    }

    public static CreditsView getInstance() {
        if (that == null) that = new CreditsView();
        return that;
    }

    private ArrayList<Person> getPersons() {
        ArrayList<Person> list = new ArrayList<>();

        list.add(new Person("hannes!", Job.idea, "2009-2011"));

        list.add(new Person("Stonefinger", Job.designer));
        list.add(new Person("KoiMuggele", Job.designer));

        list.add(new Person("Groundspeak API", Job.service, Sprites.getSprite("api-Logo-vCOMP2")));

        list.add(new Person("", Job.library, Sprites.getSprite("libgdx")));// Name at Logo image Mario Zechner
        list.add(new Person("Mapsforge", Job.library, Sprites.getSprite("mapsforge_logo")));
        list.add(new Person("OpenStreetMap", Job.service, Sprites.getSprite("osm_logo")));

        list.add(new Person("arbor95", Job.developer));
        list.add(new Person("Longri", Job.developer));
        list.add(new Person("Ging-Buh", Job.developer));

        list.add(new Person("Koblenzer", Job.tester));
        list.add(new Person("GeoLemmi", Job.tester));
        list.add(new Person("Eifelgold", Job.tester));
        list.add(new Person("Homer-S", Job.tester));
        list.add(new Person("Mozartkugel", Job.tester));
        list.add(new Person("Timo TA93", Job.tester));
        list.add(new Person("CacheBoxer", Job.tester));
        list.add(new Person("Nothelfer", Job.tester));
        list.add(new Person("Lady-in-blue", Job.tester));

        list.add(new Person("Larsie", Job.developmentAdvice));

        list.add(new Person("kia71 (Hungarian)", Job.localization)); // Attila Jáborszki (kia71) Hungarian
        list.add(new Person("Crowdin (Czech, Dutch)", Job.localization)); // ?
        list.add(new Person("Crowdin (French, Polish, Portuguese)", Job.localization)); // ?

        Collections.sort(list);

        return list;
    }

    @Override
    protected void renderInit() {
        margin = UiSizes.getInstance().getMargin();

        lineHeight = Fonts.measure("Tg").height * 1.6f;
        layout.removeChilds();

        captioned("conception");
        addPersonToLayout(Job.idea);
        divider();

        captioned("develop");
        addPersonToLayout(Job.developer);
        divider();

        captioned("developAdvice");
        addPersonToLayout(Job.developmentAdvice);
        divider();

        captioned("graphic");
        addPersonToLayout(Job.designer);
        divider();

        captioned("tester");
        addPersonToLayout(Job.tester);
        divider();

        captioned("localization");
        addPersonToLayout(Job.localization);
        divider();

        captioned("library");
        addPersonToLayout(Job.library);
        divider();

        captioned("service");
        addPersonToLayout(Job.service);
        divider();

        captioned("sponsor");
        addPersonToLayout(Job.sponsor);
        divider();
    }

    private void divider() {
        Box box = new Box(new CB_RectF(0, 0, this.getWidth(), lineHeight * 2f), "");
        layout.addChild(box);
    }

    private void captioned(String title) {
        title = Translation.get(title);
        Box box = new Box(new CB_RectF(0, 0, this.getWidth(), lineHeight * 1.2f), "");
        CB_Label label = new CB_Label(this.name + " label", box, title + ":");
        label.setFont(Fonts.getBig());
        label.setHAlignment(HAlignment.CENTER);
        box.addChild(label);
        layout.addChild(box);
    }

    private void addPersonToLayout(Job job) {
        for (Person item : getPersons()) {
            if (item.job == job) {
                String entry = item.name;
                float itemHeight = (item.image != null) ? GL_UISizes.info.getHeight() : lineHeight;
                if (item.desc != null)
                    entry += "  (" + item.desc + ")";

                Box box = new Box(new CB_RectF(0, 0, this.getWidth(), itemHeight), "");

                if (entry != null) {
                    box.addChild(new CB_Label(this.name + " boxLabel", box, entry).setHAlignment(HAlignment.CENTER));
                }

                layout.addChild(box);
                if (item.image != null) {

                    float sideRatio = item.image.getHeight() / item.image.getWidth();
                    float imageWidth = itemHeight / sideRatio;
                    float xPos = (this.getHalfWidth() - (Fonts.measure(entry).width / 2)) - itemHeight - margin - margin;
                    if (entry == null)
                        xPos = this.getHalfWidth() - (imageWidth / 2);
                    Image img = new Image(xPos, 0, imageWidth, itemHeight, "", false);
                    img.setDrawable(new SpriteDrawable(item.image));
                    box.addChild(img);
                }
            }
        }
    }

    @Override
    public void resize(float width, float height) {
        logo.setY(this.getHeight() - ((ref * 5) / 4.11f) - ref);
        scrollBox.setHeight(logo.getY() - (ref / 2));
    }

    public enum Job {
        idea, developer, designer, tester, sponsor, library, service, localization, developmentAdvice
    }

    public static class Person implements Comparable<Person> {

        public String name;
        public String desc = null;
        public Sprite image = null;
        Job job;

        Person(String Name, Job job) {
            this.job = job;
            this.name = Name;
        }

        Person(String Name, Job job, String desc) {
            this.job = job;
            this.name = Name;
            this.desc = desc;
        }

        Person(String Name, Job job, Sprite image) {
            this.job = job;
            this.name = Name;
            this.image = image;
        }

        @Override
        public int compareTo(Person o) {
            if (this.job == Job.developer || o.job == Job.developer)
                return 0;
            if (this.name == null)
                return -1;
            if (o.name == null)
                return 1;
            return this.name.compareToIgnoreCase(o.name);
        }

    }

}
