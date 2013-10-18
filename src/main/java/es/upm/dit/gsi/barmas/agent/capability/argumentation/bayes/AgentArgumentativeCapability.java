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
 * es.upm.dit.gsi.barmas.agent.capability.argumentation.AgentArgumentativeCapability.java
 */
package es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes.BayesianReasonerShanksAgent;
import es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes.ShanksAgentBayesianReasoningCapability;
import es.upm.dit.gsi.shanks.exception.ShanksException;

/**
 * Project: barmas File: es.upm.dit.gsi.barmas.agent.capability.argumentation.
 * AgentArgumentativeCapability.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 23/07/2013
 * @version 0.1
 * 
 */
public class AgentArgumentativeCapability {

	public static final int NOATTACK = 0;
	public static final int DEFEATER = 1;
	public static final int DIRECTDEFEATER = 2;
	public static final int UNDERCUT = 3;
	public static final int DIRECTUNDERCUT = 4;
	public static final int CANONICALUNDERCUT = 5;
	public static final int REBUTTAL = 6;
	public static final int DEFEATINGREBUTTAL = 7;

	/**
	 * 
	 * @param proponent
	 * @param node
	 * @param value
	 * @param conf
	 * @param evidences
	 * @return an argument with the provided info.
	 */
	public static Argument createArgument(ArgumentativeAgent proponent,
			String node, String value, double conf,
			HashMap<String, String> evidences, long step, long timestamp) {

		Argument arg = new Argument(proponent, step, timestamp);
		for (Entry<String, String> entry : evidences.entrySet()) {
			Given given = new Given(entry.getKey(), entry.getValue());
			arg.addGiven(given);
		}
		Proposal proposal = new Proposal(node);
		proposal.addValueWithConfidence(value, conf);
		arg.addProposal(proposal);

		return arg;
	}

	/**
	 * @param proponent
	 * @param proposals
	 * @param assumptions
	 * @param evidences
	 * @param step
	 * @param timestamp
	 * @return
	 */
	public static Argument createArgument(ArgumentativeAgent proponent,
			HashMap<String, HashMap<String, Double>> proposals,
			HashMap<String, HashMap<String, Double>> assumptions,
			HashMap<String, String> evidences, long step, long timestamp) {

		Argument arg = new Argument(proponent, step, timestamp);

		for (Entry<String, String> entry : evidences.entrySet()) {
			Given given = new Given(entry.getKey(), entry.getValue());
			arg.addGiven(given);
		}

		for (Entry<String, HashMap<String, Double>> entry : assumptions
				.entrySet()) {
			Assumption assumption = new Assumption(entry.getKey(),
					entry.getValue());
			arg.addAssumption(assumption);
		}

		for (Entry<String, HashMap<String, Double>> entry : proposals
				.entrySet()) {
			Proposal proposal = new Proposal(entry.getKey(), entry.getValue());
			arg.addProposal(proposal);
		}

		return arg;

	}

	/**
	 * 
	 * @param proponent
	 * @param bn
	 * @return all arguments for a given Bayesian network
	 * @throws ShanksException
	 */
	public static Set<Argument> createArguments(ArgumentativeAgent proponent,
			ProbabilisticNetwork bn, long step, long timestamp)
			throws ShanksException {
		Set<Argument> args = new HashSet<Argument>();
		HashMap<String, String> evidences = (HashMap<String, String>) ShanksAgentBayesianReasoningCapability
				.getEvidences(bn);
		HashMap<String, HashMap<String, Float>> hypotheses = ShanksAgentBayesianReasoningCapability
				.getAllHypotheses(bn);
		for (Entry<String, HashMap<String, Float>> hyp : hypotheses.entrySet()) {
			ProbabilisticNode node = (ProbabilisticNode) bn.getNode(hyp
					.getKey());
			if (!node.hasEvidence()) {
				HashMap<String, Float> states = hyp.getValue();
				for (Entry<String, Float> state : states.entrySet()) {
					Argument arg = AgentArgumentativeCapability.createArgument(
							proponent, hyp.getKey(), state.getKey(),
							state.getValue(), evidences, step, timestamp);
					args.add(arg);
				}
			}
		}
		return args;
	}

	/**
	 * 
	 * @param agent
	 * @return all arguments for a given agent
	 * @throws ShanksException
	 */
	public static Set<Argument> createArguments(
			BayesianReasonerShanksAgent agent, long step, long timestamp)
			throws ShanksException {
		return AgentArgumentativeCapability.createArguments(
				(ArgumentativeAgent) agent, agent.getBayesianNetwork(), step,
				timestamp);
	}

