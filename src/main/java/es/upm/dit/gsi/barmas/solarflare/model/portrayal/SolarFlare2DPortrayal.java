/**
 * es.upm.dit.gsi.barmas.solarflare.model.portrayal.SolarFlare2DPortrayal.java
 */
package es.upm.dit.gsi.barmas.solarflare.model.portrayal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;

import sim.portrayal.DrawInfo2D;
import es.upm.dit.gsi.barmas.solarflare.model.SolarFlare;
import es.upm.dit.gsi.shanks.model.element.device.portrayal.Device2DPortrayal;

/**
 * Project: barmas
 * File: es.upm.dit.gsi.barmas.solarflare.model.portrayal.SolarFlare2DPortrayal.java
 * 
 * Grupo de Sistemas Inteligentes
 * Departamento de Ingenier�a de Sistemas Telem�ticos
 * Universidad Polit�cnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 02/10/2013
 * @version 0.1
 * 
 */
public class SolarFlare2DPortrayal extends Device2DPortrayal {

    /**
     * 
     */
    private static final long serialVersionUID = 3180819560173840065L;

    /* (non-Javadoc)
     * @see es.upm.dit.gsi.shanks.model.element.device.portrayal.Device2DPortrayal#draw(java.lang.Object, java.awt.Graphics2D, sim.portrayal.DrawInfo2D)
     */
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

        SolarFlare device = (SolarFlare) object;
        final double width = 20;
        final double height = 20;

        HashMap<String, Boolean> status = device.getStatus();
        boolean ready = status.get(SolarFlare.READY);
        if (ready) {
            graphics.setColor(Color.green);
        } else {
            graphics.setColor(Color.red);
        }

        // Draw the devices
        final int x = (int) (info.draw.x - width / 2.0);
        final int y = (int) (info.draw.y - height / 2.0);
        final int w = (int) (width);
        final int h = (int) (height);
        graphics.fillOval(x, y, w, h);
        
        //If you want put and image use this method
        //this.putImage(path, x, y, w, h, graphics);
        
        // Draw the devices ID ID
        graphics.setColor(Color.black);
        graphics.drawString(device.getID(), x - 3, y);

    }

}
