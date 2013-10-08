/**
 * es.upm.dit.gsi.barmas.solarflare.steppable.SolarFlareEvaluator.java
 */
package es.upm.dit.gsi.barmas.solarflare.steppable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sim.engine.SimState;
import sim.engine.Steppable;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import es.upm.dit.gsi.barmas.solarflare.model.SolarFlare;
import es.upm.dit.gsi.barmas.solarflare.model.vocabulary.SolarFlareType;
import es.upm.dit.gsi.barmas.solarflare.simulation.SolarFlareClassificationSimulation;

/**
 * Project: barmas File:
 * es.upm.dit.gsi.barmas.solarflare.steppable.SolarFlareEvaluator.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingenier�a de Sistemas
 * Telem�ticos Universidad Polit�cnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 02/10/2013
 * @version 0.1
 * 
 */
public class SolarFlareEvaluator implements Steppable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6678249143484534051L;

	private String resultsPath;
	private String originalPath;

	public SolarFlareEvaluator(String results, String original) {
		this.resultsPath = results;
		this.originalPath = original;
		Reader fr;
		try {
			fr = new FileReader(originalPath);
			CsvReader reader = new CsvReader(fr);
			reader.readHeaders();
			String[] headers = reader.getHeaders();
			List<String> resultsHeaders = new ArrayList<String>();
			resultsHeaders.addAll(Arrays.asList(headers));
			resultsHeaders.add("BayesCentralClassifiedAs");
			resultsHeaders.add("ArgumentationClassifiedAs");
			int size = resultsHeaders.size();
			String[] newHeaders = new String[size];
			int i = 0;
			for(String header : resultsHeaders) {
				newHeaders[i++]=header;
			}
			CsvWriter writer = new CsvWriter(new FileWriter(resultsPath), ',');
			writer.writeRecord(newHeaders);
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.engine.Steppable#step(sim.engine.SimState)
	 */
	public void step(SimState simstate) {
		SolarFlareClassificationSimulation sim = (SolarFlareClassificationSimulation) simstate;
		SolarFlare argConclusion = (SolarFlare) sim.getScenario().getNetworkElement(
				"ArgumentationConclusion");
		SolarFlare centralConclusion = (SolarFlare) sim.getScenario().getNetworkElement(
				"ArgumentationConclusion");
		SolarFlare origflare = (SolarFlare) sim.getScenario()
				.getNetworkElement("OriginalSolarFlare");

		String argClass = (String) argConclusion
				.getProperty(SolarFlareType.class.getSimpleName());
		String centralClass = (String) centralConclusion.getProperty(SolarFlareType.class.getSimpleName());
		String origClass = (String) origflare.getProperty(SolarFlareType.class
				.getSimpleName());

//		if (classification.equals(orig)) {
//			// Result: success
//			try {
//				FileWriter fw = new FileWriter(resultsPath, true); // append
//																	// content
//				CsvWriter writer = new CsvWriter(fw, ',');
//				// writer.writeRecord();
//				// TODO write csv
//				// writer.writeNext(row);
//				writer.close();
//				// TODO UPDATE CHARTS
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//			// Result: fail
//		}
	}

}