	/**
	 * Update the BN to update all beliefs included in the given arguments
	 * 
	 * @param args
	 * @param bn
	 * @throws ShanksException
	 */
	public static void updateBeliefs(Set<Argument> args, ProbabilisticNetwork bn)
			throws ShanksException {
		HashMap<String, HashMap<String, Double>> beliefs = new HashMap<String, HashMap<String, Double>>();
		for (Argument arg : args) {
			for (Proposal p : arg.getProposals()) {
				String node = p.getNode();
				if (!beliefs.containsKey(node)) {
					beliefs.put(node, new HashMap<String, Double>());
				}
				Map<String, Double> values = p.getValuesWithConfidence();
				for (Entry<String, Double> value : values.entrySet()) {
					if (!beliefs.get(node).containsKey(value.getKey())) {
						beliefs.get(node).put(value.getKey(), value.getValue());
					} else {
						throw new ShanksException(
								"Duplicated belief in the argument: " + node
										+ "-" + value.getKey());
					}
				}
			}
		}
		ShanksAgentBayesianReasoningCapability.addSoftEvidences(bn, beliefs);
	}

	/**
	 * Update the agent BN to update all beliefs included in the given arguments
	 * 
	 * @param args
	 * @param agent
	 * @throws ShanksException
	 */
	public static void updateBeliefs(Set<Argument> args,
			BayesianReasonerShanksAgent agent) throws ShanksException {
		ProbabilisticNetwork bn = agent.getBayesianNetwork();
		AgentArgumentativeCapability.updateBeliefs(args, bn);
	}

	/**
	 * Send the arguments to the arguementation manager
	 * 
	 * 
	 * @param proponent
	 * @param args
	 */
	public static void sendArguments(ArgumentativeAgent proponent,
			Set<Argument> args) {
		for (Argument arg : args) {
			proponent.sendArgument(arg);
		}
	}

	/**
	 * @param args
	 * @param argumentation
	 * @param attackType
	 * @return a list of arguments no attacked for other arguments of given
	 *         attack type from the given args list
	 */
	public static List<Argument> getUnattackedArguments(List<Argument> args,
			Argumentation argumentation, int attackType) {
		List<Argument> unattackedArgs = new ArrayList<Argument>();
		unattackedArgs.addAll(args);
		HashMap<Argument, HashMap<Argument, Integer>> graph = argumentation
				.getGraph();
		for (Argument arg : args) {
			boolean attacked = false;
			for (Entry<Argument, HashMap<Argument, Integer>> entry : graph
					.entrySet()) {
				HashMap<Argument, Integer> attacks = entry.getValue();
				for (Entry<Argument, Integer> entry2 : attacks.entrySet()) {
					if (entry2.getKey().equals(arg)
							&& entry2.getValue() == attackType) {
						unattackedArgs.remove(arg);
						attacked = true;
						break;
					}
				}
				if (attacked) {
					break;
				}
			}
		}
		return unattackedArgs;
	}

	/**
	 * @param args
	 * @param argumentation
	 * @param attackTypes
	 * @return a list of arguments no attacked for other arguments of given
	 *         attack types from the given args list
	 */
	public static List<Argument> getUnattackedArguments(List<Argument> args,
			Argumentation argumentation, List<Integer> attackTypes) {
		List<Argument> unattackedArgs = new ArrayList<Argument>();
		unattackedArgs.addAll(args);
		for (Integer attackType : attackTypes) {
			unattackedArgs = AgentArgumentativeCapability
					.getUnattackedArguments(unattackedArgs, argumentation,
							attackType);
		}
		return unattackedArgs;
	}

