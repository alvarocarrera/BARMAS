/**
 * es.upm.dit.gsi.barmas.solarflare.simulation.SolarFlare3DGUILauncher.java
 */
package es.upm.dit.gsi.barmas.solarflare.simulation;

import java.util.Properties;

import es.upm.dit.gsi.barmas.solarflare.model.scenario.SolarFlareScenario;
import es.upm.dit.gsi.shanks.model.scenario.Scenario;

/**
 * Project: barmas
 * File: es.upm.dit.gsi.barmas.solarflare.simulation.SolarFlare3DGUILauncher.java
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
public class SolarFlare3DGUILauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Properties scenarioProperties = new Properties();
		scenarioProperties.put(Scenario.SIMULATION_GUI, Scenario.SIMULATION_3D);

		SolarFlareClassificationSimulation sim;
		try {
			sim = new SolarFlareClassificationSimulation(
					System.currentTimeMillis(), SolarFlareScenario.class,
					"SolarFlareClassificatorScenario",
					SolarFlareScenario.NORMALSTATE, scenarioProperties);
			SolarFlareClassificationSimulation3DGUI gui = new SolarFlareClassificationSimulation3DGUI(
					sim);
			gui.start();
			do {
				if (!gui.getSimulation().schedule.step(sim))
					break;
			} while (gui.getSimulation().schedule.getSteps() < 2001);
			gui.finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
