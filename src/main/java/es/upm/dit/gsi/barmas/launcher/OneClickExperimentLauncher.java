/**
 * es.upm.dit.gsi.barmas.launcher.OneClickExperimentLauncher.java
 */
package es.upm.dit.gsi.barmas.launcher;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.upm.dit.gsi.barmas.dataset.utils.DatasetSplitter;
import es.upm.dit.gsi.barmas.launcher.logging.LogConfigurator;
import es.upm.dit.gsi.barmas.launcher.utils.ConsoleOutputDisabler;
import es.upm.dit.gsi.barmas.launcher.utils.SimulationConfiguration;

/**
 * Project: barmas File:
 * es.upm.dit.gsi.barmas.launcher.OneClickExperimentLauncher.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 01/11/2013
 * @version 0.1
 * 
 */
public class OneClickExperimentLauncher {

	private Logger logger = Logger.getLogger(OneClickExperimentLauncher.class
			.getSimpleName());

	private String simulationID;
	private String dataset;
	private String experimentFolder;
	private int numberOfAgents;
	private double testRatio;
	private boolean centralApproach;
	private String summaryFile;
	private long seed;
	private int maxThreads;
	private int iterations;
	private String classificationTarget;
	private double delta;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new OneClickExperimentLauncher().click();

	}

	public OneClickExperimentLauncher() {
		ConsoleOutputDisabler.disableConsoleOutput();
	}

	/**
	 * 
	 */
	private void click() {
		// ***********************
		// MUSHROOM SIMULACION BATCH
		// ***********************
		simulationID = "MUSHROOM";
		dataset = "src/main/resources/dataset/agaricus-lepiota.csv";
		experimentFolder = "mushroom-simulation";
		numberOfAgents = 10;
		testRatio = 0.2;
		centralApproach = true;
		summaryFile = experimentFolder + "/" + experimentFolder
				+ "-summary.csv";
		seed = 0;
		maxThreads = 4;
		iterations = 1;
		classificationTarget = "mushroomPoisonous";
		delta = 0.6;
		
		this.launchFullBatchFor(simulationID, dataset, experimentFolder,
				numberOfAgents, testRatio, centralApproach, summaryFile, seed,
				maxThreads, iterations, classificationTarget, delta);
		// ***********************

//		// ***********************
//		// SOLAR FLARE SIMULACION BATCH
//		// ***********************
//		simulationID = "SOLARFLARE";
//		dataset = "src/main/resources/dataset/solarflare-global.csv";
//		experimentFolder = "solarflare-simulation";
//		numberOfAgents = 5;
//		testRatio = 0.4;
//		centralApproach = true;
//		summaryFile = experimentFolder + "/" + experimentFolder
//				+ "-summary.csv";
//		seed = 0;
//		maxThreads = 4;
//		iterations = 3;
//		classificationTarget = "SolarFlareType";
//		delta = 0.6;
//
//		this.launchFullBatchFor(simulationID, dataset, experimentFolder,
//				numberOfAgents, testRatio, centralApproach, summaryFile, seed,
//				maxThreads, iterations, classificationTarget, delta);
//		// ***********************

		// // ***********************
		// // KOWLANCZ02
		// // ***********************
		// simulationID = "KOWLANCZ02";
		// dataset =
		// "src/main/resources/dataset/kowlancz/CZ02/CZ02-dataset.csv";
		// experimentDatasetsFolder = "kowlancz02/simulation-input";
		// simulationOutputFolder = "kowlancz02/simulation-output";
		// numberOfAgents = 5;
		// testRatio = 0.2;
		// centralApproach = true;
		// summaryFile = "kowlancz02/kowlancz02-global-summary.csv";
		// seed = 0;
		// maxThreads = 4;
		// iterations = 1;
		// classificationTarget = "Diagnosis";
		// delta = 0.5;
		//
		// this.launchFullBatchFor(simulationID, dataset,
		// experimentDatasetsFolder, simulationOutputFolder,
		// numberOfAgents, testRatio, centralApproach, summaryFile, seed,
		// maxThreads, iterations, classificationTarget, delta);
		// // ***********************

		// // ***********************
		// // KOWLANCZ03 ---> it is not valid because one variable is
		// constant...
		// // ***********************
		// simulationID = "KOWLANCZ03";
		// dataset =
		// "src/main/resources/dataset/kowlancz/CZ03/CZ03-dataset.csv";
		// experimentDatasetsFolder = "kowlancz03/simulation-input";
		// simulationOutputFolder = "kowlancz03/simulation-output";
		// numberOfAgents = 3;
		// testRatio = 0.3;
		// centralApproach = true;
		// summaryFile = "kowlancz03/kowlancz03-global-summary.csv";
		// seed = 0;
		// maxThreads = 4;
		// iterations = 2;
		// classificationTarget = "Diagnosis";
		// delta = 0.5;
		//
		// this.launchFullBatchFor(simulationID, dataset,
		// experimentDatasetsFolder, simulationOutputFolder,
		// numberOfAgents, testRatio, centralApproach, summaryFile, seed,
		// maxThreads, iterations, classificationTarget, delta);
		// // ***********************

	}

	public void launchValidationBatchFor(String simulationID, String dataset,
			String experimentFolder, int agentsNumber, double ratio,
			boolean central, String summaryFile, long seed, int maxThreads,
			int iterations, String classificationTarget, double delta) {
		LogConfigurator.log2File(logger, "OneClickExperimentLauncher",
				Level.ALL, experimentFolder);

		try {
			for (int i = 0; i < iterations; i++) {
				DatasetSplitter splitter = new DatasetSplitter();
				splitter.splitDataset(
						ratio,
						agentsNumber,
						dataset,
						experimentFolder + "/input/iteration-" + i + "/dataset",
						central, simulationID);
				ExperimentExecutor executor = new ExperimentExecutor();

				// VALIDATORS
				List<Runnable> validators = executor.getValidatorsBatch(
						simulationID, agentsNumber, summaryFile, seed,
						SimulationConfiguration.SIMULATION_MODE,
						experimentFolder + "/input/iteration-" + i,
						experimentFolder + "/output/iteration-" + i,
						experimentFolder + "/input/iteration-" + i
								+ "/dataset/test-dataset.csv",
						classificationTarget, i);
				logger.info(validators.size()
						+ " validations are ready to execute for simulation: "
						+ simulationID);
				logger.info("---> Starting validations executions...");
				executor.executeValidators(validators, maxThreads, logger);
				// First all individuals validators are executed and then no
				// conflicts
				// for BN concurrent learning can occur
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void launchExperimentBatchFor(String simulationID, String dataset,
			String experimentFolder, int agentsNumber, double ratio,
			boolean central, String summaryFile, long seed, int maxThreads,
			int iterations, String classificationTarget, double delta) {
		LogConfigurator.log2File(logger, "OneClickExperimentLauncher",
				Level.ALL, experimentFolder);
		try {
			for (int i = 0; i < iterations; i++) {
				DatasetSplitter splitter = new DatasetSplitter();
				splitter.splitDataset(
						ratio,
						agentsNumber,
						dataset,
						experimentFolder + "/input/iteration-" + i + "/dataset",
						central, simulationID);
				ExperimentExecutor executor = new ExperimentExecutor();

				// EXPERIMENTS
				List<Runnable> experiments = executor.getExperimentBatch(
						simulationID, agentsNumber, summaryFile, seed,
						SimulationConfiguration.SIMULATION_MODE,
						experimentFolder + "/input/iteration-" + i,
						experimentFolder + "/output/iteration-" + i,
						experimentFolder + "/input/iteration-" + i
								+ "/dataset/test-dataset.csv",
						classificationTarget, delta, i);
				logger.info(experiments.size()
						+ " experiments are ready to execute for simulation: "
						+ simulationID);
				logger.info("---> Starting experiments executions...");
				executor.executeExperiments(experiments, maxThreads, logger);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void launchFullBatchFor(String simulationID, String dataset,
			String experimentFolder, int agentsNumber, double ratio,
			boolean central, String summaryFile, long seed, int maxThreads,
			int iterations, String classificationTarget, double delta) {

		LogConfigurator.log2File(logger, "OneClickExperimentLauncher",
				Level.ALL, experimentFolder);

		try {
			for (int i = 0; i < iterations; i++) {
				DatasetSplitter splitter = new DatasetSplitter();
				splitter.splitDataset(
						ratio,
						agentsNumber,
						dataset,
						experimentFolder + "/input/iteration-" + i + "/dataset",
						central, simulationID);
				ExperimentExecutor executor = new ExperimentExecutor();

				// VALIDATORS
				List<Runnable> validators = executor.getValidatorsBatch(
						simulationID, agentsNumber, summaryFile, seed,
						SimulationConfiguration.SIMULATION_MODE,
						experimentFolder + "/input/iteration-" + i,
						experimentFolder + "/output/iteration-" + i,
						experimentFolder + "/input/iteration-" + i
								+ "/dataset/test-dataset.csv",
						classificationTarget, i);
				logger.info(validators.size()
						+ " validations are ready to execute for simulation: "
						+ simulationID);
				logger.info("---> Starting validations executions...");
				executor.executeValidators(validators, maxThreads, logger);
				// First all individuals validators are executed and then no
				// conflicts
				// for BN concurrent learning can occur

				// EXPERIMENTS
				List<Runnable> experiments = executor.getExperimentBatch(
						simulationID, agentsNumber, summaryFile, seed,
						SimulationConfiguration.SIMULATION_MODE,
						experimentFolder + "/input/iteration-" + i,
						experimentFolder + "/output/iteration-" + i,
						experimentFolder + "/input/iteration-" + i
								+ "/dataset/test-dataset.csv",
						classificationTarget, delta, i);
				logger.info(experiments.size()
						+ " experiments are ready to execute for simulation: "
						+ simulationID);
				logger.info("---> Starting experiments executions...");
				executor.executeExperiments(experiments, maxThreads, logger);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