	/**
	 * Return the type of the attack (from a to b) using the following rules:
	 * 
	 * 0 - a does not attack b
	 * 
	 * 1 - a is defeater of b if Claim(A) implies not all Support(B)
	 * 
	 * 2 - a is a direct defeater of b if there is phi in Support(B) such that
	 * Claim(A) implies not phi
	 * 
	 * 3 - a is a undercut of b if there is Phi subset of Support(B) such that
	 * Claim(A) is exactly not all Phi
	 * 
	 * 4 - a is a direct undercut of b if there is phi in Support(B) such that
	 * Claim(A) is exactly not phi
	 * 
	 * 5 - a is a canonical undercut of b if Claim(A) is exactly not Support(B)
	 * 
	 * 6 - a is a rebuttal of b if Claim(A) is exactly not Claim(B)
	 * 
	 * 7 - a is a defeating rebuttal of b if Claim(A) implies not Claim(B)
	 * 
	 * @param a
	 * @param b
	 * @return 0-7 attack type
	 */
	public static int getAttackType(Argument a, Argument b) {

		// In the code (Claim = Givens + Proposal) and (Support = Givens +
		// Assumptions)
		// Check if argument a attacks b:

		if (!b.equals(a)) {
			Set<Given> agivens = a.getGivens();
			Set<Given> bgivens = b.getGivens();
			// Set<Assumption> aassumptions = a.getAssumptions();
			Set<Assumption> bassumptions = b.getAssumptions();
			Set<Proposal> aproposals = a.getProposals();
			Set<Proposal> bproposals = b.getProposals();

			// Type -1 if evidences are not coherent
			for (Given bgiven : bgivens) {
				for (Given agiven : agivens) {
					if (agiven.getNode().equals(bgiven.getNode())) {
						if (!agiven.getValue().equals(bgiven.getValue())) {
							Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).severe(
									"Incoherent evidence in arguments");
							return -1; // This has no sense!!
						}
					}
				}
			}

			// Check type 5 - a is a canonical undercut of b if Claim(A) is
			// exactly
			// not Support(B)
			if (bgivens.size() == 0 && agivens.size() > 0) {
				return CANONICALUNDERCUT;
			}

			// Check type 4 - a is a direct undercut of b if there is phi in
			// Support(B) such that Claim(A) is exactly not phi
			if (agivens.size() == (bgivens.size() + 1)) {
				return DIRECTUNDERCUT;
			}

			// Check type 3 - a is a undercut of b if there is Phi subset of
			// Support(B) such that Claim(A) is exactly not all Phi
			if (agivens.size() > bgivens.size()) {
				return UNDERCUT;
			}

			// Check type 1 - a is defeater of b if Claim(A) implies not all
			// Support(B)
			if (bassumptions.size() == 1) {
				for (Proposal ap : aproposals) {
					for (Assumption ba : bassumptions) {
						if (ba.getNode().equals(ap.getNode())) {
							return DEFEATER;
						}
					}
				}
			}

			// Check type 2 - a is a direct defeater of b if there is phi in
			// Support(B) such that Claim(A) implies not phi
			for (Proposal ap : aproposals) {
				for (Assumption ba : bassumptions) {
					if (ba.getNode().equals(ap.getNode())) {
						return DIRECTDEFEATER;
					}
				}
			}

			// Check types 6 and 7
			int aux = 0;
			for (Proposal bp : bproposals) {
				for (Proposal ap : aproposals) {
					String anode = bp.getNode();
					String node = ap.getNode();
					// If the proposed node are equals...
					if (node.equals(anode)) {
						String astate = ap.getMaxState();
						String bstate = bp.getMaxState();
						// if they don't aggree with the state
						if (!astate.equals(bstate)) {
							aux++;
						}
						break;
					}
				}
			}
			if (aux == bproposals.size()) {
				// Check type 6 - a is a rebuttal of b if Claim(A) is exactly
				// not
				// Claim(B)
				return REBUTTAL;
			} else if (aux > 0) {
				// Check type 7 - a is a defeating rebuttal of b if Claim(A)
				// implies not
				// Claim(B)
				return DEFEATINGREBUTTAL;
			}

		}

