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

import java.util.ArrayList;
import java.util.Iterator;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_Utils.Lists.CB_List;

/**
 * 
 * @author Longri
 *
 */
public class Html_TableView extends Box implements ListLayout {

    // http://html.nicole-wellinger.ch/tabellen/borderfarbe.html

    private final HTML_Segment_Table seg;

    public Html_TableView(float innerWidth, HTML_Segment_Table seg2) {

	super(innerWidth * 4f, 100f, "HTML_TableView");

	this.seg = seg2;

	if (seg.getBorderSize() > 0) {
	    this.setBorderSize(seg.getBorderSize());
	}
	layout(null);
    }

    @Override
    public void layout(CB_List<CB_View_Base> segmentViewList) {

	this.removeChilds();

	CB_List<Float> colWidth = new CB_List<Float>();
	CB_List<Float> rowHight = new CB_List<Float>();

	this.initRow();

	float lastRowYPos = 0;

	for (ArrayList<ArrayList<Html_Segment>> row : seg.tableSegments) {

	    // create Row box
	    Box rowBox = new Box(this, "Table Row Box");
	    rowBox.initRow();
	    boolean firstCol = true;
	    for (ArrayList<Html_Segment> col : row) {
		// create Col box

		// add content
		segmentViewList = new CB_List<CB_View_Base>();

		HtmlView.addViewsToBox(col, segmentViewList, innerWidth, this);

		Box colBox = HtmlView.addViewsToContentBox(this, segmentViewList);
		rowBox.addNext(colBox, CB_View_Base.FIXED);
		rowBox.setHeight(Math.max(rowBox.getHeight(), colBox.getHeight()));
		if (firstCol) {
		    rowBox.setWidth(colBox.getWidth());
		    firstCol = false;
		} else {
		    rowBox.setWidth(rowBox.getWidth() + colBox.getWidth());
		}

		if (seg.getBorderSize() > 0) {
		    colBox.setBorderSize(seg.getBorderSize());
		}

	    }
	    rowBox.FinaliseRow();

	    rowBox.setY(lastRowYPos);
	    lastRowYPos += rowBox.getHeight();
	    this.setWidth(rowBox.getWidth());
	    this.addChild(rowBox);
	}
	this.setHeight(lastRowYPos);

	// repos rows
	Iterator<GL_View_Base> reverse = this.getchilds().reverseIterator();

	float newYPos = 0;

	// TODO set new ColSize and rowSize

	while (reverse.hasNext()) {
	    GL_View_Base v = reverse.next();
	    v.setY(newYPos);
	    newYPos = v.getMaxY();
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
