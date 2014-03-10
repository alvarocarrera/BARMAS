/**
 * es.upm.dit.gsi.barmas.launcher.WekaClassifiersValidator.java
 */
package es.upm.dit.gsi.barmas.launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LADTree;
import weka.classifiers.trees.NBTree;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import com.csvreader.CsvWriter;

import es.upm.dit.gsi.barmas.launcher.logging.LogConfigurator;

/**
 * Project: barmas File:
 * es.upm.dit.gsi.barmas.launcher.WekaClassifiersValidator.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 17/02/2014
 * @version 0.1
 * 
 */
public class WekaClassifiersValidator {

	private Logger logger;
	private String dataset;
	private String inputFolder;
	private String outputFolder;
	private String resultsFilePath;
	private CsvWriter writer;
	private int folds;
	private int columns;
	private Integer[] lebas;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		HashMap<String, Integer[]> datasets = new HashMap<String, Integer[]>();

		// Integer[] zoolebas = { 0, 4, 8 };
		// datasets.put("zoo", zoolebas);

		// Integer[] solarflarelebas = { 0, 3, 6 };
		// datasets.put("solarflare", solarflarelebas);

		// Integer[] marketinglebas = { 0, 3, 7 };
		// datasets.put("marketing", marketinglebas);

		// Integer[] mushroomlebas = { 0, 6, 11 };
		// datasets.put("mushroom", mushroomlebas);

		// Integer[] kowlanlebas = { 0, 7, 14 };
		// datasets.put("kowlancz02", kowlanlebas);

		Integer[] pokerlebas = { 0, 3, 5 };
		datasets.put("poker", pokerlebas);

		// Integer[] chesslebas = { 0, 2, 3 };
		// datasets.put("chess", chesslebas);
		// Integer[] nurserylebas = { 0, 2, 5 };
		// datasets.put("nursery", nurserylebas);

		List<Classifier> classifiers = null;

