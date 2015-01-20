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
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Tag;

import org.slf4j.LoggerFactory;

import CB_UI_Base.Math.Stack;

/**
 * @author Longri
 */
public class CB_HtmlProcessor extends Processor
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_HtmlProcessor.class);

	final static Stack<Tag> AtributeStack = new Stack<Tag>();
	List<Appendable> apendableList = new ArrayList<Appendable>();
	List<HtmlSegment> segmentList;

	boolean isImage = false;

	public CB_HtmlProcessor(Renderer renderer, Segment rootSegment, int maxLineLength, int hrLineLength, String newLine, boolean includeHyperlinkURLs, boolean includeAlternateText, boolean decorateFontStyles, boolean convertNonBreakingSpaces, int blockIndentSize, int listIndentSize, char[] listBullets, String tableCellSeparator)
	{
		super(renderer, rootSegment, maxLineLength, hrLineLength, newLine, includeHyperlinkURLs, includeAlternateText, decorateFontStyles, convertNonBreakingSpaces, blockIndentSize, listIndentSize, listBullets, tableCellSeparator);
	}

	// @Override
	// protected void appendElementContent(final Element element) throws IOException
	// {
	// final int contentEnd = element.getContentEnd();
	// if (element.isEmpty() || renderedIndex >= contentEnd) return;
	// final int contentBegin = element.getStartTag().getEnd();
	//
	// appendSegmentProcessingChildElements(Math.max(renderedIndex, contentBegin), contentEnd, element.getChildElements());
	//
	// }
	//
	// @Override
	// protected void appendNonPreformattedSegment(final int begin, final int end) throws IOException
	// {
	// assert begin < end;
	// assert begin >= renderedIndex;
	// final String text = CharacterReference.decodeCollapseWhiteSpace(source.subSequence(begin, end), convertNonBreakingSpaces);
	// if (text.length() == 0)
	// {
	// // collapsed text is zero length but original segment wasn't, meaning it consists purely of white space.
	// if (!ignoreInitialWhiteSpace) lastCharWhiteSpace = true;
	// }
	// appendNonPreformattedText(text, Segment.isWhiteSpace(source.charAt(begin)), Segment.isWhiteSpace(source.charAt(end - 1)));
	//
	// }

	// @Override
	// protected void appendSegmentProcessingChildElements(final int begin, final int end, final List<Element> childElements) throws
	// IOException
	// {
	// int index = begin;
	// for (Element childElement : childElements)
	// {
	// if (index >= childElement.getEnd()) continue;
	// if (index < childElement.getBegin()) appendSegmentRemovingTags(index, childElement.getBegin());
	// getElementHandler(childElement).process(this, childElement);
	// index = Math.max(renderedIndex, childElement.getEnd());
	// }
	// if (index < end)
	// {
	// appendSegmentRemovingTags(index, end);
	// }
	// }

	// @Override
	// protected void appendSegmentRemovingTags(final int begin, final int end) throws IOException
	// {
	// int index = begin;
	// while (true)
	// {
	//
	// Tag tag = source.getNextTag(index);
	//
	// if (tag == null || tag.getBegin() >= end) break;
	// appendSegment(index, tag.getBegin());
	// index = tag.getEnd();
	// }
	// appendSegment(index, end);
	// }

	// @Override
	// protected ElementHandler getElementHandler(final Element element)
	// {
	// if (element.getStartTag().getStartTagType().isServerTag()) return Renderer.RemoveElementHandler.INSTANCE;
	// ElementHandler elementHandler = Renderer.ELEMENT_HANDLERS.get(element.getName());
	//
	// if (elementHandler instanceof ImagelementHandler)
	// {
	// isImage = true;
	// }
	// return (elementHandler != null) ? elementHandler : Renderer.StandardInlineElementHandler.INSTANCE;
	// }

	public List<HtmlSegment> getElementList()
	{
		reset();
		segmentList = new ArrayList<HtmlSegment>();
		{// CB-CHANGE
			super.appendable = new StringBuilder();// = appendable;
		}
		List<Element> elements = rootSegment instanceof Element ? Collections.singletonList((Element) rootSegment) : rootSegment.getChildElements();
		try
		{
			appendSegmentProcessingChildElements(rootSegment.getBegin(), rootSegment.getEnd(), elements);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		checkNewSegment();

		return segmentList;

	}

	@Override
	protected void appendSegment(int begin, final int end) throws IOException
	{

		Tag tag = source.getPreviousTag(begin);

		if (isOpenStartTag(tag))
		{

			checkNewSegment();
			log.debug("Push Tag >" + tag.toString());
			AtributeStack.push(tag);
		}
		else if (isClosedEndTag(tag))
		{

			checkNewSegment();
			Tag pop = AtributeStack.pop();
			if (pop != null)
			{
				log.debug("Pop Tag >" + pop.toString());
			}
			else
			{
				log.error("Pop Tag > ERROR Stack are empty");
			}

		}

		assert begin <= end;
		if (begin < renderedIndex) begin = renderedIndex;
		if (begin >= end) return;
		try
		{
			if (preformatted)
			{
				appendPreformattedSegment(begin, end);
			}
			else
				appendNonPreformattedSegment(begin, end);
		}
		finally
		{
			if (renderedIndex < end) renderedIndex = end;
		}
	}

	void checkNewSegment()
	{
		String innerText = appendable.toString();
		if (innerText != null && !innerText.isEmpty() && isNotSpace(innerText))
		{
			log.debug("Append Text:" + innerText);
			HtmlSegment segment = new HtmlSegment(AtributeStack, innerText).setIsImage(isImage);
			if (!(segment.formatetText == null || segment.formatetText.isEmpty())) segmentList.add(segment);
			apendableList.add(appendable);
			appendable = new StringBuilder();
			isImage = false;
		}
	}

	@Override
	protected void appendSegmentProcessingChildElements(final int begin, final int end, final List<Element> childElements) throws IOException
	{
		int index = begin;
		for (Element childElement : childElements)
		{
			if (index >= childElement.getEnd()) continue;
			if (index < childElement.getBegin()) appendSegmentRemovingTags(index, childElement.getBegin());
			ElementHandler handler = getElementHandler(childElement);
			handler.process(this, childElement);
			if (isImage)
			{
				checkNewSegment();
			}

			index = Math.max(renderedIndex, childElement.getEnd());
		}
		if (index < end)
		{
			appendSegmentRemovingTags(index, end);
		}
	}

	private boolean isNotSpace(String innerText)
	{

		if (innerText.equals(" ")) return false;
		return true;
	}

	private boolean isClosedEndTag(Tag tag)
	{
		if (tag instanceof EndTag) return true;
		return false;

	}

	private boolean isOpenStartTag(Tag tag)
	{
		if (tag instanceof EndTag) return false;
		if (tag.toString().endsWith("/>")) return false;
		return true;
	}
}
