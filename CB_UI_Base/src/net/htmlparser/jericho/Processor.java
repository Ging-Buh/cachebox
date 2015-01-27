package net.htmlparser.jericho;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.htmlparser.jericho.Renderer.ElementHandler;
import net.htmlparser.jericho.Renderer.RemoveElementHandler;
import net.htmlparser.jericho.Renderer.StandardInlineElementHandler;

/** This class does the actual work, but is first passed final copies of all the parameters for efficiency. */
public class Processor {
    public final Renderer renderer;
    protected final Segment rootSegment;
    protected final Source source;
    public final int maxLineLength;
    public final int hrLineLength;
    private final String newLine;
    public final boolean includeHyperlinkURLs;
    public final boolean includeAlternateText;
    final boolean decorateFontStyles;
    protected final boolean convertNonBreakingSpaces;
    final int blockIndentSize;
    private final int listIndentSize;
    private final char[] listBullets;
    final String tableCellSeparator;

    public Appendable appendable;
    protected int renderedIndex; // keeps track of where rendering is up to in case of overlapping elements
    public boolean atStartOfLine;
    public boolean skipInitialNewLines;
    public int col;
    protected int listIndentLevel;
    protected int indentSize;
    protected int blockVerticalMargin; // minimum number of blank lines to output at the current block boundary, or NO_MARGIN (-1) if we
				       // are not currently at a block boundary.
    protected boolean preformatted;
    public boolean lastCharWhiteSpace;
    protected final boolean ignoreInitialWhiteSpace = false; // can remove this at some stage once we're sure it won't be used.
    boolean bullet;
    int listBulletNumber;

    private static final int NO_MARGIN = -1;

    public Processor(final Renderer renderer, final Segment rootSegment, final int maxLineLength, final int hrLineLength, final String newLine, final boolean includeHyperlinkURLs, final boolean includeAlternateText, final boolean decorateFontStyles, final boolean convertNonBreakingSpaces, final int blockIndentSize, final int listIndentSize, final char[] listBullets, final String tableCellSeparator) {
	this.renderer = renderer;
	this.rootSegment = rootSegment;
	source = rootSegment.source;
	this.maxLineLength = maxLineLength;
	this.hrLineLength = hrLineLength;
	this.newLine = newLine;
	this.includeHyperlinkURLs = includeHyperlinkURLs;
	this.includeAlternateText = includeAlternateText;
	this.decorateFontStyles = decorateFontStyles;
	this.convertNonBreakingSpaces = convertNonBreakingSpaces;
	this.blockIndentSize = blockIndentSize;
	this.listIndentSize = listIndentSize;
	this.listBullets = listBullets;
	this.tableCellSeparator = tableCellSeparator;
    }

    public void appendTo(final Appendable appendable) throws IOException {
	reset();
	List<Element> elements = rootSegment instanceof Element ? Collections.singletonList((Element) rootSegment) : rootSegment.getChildElements();
	appendSegmentProcessingChildElements(rootSegment.begin, rootSegment.end, elements);
    }

    protected void reset() {
	renderedIndex = 0;
	atStartOfLine = true;
	skipInitialNewLines = !renderer.includeFirstElementTopMargin;
	col = 0;
	listIndentLevel = 0;
	indentSize = 0;
	blockVerticalMargin = NO_MARGIN;
	preformatted = false;
	lastCharWhiteSpace = false;
	// ignoreInitialWhiteSpace=false;
	bullet = false;
    }

    public void appendElementContent(final Element element) throws IOException {
	final int contentEnd = element.getContentEnd();
	if (element.isEmpty() || renderedIndex >= contentEnd)
	    return;
	final int contentBegin = element.getStartTag().end;
	appendSegmentProcessingChildElements(Math.max(renderedIndex, contentBegin), contentEnd, element.getChildElements());
    }

