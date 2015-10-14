/*****************************************************************
 SAJaS - Simple API for JADE-based Simulations is a framework to 
 facilitate running multi-agent simulations using the JADE framework.
 Copyright (C) 2015 Henrique Lopes Cardoso
 Universidade do Porto

 GNU Lesser General Public License

 This file is part of SAJaS.

 SAJaS is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SAJaS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with SAJaS.  If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************/

package sajas.proto.states;

//#CUSTOM_EXCLUDE_FILE

import sajas.core.behaviours.*;

/**
 * Note: this class has been re-implemented to redirect the use of behaviour classes to SAJaS versions.
 * 
 * @see jade.proto.states.StateResetter
 * @author hlc
 *
 */
public class StateResetter extends OneShotBehaviour{
  
	String[] sname;
	
  /**
   *  Constructor.
   * @param states  Represent the names of FSM's to reset. 
   * If null the parent Behaviour is resetted
   * 
   */
	public StateResetter(String[] states) {
		sname=states;
	}
	
	/**
   *  Constructor.
   *  equivalent to StateResetter(null)
   */
	public StateResetter(){
		this(null);
	}
	
	public void action() {
		Behaviour st;
		FSMBehaviour p = (FSMBehaviour)parent;
		if(sname == null){
			p.reset();
		}
		else{
			for(int i=0;i<sname.length;i++){
					st=p.getState(sname[i]);
					st.reset();
			}
		}	
	}
}
	
	
	
	

  
	

	