/**
 * es.upm.dit.gsi.barmas.model.AbstractArgument.java
 */
package es.upm.dit.gsi.barmas.model;

import java.util.Set;

/**
 * Project: barmas
 * File: es.upm.dit.gsi.barmas.model.AbstractArgument.java
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
public abstract class AbstractArgument {
	
	/**
	 * @return The givens/evidences of the argument
	 */
	public abstract Set<? extends AbstractGiven> getGivens();
	
	/**
	 * @return The proposals/beliefs of the argument
	 */
	public abstract Set<? extends AbstractProposal> getProposals();

}
