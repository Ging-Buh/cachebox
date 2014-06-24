/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright © 2014 devemux86
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.rendertheme.renderinstruction;

import java.util.Locale;

import org.mapsforge.core.graphics.Align;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.FontFamily;
import org.mapsforge.core.graphics.FontStyle;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.rendertheme.XmlUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A builder for {@link Caption} instances.
 */
public class CaptionBuilder {
	static final String DY = "dy";
	static final String FILL = "fill";
	static final String FONT_FAMILY = "font-family";
	static final String FONT_SIZE = "font-size";
	static final String FONT_STYLE = "font-style";
	static final String K = "k";
	static final String STROKE = "stroke";
	static final String STROKE_WIDTH = "stroke-width";

	float dy;
	final Paint fill;
	float fontSize;
	final Paint stroke;
	TextKey textKey;

	public CaptionBuilder(GraphicFactory graphicFactory, DisplayModel displayModel, String elementName,
			Attributes attributes) throws SAXException {
		this.fill = graphicFactory.createPaint();
		this.fill.setColor(Color.BLACK);
		this.fill.setStyle(Style.FILL);
		this.fill.setTextAlign(Align.LEFT);

		this.stroke = graphicFactory.createPaint();
		this.stroke.setColor(Color.BLACK);
		this.stroke.setStyle(Style.STROKE);
		this.stroke.setTextAlign(Align.LEFT);

		extractValues(graphicFactory, displayModel, elementName, attributes);
	}

	/**
	 * @return a new {@code Caption} instance.
	 */
	public Caption build() {
		return new Caption(this);
	}

	private void extractValues(GraphicFactory graphicFactory, DisplayModel displayModel, String elementName,
			Attributes attributes) throws SAXException {
		FontFamily fontFamily = FontFamily.DEFAULT;
		FontStyle fontStyle = FontStyle.NORMAL;

		for (int i = 0; i < attributes.getLength(); ++i) {
			String name = attributes.getQName(i);
			String value = attributes.getValue(i);

			if (K.equals(name)) {
				this.textKey = TextKey.getInstance(value);
			} else if (DY.equals(name)) {
				this.dy = Float.parseFloat(value) * displayModel.getScaleFactor();
			} else if (FONT_FAMILY.equals(name)) {
				fontFamily = FontFamily.valueOf(value.toUpperCase(Locale.ENGLISH));
			} else if (FONT_STYLE.equals(name)) {
				fontStyle = FontStyle.valueOf(value.toUpperCase(Locale.ENGLISH));
			} else if (FONT_SIZE.equals(name)) {
				this.fontSize = XmlUtils.parseNonNegativeFloat(name, value) * displayModel.getScaleFactor();
			} else if (FILL.equals(name)) {
				this.fill.setColor(XmlUtils.getColor(graphicFactory, value));
			} else if (STROKE.equals(name)) {
				this.stroke.setColor(XmlUtils.getColor(graphicFactory, value));
			} else if (STROKE_WIDTH.equals(name)) {
				this.stroke.setStrokeWidth(XmlUtils.parseNonNegativeFloat(name, value) * displayModel.getScaleFactor());
			} else {
				throw XmlUtils.createSAXException(elementName, name, value, i);
			}
		}

		this.fill.setTypeface(fontFamily, fontStyle);
		this.stroke.setTypeface(fontFamily, fontStyle);

		XmlUtils.checkMandatoryAttribute(elementName, K, this.textKey);
	}
}
