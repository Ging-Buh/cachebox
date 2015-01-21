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


/**
 * @author Longri
 */
public class Utils
{
	// public static List<HtmlSegment_TextBlock> getAllSegments(String html, List<HtmlSegment_TextBlock> list, List<StartTag> aditionalTags)
	// {
	// HtmlSegment_TextBlock segment = getDeepestSegment(html);
	//
	// if (segment == null) return list;
	//
	// // is segment a inner segment?
	// String beforeHtml = html.substring(0, segment.begin);
	// HtmlSegment_TextBlock beforesegment = getDeepestSegment(beforeHtml);
	//
	// if (beforesegment != null)
	// {
	// if (aditionalTags != null && !aditionalTags.isEmpty())
	// {
	// beforesegment.addStartTags(aditionalTags);
	// }
	// list.add(beforesegment);
	// String newHtml = html.substring(0, beforesegment.begin) + html.substring(beforesegment.end, html.length());
	// if (!newHtml.isEmpty())
	// {
	//
	// List<StartTag> beforeTags = new ArrayList<StartTag>();
	// if (beforesegment != null && beforesegment.tags != null) for (StartTag tag : beforesegment.tags)
	// beforeTags.add(tag);
	//
	// getAllSegments(newHtml, list, beforesegment.tags);
	// }
	// }
	// else
	// {
	// if (aditionalTags != null && !aditionalTags.isEmpty())
	// {
	// segment.addStartTags(aditionalTags);
	// }
	//
	// list.add(segment);
	//
	// // remove segment from source
	// String newHtml = html.substring(0, segment.begin) + html.substring(segment.end, html.length());
	// if (!newHtml.isEmpty())
	// {
	// getAllSegments(newHtml, list, aditionalTags);
	// }
	// }
	//
	// return list;
	// }
	//
	// public static HtmlSegment_TextBlock getDeepestSegment(String html)
	// {
	// if (html == null || html.isEmpty()) return null;
	//
	// Source source = new Source(html);
	// List<Tag> tags = source.getAllTags();
	//
	// if (tags.isEmpty()) return null;
	//
	// // find first start tag after end Tag
	//
	// Tag end = null;
	// int idx = 0;
	//
	// Tag tag = tags.get(idx);
	// boolean endFound = false;
	// while (end == null)
	// {
	// if (!endFound && tag instanceof EndTag)
	// {
	// endFound = true;
	// }
	// else if (endFound && tag instanceof StartTag)
	// {
	// end = tags.get(--idx);
	// break;
	// }
	//
	// if (tags.size() - 1 == idx)
	// {
	// // find no more end tags, last tag == end
	// end = tags.get(idx);
	// break;
	// }
	//
	// tag = tags.get(++idx);
	// }
	//
	// // search start tag
	// end.getElement();
	// int start = 0;
	//
	// // if end tag the last tag and is start tag, the first tag is the start tag of this segment!
	// if (idx == tags.size() - 1 && end instanceof StartTag)
	// {
	// start = 0;
	// }
	// else
	// {
	// // find start tag from end Tag
	// if (end == null || end.getElement() == null) start = 0;
	// else
	// start = end.getElement().getBegin();
	// }
	// HtmlSegment_TextBlock ret = new HtmlSegment_TextBlock();
	// ret.begin = start;
	// ret.end = end.getEnd();
	// String segmentString = html.substring(ret.begin, ret.end);
	// Source segmentSource = new Source(segmentString);
	// ret.formatetText = segmentSource.getRenderer().toString();
	//
	// List<Element> imgTags = segmentSource.getAllElements("img");
	//
	// String src = null;
	// if (!imgTags.isEmpty())
	// {
	// src = imgTags.get(0).getAttributeValue("src");
	// if (src != null && !src.isEmpty())
	// {
	// ret.formatetText = src;
	// ret.isImage = true;
	// }
	// }
	//
	// String allSegmentString = html.substring(0, ret.end);
	// ret.addStartTags(new Source(allSegmentString).getAllStartTags());
	//
	// // if no Image or formatetText are empty return null;
	// if (ret.formatetText == null || ret.formatetText.isEmpty()) return null;
	//
	// return ret;
	// }

}
