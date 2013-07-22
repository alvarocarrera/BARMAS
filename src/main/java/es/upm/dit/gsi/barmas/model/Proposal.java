/**
 * es.upm.dit.gsi.barmas.model.Proposal.java
 */
package es.upm.dit.gsi.barmas.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Project: barmas
 * File: es.upm.dit.gsi.barmas.model.Proposal.java
 * 
 * Grupo de Sistemas Inteligentes
 * Departamento de Ingenier�a de Sistemas Telem�ticos
 * Universidad Polit�cnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 22/07/2013
 * @version 0.1
 * 
 */
public class Proposal extends AbstractProposal {

	private String node;
	private Map<String, Double> beliefs;

	/**
	 * Constructor
	 *
	 * @param node The name of the proposed node
	 * @param beliefs The map with value-confidence. Consistency of this probability distribution is not checked in this class.
	 */
	public Proposal(String node, Map<String,Double> beliefs) {
		this.node = node;
		this.beliefs = beliefs;
	}
	
	/**
	 * Constructor
	 *
	 * @param node
	 */
	public Proposal(String node) {
		this.node = node;
		this.beliefs = new HashMap<String, Double>();
	}
	
	/**
	 * @param value A possible value of the node
	 * @param confidence Its confidence. Consistency of this probability distribution is not checked in this class.
	 */
	public void addValueWithConfidence(String value, double confidence) {
		this.beliefs.put(value, confidence);
	}

	/* (non-Javadoc)
	 * @see es.upm.dit.gsi.barmas.model.AbstractProposal#getNode()
	 */
	@Override
	public String getNode() {
		return this.node;
	}

	/* (non-Javadoc)
	 * @see es.upm.dit.gsi.barmas.model.AbstractProposal#getValues()
	 */
	@Override
	public Set<String> getValues() {
		return this.beliefs.keySet();
	}

	/* (non-Javadoc)
	 * @see es.upm.dit.gsi.barmas.model.AbstractProposal#getValuesWithConfidence()
	 */
	@Override
	public Map<String, Double> getValuesWithConfidence() {
		return this.beliefs;
	}

	/* (non-Javadoc)
	 * @see es.upm.dit.gsi.barmas.model.AbstractProposal#getConfidenceForValue(java.lang.String)
	 */
	@Override
	public double getConfidenceForValue(String value) {
		double confidence = this.beliefs.get(value);
		return confidence;
	}

}
