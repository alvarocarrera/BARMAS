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
 * es.upm.dit.gsi.barmas.agent.capability.argumentation.manager.ArgumentationManagerAgent.java
 */
package es.upm.dit.gsi.barmas.agent.capability.argumentation.manager;

import java.util.List;

import es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent;
import es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes.Argument;
import es.upm.dit.gsi.shanks.ShanksSimulation;

/**
 * Project: barmas
 * File: es.upm.dit.gsi.barmas.agent.capability.argumentation.manager.ArgumentationManagerAgent.java
 * 
 * Grupo de Sistemas Inteligentes
 * Departamento de Ingenier�a de Sistemas Telem�ticos
 * Universidad Polit�cnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 23/07/2013
 * @version 0.1
 * 
 */
public interface ArgumentationManagerAgent {
	
	/**
	 * @return
	 */
	public Argumentation getCurrentArgumentation();

	/**
	 * @return
	 */
	public List<Argumentation> getArgumentations();
	
	/**
	 * @param arg
	 * @param simuluation
	 */
	public void processNewArgument(Argument arg, ShanksSimulation simuluation);
	
	/**
	 * @param id
	 * @return
	 */
	public Argumentation getArgumentation(int id);
	
	/**
	 * @param agent
	 */
	public void addSubscriber(ArgumentativeAgent agent);
	
	/**
	 * @param agent
	 */
	public void removeSubscriber(ArgumentativeAgent agent);
	
	/**
	 * @return
	 */
	public List<ArgumentativeAgent> getSubscribers();
	
}
