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
 * es.upm.dit.gsi.barmas.model.AbstractProposal.java
 */
package es.upm.dit.gsi.barmas.agent.capability.argumentation;

import java.util.Map;
import java.util.Set;

/**
 * Project: barmas
 * File: es.upm.dit.gsi.barmas.model.AbstractProposal.java
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
public abstract class AbstractProposal {
	
	/**
	 * @return The name of the proposed node.
	 */
	public abstract String getNode();
	
	/**
	 * @return The possible values of the node.
	 */
	public abstract Set<String> getValues();
	
	/**
	 * @return All values of the node with their confidences.
	 */
	public abstract Map<String,Double> getValuesWithConfidence();
	
	/**
	 * @param value
	 * @return The confidence of this value/state for the proposed node.
	 */
	public abstract double getConfidenceForValue(String value);	
	

}
