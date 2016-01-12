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
package CB_UI.GL_UI.Views;

import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Linearlayout;
import CB_UI_Base.GL_UI.Controls.Linearlayout.LayoutChanged;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UI_Size_Base;

public class CreditsView extends CB_View_Base {
    private float ref, lineHeight, margin;
    private Image logo;
    private ScrollBox scrollBox;
    private Linearlayout layout;

    private ArrayList<Person> getPersons() {
	ArrayList<Person> list = new ArrayList<CreditsView.Person>();

	list.add(new Person("hannes!", Job.idea, "2009-2011"));
	list.add(new Person("Stonefinger", Job.designer));
	list.add(new Person("Groundspeak API", Job.service, SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal())));
	list.add(new Person(null, Job.library, SpriteCacheBase.getThemedSprite("libgdx")));// Name at Logo image
	list.add(new Person("Mapsforge", Job.library, SpriteCacheBase.getThemedSprite("mapsforge_logo")));
	list.add(new Person("OpenRouteService.org", Job.service, SpriteCacheBase.getThemedSprite("openrouteservice_logo")));
	list.add(new Person("OpenStreetMap", Job.service, SpriteCacheBase.getThemedSprite("osm_logo")));
	list.add(new Person("Ging-Buh", Job.developer));
	list.add(new Person("Longri", Job.developer));
	list.add(new Person("ersthelfer", Job.developer));
	list.add(new Person("arbor95", Job.developer));
	list.add(new Person("droogi", Job.tester));
	list.add(new Person("droogi", Job.localization));
	list.add(new Person("jGda", Job.localization));
	list.add(new Person("Teleskopix", Job.tester));
	list.add(new Person("hulkman", Job.localization));
	list.add(new Person("Lady-in-blue", Job.tester));
	list.add(new Person("Koblenzer", Job.tester));
	list.add(new Person("GeoSilverio", Job.tester));
	list.add(new Person("GeoPfaff", Job.tester));
	list.add(new Person("Homer-S", Job.tester));
	list.add(new Person("Mozartkugel", Job.tester));
	list.add(new Person("millimeterfuchser", Job.tester));
	list.add(new Person("Larsie", Job.developmentAdvice));
	list.add(new Person("KoiMuggele", Job.designer));

	Collections.sort(list);

	return list;
    }

    public CreditsView(CB_RectF rec, String Name) {
	super(rec, Name);
	this.setBackground(SpriteCacheBase.AboutBack);

	ref = UI_Size_Base.that.getWindowHeight() / 13;
	CB_RectF CB_LogoRec = new CB_RectF(this.getHalfWidth() - (ref * 2.5f), this.getHeight() - ((ref * 5) / 4.11f) - ref, ref * 5, (ref * 5) / 4.11f);

	logo = new Image(CB_LogoRec, "Logo", false);
	logo.setDrawable(SpriteCacheBase.logo);
	this.addChild(logo);

	scrollBox = new ScrollBox(rec);
	scrollBox.setHeight(logo.getY() - (ref / 2));
	scrollBox.setZeroPos();
	// scrollBox.setBackground(new ColorDrawable(Color.RED)); //Debug

	this.addChild(scrollBox);

	layout = new Linearlayout(rec.getWidth(), "LinearLayout");
	layout.setLayoutChangedListener(new LayoutChanged() {

	    @Override
	    public void LayoutIsChanged(Linearlayout linearLayout, float newHeight) {
		scrollBox.setVirtualHeight(newHeight);
	    }
	});

	scrollBox.addChild(layout);

    }

    @Override
    protected void Initial() {
	margin = UI_Size_Base.that.getMargin();

	lineHeight = Fonts.Measure("Tg").height * 1.6f;
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
	title = Translation.Get(title);
	Box box = new Box(new CB_RectF(0, 0, this.getWidth(), lineHeight * 1.2f), "");
	Label label = new Label(this.name + " label", box, title + ":");
	label.setFont(Fonts.getBig());
	label.setHAlignment(HAlignment.CENTER);
	box.addChild(label);
	layout.addChild(box);
    }

    private void addPersonToLayout(Job job) {
	for (Person item : getPersons()) {
	    if (item.job == job) {
		String entry = item.name;
		float itemHeight = (item.image != null) ? GL_UISizes.Info.getHeight() : lineHeight;
		if (item.desc != null)
		    entry += "  (" + item.desc + ")";

		Box box = new Box(new CB_RectF(0, 0, this.getWidth(), itemHeight), "");

		if (entry != null) {
		    box.addChild(new Label(this.name + " boxLabel", box, entry).setHAlignment(HAlignment.CENTER));
		}

		layout.addChild(box);
		if (item.image != null) {

		    float sideRatio = item.image.getHeight() / item.image.getWidth();
		    float imageWidth = itemHeight / sideRatio;
		    float xPos = (this.getHalfWidth() - (Fonts.Measure(entry).width / 2)) - itemHeight - margin - margin;
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
    protected void SkinIsChanged() {

    }

    @Override
    public void resize(float width, float height) {
	logo.setY(this.getHeight() - ((ref * 5) / 4.11f) - ref);
	scrollBox.setHeight(logo.getY() - (ref / 2));
    }

    public enum Job {
	idea, developer, designer, tester, sponsor, library, service, localization, developmentAdvice
    }

    public class Person implements Comparable<Person> {

	public String name;
	public String nick;
	public Job job;
	public String email;
	public String desc = null;
	public Sprite image = null;

	public Person(String Name, Job job) {
	    this.job = job;
	    this.name = Name;
	}

	public Person(String Name, Job job, String desc) {
	    this.job = job;
	    this.name = Name;
	    this.desc = desc;
	}

	public Person(String Name, Job job, Sprite image) {
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
