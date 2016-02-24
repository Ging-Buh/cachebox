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
package CB_UI_Base.GL_UI.Controls.html;

import java.util.List;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_Utils.Lists.CB_List;

/**
 * 
 * @author Longri
 *
 */
public class Html_ListView extends Box implements ListLayout {

	final List<Html_Segment> segmentList;
	private final CB_List<CB_View_Base> segmentViewList = new CB_List<CB_View_Base>();
	private final float tapMargin;
	private final HTML_Segment_List seg;
	private final int tabCount;
	private final float bulletWidth;

	public Html_ListView(float innerWidth, HTML_Segment_List seg) {

	this.bulletWidth = seg.getFondSize() * 1.5f;

	this.seg = seg;
	this.tabCount = seg.getTabLevel();
	this.segmentList = seg.getSegmentList();

	this.tapMargin = HtmlView.margin * 0.11f;

	float calcWidth = innerWidth - ((tapMargin * tabCount) + this.bulletWidth);

	this.setWidth(calcWidth);

	for (List<Html_Segment> listItems : seg.items) {
		HtmlView.addViewsToBox(listItems, segmentViewList, calcWidth * 2, this);
	}

	layout(segmentViewList);

	}

	@Override
	public void layout(CB_List<CB_View_Base> segmentViewList) {

	this.removeChilds();

	float contentHeight = 0;
	for (int i = 0, n = segmentViewList.size(); i < n; i++) {
		contentHeight += segmentViewList.get(i).getHeight() + (HtmlView.margin * 2);
	}

	this.setHeight(contentHeight);
	this.setZeroPos();
	this.setX(HtmlView.margin + this.tapMargin);

	this.setMargins(0, HtmlView.margin * 2);
	this.initRow();

	for (int i = 0, n = segmentViewList.size(); i < n; i++) {

		Html_ListView_Bullet bulletView = new Html_ListView_Bullet(i + 1, this.bulletWidth, segmentViewList.get(i).getHeight(), seg.getBulletNumber());
		this.addNext(bulletView, -1);
		this.addLast(segmentViewList.get(i));
	}
	}

	public float getContentWidth() {
	return this.getWidth();
	}

	@Override
	public void resize(float width, float height) {
	System.out.println();
	}

}
