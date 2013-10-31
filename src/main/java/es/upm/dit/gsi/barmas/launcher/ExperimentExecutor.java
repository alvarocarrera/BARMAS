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
package es.upm.dit.gsi.barmas.launcher;

import java.util.ArrayList;
import java.util.List;

import es.upm.dit.gsi.barmas.launcher.experiments.kowlancz.Experiment5;
import es.upm.dit.gsi.barmas.launcher.utils.ConsoleOutputDisabler;
import es.upm.dit.gsi.barmas.launcher.utils.SimulationConfiguration;

/**
 * Project: barmas
 * File: es.upm.dit.gsi.barmas.launcher.ExperimentExecutor.java
 * 
 * Grupo de Sistemas Inteligentes
 * Departamento de Ingeniería de Sistemas Telemáticos
 * Universidad Politécnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 31/10/2013
 * @version 0.1
 * 
 */
public class ExperimentExecutor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Because unbbayes print a lot of things in console...
		ConsoleOutputDisabler.disableConsoleOutput();

		String summaryFile = "global-summary.csv";
		long seed = 0;
		 int mode = SimulationConfiguration.DEBUGGING_MODE;
//		int mode = SimulationConfiguration.SIMULATION_MODE;
		List<Runnable> experiments = new ArrayList<Runnable>();

		// **************************
		// SOLAR FLARE EXPERIMENTS
		// **************************
		ExperimentExecutor.addSolarFlareExperiments(experiments, mode, seed,
				summaryFile);
		// ****************************************

		// **************************
		// KOWLANCZ EXPERIMENTS
		// **************************
		ExperimentExecutor.addKowlanCZExperiments(experiments, mode, seed,
				summaryFile);
		// ****************************************

		// Lauch experiments
		int maxThreads = new Integer(args[0]);
		ExperimentExecutor executor = new ExperimentExecutor();
		executor.executeExperiments(experiments, maxThreads);

	}

	public void executeExperiments(List<Runnable> experiments, int maxThreads) {

		List<Thread> threads = new ArrayList<Thread>();
		for (Runnable experiment : experiments) {
			while (threads.size() >= maxThreads) {
				try {
					Thread.sleep(5000);
					List<Thread> threads2Remove = new ArrayList<Thread>();
					for (Thread thread : threads) {
						if (!thread.isAlive()) {
							threads2Remove.add(thread);
						}
					}
					if (!threads2Remove.isEmpty()) {
						for (Thread t : threads2Remove) {
							threads.remove(t);
						}
						threads2Remove.clear();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

			Thread t = new Thread(experiment);
			threads.add(t);
			t.start();
		}
	}

	/**
	 * @param experiments
	 * @param mode
	 * @param seed
	 * @param summaryFile
	 */
	private static void addSolarFlareExperiments(List<Runnable> experiments,
			int mode, long seed, String summaryFile) {
		// Experiment1 exp1 = new Experiment1(summaryFile, seed, mode, true);
		// experiments.add(exp1);

		// Experiment3 exp3 = new Experiment3(summaryFile, seed, mode, true);
		// experiments.add(exp3);
		// Experiment3A exp3a = new Experiment3A(summaryFile, seed, mode, true);
		// experiments.add(exp3a);
		// Experiment3B exp3b = new Experiment3B(summaryFile, seed, mode, true);
		// experiments.add(exp3b);
		// Experiment3C exp3c = new Experiment3C(summaryFile, seed, mode, true);
		// experiments.add(exp3c);
		//
		// double threshold = 1;
		// double beliefThreshold = 1;
		// boolean validated = false;
		// // double delta = 0.05;
		// double delta = 0.2;
		//
		// // double threshold = 0.2;
		// // double beliefThreshold = 0.1;
		// // boolean validated = true;
		//
		// while (threshold > 0.01) {
		// while (beliefThreshold > 0.01) {
		// // Experiment2 exp2 = new Experiment2(summaryFile, seed,
		// // threshold, beliefThreshold, mode, !validated);
		// // experiments.add(exp2);
		// Experiment4 exp4 = new Experiment4(summaryFile, seed,
		// threshold, beliefThreshold, mode, !validated);
		// experiments.add(exp4);
		// Experiment4A exp4a = new Experiment4A(summaryFile, seed,
		// threshold, beliefThreshold, mode, !validated);
		// experiments.add(exp4a);
		// Experiment4B exp4b = new Experiment4B(summaryFile, seed,
		// threshold, beliefThreshold, mode, !validated);
		// experiments.add(exp4b);
		// Experiment4C exp4c = new Experiment4C(summaryFile, seed,
		// threshold, beliefThreshold, mode, !validated);
		// experiments.add(exp4c);
		// validated = true;
		// beliefThreshold = beliefThreshold - delta;
		// }
		// beliefThreshold = 1;
		// threshold = threshold - delta;
		// }
	}

	/**
	 * @param experiments
	 * @param mode
	 * @param seed
	 * @param summaryFile
	 */
	private static void addKowlanCZExperiments(List<Runnable> experiments,
			int mode, long seed, String summaryFile) {
//		AgentValidator exp00 = new AgentValidator(summaryFile, seed, mode, 0);
//		experiments.add(exp00);
//		AgentValidator exp01 = new AgentValidator(summaryFile, seed, mode, 1);
//		experiments.add(exp01);
//		AgentValidator exp02 = new AgentValidator(summaryFile, seed, mode, 2);
//		experiments.add(exp02);
//		AgentValidator exp03 = new AgentValidator(summaryFile, seed, mode, 3);
//		experiments.add(exp03);

		Experiment5 exp5 = new Experiment5(summaryFile, seed, mode);
		experiments.add(exp5);
		
	}

}
