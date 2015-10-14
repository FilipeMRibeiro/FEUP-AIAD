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

package sajas.core.behaviours;

import sajas.core.Agent;

/**
 * @see jade.core.behaviours.SimpleBehaviour
 * @author hlc
 *
 */
public abstract class SimpleBehaviour extends Behaviour {

  /**
     Default constructor. It does not set the owner agent for this
     behaviour.
  */
  public SimpleBehaviour() {
    super();
  }

  /**
     This constructor sets the owner agent for this behaviour.
     @param a The agent this behaviour belongs to.
  */
  public SimpleBehaviour(Agent a) {
    super(a);
  }    

  /**
     Resets a <code>SimpleBehaviour</code>. 
  */
  public void reset() {
    super.reset();
  }

}
