/*******************************************************************************
 * Copyright (c) 2013 alvarocarrera Grupo de Sistemas Inteligentes - Universidad Polit�cnica de Madrid. (GSI-UPM)
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
 * es.upm.dit.gsi.barmas.agent.BarmasAgent.java
 */
package es.upm.dit.gsi.barmas.solarflare.agent.assumptions;

import jason.asSemantics.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import unbbayes.prs.bn.ProbabilisticNetwork;
import es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes.AgentArgumentativeCapability;
import es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes.Argument;
import es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes.Argumentation;
import es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes.ArgumentativeAgent;
import es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes.Given;
import es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes.Proposal;
import es.upm.dit.gsi.barmas.solarflare.model.SolarFlare;
import es.upm.dit.gsi.barmas.solarflare.model.scenario.SolarFlareScenario;
import es.upm.dit.gsi.barmas.solarflare.model.vocabulary.SolarFlareType;
import es.upm.dit.gsi.barmas.solarflare.simulation.SolarFlareClassificationSimulation;
import es.upm.dit.gsi.shanks.ShanksSimulation;
import es.upm.dit.gsi.shanks.agent.SimpleShanksAgent;
import es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes.BayesianReasonerShanksAgent;
import es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes.ShanksAgentBayesianReasoningCapability;
import es.upm.dit.gsi.shanks.exception.ShanksException;

/**
 * Project: barmas File: es.upm.dit.gsi.barmas.agent.BarmasAgent.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingenier�a de Sistemas
 * Telem�ticos Universidad Polit�cnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 23/07/2013
 * @version 0.1
 * 
 */
