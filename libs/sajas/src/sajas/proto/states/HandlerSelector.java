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

import sajas.core.Agent;
import sajas.core.behaviours.*;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

/**
 * Note: this class has been re-implemented to redirect the use of the agent and behaviour classes to SAJaS versions.
 * 
 * @see jade.proto.states.HandlerSelector
 * @author hlc
 *
 */
public abstract class HandlerSelector extends FSMBehaviour {
	private Map handlers = new HashMap();
        private Object accesKey;
	
        // FSM states names
	private static final String SELECT = "Select";
	private static final String HANDLE = "Handle";
	private static final String DUMMY = "Dummy";
	
	// States exit values
    /** Value returned by <code>onEnd</code> method if
     * an handler was found mapped to the key 
     **/
	public static final int SELECTION_OK = 1;
    /** Value returned by <code>onEnd</code> method if
     * no handler was found mapped to the key 
     **/
	public static final int SELECTION_NOK = 0;
	

    /**
     * Constructor for this HandlerSelector.
     * @param a is a reference to the Agent object
     * @param s is the DataStore where the object can be retrieved from
     * @param accessKey is the key to get the proper object from the DataStore,
     * this is the object that will be later passed as argument to the
     * method <code>getSelectionKey</code>
     * @see #getSelectionKey(Object)
     **/
	public HandlerSelector(Agent a, DataStore s, Object accessKey) {
		super(a);
		
		setDataStore(s);
		this.accesKey = accessKey;

		Behaviour b = null;
		// Create and register the states that make up the FSM
		// SELECT
		b = new OneShotBehaviour(myAgent) {
			int ret; 
			
			public void action() {
				ret = SELECTION_NOK;
				Object key = getSelectionKey(getDataStore().get(accesKey));
				if (key != null) {
					Behaviour b1 = (Behaviour) handlers.get(key);
					if (b1 != null) {
						// The HANDLE state is registered on the fly
						registerLastState(b1, HANDLE);
						ret = SELECTION_OK;
					}
				}
			}
			
			public int onEnd() {
				return ret;
			}
		};
		b.setDataStore(getDataStore());		
		registerFirstState(b, SELECT);
				
		// DUMMY
		b = new OneShotBehaviour(myAgent) {
			public void action() {}
		};
		registerLastState(b, DUMMY);
		
		// Register the FSM transitions
		registerTransition(SELECT, HANDLE, SELECTION_OK);
		registerDefaultTransition(SELECT, DUMMY);
	}
	
    /**
     * Subclasses must provide a concrete implementation for this method.
     * It must return the key to which the handler was bound.
     * @param selectionVar the object retrieved from the datastore at
     * the <code>accessKey</code> passed in the constructor 
     * @return the key to which the handler was bound
     **/
	protected abstract Object getSelectionKey(Object selectionVar);
	
    /**
     * Register the bounding between an handler and a key.
     * @param key this is the key that must be later returned by
     * the method <code>getSelectionKey</code> when the passed
     * Behaviour must be selected
     * @param h the handler for this key
     **/
	public void registerHandler(Object key, Behaviour h) {
		handlers.put(key, h);
	}
}
		
