package com.darklighting;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPriority;

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;

public class DarklightingOverlay extends Overlay {

    public int gameWidth;
    public int gameHeight;

    public Color unlitColor;
    public Color litColor;

    public int padding = 0;

    public ArrayList<Rectangle> rectangles = new ArrayList<>();

    public DarklightingOverlay(DarklightingPlugin plugin, Color unlitColor, Color litColor, int gameWidth, int gameHeight, int padding) {
        super(plugin);

        setBounds(new Rectangle(0,0,gameWidth, gameHeight));
        setDragTargetable(false);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setColors(unlitColor, litColor);
        this.padding = padding;
    }

    public Rectangle getRectangle(Polygon gon) {
        if(gon == null) {
            return new Rectangle(0,0,0,0);
        }
        Rectangle bounds = gon.getBounds();
        if(bounds == null) {
            return new Rectangle(0,0,0,0);
        }
        return new Rectangle(bounds.x-padding,bounds.y-padding,bounds.width + (padding*2),bounds.height + (padding*2));
    }
    public Rectangle getRectangle(Rectangle bounds) {
        return new Rectangle(bounds.x-padding,bounds.y-padding,bounds.width + (padding*2),bounds.height + (padding*2));
    }

    public void setColors(Color unlitColor, Color litColor) {
        this.unlitColor = unlitColor;
        this.litColor = litColor;
    }

    public void update(int gameWidth, int gameHeight, ArrayList<Rectangle> rectangles) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.rectangles = rectangles;
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        Dimension returnDimension = new Dimension(gameWidth,gameHeight);

        if((rectangles.size() == 0 && unlitColor.getAlpha() == 255)
        || (rectangles.size() > 0 && litColor.getAlpha() == 255)) {
            return returnDimension;
        }

        Area areaToRender = new Area(new Rectangle(0,0,gameWidth,gameHeight));
        for(Rectangle rectangle : rectangles) {
            if(rectangle == null) {
                continue;
            }
            areaToRender.subtract(new Area(rectangle));
        }
        graphics.setClip(areaToRender);

        graphics.setColor(rectangles.size() > 0 ? litColor : unlitColor);

        graphics.fillRect(0,0,gameWidth,gameHeight);

        return returnDimension;
    }
}