		for (String dataset : datasets.keySet()) {
			String simName = dataset + "-simulation";
			String inputFolder = "../experiments/" + simName + "/input";
			String outputFolder = "../experiments/" + simName + "/weka";
			int folds = 10;
			int maxAgents = 4;
			int minAgents = 2;

			WekaClassifiersValidator validator = new WekaClassifiersValidator(dataset, inputFolder,
					outputFolder, folds, minAgents, maxAgents, datasets.get(dataset));
			classifiers = validator.validateWekaClassifiers(classifiers);
		}

	}

	/**
	 * Constructor
	 * 
	 * @param simulationID
	 * @param inputPath
	 * @param outputPath
	 * @param folds
	 * @param minAgents
	 * @param maxAgents
	 * @param lebas
	 */
	public WekaClassifiersValidator(String simulationID, String inputPath, String outputPath,
			int folds, int minAgents, int maxAgents, Integer[] lebas) {
		this.dataset = simulationID;
		this.inputFolder = inputPath;
		this.outputFolder = outputPath;
		this.logger = Logger.getLogger("WekaClassifierValidator-" + this.dataset);
		LogConfigurator.log2File(logger, "WekaClassifierValidator-" + this.dataset, Level.ALL,
				Level.INFO, this.outputFolder);

		logger.info("--> Configuring WekaClassifierValidator...");
		this.resultsFilePath = outputPath + "/weka-results.csv";
		this.columns = 9;
		this.folds = folds;
		this.lebas = lebas;
		// this.minAgents = minAgents;
		// this.maxAgents = maxAgents;
		// this.minLEBA = minLEBA;
		// this.maxLEBA = maxLEBA;
		File dir = new File(this.outputFolder);
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();
		}
		try {
			File file = new File(resultsFilePath);
			if (!file.exists()) {
				this.writer = new CsvWriter(new FileWriter(file), ',');
				String[] headers = new String[this.columns];
				headers[0] = "dataset";
				headers[1] = "kfold";
				headers[2] = "classifier";
				headers[3] = "iteration";
				headers[4] = "ratioOk";
				headers[5] = "ratioWrong";
				headers[6] = "agentID";
				headers[7] = "agents";
				headers[8] = "leba";
				writer.writeRecord(headers);
			} else {
				this.writer = new CsvWriter(new FileWriter(file, true), ',');
			}
		} catch (IOException e) {
			logger.severe("Problems creating weka-results.csv file");
			logger.severe(e.getMessage());
		}
		logger.info("<-- WekaClassifierValidator configured");
	}

	/**
	 * 
	 */
	public List<Classifier> validateWekaClassifiers(List<Classifier> classifiers) {

		if (classifiers == null) {
			classifiers = this.getNewClassifiers();
			logger.info("All classifiers are going to be validated, because no list of classifiers has been provided.");
		} else if (classifiers.isEmpty()) {
			logger.warning("No algorithms available!! All of them were eliminated :( Jop");
		} else {
			logger.info("The following algorithms are going to be tested: ");
			for (Classifier classifier : classifiers) {
				logger.info(">> " + classifier.getClass().getSimpleName());
			}
		}

		logger.info(">> Validating all classifiers for dataset: " + this.dataset);

		List<Classifier> eliminateds = new ArrayList<Classifier>();

		for (Classifier classifier : classifiers) {
			try {
				this.validateClassifier(classifier);
			} catch (Exception e) {
				logger.info(">> Eliminating classifier: " + classifier.getClass().getSimpleName());
				eliminateds.add(classifier);
			}
		}

		classifiers.removeAll(eliminateds);

		logger.info(">> Dataset: " + this.dataset + " -> These are the survivals:");
		for (Classifier classifier : classifiers) {
			logger.info("--> " + classifier.getClass().getSimpleName());
		}
		logger.info("<----------------------------------------------->");
		logger.info(">> Dataset: " + this.dataset + " -> These are the eliminated algorithms:");
		for (Classifier classifier : eliminateds) {
			logger.info("--> " + classifier.getClass().getSimpleName());
		}

		logger.info("<-- All classifiers validated for dataset: " + this.dataset);
		this.writer.close();
		return classifiers;
	}

	/**
	 * @throws Exception
	 * 
	 * 
	 */
	public void validateClassifier(Classifier classifier) throws Exception {

		String classifierName = classifier.getClass().getSimpleName();

		logger.info("--> Starting validation for classfier " + classifierName);
		int ratioint = (int) ((1 / (double) folds) * 100);
		double roundedratio = ((double) ratioint) / 100;
		String[] row;
		Classifier copiedClassifier;

		try {
			// Central Agent

			HashMap<Integer, double[][]> resultsMap = new HashMap<Integer, double[][]>();
			HashMap<Integer, double[][]> resultsNoEssMap = new HashMap<Integer, double[][]>();

			for (int leba : lebas) {
				resultsMap.put(leba, new double[this.folds][2]);
				resultsNoEssMap.put(leba, new double[this.folds][2]);
			}
			logger.info("Starting validation for BayesCentralAgent dataset with " + classifierName
					+ " done for dataset: " + this.dataset);
			for (int iteration = 0; iteration < this.folds; iteration++) {
				String inputPath = this.inputFolder + "/" + roundedratio + "testRatio/iteration-"
						+ iteration;
				Instances testData = WekaClassifiersValidator.getDataFromCSV(inputPath
						+ "/test-dataset.arff");
				Instances trainData = WekaClassifiersValidator.getDataFromCSV(inputPath
						+ "/bayes-central-dataset.arff");
				try {
					logger.info("Learning model...");
					copiedClassifier = Classifier.makeCopy(classifier);
					copiedClassifier.buildClassifier(trainData);

					logger.info("Finishing learning process. Model built for classifier "
							+ classifierName + " in iteration " + iteration);
				} catch (Exception e) {
					logger.severe("Problems training model for "
							+ classifier.getClass().getSimpleName());
					logger.severe(e.getMessage());
					throw e;
				}

				for (int leba : lebas) {
					double[][] results = resultsMap.get(leba);
					double[] pcts = this.getValidation(copiedClassifier, trainData, testData, leba);
					results[iteration][0] = results[iteration][0] + pcts[0];
					results[iteration][1] = results[iteration][1] + pcts[1];

					resultsMap.put(leba, results);

					row = new String[this.columns];
					row[0] = this.dataset;
					row[1] = Integer.toString(this.folds);
					row[2] = classifierName;
					row[3] = Integer.toString(iteration);
					row[4] = Double.toString(pcts[0]);
					row[5] = Double.toString(pcts[1]);
					row[6] = "BayesCentralAgent";
					row[7] = "1";
					row[8] = Integer.toString(leba);
					writer.writeRecord(row);
				}

				/*
				 * Instances testDataNoEssentials =
				 * WekaClassifiersValidator.getDataFromCSV(inputPath +
				 * "/test-dataset.arff"); Instances trainDataNoEssentials =
				 * WekaClassifiersValidator.getDataFromCSV(inputPath +
				 * "/bayes-central-dataset-noEssentials.arff"); try {
				 * logger.info("Learning model..."); copiedClassifier =
				 * Classifier.makeCopy(classifier);
				 * copiedClassifier.buildClassifier(trainDataNoEssentials);
				 * 
				 * logger.info(
				 * "Finishing learning process. Model built for classifier " +
				 * classifierName + " in iteration " + iteration +
				 * " without essentials"); } catch (Exception e) {
				 * logger.severe("Problems training model for " +
				 * classifier.getClass().getSimpleName());
				 * logger.severe(e.getMessage()); throw e; }
				 * 
				 * for (int leba = this.minLEBA; leba <= this.maxLEBA; leba++) {
				 * double[][] resultsNoEss = resultsNoEssMap.get(leba); double[]
				 * pcts = this.getValidation(copiedClassifier,
				 * trainDataNoEssentials, testDataNoEssentials, leba);
				 * resultsNoEss[iteration][0] = resultsNoEss[iteration][0] +
				 * pcts[0]; resultsNoEss[iteration][1] =
				 * resultsNoEss[iteration][1] + pcts[1];
				 * 
				 * resultsNoEssMap.put(leba, resultsNoEss);
				 * 
				 * row = new String[this.columns]; row[0] = this.dataset; row[1]
				 * = Integer.toString(this.folds); row[2] = classifierName;
				 * row[3] = Integer.toString(iteration); row[4] =
				 * Double.toString(pcts[0]); row[5] = Double.toString(pcts[1]);
				 * row[6] = "BayesCentralAgent-NoEssentials"; row[7] = "1";
				 * row[8] = Integer.toString(leba); writer.writeRecord(row); }
				 */

				// -------------------------------------------------------------
				// --------------------------- FOR AGENTS DATASETS ISOLATED - NO
				// SENSE BECAUSE WEKA CLASSIFIER WERE DESIGNED TO BE CENTRALISED
				// -------------------------------------------------------------

				// // Agents combinations
				// for (int i = this.minAgents; i <= this.maxAgents; i++) {
				// logger.info("Validation for agents datasets with " +
				// classifierName
				// + " done for dataset: " + this.dataset + " with LEBA=" +
				// leba);
				// HashMap<Integer, Double> successRatio = new HashMap<Integer,
				// Double>();
				// HashMap<Integer, Double> wrongRatio = new HashMap<Integer,
				// Double>();
				// for (int j = 0; j < i; j++) {
				// successRatio.put(j, 0.0);
				// wrongRatio.put(j, 0.0);
				// }
				// for (int iteration = 0; iteration < this.folds; iteration++)
				// {
				// String inputPath = this.inputFolder + "/" + roundedratio
				// + "testRatio/iteration-" + iteration;
				// Instances testData = this.getDataFromCSV(inputPath +
				// "/test-dataset.csv");
				// for (int j = 0; j < i; j++) {
				// Instances trainData = this.getDataFromCSV(inputPath + "/" + i
				// + "agents/agent-" + j + "-dataset.csv");
				// double[] pcts = this.getValidation(classifier, trainData,
				// testData,
				// leba);
				// successRatio.put(j, successRatio.get(j) + pcts[0]);
				// wrongRatio.put(j, wrongRatio.get(j) + pcts[1]);
				//
				// row = new String[this.columns];
				// row[0] = this.dataset;
				// row[1] = Integer.toString(this.folds);
				// row[2] = classifierName;
				// row[3] = Integer.toString(iteration);
				// row[4] = Double.toString(pcts[0]);
				// row[5] = Double.toString(pcts[1]);
				// row[6] = "Agent" + j;
				// row[7] = Integer.toString(i);
				// row[8] = Integer.toString(leba);
				// writer.writeRecord(row);
				// }
				//
				// writer.flush();
				// }
				//
				// for (int j = 0; j < i; j++) {
				// row = new String[this.columns];
				// row[0] = this.dataset;
				// row[1] = Integer.toString(this.folds);
				// row[2] = classifierName;
				// row[3] = "AVERAGE";
				// row[4] = Double.toString(successRatio.get(j) / this.folds);
				// row[5] = Double.toString(wrongRatio.get(j) / this.folds);
				// row[6] = "Agent" + j;
				// row[7] = Integer.toString(i);
				// row[8] = Integer.toString(leba);
				// writer.writeRecord(row);
				//
				// logger.info("Validation for Agent" + j + " dataset (for " + i
				// + " agents configuration) with " + classifierName
				// + " done for dataset: " + this.dataset + " with LEBA=" +
				// leba);
				// }
				//
				// writer.flush();
				// }

				// -------------------------------------------------------------
				// ---------- END FOR AGENTS DATASETS ISOLATED -----------------
				// -------------------------------------------------------------

				logger.info("<-- Validation for classfier " + classifierName
						+ " done for dataset: " + this.dataset + " for iteration " + iteration);
			}

			for (int leba : lebas) {
				double[] sum = new double[2];
				double[][] results = resultsMap.get(leba);
				for (int iteration = 0; iteration < this.folds; iteration++) {
					sum[0] = sum[0] + results[iteration][0];
					sum[1] = sum[1] + results[iteration][1];
				}

				row = new String[this.columns];
				row[0] = this.dataset;
				row[1] = Integer.toString(this.folds);
				row[2] = classifierName;
				row[3] = "AVERAGE";
				row[4] = Double.toString(sum[0] / this.folds);
				row[5] = Double.toString(sum[1] / this.folds);
				row[6] = "BayesCentralAgent";
				row[7] = "1";
				row[8] = Integer.toString(leba);
				writer.writeRecord(row);

				logger.info("Validation for BayesCentralAgent dataset with " + classifierName
						+ " done for dataset: " + this.dataset + " with LEBA=" + leba);
				writer.flush();
			}

			// for (int leba : lebas) {
			// double[] sum = new double[2];
			// double[][] results = resultsNoEssMap.get(leba);
			// for (int iteration = 0; iteration < this.folds; iteration++) {
			// sum[0] = sum[0] + results[iteration][0];
			// sum[1] = sum[1] + results[iteration][1];
			// }
			//
			// row = new String[this.columns];
			// row[0] = this.dataset;
			// row[1] = Integer.toString(this.folds);
			// row[2] = classifierName;
			// row[3] = "AVERAGE";
			// row[4] = Double.toString(sum[0] / this.folds);
			// row[5] = Double.toString(sum[1] / this.folds);
			// row[6] = "BayesCentralAgent-NoEssentials";
			// row[7] = "1";
			// row[8] = Integer.toString(leba);
			// writer.writeRecord(row);
			//
			// logger.info("Validation for BayesCentralAgent dataset with " +
			// classifierName
			// + " done for dataset: " + this.dataset + " with LEBA=" + leba);
			// writer.flush();
			// }

			logger.info("Validation for BayesCentralAgent dataset with " + classifierName
					+ " done for dataset: " + this.dataset);
			writer.flush();

		} catch (Exception e) {
			logger.severe("Problem validating classifier " + classifierName);
			logger.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @param cls
	 * @param trainingData
	 * @param testData
	 * @param leba
	 * @return [0] = pctCorrect, [1] = pctIncorrect
	 * @throws Exception
	 */
	public double[] getValidation(Classifier cls, Instances trainingData, Instances testData,
			int leba) throws Exception {

		Instances testDataWithLEBA = new Instances(testData);

		for (int j = 0; j < leba; j++) {
			if (j < testDataWithLEBA.numAttributes() - 1) {
				for (int i = 0; i < testDataWithLEBA.numInstances(); i++) {
					testDataWithLEBA.instance(i).setMissing(j);
				}
			}
		}

		Evaluation eval;
		try {
			eval = new Evaluation(trainingData);
			logger.fine("Evaluating model with leba: " + leba);
			eval.evaluateModel(cls, testDataWithLEBA);

			double[] results = new double[2];
			results[0] = eval.pctCorrect() / 100;
			results[1] = eval.pctIncorrect() / 100;
			return results;
		} catch (Exception e) {
			logger.severe("Problems evaluating model for " + cls.getClass().getSimpleName());
			logger.severe(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @return a list of all WEKA classifiers
	 */
	public List<Classifier> getNewClassifiers() {
		Classifier classifier;
		List<Classifier> classifiers = new ArrayList<Classifier>();

		// NBTree
		classifier = new NBTree();
		classifiers.add(classifier);

		// PART
		classifier = new PART();
		classifiers.add(classifier);

		// J48
		classifier = new J48();
		((J48) classifier).setUnpruned(true);
		classifiers.add(classifier);

		// // J48Graft
		// classifier = new J48graft();
		// ((J48graft) classifier).setUnpruned(true);
		// classifiers.add(classifier);

		// // OneR
		// classifier = new OneR();
		// classifiers.add(classifier);

		// LADTree
		classifier = new LADTree();
		classifiers.add(classifier);

		// // REPTree
		// classifier = new REPTree();
		// classifiers.add(classifier);

		// // SimpleLogistic
		// classifier = new SimpleLogistic();
		// classifiers.add(classifier);

		// // Logistic
		// classifier = new Logistic();
		// classifiers.add(classifier);

		// // MultiLayerPerceptron
		// classifier = new MultilayerPerceptron();
		// classifiers.add(classifier);

		// // DecisionStump
		// classifier = new DecisionStump();
		// classifiers.add(classifier);

		// // LMT
		// classifier = new LMT();
		// classifiers.add(classifier);

		// // SimpleCart
		// classifier = new SimpleCart();
		// classifiers.add(classifier);

		// // BFTree
		// classifier = new BFTree();
		// classifiers.add(classifier);

		// // RBFNetwork
		// classifier = new RBFNetwork();
		// classifiers.add(classifier);

		// // DTNB
		// classifier = new DTNB();
		// classifiers.add(classifier);

		// // Jrip
		// classifier = new JRip();
		// classifiers.add(classifier);

		// // Conjunction Rule
		// classifier = new ConjunctiveRule();
		// classifiers.add(classifier);

		// // ZeroR
		// classifier = new ZeroR();
		// classifiers.add(classifier);

		// SMO
		classifier = new SMO();
		classifiers.add(classifier);

		// // OneR
		// classifier = new OneR();
		// classifiers.add(classifier);

		// // RandomForest
		// classifier = new RandomForest();
		// classifiers.add(classifier);

		return classifiers;

	}

	/**
	 * @param csvFilePath
	 * @return
	 * @throws Exception
	 */
	public static Instances getDataFromCSV(String csvFilePath) throws Exception {
		DataSource source = new DataSource(csvFilePath);
		Instances data = source.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
}
