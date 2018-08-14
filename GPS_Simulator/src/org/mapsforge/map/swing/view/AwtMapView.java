package org.mapsforge.map.swing.view;

import org.mapsforge.core.graphics.GraphicContext;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.controller.FrameBufferController;
import org.mapsforge.map.controller.LayerManagerController;
import org.mapsforge.map.controller.MapViewController;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.Model;
import org.mapsforge.map.view.FpsCounter;
import org.mapsforge.map.view.FrameBuffer;

import java.awt.*;

public class AwtMapView extends MapView {

    private static final long serialVersionUID = 1L;

    public AwtMapView() {
        super();

        this.model = new Model();

        DisplayModel dispModel = new DisplayModel();

        this.fpsCounter = new FpsCounter(GRAPHIC_FACTORY, dispModel);
        this.frameBuffer = new FrameBuffer(this.model.frameBufferModel, dispModel, GRAPHIC_FACTORY);
        FrameBufferController.create(this.frameBuffer, this.model);

        this.layerManager = new LayerManager(this, this.model.mapViewPosition, GRAPHIC_FACTORY);
        this.layerManager.start();
        LayerManagerController.create(this.layerManager, this.model);

        MapViewController.create(this, this.model);
    }

    @Override
    public void paint(Graphics graphics) {
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