public class AdvancedClassificatorAgent extends SimpleShanksAgent implements
		BayesianReasonerShanksAgent, ArgumentativeAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8582918551821278046L;

	private ArgumentativeAgent manager;
	private String bnFilePath;
	private ProbabilisticNetwork bn;
	private List<String> sensors;

	private List<ArgumentativeAgent> argumentationGroup;

	private HashMap<String, String> evidences;

	private ArrayList<Argument> pendingArguments;

	private Argumentation argumentation;
	
	private HashMap<Integer, Argument> mySentArguments;
	
	// STATES
	private boolean IDLE;
	private boolean ARGUMENTING;
	private boolean PROCESSING;
	private boolean WAITING;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param manager
	 * @param bnPath
	 */
	public AdvancedClassificatorAgent(String id,
			ArgumentativeAgent manager, String bnPath,
			List<String> sensors) {
		super(id);
		this.bnFilePath = bnPath;
		this.sensors = sensors;
		this.setArgumentationManager(manager);
		this.pendingArguments = new ArrayList<Argument>();
		this.evidences = new HashMap<String, String>();
		this.mySentArguments = new HashMap<Integer, Argument>();
		this.argumentationGroup = new ArrayList<ArgumentativeAgent>();
		try {
			ShanksAgentBayesianReasoningCapability.loadNetwork(this);
		} catch (ShanksException e) {
			e.printStackTrace();
		}
		this.goToIdle();

		// Register in manager
		this.getArgumentationManager().addArgumentationGroupMember(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.upm.dit.gsi.shanks.agent.ShanksAgent#checkMail()
	 */
	public void checkMail() {
		// Check inbox
		List<Message> inbox = this.getInbox();
		if (inbox.size() > 0) {

			this.pendingArguments = new ArrayList<Argument>();
			for (Message msg : inbox) {
				Argument arg = (Argument) msg.getPropCont();
				this.pendingArguments.add(arg);
			}

			inbox.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.shanks.agent.SimpleShanksAgent#executeReasoningCycle(es
	 * .upm.dit.gsi.shanks.ShanksSimulation)
	 */
	@Override
	public void executeReasoningCycle(ShanksSimulation simulation) {
		SolarFlareClassificationSimulation sim = (SolarFlareClassificationSimulation) simulation;
		// Check sensors
		boolean newEvent = this.getSensorsUpdateData(sim);

		// State machine
		if (this.pendingArguments.size() > 0) {
			if (this.IDLE) {
				this.argumentation = new Argumentation(0);
			} else if (this.IDLE || this.WAITING) {
				this.goToProcessing(sim);
				this.evaluateNextAction(sim);
			}
		} else if (this.IDLE && newEvent) {
			this.argumentation = new Argumentation(1);
			this.goToArgumenting(sim);
		}
	}

	private void evaluateNextAction(SolarFlareClassificationSimulation sim) {
		// Check graph and/or argumentation and try to generate arguments
		boolean usefulArgument = this.evaluatePossibleArguments();
		if (usefulArgument) {
			this.goToArgumenting(sim);
		} else {
			this.goToIdle();
		}
	}

	private boolean evaluatePossibleArguments() {
		// Get all available givens
		boolean newEvidences = this.updateEvidences(this.pendingArguments);
		
		//TODO check other possibilities
		
		return newEvidences;
	}

	/**
	 * Process all pending arguments
	 */
	private void processPendingArguments(
			SolarFlareClassificationSimulation simulation) {

		// Process incoming messages
		for (Argument arg : this.pendingArguments) {
			simulation.getLogger().finer(
					"Agent: " + this.getID() + " -> Received arguments from: "
							+ arg.getProponent().getProponentName());
			this.argumentation.addArgument(arg);
		}
		this.pendingArguments.clear();

	}

	/**
	 * Check if new givens are found in the pending arguments
	 * and update evidences if any.
	 * 
	 * @param pendingArguments
	 * @return
	 */
	private boolean updateEvidences(List<Argument> pendingArguments) {
		// Update current evidences with new givens received in the incoming
		// argument
		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		boolean newInfo = false;
		for (Argument arg : pendingArguments) {
			Set<Given> givens = arg.getGivens();
			for (Given given : givens) {
				if (evidences.keySet().contains(given.getNode())) {
					if (!evidences.get(given.getNode())
							.equals(given.getValue())) {
						logger.warning("No sense! Different evidences from different agents.");
						logger.finest("Agent: " + this.getID() + " Evidence: "
								+ given.getNode() + " - "
								+ evidences.get(given.getValue()));
						logger.finest("Agent: "
								+ arg.getProponent().getProponentName()
								+ " Evidence: " + given.getNode() + " - "
								+ given.getValue());
					}
					// else {
					// logger.finer("Agent: " + this.getID()
					// + " -> Old evidence received from "
					// + arg.getProponent().getProponentName()
					// + " Evidence: " + given.getNode() + " - "
					// + given.getValue());
					// }
				} else {
					newInfo = true;
					this.evidences.put(given.getNode(), given.getValue());
					logger.finer("Agent: " + this.getID()
							+ " -> Adding evidence received from "
							+ arg.getProponent().getProponentName()
							+ " Evidence: " + given.getNode() + " - "
							+ given.getValue());

				}
			}
		}
		return newInfo;
	}

	private boolean processNewArgument(Argument arg, boolean newInfo,
			SolarFlareClassificationSimulation sim) {
		
		this.argumentation.addArgument(arg);
		
		
		boolean sent = false;
		if (newInfo) {
			try {
				// ********
				// TODO check if this is required
				// Maybe it could be done only once per argumentation, at
				// finishing steps
				ShanksAgentBayesianReasoningCapability.clearEvidences(this
						.getBayesianNetwork());
				// *********

				sim.getLogger().finer(
						"Agent: " + this.getID() + " -> Number of evidences: "
								+ this.evidences.size());
				for (Entry<String, String> entry : evidences.entrySet()) {
					sim.getLogger()
							.finest("Agent: " + this.getID()
									+ " adding evidence: " + entry.getKey()
									+ " - " + entry.getValue());
					try {

						ShanksAgentBayesianReasoningCapability.addEvidence(
								this, entry.getKey(), entry.getValue());
					} catch (ShanksException e) {
						sim.getLogger().fine(
								"Agent: " + this.getID()
										+ " -> Unknown state for node: "
										+ entry.getKey() + " -> State: "
										+ entry.getValue());

						sim.getLogger().warning(e.getMessage());
					}
				}
			} catch (ShanksException e) {
				sim.getLogger().warning(
						"Given received from agent: "
								+ arg.getProponent().getProponentName()
								+ ": -->");
				sim.getLogger().warning(e.getMessage());
			}
		}

		// Get hypothesis
		HashMap<String, Float> hyps = null;
		try {
			hyps = ShanksAgentBayesianReasoningCapability
					.getNodeStatesHypotheses(this,
							SolarFlareType.class.getSimpleName());
		} catch (ShanksException e) {
			e.printStackTrace();
		}
		String hyp = "";
		float maxValue = 0;
		for (Entry<String, Float> entry : hyps.entrySet()) {
			if (entry.getValue() > maxValue) {
				maxValue = entry.getValue();
				hyp = entry.getKey();
			}
		}
		SolarFlare orig = (SolarFlare) sim.getScenario().getNetworkElement(
				SolarFlareScenario.ORIGINALFLARE);
		sim.logger.finer("Agent: " + this.getID()
				+ " has hypothesis for case SolarFlareID: " + orig.getCaseID()
				+ " " + SolarFlareType.class.getSimpleName() + " - " + hyp
				+ " -> Confidence: " + maxValue);

		Set<Proposal> proposals = arg.getProposals();
		String phyp = "";
		double pmaxValue = 0;
		for (Proposal p : proposals) {
			if (p.getNode().equals(SolarFlareType.class.getSimpleName())) {
				HashMap<String, Double> values = (HashMap<String, Double>) p
						.getValuesWithConfidence();
				for (Entry<String, Double> entry : values.entrySet()) {
					if (entry.getValue() > pmaxValue) {
						pmaxValue = entry.getValue();
						phyp = entry.getKey();
					}
				}
			}
		}

		if (!hyp.equals(phyp)) {
			sim.getLogger().finer(
					"Agent: " + this.getID() + " disagrees with "
							+ arg.getProponent().getProponentName());
			if (newInfo) {
				// Create and send counter argument
				Argument counterArg = AgentArgumentativeCapability
						.createArgument(this,
								SolarFlareType.class.getSimpleName(), hyp,
								maxValue, evidences, sim.schedule.getSteps(),
								System.currentTimeMillis());
				AgentArgumentativeCapability.sendArgument(this, counterArg);
				sent = true;
				// TODO if no new evidences can be offered - add assumptions??

				sim.getLogger().finer(
						"Counter argument sent by agent: " + this.getID());
			}
		} else if (evidences.size() > arg.getGivens().size()) {
			sim.getLogger()
					.finer("Agent: " + this.getID() + " agrees with "
							+ arg.getProponent().getProponentName()
							+ " and sends a support argument with more givens.");
			// Create and send defensive argument (to support)
			Argument supportArg = AgentArgumentativeCapability.createArgument(
					this, SolarFlareType.class.getSimpleName(), hyp, maxValue,
					evidences, sim.schedule.getSteps(),
					System.currentTimeMillis());
			AgentArgumentativeCapability.sendArgument(this, supportArg);
			sent = true;
		} else {
			sim.getLogger().finer(
					"Agent: " + this.getID() + " agrees with "
							+ arg.getProponent().getProponentName());
		}
		return sent;
	}

	private void generateArguments(SolarFlareClassificationSimulation sim) {

		try {

			sim.getLogger().finer(
					"Agent: " + this.getID() + " -> Number of evidences: "
							+ this.evidences.size());
			for (Entry<String, String> entry : evidences.entrySet()) {
				try {
					sim.getLogger()
							.finer("Agent: " + this.getID()
									+ " adding evidence: " + entry.getKey()
									+ " - " + entry.getValue());
					ShanksAgentBayesianReasoningCapability.addEvidence(this,
							entry.getKey(), entry.getValue());
				} catch (Exception e) {
					sim.getLogger().warning(
							"Agent: " + this.getID()
									+ " -> Unknown state for node: "
									+ entry.getKey() + " -> State: "
									+ entry.getValue());
				}

			}
			// Get hypothesis
			HashMap<String, Float> hyps = ShanksAgentBayesianReasoningCapability
					.getNodeStatesHypotheses(this,
							SolarFlareType.class.getSimpleName());
			String hyp = "";
			float maxValue = 0;
			for (Entry<String, Float> entry : hyps.entrySet()) {
				if (entry.getValue() > maxValue) {
					maxValue = entry.getValue();
					hyp = entry.getKey();
				}
			}
			SolarFlare orig = (SolarFlare) sim.getScenario().getNetworkElement(
					SolarFlareScenario.ORIGINALFLARE);
			sim.logger.finer("Agent: " + this.getID()
					+ " has hypothesis for case SolarFlareID: "
					+ orig.getCaseID() + " "
					+ SolarFlareType.class.getSimpleName() + " - " + hyp
					+ " -> Confidence: " + maxValue);

			// Create and send initial argument
			Argument arg = AgentArgumentativeCapability.createArgument(this,
					SolarFlareType.class.getSimpleName(), hyp, maxValue,
					evidences, sim.schedule.getSteps(),
					System.currentTimeMillis());
			AgentArgumentativeCapability.sendArgument(this, arg);

			
			sim.getLogger().fine(
					"Initial argument sent by agent: " + this.getID());
		} catch (ShanksException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param sim
	 * @return
	 */
	private boolean getSensorsUpdateData(SolarFlareClassificationSimulation sim) {
		SolarFlare orig = (SolarFlare) sim.getScenario().getNetworkElement(
				SolarFlareScenario.ORIGINALFLARE);
		boolean newEvent = orig.getStatus().get(SolarFlare.READY);
		if (newEvent) {
			for (String sensor : this.sensors) {
				String value = (String) orig.getProperty(sensor);
				evidences.remove(sensor);
				evidences.put(sensor, value);
			}
		}
		return newEvent;
	}

	public String getProponentName() {
		return this.getID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent
	 * #getProponent()
	 */
	public ArgumentativeAgent getProponent() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent
	 * #getArgumentationManager()
	 */
	public ArgumentativeAgent getArgumentationManager() {
		return this.manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent
	 * #getArgumentationManagerName()
	 */
	public String getArgumentationManagerName() {
		SimpleShanksAgent ag = (SimpleShanksAgent) this.manager;
		return ag.getID();
	}

	public Set<Argument> getCurrentArguments() throws ShanksException {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateBeliefsWithNewArguments(Set<Argument> args)
			throws ShanksException {
		// TODO Auto-generated method stub

	}

	public ProbabilisticNetwork getBayesianNetwork() {
		return this.bn;
	}

	public void setBayesianNetwork(ProbabilisticNetwork bn) {
		this.bn = bn;
	}

	public String getBayesianNetworkFilePath() {
		return this.bnFilePath;
	}

	public void setArgumentationManager(ArgumentativeAgent manager) {
		this.manager = manager;
	}

	public void finishArgumenation() {
		this.goToIdle();
		this.evidences.clear();
		this.pendingArguments.clear();
		this.argumentation = null;
		try {
			ShanksAgentBayesianReasoningCapability.clearEvidences(this);
		} catch (ShanksException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent
	 * #sendArgument
	 * (es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes.Argument)
	 */
	public void sendArgument(Argument arg) {
		for (ArgumentativeAgent agent : this.argumentationGroup) {
			Message m = new Message();
			m.setPropCont(arg);
			m.setReceiver(((SimpleShanksAgent) agent).getID());
			super.sendMsg(m);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent
	 * #addArgumentationGroupMember
	 * (es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent)
	 */
	public void addArgumentationGroupMember(ArgumentativeAgent agent) {
		if (!agent.equals(this) && !this.argumentationGroup.contains(agent)) {
			this.argumentationGroup.add(agent);
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
					.fine("Agent: "
							+ this.getID()
							+ " has added a new agent as argumentation member -> New member: "
							+ agent.getProponentName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent
	 * #removeArgumentationGroupMember
	 * (es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent)
	 */
	public void removeArgumentationGroupMember(ArgumentativeAgent agent) {
		this.argumentationGroup.remove(agent);
	}

	/**
	 * Go to status IDLE
	 */
	private void goToIdle() {
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).fine(this.getID()+ " going to status: IDLE");
		this.IDLE = true;
		this.ARGUMENTING = false;
		this.PROCESSING = false;
		this.WAITING = false;
	}

	/**
	 * Go to status ARGUMENTING
	 */
	private void goToArgumenting(SolarFlareClassificationSimulation sim) {
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).fine(this.getID()+ " going to status: ARGUMENTING");
		this.IDLE = false;
		this.ARGUMENTING = true;
		this.PROCESSING = false;
		this.WAITING = false;
		this.generateArguments(sim);
		this.goToWaiting();
	}

	/**
	 * Go to status PROCESSING
	 * 
	 * @param sim
	 */
	private void goToProcessing(SolarFlareClassificationSimulation sim) {
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).fine(this.getID()+ " going to status: PROCESSING");
		this.IDLE = false;
		this.ARGUMENTING = false;
		this.PROCESSING = true;
		this.WAITING = false;
		this.processPendingArguments(sim);
	}

	/**
	 * Go to status WAITING
	 */
	private void goToWaiting() {
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).fine(this.getID()+ " going to status: WAITING");
		this.IDLE = false;
		this.ARGUMENTING = false;
		this.PROCESSING = false;
		this.WAITING = true;
	}
}
