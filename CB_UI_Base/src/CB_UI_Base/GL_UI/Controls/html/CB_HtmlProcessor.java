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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Renderer.ElementHandler;
import net.htmlparser.jericho.Renderer.RemoveElementHandler;
import net.htmlparser.jericho.Renderer.StandardInlineElementHandler;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Tag;

import org.slf4j.LoggerFactory;

import CB_UI_Base.Math.Stack;
import CB_Utils.Lists.CB_List;

/**
 * @author Longri
 */
public class CB_HtmlProcessor extends Processor {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_HtmlProcessor.class);

    final static Stack<Tag> AtributeStack = new Stack<Tag>();
    List<Appendable> apendableList = new ArrayList<Appendable>();
    List<Html_Segment> segmentList;

    HTML_Segment_List actList = null;

    boolean isImage = false;

    public CB_HtmlProcessor(Renderer renderer, Segment rootSegment, int hrLineLength, String newLine, boolean includeHyperlinkURLs, boolean includeAlternateText, boolean decorateFontStyles, boolean convertNonBreakingSpaces, int blockIndentSize, int listIndentSize, char[] listBullets, String tableCellSeparator) {
	super(renderer, rootSegment, Integer.MAX_VALUE, hrLineLength, newLine, includeHyperlinkURLs, includeAlternateText, decorateFontStyles, convertNonBreakingSpaces, blockIndentSize, listIndentSize, listBullets, tableCellSeparator);
    }

    public List<Html_Segment> getElementList() {
	reset();
	segmentList = new ArrayList<Html_Segment>();
	{// CB-CHANGE
	    super.appendable = new StringBuilder();// = appendable;
	}
	List<Element> elements = rootSegment instanceof Element ? Collections.singletonList((Element) rootSegment) : rootSegment.getChildElements();
	try {
	    appendSegmentProcessingChildElements(rootSegment.getBegin(), rootSegment.getEnd(), elements);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	createNewSegment();

	return segmentList;

    }

    @Override
    protected void appendSegment(int begin, final int end) throws IOException {

	Tag tag = source.getPreviousTag(begin);

	if (isOpenStartTag(tag)) {

	    createNewSegment();
	    //	    log.debug("Push Tag >" + tag.toString());
	    AtributeStack.push(tag);
	} else if (isClosedEndTag(tag)) {

	    if (!tag.getName().toLowerCase().equals("a") && !nextIsLI)
		createNewSegment();

	    AtributeStack.pop();

	    //	    Tag pop = AtributeStack.pop();
	    //	    if (pop != null) {
	    //		log.debug("Pop Tag >" + pop.toString());
	    //	    } else {
	    //		log.error("Pop Tag > ERROR Stack are empty");
	    //	    }

	}

	assert begin <= end;
	if (begin < renderedIndex)
	    begin = renderedIndex;
	if (begin >= end)
	    return;
	try {
	    if (preformatted) {
		appendPreformattedSegment(begin, end);
	    } else
		appendNonPreformattedSegment(begin, end);
	} finally {
	    if (renderedIndex < end)
		renderedIndex = end;
	}
    }

    boolean nextIsLI = false;

    public boolean spanelement;
    public boolean listelement;

    void createNewSegment() {
	createNewSegment(false);
    }

    void createNewSegment(boolean force) {
	if (!force && (spanelement || listelement)) {
	    return;
	}

	// Truncate to 2500 chars
	CB_List<String> innerTexts = turncate2500(appendable.toString());
	for (String innerText : innerTexts) {
	    handleInnerText(innerText);
	}

    }

    private void handleInnerText(String innerText) {
	if (innerText != null && !innerText.isEmpty() && isNotSpace(innerText)) {

	    Html_Segment segment;

	    if (nextIsLI && actList != null) {
		//		log.debug("Append new LI element:" + innerText);

		while (innerText.startsWith(" "))
		    innerText = innerText.replaceFirst(" ", "");

		segment = new Html_Segment_TextBlock(AtributeStack, innerText);
		if (!hyperLinkList.isEmpty()) {
		    ((Html_Segment_TextBlock) segment).add(hyperLinkList);
		}
		if (!(segment.formatedText == null || segment.formatedText.isEmpty()))
		    actList.addListEntry(segment);

		appendable = new StringBuilder();
		isImage = false;
		return;

	    }

	    if (actList != null && !actList.getSegmentList().isEmpty()) {
		segmentList.add(actList);
		actList = null;
	    }

	    //	    log.debug("Append Text:" + innerText);

	    if (isImage) {
		segment = new Html_Segment_Image(AtributeStack, innerText);
	    } else {

		segment = new Html_Segment_TextBlock(AtributeStack, innerText);
		if (!hyperLinkList.isEmpty()) {
		    ((Html_Segment_TextBlock) segment).add(hyperLinkList);
		}
	    }

	    if (!(segment.formatedText == null || segment.formatedText.isEmpty()))
		segmentList.add(segment);
	    apendableList.add(appendable);
	    appendable = new StringBuilder();
	    isImage = false;
	}
    }

    CB_List<String> turncate2500(String text) {

	if (text.length() < 2500) {
	    CB_List<String> ret = new CB_List<String>();
	    ret.add(text);
	    return ret;
	}

	CB_List<String> textList = new CB_List<String>();
	String[] split = null;
	while (text.length() > 2500) {
	    split = split(text);
	    textList.add(split[0]);
	    text = split[1];
	}
	textList.add(split[1]);

	return textList;

    }

    private String[] split(String text) {
	//search first line break before 2500 char
	int pos = text.lastIndexOf(this.newLine, 2500);
	String first = text.substring(0, pos);
	String second = text.substring(pos);

	return new String[] { first, second };
    }

    void createNewHrSegment() {
	//	log.debug("Append HR segment:");

	Html_Segment segment = new Html_Segment_HR(AtributeStack);
	segmentList.add(segment);
	appendable = new StringBuilder();
	isImage = false;

    }

    @Override
    protected void appendSegmentProcessingChildElements(final int begin, final int end, final List<Element> childElements) throws IOException {
	int index = begin;
	for (Element childElement : childElements) {
	    if (index >= childElement.getEnd())
		continue;
	    if (index < childElement.getBegin())
		appendSegmentRemovingTags(index, childElement.getBegin());
	    ElementHandler handler = getElementHandler(childElement);
	    handler.process(this, childElement);
	    if (isImage) {
		createNewSegment();
	    }

	    index = Math.max(renderedIndex, childElement.getEnd());
	}
	if (index < end) {
	    appendSegmentRemovingTags(index, end);
	}
    }

    private boolean isNotSpace(String innerText) {

	if (innerText.equals(" "))
	    return false;
	return true;
    }

    private boolean isClosedEndTag(Tag tag) {
	if (tag == null)
	    return false;
	if (tag instanceof EndTag)
	    return true;
	return false;

    }

    private boolean isOpenStartTag(Tag tag) {
	if (tag == null)
	    return false;
	if (tag instanceof EndTag)
	    return false;
	if (tag.toString().endsWith("/>"))
	    return false;
	return true;
    }

    @Override
    protected ElementHandler getElementHandler(final Element element) {
	if (element.getStartTag().getStartTagType().isServerTag())
	    return RemoveElementHandler.INSTANCE; // hard-coded configuration
	ElementHandler elementHandler = CB_Html_Renderer.ELEMENT_HANDLERS.get(element.getName());
	return (elementHandler != null) ? elementHandler : StandardInlineElementHandler.INSTANCE;
    }

    CB_List<HyperLinkText> hyperLinkList = new CB_List<HyperLinkText>();

    public void add(HyperLinkText hyperLinkText) {
	hyperLinkList.add(hyperLinkText);
    }
}