    protected void appendSegmentProcessingChildElements(final int begin, final int end, final List<Element> childElements) throws IOException {
	int index = begin;
	for (Element childElement : childElements) {
	    if (index >= childElement.end)
		continue;
	    if (index < childElement.begin)
		appendSegmentRemovingTags(index, childElement.begin);
	    getElementHandler(childElement).process(this, childElement);
	    index = Math.max(renderedIndex, childElement.end);
	}
	if (index < end) {
	    appendSegmentRemovingTags(index, end);
	}
    }

    protected ElementHandler getElementHandler(final Element element) {
	if (element.getStartTag().getStartTagType().isServerTag())
	    return RemoveElementHandler.INSTANCE; // hard-coded configuration
	ElementHandler elementHandler = Renderer.ELEMENT_HANDLERS.get(element.getName());
	return (elementHandler != null) ? elementHandler : StandardInlineElementHandler.INSTANCE;
    }

    protected void appendSegmentRemovingTags(final int begin, final int end) throws IOException {
	int index = begin;
	while (true) {
	    Tag tag = source.getNextTag(index);
	    if (tag == null || tag.begin >= end)
		break;
	    appendSegment(index, tag.begin);
	    index = tag.end;
	}
	appendSegment(index, end);
    }

    protected void appendSegment(int begin, final int end) throws IOException {
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

    protected void appendPreformattedSegment(final int begin, final int end) throws IOException {
	assert begin < end;
	assert begin >= renderedIndex;
	if (isBlockBoundary())
	    appendBlockVerticalMargin();
	final String text = CharacterReference.decode(source.subSequence(begin, end), false, convertNonBreakingSpaces);
	for (int i = 0; i < text.length(); i++) {
	    final char ch = text.charAt(i);
	    if (ch == '\n') {
		newLine();
	    } else if (ch == '\r') {
		newLine();
		final int nextI = i + 1;
		if (nextI == text.length())
		    break;
		if (text.charAt(nextI) == '\n')
		    i++;
	    } else {
		append(ch);
	    }
	}
    }

    protected void appendNonPreformattedSegment(final int begin, final int end) throws IOException {
	assert begin < end;
	assert begin >= renderedIndex;
	final String text = CharacterReference.decodeCollapseWhiteSpace(source.subSequence(begin, end), convertNonBreakingSpaces);
	if (text.length() == 0) {
	    // collapsed text is zero length but original segment wasn't, meaning it consists purely of white space.
	    if (!ignoreInitialWhiteSpace)
		lastCharWhiteSpace = true;
	    return;
	}
	appendNonPreformattedText(text, Segment.isWhiteSpace(source.charAt(begin)), Segment.isWhiteSpace(source.charAt(end - 1)));
    }

    public void appendText(final String text) throws IOException {
	assert text.length() > 0;
	appendNonPreformattedText(text, Segment.isWhiteSpace(text.charAt(0)), Segment.isWhiteSpace(text.charAt(text.length() - 1)));
    }

    protected void appendNonPreformattedText(final String text, final boolean isWhiteSpaceAtStart, final boolean isWhiteSpaceAtEnd) throws IOException {
	if (isBlockBoundary()) {
	    appendBlockVerticalMargin();
	} else if (lastCharWhiteSpace || (isWhiteSpaceAtStart && !ignoreInitialWhiteSpace)) {
	    // output white space only if not on a block boundary
	    append(' ');
	}
	int textIndex = 0;
	int i = 0;
	lastCharWhiteSpace = false;
	// ignoreInitialWhiteSpace=false;
	while (true) {
	    for (; i < text.length(); i++) {
		if (text.charAt(i) != ' ')
		    continue; // search for end of word
		// At end of word. To comply with RFC264 Format=Flowed protocol, need to make sure we don't wrap immediately before ">"
		// or "From ".
		if (i + 1 < text.length() && text.charAt(i + 1) == '>')
		    continue;
		if (i + 6 < text.length() && text.startsWith("From ", i + 1))
		    continue;
		break; // OK to wrap here if necessary
	    }
	    if (maxLineLength > 0 && col + i - textIndex + 1 >= maxLineLength) {
		if (lastCharWhiteSpace && (listIndentLevel | indentSize) == 0)
		    append(' ');
		startNewLine(0);
	    } else if (lastCharWhiteSpace) {
		append(' ');
	    }
	    append(text, textIndex, i);
	    if (i == text.length())
		break;
	    lastCharWhiteSpace = true;
	    textIndex = ++i;
	}
	lastCharWhiteSpace = isWhiteSpaceAtEnd;
    }

    public boolean isBlockBoundary() {
	return blockVerticalMargin != NO_MARGIN;
    }

    public void appendBlockVerticalMargin() throws IOException {
	assert blockVerticalMargin != NO_MARGIN;
	if (skipInitialNewLines) {
	    // at first text after <li> element or start of document
	    skipInitialNewLines = false;
	    final int indentCol = indentSize + listIndentLevel * listIndentSize;
	    if (col == indentCol) {
		atStartOfLine = false; // no need to call appendIndent() from appendTextInit().
	    } else {
		// there was an indenting block since the <li> or start of document
		if (bullet || col > indentCol) {
		    // just start new line as normal if the last indenting block is another <li>, or if the current column is already
		    // past the required indent
		    startNewLine(0);
		} else {
		    // just append spaces to get the column up to the required indent
		    while (indentCol > col) {
			appendable.append(' ');
			col++;
		    }
		    atStartOfLine = false; // make sure appendIndent() isn't called again from appendTextInit()
		}
	    }
	} else {
	    startNewLine(blockVerticalMargin);
	}
	blockVerticalMargin = NO_MARGIN;
    }

    public void blockBoundary(final int verticalMargin) throws IOException {
	// Set a block boundary with the given vertical margin. The vertical margin is the minimum number of blank lines to output
	// between the blocks.
	// This method can be called multiple times at a block boundary, and the next textual output will output the number of blank
	// lines determined by the
	// maximum vertical margin of all the method calls.
	if (blockVerticalMargin < verticalMargin)
	    blockVerticalMargin = verticalMargin;
    }

    public void startNewLine(int verticalMargin) throws IOException {
	// ensures we end up at the start of a line with the specified vertical margin between the previous textual output and the next
	// textual output.
	final int requiredNewLines = verticalMargin + (atStartOfLine ? 0 : 1);
	for (int i = 0; i < requiredNewLines; i++)
	    appendable.append(newLine);
	atStartOfLine = true;
	col = 0;
    }

    public void newLine() throws IOException {
	appendable.append(newLine);
	atStartOfLine = true;
	col = 0;
    }

    private void appendTextInit() throws IOException {
	skipInitialNewLines = false;
	if (atStartOfLine)
	    appendIndent();
    }

    void appendIndent() throws IOException {
	for (int i = indentSize; i > 0; i--)
	    appendable.append(' ');
	if (bullet) {
	    for (int i = (listIndentLevel - 1) * listIndentSize; i > 0; i--)
		appendable.append(' ');
	    if (listBulletNumber == Renderer.UNORDERED_LIST) {
		for (int i = listIndentSize - 2; i > 0; i--)
		    appendable.append(' ');
		appendable.append(listBullets[(listIndentLevel - 1) % listBullets.length]).append(' ');
	    } else {
		String bulletNumberString = Integer.toString(listBulletNumber);
		for (int i = listIndentSize - bulletNumberString.length() - 2; i > 0; i--)
		    appendable.append(' ');
		appendable.append(bulletNumberString).append(". ");
	    }
	    bullet = false;
	} else {
	    for (int i = listIndentLevel * listIndentSize; i > 0; i--)
		appendable.append(' ');
	}
	col = indentSize + listIndentLevel * listIndentSize;
	atStartOfLine = false;
    }

    public Processor append(final char ch) throws IOException {
	appendTextInit();
	appendable.append(ch);
	col++;
	return this;
    }

    public Processor append(final String text) throws IOException {
	appendTextInit();
	appendable.append(text);
	col += text.length();
	return this;
    }

    private void append(final CharSequence text, final int begin, final int end) throws IOException {
	appendTextInit();
	for (int i = begin; i < end; i++)
	    appendable.append(text.charAt(i));
	col += end - begin;
    }
}