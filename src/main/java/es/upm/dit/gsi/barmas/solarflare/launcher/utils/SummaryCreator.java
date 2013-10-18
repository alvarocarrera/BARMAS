/*******************************************************************************
 * Copyright (c) 2013 alvarocarrera Grupo de Sistemas Inteligentes - Universidad Politécnica de Madrid. (GSI-UPM)
 * http://www.gsi.dit.upm.es/
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 * 
 * Contributors:
 *     alvarocarrera - initial API and implementation
 ******************************************************************************/
/**
 * es.upm.dit.gsi.barmas.solarflare.launcher.utils.SummaryCreator.java
 */
package es.upm.dit.gsi.barmas.solarflare.launcher.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 * Project: barmas File:
 * es.upm.dit.gsi.barmas.solarflare.launcher.utils.SummaryCreator.java
 * 
 * Grupo de Sistemas Inteligentes
 * Departamento de Ingeniería de Sistemas Telemáticos
 * Universidad Politécnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 16/10/2013
 * @version 0.1
 * 
 */
public class SummaryCreator {

	public static void makeNumbers(String simulationName, String origPath,
			String outputPath) {
		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		File origFile = new File(origPath);
		File outputFile = new File(outputPath);
		// Calculate comparative data
		CsvWriter writer = null;
		try {
			if (!outputFile.exists()) {
				writer = new CsvWriter(new FileWriter(outputFile), ',');
				String[] headers = new String[9];
				headers[0] = "SimulationID";
				headers[1] = "Type";
				headers[2] = "Cases";
				headers[3] = "BayesCentralOK";
				headers[4] = "ArgumentationOK";
				headers[5] = "BayesCentralBetter";
				headers[6] = "ArgumentationBetter";
				headers[7] = "BothOK";
				headers[8] = "BothWrong";
				writer.writeRecord(headers);
			} else {
				writer = new CsvWriter(new FileWriter(outputFile, true), ',');
			}
			writer.flush();

			CsvReader reader = new CsvReader(new FileReader(origFile));
			reader.readHeaders();
			HashMap<String, HashMap<String, Integer>> counters = new HashMap<String, HashMap<String, Integer>>();
			while (reader.readRecord()) {
				String[] origdata = reader.getValues();
				if (!counters.keySet().contains(origdata[0])) {
					HashMap<String, Integer> cs = new HashMap<String, Integer>();
					cs.put("Cases", 0);
					cs.put("BayesCentralOK", 0);
					cs.put("ArgumentationOK", 0);
					cs.put("BayesCentralBetter", 0);
					cs.put("ArgumentationBetter", 0);
					cs.put("BothOK", 0);
					cs.put("BothWrong", 0);
					counters.put(origdata[0], cs);
				}
				HashMap<String, Integer> counter = counters.get(origdata[0]);
				int aux = counter.get("Cases");
				counter.put("Cases", aux + 1);
				if (origdata[2].equals("1")) {
					aux = counter.get("BayesCentralOK");
					counter.put("BayesCentralOK", aux + 1);
				}
				if (origdata[3].equals("1")) {
					aux = counter.get("ArgumentationOK");
					counter.put("ArgumentationOK", aux + 1);
				}
				if (origdata[4].equals("1")) {
					aux = counter.get("BayesCentralBetter");
					counter.put("BayesCentralBetter", aux + 1);
				}
				if (origdata[5].equals("1")) {
					aux = counter.get("ArgumentationBetter");
					counter.put("ArgumentationBetter", aux + 1);
				}
				if (origdata[6].equals("1")) {
					aux = counter.get("BothOK");
					counter.put("BothOK", aux + 1);
				}
				if (origdata[7].equals("1")) {
					aux = counter.get("BothWrong");
					counter.put("BothWrong", aux + 1);

				}
			}
			// Write all types & TOTAL row
			int[] total = new int[7];
			for (Entry<String, HashMap<String, Integer>> entry : counters
					.entrySet()) {
				String[] data = new String[9];
				data[0] = simulationName;
				data[1] = entry.getKey();
				HashMap<String, Integer> summaries = entry.getValue();
				data[2] = Integer.toString(summaries.get("Cases"));
				data[3] = Integer.toString(summaries.get("BayesCentralOK"));
				data[4] = Integer.toString(summaries.get("ArgumentationOK"));
				data[5] = Integer.toString(summaries.get("BayesCentralBetter"));
				data[6] = Integer
						.toString(summaries.get("ArgumentationBetter"));
				data[7] = Integer.toString(summaries.get("BothOK"));
				data[8] = Integer.toString(summaries.get("BothWrong"));

				writer.writeRecord(data);
				writer.flush();
				logger.info("New row in " + outputPath);
				String info = "";
				for (String s : data) {
					info = info + " - " + s;
				}
				logger.info(info);

				total[0] = total[0] + summaries.get("Cases");
				total[1] = total[1] + summaries.get("BayesCentralOK");
				total[2] = total[2] + summaries.get("ArgumentationOK");
				total[3] = total[3] + summaries.get("BayesCentralBetter");
				total[4] = total[4] + summaries.get("ArgumentationBetter");
				total[5] = total[5] + summaries.get("BothOK");
				total[6] = total[6] + summaries.get("BothWrong");
			}
			String[] totalData = new String[9];
			totalData[0] = simulationName;
			totalData[1] = "TOTAL";
			for (int i = 0; i < total.length; i++) {
				totalData[i + 2] = Integer.toString(total[i]);
			}
			writer.writeRecord(totalData);
			writer.flush();
			logger.info("TOTAL row in " + outputPath);
			String info = "";
			for (String s : totalData) {
				info = info + " - " + s;
			}
			logger.info(info);

			String[] totalRatio = new String[9];
			totalRatio[0] = simulationName;
			totalRatio[1] = "RATIO-TOTAL";
			for (int i = 0; i < total.length; i++) {
				double aux = new Double(total[i]) / total[0];
				totalRatio[i + 2] = Double.toString(aux);
			}
			writer.writeRecord(totalRatio);
			writer.flush();
			logger.info("RATIO-TOTAL row in " + outputPath);
			info = "";
			for (String s : totalRatio) {
				info = info + " - " + s;
			}
			logger.info(info);

			writer.close();

		} catch (IOException e) {
			logger.warning("Impossible to create summary file for simulation: "
					+ simulationName + " in file: " + outputPath);
			e.printStackTrace();
		}

	}

}