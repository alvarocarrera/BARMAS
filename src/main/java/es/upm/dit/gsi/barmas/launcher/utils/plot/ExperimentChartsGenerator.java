/**
 * es.upm.dit.gsi.barmas.launcher.utils.plot.ExperimentChartsGenerator.java
 */
package es.upm.dit.gsi.barmas.launcher.utils.plot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Cylinder;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import es.upm.dit.gsi.barmas.launcher.experiments.BarmasAgentValidator;
import es.upm.dit.gsi.barmas.launcher.experiments.BarmasExperiment;
import es.upm.dit.gsi.barmas.launcher.logging.LogConfigurator;

/**
 * Project: barmas File:
 * es.upm.dit.gsi.barmas.launcher.utils.plot.ExperimentChartsGenerator.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 05/11/2013
 * @version 0.1
 * 
 */
public class ExperimentChartsGenerator {

	private Logger logger;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ExperimentChartsGenerator chartGenerator = new ExperimentChartsGenerator(
				Logger.getLogger(ExperimentChartsGenerator.class
						.getSimpleName()));
		String experimentFolder = "nursery-simulation";
		String summaryFile = experimentFolder + "/" + experimentFolder
				+ "-summary.csv";
		chartGenerator.generateAndSaveAllChartsAndExit(summaryFile,
				experimentFolder + "/output/charts");
	}

	public ExperimentChartsGenerator(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @param summaryFile
	 * @param chartOutputFolder
	 * @param iterations
	 */
	public void generateAndSaveAllCharts(String summaryFile,
			String chartOutputFolder) {

		LogConfigurator.log2File(logger, "ExperimentChartsGenerator",
				Level.ALL, Level.INFO, chartOutputFolder);

		CsvReader reader;
		try {
			reader = new CsvReader(new FileReader(new File(summaryFile)));
			reader.readHeaders();
//TODO create the matrix in the constructor or wherever to share the matrix with all methods
			int itsNum = 0;
			while (reader.readRecord()) {
				String[] row = reader.getValues();
				String[] splits = row[0].split("-");
				for (int i = 0; i < splits.length; i++) {
					if (splits[i].equals("IT")) {
						int aux = new Integer(splits[++i]);
						if (aux >= itsNum) {
							itsNum = aux + 1;
						}
					}
				}
			}

			reader.close();

			for (int i = 0; i < itsNum; i++) {
				this.saveValidationCylinderChartForIteration(summaryFile,
						chartOutputFolder, i);
				this.saveGlobalImprovementDelaunayChartsForIteration(
						summaryFile, chartOutputFolder, i);
			}

			if (itsNum > 1) {
				// Save all global charts
				// Validation global with cylinders
				this.saveValidationCylinderChart(summaryFile, chartOutputFolder);
				// TODO think in interesting scatter plots
			}

		} catch (FileNotFoundException e) {
			logger.severe(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * @param summaryFile
	 * @param chartOutputFolder
	 */
	private void saveValidationCylinderChart(String summaryFile,
			String chartOutputFolder) {

		String globalChartFolder = chartOutputFolder + "/global";
		File chartFolderFile = new File(globalChartFolder);
		if (!chartFolderFile.isDirectory() || !chartFolderFile.exists()) {
			chartFolderFile.mkdirs();
		}

		try {
			CsvReader reader = new CsvReader(new FileReader(new File(
					summaryFile)));
			reader.readHeaders();
			String[] headers = reader.getHeaders();

			List<String[]> validationResults = new ArrayList<String[]>();
			while (reader.readRecord()) {
				String[] row = reader.getValues();
				if (row[0].contains(BarmasAgentValidator.class.getSimpleName())) {
					validationResults.add(row);
				}
			}
			reader.close();

			// Write little info file
			CsvWriter writer = new CsvWriter(new FileWriter(new File(
					globalChartFolder + "/global-validations.csv")), ',');
			writer.writeRecord(headers);
			for (String[] row : validationResults) {
				writer.writeRecord(row);
			}
			writer.flush();
			writer.close();

			// Build cylinders and save cylinders charts
			Plotter plotter = new Plotter(logger);
			List<Cylinder> cylinders = new ArrayList<Cylinder>();
			HashMap<Integer, Float> mins = new HashMap<Integer, Float>();
			HashMap<Integer, Float> maxs = new HashMap<Integer, Float>();
			for (String[] row : validationResults) {
				float ratio = new Float(row[3]);
				if (row[0].contains("-BayesCentralAgent-")) {
					int agentNumberInteger = -1;
					if (!mins.containsKey(agentNumberInteger)) {
						mins.put(agentNumberInteger, ratio);
						maxs.put(agentNumberInteger, ratio);
					} else if (ratio < mins.get(agentNumberInteger)) {
						mins.put(agentNumberInteger, ratio);
					} else if (ratio > maxs.get(agentNumberInteger)) {
						maxs.put(agentNumberInteger, ratio);
					}
				} else {
					String simulationID = row[0];
					String[] splits = simulationID.split("-");
					for (String split : splits) {
						String aux = "Agent";
						if (split.startsWith(aux)) {
							String agentNumber = split.substring(aux.length());
							int agentNumberInteger = new Integer(agentNumber);
							if (!mins.containsKey(agentNumberInteger)) {
								mins.put(agentNumberInteger, ratio);
								maxs.put(agentNumberInteger, ratio);
							} else if (ratio < mins.get(agentNumberInteger)) {
								mins.put(agentNumberInteger, ratio);
							} else if (ratio > maxs.get(agentNumberInteger)) {
								maxs.put(agentNumberInteger, ratio);
							}
							break;
						}
					}
				}
			}

			float radius = 0.5f;
			for (int i = -1; i < mins.size() - 1; i++) {
				Coord3d baseCentre = new Coord3d(i * 2, i * 2,
						100 * mins.get(i));
				float height = (maxs.get(i) - mins.get(i)) * 100;
				if (height == 0) {
					height = 0.01f;
				}
				Cylinder cylinder = plotter.getCylinder(baseCentre, height,
						radius);
				cylinder.setColorMapper(new ColorMapper(new ColorMapRainbow(),
						new Color(height, height, height)));
				cylinders.add(cylinder);
			}

			logger.info("Generating global validation chart");
			String[] axisLabels = new String[3];
			axisLabels[0] = "";
			axisLabels[1] = "Agent Number";
			axisLabels[2] = "Ratio";
			// plotter.saveCylinder3DChart(globalChartFolder
			// + "/global-validations.png", axisLabels, cylinders,
			// ViewPositionMode.FREE, null);
			plotter.saveCylinder3DChart(globalChartFolder
					+ "/global-validations.png", axisLabels, cylinders,
					ViewPositionMode.PROFILE, new Coord3d(0, 1, 0));

		} catch (FileNotFoundException e) {
			logger.severe(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			System.exit(1);
		}

	}

	/**
	 * @param summaryFile
	 * @param chartOutputFolder
	 * @param iterations
	 */
	public void generateAndSaveAllChartsAndExit(String summaryFile,
			String chartOutputFolder) {

		this.generateAndSaveAllCharts(summaryFile, chartOutputFolder);
		logger.info("All Charts generated. Execution finished successfully.");
		System.exit(0);
	}

	/**
	 * @param summaryFile
	 * @param chartFolder
	 * @param iteration
	 */
	private void saveGlobalImprovementDelaunayChartsForIteration(
			String summaryFile, String chartFolder, int iteration) {
		String iterationChartFolder = chartFolder + "/iteration-" + iteration;
		File chartFolderFile = new File(iterationChartFolder);
		if (!chartFolderFile.isDirectory() || !chartFolderFile.exists()) {
			chartFolderFile.mkdirs();
		}

		try {
			CsvReader reader = new CsvReader(new FileReader(new File(
					summaryFile)));
			reader.readHeaders();
			String[] headers = reader.getHeaders();

			List<String[]> experimentResultsRatios = new ArrayList<String[]>();
			while (reader.readRecord()) {
				String[] row = reader.getValues();
				if (row[0].contains(BarmasExperiment.class.getSimpleName())
						&& row[0].contains("-IT-" + iteration)) {
					experimentResultsRatios.add(row);
				}
			}
			reader.close();

			// Write little info file
			CsvWriter writer = new CsvWriter(new FileWriter(new File(
					iterationChartFolder + "/experiments.csv")), ',');
			writer.writeRecord(headers);
			for (String[] row : experimentResultsRatios) {
				writer.writeRecord(row);
			}
			writer.flush();
			writer.close();

			logger.info("Getting parameters for The Matrix...");
			List<String> lepaList = new ArrayList<String>();
			List<String> tthList = new ArrayList<String>();
			List<String> dthList = new ArrayList<String>();
			List<String> bthList = new ArrayList<String>();
			List<String> itList = new ArrayList<String>();
			List<String> agentsList = new ArrayList<String>();
			List<String> ratiosList = new ArrayList<String>();

			for (String[] row : experimentResultsRatios) {
				this.checkAndAdd(lepaList, row[13]);
				this.checkAndAdd(tthList, row[14]);
				this.checkAndAdd(dthList, row[11]);
				this.checkAndAdd(bthList, row[12]);
				this.checkAndAdd(itList, row[15]);
				this.checkAndAdd(agentsList, row[16]);
				this.checkAndAdd(ratiosList, row[17]);
			}

			HashMap<String, Integer> lepas = this.getSortedMap(lepaList);
			HashMap<String, Integer> tths = this.getSortedMap(tthList);
			HashMap<String, Integer> dths = this.getSortedMap(dthList);
			HashMap<String, Integer> bths = this.getSortedMap(bthList);
			HashMap<String, Integer> agents = this.getSortedMap(agentsList);
			HashMap<String, Integer> its = this.getSortedMap(itList);
			HashMap<String, Integer> testRatios = this.getSortedMap(ratiosList);

			logger.info("Creating The Matrix");
			double[][][][][][][] theMatrix = new double[lepas.size()][tths
					.size()][dths.size()][bths.size()][agents.size()][its
					.size()][testRatios.size()];
			logger.info("Filling The Matrix...");
			for (String[] row : experimentResultsRatios) {
				String globalImp = row[9];
				double imp = Double.valueOf(globalImp);
				int lepaPos = lepas.get(row[13]);
				int tthPos = tths.get(row[14]);
				int dthPos = dths.get(row[11]);
				int bthPos = bths.get(row[12]);
				int itPos = its.get(row[15]);
				int agentsPos = agents.get(row[16]);
				int testRatiosPos = testRatios.get(row[17]);
				this.addToTheMatrix(theMatrix, lepaPos, tthPos, dthPos, bthPos,
						agentsPos, itPos, testRatiosPos, imp);
			}
			reader.close();

			logger.info("Painting...");
			// Build cylinders and save cylinders charts
			Plotter plotter = new Plotter(logger);

			// LEPAvsTTH with the rest of variables constants
			List<Coord3d> globalCoords = new ArrayList<Coord3d>();
			for (int agent = 0; agent < agents.size(); agent++) {
				int agentValue = (int) this.getValueForPosInTheMatrix(agents,
						agent);
				for (int dth = 0; dth < dths.size(); dth++) {
					double dthValue = this.getValueForPosInTheMatrix(dths, dth);
					for (int bth = 0; bth < bths.size(); bth++) {
						double bthValue = this.getValueForPosInTheMatrix(bths,
								bth);
						for (int it = 0; it < its.size(); it++) {
							int itValue = (int) this.getValueForPosInTheMatrix(
									its, it);
							for (int ratio = 0; ratio < testRatios.size(); ratio++) {
								double ratioValue = this
										.getValueForPosInTheMatrix(testRatios,
												ratio);
								List<Coord3d> coords = new ArrayList<Coord3d>();
								for (int lepa = 0; lepa < lepas.size(); lepa++) {
									double lepaValue = this
											.getValueForPosInTheMatrix(lepas,
													lepa);
									for (int tth = 0; tth < tths.size(); tth++) {
										double tthValue = this
												.getValueForPosInTheMatrix(
														tths, tth);
										double imp = this.getImpFromTheMatrix(
												theMatrix, lepa, tth, dth, bth,
												agent, it, ratio);
										if (dthValue == 20.0
												|| bthValue == 20.0) {
											if (dthValue == 20.0
													&& bthValue == 20.0) {
												Coord3d coord = new Coord3d(
														lepaValue, tthValue,
														imp);
												coords.add(coord);
												// globalCoords.add(coord);
											}
										} else if (tthValue != 20.0) {
											Coord3d coord = new Coord3d(
													lepaValue, tthValue, imp);
											coords.add(coord);
											globalCoords.add(coord);
										}

									}
								}
								// screenshot
								if (dthValue == 20.0 || bthValue == 20.0) {
									if ((dthValue == 20.0 && bthValue == 20.0)) {
										String[] axisLabels = new String[3];
										axisLabels[0] = "LEPA";
										axisLabels[1] = "TTH";
										axisLabels[2] = "ImprovementRatio";
										plotter.saveDelaunaySurface3DChart(
												iterationChartFolder
														+ "/globalImprovement-LEPAvsTTH"
														+ "-DTH-" + dthValue
														+ "-BTH-" + bthValue
														+ "-Agents-"
														+ agentValue + "-IT-"
														+ itValue
														+ "-TestRatio-"
														+ ratioValue + ".png",
												axisLabels, coords,
												ViewPositionMode.FREE, null);
									}
								} else {
									String[] axisLabels = new String[3];
									axisLabels[0] = "LEPA";
									axisLabels[1] = "TTH";
									axisLabels[2] = "ImprovementRatio";
									plotter.saveDelaunaySurface3DChart(
											iterationChartFolder
													+ "/globalImprovement-LEPAvsTTH"
													+ "-DTH-" + dthValue
													+ "-BTH-" + bthValue
													+ "-Agents-" + agentValue
													+ "-IT-" + itValue
													+ "-TestRatio-"
													+ ratioValue + ".png",
											axisLabels, coords,
											ViewPositionMode.FREE, null);
								}
							}
						}
					}
				}
			}
			String[] axisLabels = new String[3];
			axisLabels[0] = "LEPA";
			axisLabels[1] = "TTH";
			axisLabels[2] = "ImprovementRatio";
			plotter.saveScatter3DChart(iterationChartFolder
					+ "/globalImprovement-LEPAvsTTH-Plotter.png", axisLabels,
					globalCoords, 10, ViewPositionMode.FREE, null);

			
			// LEPAvsDTH with the rest of variables constants
			globalCoords = new ArrayList<Coord3d>();
			double dthValue = 0;
			for (int agent = 0; agent < agents.size(); agent++) {
				int agentValue = (int) this.getValueForPosInTheMatrix(agents,
						agent);
				for (int tth = 0; tth < tths.size(); tth++) {
					double tthValue = this.getValueForPosInTheMatrix(tths, tth);
					for (int bth = 0; bth < bths.size(); bth++) {
						double bthValue = this.getValueForPosInTheMatrix(bths,
								bth);
						for (int it = 0; it < its.size(); it++) {
							int itValue = (int) this.getValueForPosInTheMatrix(
									its, it);
							for (int ratio = 0; ratio < testRatios.size(); ratio++) {
								double ratioValue = this
										.getValueForPosInTheMatrix(testRatios,
												ratio);
								List<Coord3d> coords = new ArrayList<Coord3d>();
								for (int lepa = 0; lepa < lepas.size(); lepa++) {
									double lepaValue = this
											.getValueForPosInTheMatrix(lepas,
													lepa);
									for (int dth = 0; dth < dths.size(); dth++) {
										dthValue = this
												.getValueForPosInTheMatrix(
														dths, dth);
										double imp = this.getImpFromTheMatrix(
												theMatrix, lepa, tth, dth, bth,
												agent, it, ratio);
										if (dthValue != 20.0
												&& tthValue != 20.0
												&& bthValue != 20.0) {
											Coord3d coord = new Coord3d(
													lepaValue, dthValue, imp);
											coords.add(coord);
											globalCoords.add(coord);
										} else if (dthValue == 20.0
												&& bthValue == 20.0
												&& tthValue == 20.0) {
											Coord3d coord = new Coord3d(
													lepaValue, dthValue, imp);
											coords.add(coord);
											// globalCoords.add(coord);
										}

									}
								}
								// screenshot
								if (tthValue != 20.0 && bthValue != 20.0) {
									axisLabels = new String[3];
									axisLabels[0] = "LEPA";
									axisLabels[1] = "DTH";
									axisLabels[2] = "ImprovementRatio";
									plotter.saveDelaunaySurface3DChart(
											iterationChartFolder
													+ "/globalImprovement-LEPAvsDTH"
													+ "-TTH-" + tthValue
													+ "-BTH-" + bthValue
													+ "-Agents-" + agentValue
													+ "-IT-" + itValue
													+ "-TestRatio-"
													+ ratioValue + ".png",
											axisLabels, coords,
											ViewPositionMode.FREE, null);
								}
							}
						}
					}
				}
			}
			axisLabels = new String[3];
			axisLabels[0] = "LEPA";
			axisLabels[1] = "DTH";
			axisLabels[2] = "ImprovementRatio";
			plotter.saveScatter3DChart(iterationChartFolder
					+ "/globalImprovement-LEPAvsDTH-Plotter.png", axisLabels,
					globalCoords, 10, ViewPositionMode.FREE, null);

			// // Build cylinders and save cylinders charts
			// Plotter plotter = new Plotter(logger);
			// logger.info("Generating success chart for iteration " +
			// iteration);
			//
			// String[] axisLabels = new String[3];
			// axisLabels[0] = "DiffProbDistThreshold";
			// axisLabels[1] = "LEPA";
			// axisLabels[2] = "Ratio";
			// for (Entry<Double, List<Coord3d>> entry :
			// bthConstantGlobalImpCoordsNoTrust
			// .entrySet()) {
			// plotter.getDelaunayChart(entry.getValue());
			// plotter.saveDelaunaySurface3DChart(iterationChartFolder
			// + "/globalImprovement-BTH-" + entry.getKey()
			// + "-TRUSTMODE-OFF.png", axisLabels, entry.getValue(),
			// ViewPositionMode.FREE, null);
			// }
			// for (Entry<Double, List<Coord3d>> entry :
			// bthConstantGlobalImpCoordsTrust
			// .entrySet()) {
			// plotter.getDelaunayChart(entry.getValue());
			// plotter.saveDelaunaySurface3DChart(iterationChartFolder
			// + "/globalImprovement-BTH-" + entry.getKey()
			// + "-TRUSTMODE-ON.png", axisLabels, entry.getValue(),
			// ViewPositionMode.FREE, null);
			// }
			//
			// axisLabels[0] = "BeliefThreshold";
			// for (Entry<Double, List<Coord3d>> entry :
			// thConstantGlobalImpCoordsNoTrust
			// .entrySet()) {
			// plotter.saveDelaunaySurface3DChart(iterationChartFolder
			// + "/globalImprovement-DTH-" + entry.getKey()
			// + "-TRUSTMODE-OFF.png", axisLabels, entry.getValue(),
			// ViewPositionMode.FREE, null);
			// }
			//
			// axisLabels[0] = "BeliefThreshold";
			// for (Entry<Double, List<Coord3d>> entry :
			// thConstantGlobalImpCoordsTrust
			// .entrySet()) {
			// plotter.saveDelaunaySurface3DChart(iterationChartFolder
			// + "/globalImprovement-DTH-" + entry.getKey()
			// + "-TRUSTMODE-ON.png", axisLabels, entry.getValue(),
			// ViewPositionMode.FREE, null);
			// }
			//
			// axisLabels[0] = "DiffProbDistThreshold";
			// axisLabels[1] = "BeliefThreshold";
			// for (Entry<Integer, List<Coord3d>> entry :
			// lepaConstantGlobalImpCoordsNoTrust
			// .entrySet()) {
			// plotter.saveDelaunaySurface3DChart(iterationChartFolder
			// + "/globalImprovement-LEPA-" + entry.getKey()
			// + "-TRUSTMODE-OFF.png", axisLabels, entry.getValue(),
			// ViewPositionMode.FREE, null);
			// }
			// for (Entry<Integer, List<Coord3d>> entry :
			// lepaConstantGlobalImpCoordsTrust
			// .entrySet()) {
			// plotter.saveDelaunaySurface3DChart(iterationChartFolder
			// + "/globalImprovement-LEPA-" + entry.getKey()
			// + "-TRUSTMODE-ON.png", axisLabels, entry.getValue(),
			// ViewPositionMode.FREE, null);
			// }

		} catch (FileNotFoundException e) {
			logger.severe(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			System.exit(1);
		}

	}

	/**
	 * @param theMatrix
	 * @param lepaPos
	 * @param tthPos
	 * @param dthPos
	 * @param bthPos
	 * @param agentsPos
	 * @param itPos
	 * @param testRatiosPos
	 * @param imp
	 */
	private void addToTheMatrix(double[][][][][][][] theMatrix, int lepaPos,
			int tthPos, int dthPos, int bthPos, int agentsPos, int itPos,
			int testRatiosPos, double imp) {
		theMatrix[lepaPos][tthPos][dthPos][bthPos][agentsPos][itPos][testRatiosPos] = imp;

	}

	/**
	 * @param theMatrix
	 * @param lepa
	 * @param tth
	 * @param dth
	 * @param bth
	 * @param agent
	 * @param it
	 * @param ratio
	 * @return
	 */
	private double getImpFromTheMatrix(double[][][][][][][] theMatrix,
			int lepa, int tth, int dth, int bth, int agent, int it, int ratio) {
		return theMatrix[lepa][tth][dth][bth][agent][it][ratio];
	}

	/**
	 * @param map
	 * @param pos
	 * @return
	 */
	private double getValueForPosInTheMatrix(HashMap<String, Integer> map,
			int pos) {
		for (Entry<String, Integer> entry : map.entrySet()) {
			if (pos == entry.getValue()) {
				String key = entry.getKey();
				double value = Double.valueOf(key);
				return value;
			}
		}
		return Double.MAX_VALUE;
	}

	/**
	 * @param map
	 * @param pos
	 * @return
	 */
	private String getStringForPosInTheMatrix(HashMap<String, Integer> map,
			int pos) {
		for (Entry<String, Integer> entry : map.entrySet()) {
			if (pos == entry.getValue()) {
				String key = entry.getKey();
				return key;
			}
		}
		return "";
	}

	/**
	 * @param list
	 */
	private HashMap<String, Integer> getSortedMap(List<String> list) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		List<String> values = new ArrayList<String>();
		values.addAll(list);
		String[] sortedList = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			String minValue = this.getMinValue(values);
			values.remove(minValue);
			sortedList[i] = minValue;
		}
		for (int i = 0; i < sortedList.length; i++) {
			map.put(sortedList[i], i);
		}
		return map;

	}

	/**
	 * @param values
	 * @return
	 */
	private String getMinValue(List<String> values) {
		double min = Double.MAX_VALUE;
		String minValue = "";
		for (String value : values) {
			double v = new Double(value);
			if (v < min) {
				min = v;
				minValue = value;
			}
		}
		return minValue;
	}

	/**
	 * @param lepaList
	 * @param value
	 */
	private void checkAndAdd(List<String> lepaList, String value) {
		if (!lepaList.contains(value)) {
			lepaList.add(value);
		}
	}

	/**
	 * @param summaryFile
	 * @param chartFolder
	 * @param iteration
	 */
	private void saveValidationCylinderChartForIteration(String summaryFile,
			String chartFolder, int iteration) {
		String iterationChartFolder = chartFolder + "/iteration-" + iteration;
		File chartFolderFile = new File(iterationChartFolder);
		if (!chartFolderFile.isDirectory() || !chartFolderFile.exists()) {
			chartFolderFile.mkdirs();
		}

		try {
			CsvReader reader = new CsvReader(new FileReader(new File(
					summaryFile)));
			reader.readHeaders();
			String[] headers = reader.getHeaders();

			List<String[]> validationResults = new ArrayList<String[]>();
			while (reader.readRecord()) {
				String[] row = reader.getValues();
				if (row[0].contains(BarmasAgentValidator.class.getSimpleName())
						&& row[0].contains("-IT-" + iteration)) {
					validationResults.add(row);
				}
			}
			reader.close();

			// Write little info file
			CsvWriter writer = new CsvWriter(new FileWriter(new File(
					iterationChartFolder + "/validations.csv")), ',');
			writer.writeRecord(headers);
			for (String[] row : validationResults) {
				writer.writeRecord(row);
			}
			writer.flush();
			writer.close();

			// Build cylinders and save cylinders charts
			Plotter plotter = new Plotter(logger);
			List<Cylinder> cylinders = new ArrayList<Cylinder>();
			for (String[] row : validationResults) {
				Coord3d baseCentre = null;
				if (row[0].contains("-BayesCentralAgent-")) {
					baseCentre = new Coord3d(-2, -2, 0);
				} else {
					String simulationID = row[0];
					String[] splits = simulationID.split("-");
					for (String split : splits) {
						String aux = "Agent";
						if (split.startsWith(aux)) {
							String agentNumber = split.substring(aux.length());
							int agentNumberInteger = new Integer(agentNumber);
							baseCentre = new Coord3d(agentNumberInteger * 2,
									agentNumberInteger * 2, 0);
							break;
						}
					}
				}

				float height = new Float(row[3]);
				float radius = 0.5f;
				Cylinder cylinder = plotter.getCylinder(baseCentre, height,
						radius);
				cylinder.setColorMapper(new ColorMapper(new ColorMapRainbow(),
						new Color(height * 200, height * 200, height * 200)));
				cylinders.add(cylinder);
			}

			logger.info("Generating validations chart for iteration "
					+ iteration);
			String[] axisLabels = new String[3];
			axisLabels[0] = "";
			axisLabels[1] = "Agent Number";
			axisLabels[2] = "SuccessRatio";
			plotter.saveCylinder3DChart(iterationChartFolder
					+ "/validations.png", axisLabels, cylinders,
					ViewPositionMode.PROFILE, new Coord3d(0, 1, 0));

		} catch (FileNotFoundException e) {
			logger.severe(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			System.exit(1);
		}

	}

}
