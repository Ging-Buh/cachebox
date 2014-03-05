/*
 * Copyright 2010, 2011, 2012 mapsforge.org
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
package org.mapsforge.map.swing.view;

import java.awt.Container;
import java.awt.Graphics;

import org.mapsforge.core.graphics.GraphicContext;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.map.awt.AwtGraphicFactory;
import org.mapsforge.map.controller.FrameBufferController;
import org.mapsforge.map.controller.LayerManagerController;
import org.mapsforge.map.controller.MapViewController;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.Model;
import org.mapsforge.map.view.FpsCounter;
import org.mapsforge.map.view.FrameBuffer;

public class AwtMapView extends Container implements org.mapsforge.map.view.MapView
{
	private static final GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;
	private static final long serialVersionUID = 1L;

	private final FpsCounter fpsCounter;
	private final FrameBuffer frameBuffer;
	private final LayerManager layerManager;
	private final Model model;

	public AwtMapView()
	{
		super();

		this.model = new Model();

		this.fpsCounter = new FpsCounter(GRAPHIC_FACTORY);
		this.frameBuffer = new FrameBuffer(this.model.frameBufferModel, new DisplayModel(), GRAPHIC_FACTORY);
		FrameBufferController.create(this.frameBuffer, this.model);

		this.layerManager = new LayerManager(this, this.model.mapViewPosition, GRAPHIC_FACTORY);
		this.layerManager.start();
		LayerManagerController.create(this.layerManager, this.model);

		MapViewController.create(this, this.model);
	}

	@Override
	public void destroy()
	{
		this.layerManager.interrupt();
	}

	@Override
	public Dimension getDimension()
	{
		return new Dimension(getWidth(), getHeight());
	}

	@Override
	public FpsCounter getFpsCounter()
	{
		return this.fpsCounter;
	}

	@Override
	public FrameBuffer getFrameBuffer()
	{
		return this.frameBuffer;
	}

	@Override
	public LayerManager getLayerManager()
	{
		return this.layerManager;
	}

	@Override
	public Model getModel()
	{
		return this.model;
	}

	@Override
	public void paint(Graphics graphics)
	{
		super.paint(graphics);

		GraphicContext graphicContext = AwtGraphicFactory.createGraphicContext(graphics);
		this.frameBuffer.draw(graphicContext);

		this.fpsCounter.draw(graphicContext);

		int xc = this.getWidth() / 2;
		int yc = this.getHeight() / 2;

		graphics.drawLine(xc - 50, yc - 50, xc + 50, yc + 50);
		graphics.drawLine(xc - 50, yc + 50, xc + 50, yc - 50);

		graphics.drawOval(xc - 25, yc - 25, 50, 50);
	}
}
