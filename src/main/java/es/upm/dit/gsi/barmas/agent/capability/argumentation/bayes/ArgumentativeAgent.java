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
 * es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent.java
 */
package es.upm.dit.gsi.barmas.agent.capability.argumentation.bayes;


/**
 * Project: barmas File:
 * es.upm.dit.gsi.barmas.agent.capability.argumentation.ArgumentativeAgent.java
 * 
 * Grupo de Sistemas Inteligentes
 * Departamento de Ingeniería de Sistemas Telemáticos
 * Universidad Politécnica de Madrid (UPM)
 * 
 * @author alvarocarrera
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 23/07/2013
 * @version 0.1
 * 
 */
public interface ArgumentativeAgent {

	/**
	 * @return the name of the
	 */
	public String getProponentName();

	/**
	 * @return the agent itself
	 */
	public ArgumentativeAgent getProponent();

	/**
	 * @return return the argumentation manager agent
	 */
	public ArgumentativeAgent getArgumentationManager();

	/**
	 * @param manager
	 */
	public void setArgumentationManager(ArgumentativeAgent manager);

	/**
	 * @return the name of the argumentation manager agent
	 */
	public String getArgumentationManagerName();

	/**
	 * Send the argument
	 * 
	 * @param arg
	 */
	public void sendArgument(Argument arg);

	/**
	 * 
	 */
	public void finishArgumenation();

	/**
	 * @param agent
	 */
	public void addArgumentationGroupMember(ArgumentativeAgent agent);

	/**
	 * @param agent
	 */
	public void removeArgumentationGroupMember(ArgumentativeAgent agent);

}