		// If not... a does not attack b (Type 0)
		return NOATTACK;
	}

	/**
	 * Update the graph of the argumentation
	 * 
	 * @param argument
	 * @param argumentation
	 */
	public static void updateAttacksGraph(Argument argument,
			Argumentation argumentation) {
		HashMap<Argument, HashMap<Argument, Integer>> graph = argumentation
				.getGraph();
		for (Argument arg : argumentation.getArguments()) {
			int attackType = AgentArgumentativeCapability.getAttackType(
					argument, arg);
			graph.get(argument).put(arg, attackType);
			attackType = AgentArgumentativeCapability.getAttackType(arg,
					argument);
			graph.get(arg).put(argument, attackType);
		}
	}

	/**
	 * Resolution conflicts method
	 * 
	 * @param argumentation
	 */
	public static void addConclusionHigherHypothesis(Argumentation argumentation) {

		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.fine("Getting the higher hypothesis...");
		logger.finest("Evaluating possible conclusions...");
		List<Argument> allArguments = new ArrayList<Argument>();
		allArguments.addAll(argumentation.getArguments());
		List<Argument> possibleConclusions = AgentArgumentativeCapability
				.getUnattackedArguments(allArguments, argumentation, UNDERCUT);

		int maxEvidences = 0;
		for (Argument arg : argumentation.getArguments()) {
			int evCardinal = arg.getGivens().size();
			if (evCardinal > maxEvidences) {
				maxEvidences = evCardinal;
			}
		}
		// Pick possible arguments
		String hyp = "";
		double max = 0;
		Argument argumentConclusion = null;
		for (Argument arg : possibleConclusions) {
			if (arg.getGivens().size() == maxEvidences) {
				for (Proposal p : arg.getProposals()) {
					if (p.getMaxValue() > max) {
						max = p.getMaxValue();
						hyp = p.getMaxState();
						argumentConclusion = arg;
					}
				}
			}
		}

		logger.fine("Argumentation Manager --> Higher hypothesis found: " + hyp
				+ " - " + max + " from "
				+ argumentConclusion.getProponent().getProponentName()
				+ " - ArgumentID: " + argumentConclusion.getId());

		argumentation.getConclusions().add(argumentConclusion);
	}

	/**
	 * @param p
	 * @param q
	 * @return normalised euclidean distance between both probability
	 *         distributions, -1 if there is any problem
	 * 
	 */
	public static double getNormalisedEuclideanDistance(
			HashMap<String, Double> p, HashMap<String, Double> q) {
		if (p.size() != q.size()
				|| !(AgentArgumentativeCapability
						.isCoherentProbabilityDistribution(p))
				|| !(AgentArgumentativeCapability
						.isCoherentProbabilityDistribution(q))) {
			return -1;
		}
		double distance = 0;
		for (String s : p.keySet()) {
			distance = distance + Math.pow((p.get(s) - q.get(s)), 2);
		}
		distance = Math.sqrt(distance) / Math.sqrt(2);
		return distance;
	}

	/**
	 * @param p
	 * @param q
	 * @return normalised hellinger distance between both probability
	 *         distributions, -1 if there is any problem
	 */
	public static double getNormalisedHellingerDistance(
			HashMap<String, Double> p, HashMap<String, Double> q) {
		if (p.size() != q.size()
				|| !(AgentArgumentativeCapability
						.isCoherentProbabilityDistribution(p))
				|| !(AgentArgumentativeCapability
						.isCoherentProbabilityDistribution(q))) {
			return -1;
		}
		double distance = 0;
		for (String s : p.keySet()) {
			distance = distance
					+ Math.pow((Math.sqrt(p.get(s)) - Math.sqrt(q.get(s))), 2);
		}
		distance = Math.sqrt(distance) / Math.sqrt(2);
		return distance;
	}

	/**
	 * @param p
	 * @param q
	 * @return normalised j-divergence distance between both probability
	 *         distributions, -1 if there is any problem
	 */
	public static double getNormalisedJDivergeDistance(
			HashMap<String, Double> p, HashMap<String, Double> q) {
		if (p.size() != q.size()
				|| !(AgentArgumentativeCapability
						.isCoherentProbabilityDistribution(p))
				|| !(AgentArgumentativeCapability
						.isCoherentProbabilityDistribution(q))) {
			return -1;
		}
		for (String s : p.keySet()) {
			if (p.get(s) == 0 || q.get(s) == 0) {
				return 1;
			}
		}
		double distance = AgentArgumentativeCapability
				.getKullBackLeiberDistance(p, q)
				+ AgentArgumentativeCapability.getKullBackLeiberDistance(q, p);
		distance = distance / 2;
		int alpha = 10;
		distance = distance
				/ Math.sqrt(Math.sqrt(Math.pow(distance, 2)) + alpha);
		return distance;
	}

	/**
	 * @param p
	 * @param q
	 * @return kullback-leiber distance between both probability distributions,
	 *         -1 if there is any problem
	 */
	private static double getKullBackLeiberDistance(HashMap<String, Double> p,
			HashMap<String, Double> q) {
		if (p.size() != q.size()
				|| !(AgentArgumentativeCapability
						.isCoherentProbabilityDistribution(p))
				|| !(AgentArgumentativeCapability
						.isCoherentProbabilityDistribution(q))) {
			return -1;
		}
		double distance = 0;
		for (String s : p.keySet()) {
			distance = distance
					- (p.get(s) * (Math.log10(q.get(s) / Math.log10(2))))
					+ (p.get(s) * (Math.log10(p.get(s) / Math.log10(2))));
		}
		return distance;

	}

	/**
	 * @param p
	 * @return true if sum(pi)=1+-0.01; false if the distribution is not
	 *         coherent
	 */
	private static boolean isCoherentProbabilityDistribution(
			HashMap<String, Double> p) {
		double counter = 0;
		for (Double d : p.values()) {
			if (d < 0 || d > 1.001) {
				return false;
			}
			counter = counter + d;
		}
		if (counter > 1.001 || counter < 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @param map
	 * @return
	 */
	public static HashMap<String, Double> convertToDoubleValues(
			HashMap<String, Float> map) {
		HashMap<String, Double> newMap = new HashMap<String, Double>();
		for (Entry<String, Float> entry : map.entrySet()) {
			newMap.put(entry.getKey(), new Double(entry.getValue()));
		}
		return newMap;
	}

	/**
	 * @param map
	 * @return
	 */
	public static HashMap<String, Float> convertToFloatValues(
			HashMap<String, Double> map) {
		HashMap<String, Float> newMap = new HashMap<String, Float>();
		for (Entry<String, Double> entry : map.entrySet()) {
			newMap.put(entry.getKey(), new Float(entry.getValue()));
		}
		return newMap;
	}

}
