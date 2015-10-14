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

package sajas.core;

/**
 * This new AID class, which extends the JADE version, provides a means to properly set and use a platform name.
 * 
 * @see jade.core.AID
 * @author hlc
 *
 */
public class AID extends jade.core.AID {
	
	private static final long serialVersionUID = 1L;

	private static String platformID = "SAJaS";
	
	/**
	 * @see jade.core.AID#AID()
	 */
	public AID() {
		super();
	}

	/**
	 * @see jade.core.AID#AID(String, boolean)
	 */
	public AID(String name, boolean isGUID) {
		super(name, isGUID);
	}

	/**
	 * @see jade.core.AID#getPlatformID(String)
	 */
	public static final String getPlatformID() {
		return platformID;
	}
	
	/**
	 * @see jade.core.AID#setPlatformID(String)
	 */
	public static final void setPlatformID(String id) {
		platformID = id;
	}

	/**
	 * Overridden to use a proper platform name.
	 * The problem is that jade.core.AID#setPlatformID(String) is not public.
	 * @see jade.core.AID#setLocalName(String)
	 */
	public void setLocalName(String n) {
		String hap = getPlatformID();
		if (hap == null) {
			throw new RuntimeException("Unknown Platform Name");
		}
		String name = n.trim(); 
		name = createGUID(name, hap);
		setName(name);
	}
	
}
