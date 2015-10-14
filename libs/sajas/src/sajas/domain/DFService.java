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

package sajas.domain;

import jade.content.lang.sl.SL0Vocabulary;
import jade.content.lang.sl.SimpleSLTokenizer;
import jade.core.AID;
import jade.domain.DFMemKB;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FIPAManagementVocabulary;
import jade.domain.FIPAAgentManagement.MissingParameter;
import jade.domain.FIPAAgentManagement.MultiValueProperty;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ISO8601;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

import java.util.Date;
import java.util.Iterator;

import sajas.core.Agent;

/**
 * The DFService is re-implemented to avoid blocking approaches, given SAJaS single thread nature.
 * Public static methods to register, deregister and search are still available, but implemented as database operations,
 * instead of requiring a FIPA communication with the DF agent.
 * 
 * @see jade.domain.DFService
 * @author joaolopes, hlc
 *
 */
public class DFService extends FIPAService {

	private static AID dfAID;
	
	private static java.util.List<DFAgentDescription> registeredDfds = new java.util.ArrayList<DFAgentDescription>();
	
	// sets the DF AID (used from DFAgent to set the default DF)
	protected static void setDFAID(AID aid) {
		dfAID = aid;
	}
	
	// gets the DF AID (used from Agent to get the default DF)
	public static AID getDFAID() {
		return dfAID;
	}
	
	/**
	 * Initializes the DF service.
	 * Programmers should have no need to use this method.
	 * @exclude
	 */
	public static void initialize() {
		registeredDfds = new java.util.ArrayList<DFAgentDescription>();
	}

	/**
	 * @see jade.domain.DFService#register(jade.core.Agent, jade.domain.FIPAAgentManagement.DFAgentDescription)
	 */
	public static DFAgentDescription register(Agent agent, DFAgentDescription dfd) throws FIPAException {
		if (dfd == null) {
			dfd = new DFAgentDescription();
		}
		if (dfd.getName() == null) {
			dfd.setName(agent.getAID());
		}
		checkIsValid(dfd, true); // throws FIPAException

		if(dfd.getName().equals(agent.getAID())) {
			registeredDfds.add(dfd);
		}
		
		// for subscriptions
		((DFAgent) AMSService.getAgent(dfAID)).handleChange(dfd, null);
		
		return dfd;
	}

	/**
	 * @see jade.domain.DFService#deregister(jade.core.Agent, jade.domain.FIPAAgentManagement.DFAgentDescription)
	 */
	public static void deregister(Agent agent, DFAgentDescription dfd) throws FIPAException {
		if (dfd == null) {
			dfd = new DFAgentDescription();
		}
		if (dfd.getName() == null) {
			dfd.setName(agent.getAID());
		}
		
		if(!dfd.getName().equals(agent.getAID())) {
			return;
		}

		java.util.List<DFAgentDescription> dfAgentDescriptions = new java.util.ArrayList<DFAgentDescription>(registeredDfds);   // avoiding java.util.ConcurrentModificationException
		for(DFAgentDescription regDfd : dfAgentDescriptions) {
//			if(dfdmatch(regDfd, dfd)) {
			if(DFMemKB.compare(dfd, regDfd)) {
				registeredDfds.remove(regDfd);
				
				// for subscriptions
				dfd.clearAllServices(); //clear all services since we are deregistering
				((DFAgent) AMSService.getAgent(dfAID)).handleChange(dfd, regDfd);
			}
		}
	}

	/**
	 * @see jade.domain.DFService#deregister(jade.core.Agent)
	 */
	public static void deregister(Agent agent) throws FIPAException {
		deregister(agent, null);
	}

	/**
	 * @see jade.domain.DFService#search(jade.core.Agent, jade.domain.FIPAAgentManagement.DFAgentDescription)
	 */
	public static DFAgentDescription[] search(Agent agent, DFAgentDescription dfd) throws FIPAException {
		return search(agent, dfd, null);
	}

	/**
	 * @see jade.domain.DFService#search(jade.core.Agent, jade.domain.FIPAAgentManagement.DFAgentDescription, jade.domain.FIPAAgentManagement.SearchConstraints)
	 */
	public static DFAgentDescription[] search(Agent agent, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
		if (dfd == null) {
			dfd = new DFAgentDescription();
		}
		
		long max;
		if(constraints == null || constraints.getMaxResults() == null || constraints.getMaxResults() < 0) {
			max = (long) registeredDfds.size();
		} else {
			max = constraints.getMaxResults();
		}
		
		java.util.List<DFAgentDescription> results = new java.util.ArrayList<DFAgentDescription>();
		for(int i=0; i<registeredDfds.size() && results.size() < max; i++) {
//			if(dfdmatch(registeredDfds.get(i), dfd)) {
			if(DFMemKB.compare(dfd, registeredDfds.get(i))) {
				results.add(registeredDfds.get(i));
			}
		}
		
		// build results array
		DFAgentDescription[] resultsArray = new DFAgentDescription[results.size()];
		for(int i=0; i<results.size(); i++) {
			resultsArray[i] = results.get(i);
		}
		
		return resultsArray;
	}
	
	/**
	 * Re-implemented here because jade.domain.DFService#checkIsValid(jade.domain.FIPAAgentManagement.DFAgentDescription, boolean) is not declared as public.
	 * @see jade.domain.DFService#checkIsValid(jade.domain.FIPAAgentManagement.DFAgentDescription, boolean)
	 */
	private static void checkIsValid(DFAgentDescription dfd, boolean checkServices) throws MissingParameter {
		try {
			if (dfd.getName().getLocalName().length() == 0) {
				throw new MissingParameter(FIPAManagementVocabulary.DFAGENTDESCRIPTION, FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME);
			}
		}
		catch (NullPointerException npe) {
			throw new MissingParameter(FIPAManagementVocabulary.DFAGENTDESCRIPTION, FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME);
		}

		if(checkServices) {
			// search for null services (name and type are mandatory)
			ServiceDescription sd;
			for(Iterator<ServiceDescription> servicesIt = dfd.getAllServices(); servicesIt.hasNext();) {
				sd = servicesIt.next();
				if (sd.getName() == null) {
					throw new MissingParameter(FIPAManagementVocabulary.SERVICEDESCRIPTION, FIPAManagementVocabulary.SERVICEDESCRIPTION_NAME);
				}
				if (sd.getType() == null) {
					throw new MissingParameter(FIPAManagementVocabulary.SERVICEDESCRIPTION, FIPAManagementVocabulary.SERVICEDESCRIPTION_TYPE);
				}
			}
		}
	}

	/**
	 * Matches a DFAgentDescription against a template.
	 * @param dfd
	 * @param template
	 * @return
	 */
	private static boolean dfdmatch(DFAgentDescription dfd, DFAgentDescription template) {
		
		// AID name
		if(template.getName() != null && !dfd.getName().equals(template.getName())) {
			return false;
		}
		
		// List services
		if(!servicesMatch(dfd.getAllServices(), template.getAllServices())) {
			return false;
		}
		
		// List interactionProtocols
		if(!match(dfd.getAllProtocols(), template.getAllProtocols())) {
			return false;
		}
		
		// List ontology
		if(!match(dfd.getAllOntologies(), template.getAllOntologies())) {
			return false;
		}
		
		// List language
		if(!match(dfd.getAllLanguages(), template.getAllLanguages())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Matches a set of ServiceDescription against a template.
	 * For each ServiceDescription in the template, there must be a matching ServiceDescription in the set.
	 * @param it
	 * @param template
	 * @return
	 */
	private static boolean servicesMatch(Iterator<ServiceDescription> it, Iterator<ServiceDescription> template) {
		java.util.List<Object> targetSDs = iterator2list(it);
		
		ServiceDescription templateSD;
		while(template.hasNext()) {
			// pick next service description template
			templateSD = template.next();
			boolean found = false;
			// search for suitable target service description
			for(int i=0; i<targetSDs.size() && !found; i++) {
				ServiceDescription sd = (ServiceDescription) targetSDs.get(i);
				
				// String name
				if(templateSD.getName() != null && !sd.getName().equals(templateSD.getName())) {
					continue;
				}
				
				// String type
				if(templateSD.getType() != null && !sd.getType().equals(templateSD.getType())) {
					continue;
				}
				
				// String ownership
				if(templateSD.getOwnership() != null && !sd.getOwnership().equals(templateSD.getOwnership())) {
					continue;
				}
				
				// List interactionProtocols
				if(!match(sd.getAllProtocols(), templateSD.getAllProtocols())) {
					continue;
				}
				
				// List ontology
				if(!match(sd.getAllOntologies(), templateSD.getAllOntologies())) {
					continue;
				}
				
				// List language
				if(!match(sd.getAllLanguages(), templateSD.getAllLanguages())) {
					continue;
				}
				
				// List properties
				Iterator templatePropertiesIt = templateSD.getAllProperties();
				boolean propertiesMatch = true;
				while(templatePropertiesIt.hasNext() && propertiesMatch) {
					// pick next property
					Property templateProperty = (Property) templatePropertiesIt.next();
					Iterator sdPropertiesIt = sd.getAllProperties();
					boolean foundProperty = false;
					// search for suitable target property
					while(sdPropertiesIt.hasNext() && !foundProperty) {
						if(((Property) sdPropertiesIt.next()).match(templateProperty)) {
							foundProperty = true;
						}
					}
					if(!foundProperty) {
						// no match for this property template in this ServiceDescription
						propertiesMatch = false;
					}
				}
				if(!propertiesMatch) {
					continue;
				}
				
				// passed all tests -- found matching ServiceDescription for current template ServiceDescription
				found = true;
			}
			if(!found) {
				// no match for this service description template
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Matches a set of objects against a template.
	 * The set of objects must contain all objects in the template.
	 * @param it
	 * @param template
	 * @return
	 */
	private static boolean match(Iterator<Object> it, Iterator<Object> template) {
		return iterator2list(it).containsAll(iterator2list(template));
	}
	
	/**
	 * Converts an iterator to a list.
	 * @param it
	 * @return
	 */
	private static java.util.List<Object> iterator2list(Iterator<?> it) {
		java.util.List<Object> theList = new java.util.ArrayList<Object>();
		while(it.hasNext()) {
			theList.add(it.next());
		}
		return theList;
	}

	
	//**********************
	// The three createXXX static methods have been re-implemented here because we need to rely on the
	// SAJaS version of Agent.
	//**********************
	
	/**
	 * @see jade.domain.DFService#createRequestMessage(jade.core.Agent, jade.core.AID, String, jade.domain.FIPAAgentManagement.DFAgentDescription, jade.domain.FIPAAgentManagement.SearchConstraints)
	 */
	public static ACLMessage createRequestMessage(Agent a, AID dfName, String action, DFAgentDescription dfd, SearchConstraints constraints) {
		ACLMessage request = createRequestMessage(a, dfName);
		request.setContent(encodeAction(dfName, action, dfd, constraints));
		return request;
	}

	/**
	 * @see jade.domain.DFService#createSubscriptionMessage(jade.core.Agent, jade.core.AID, jade.domain.FIPAAgentManagement.DFAgentDescription, jade.domain.FIPAAgentManagement.SearchConstraints)
	 */
	public static ACLMessage createSubscriptionMessage(Agent a, AID dfName, DFAgentDescription template, SearchConstraints constraints) {
		ACLMessage subscribe = createRequestMessage(a, dfName);
		subscribe.setPerformative(ACLMessage.SUBSCRIBE);
		subscribe.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		// Note that iota is not included in SL0
		subscribe.setLanguage(FIPANames.ContentLanguage.FIPA_SL);	
		subscribe.setContent(encodeIota(dfName, template, constraints));
		return subscribe;
	}

	/**
	 * @see jade.domain.DFService#createCancelMessage(jade.core.Agent, jade.core.AID, jade.lang.acl.ACLMessage)
	 */
	public static ACLMessage createCancelMessage(Agent a, AID dfName, ACLMessage subscribe) {
		ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);
		cancel.addReceiver(dfName);
		cancel.setLanguage(subscribe.getLanguage());
		cancel.setOntology(subscribe.getOntology());
		cancel.setProtocol(subscribe.getProtocol());
		cancel.setConversationId(subscribe.getConversationId());
		cancel.setContent(encodeCancel(dfName, subscribe));
		return cancel;
	}

	
	//**********************
	// Decoding and encoding static methods were copied from jade.domain.DFService to here because
	// they are not declared as public.
	//**********************

	private static Long MINUSONE = new Long(-1);
	private static final String SPACE_COLON = " :";
	private static final String SPACE_BRACKET = " (";

	///////////////////////////////////
	// Decoding methods
	///////////////////////////////////
	
	/**
	 Process the content of the final <code>inform (Done)</code> message
	 resulting from a <code>register</code> or <code>deregister</code>
	 action requested to a DF agent, extracting the
	 <code>df-agent-description</code> contained within.
	 @return The <code>DFAgentDescription</code> object included
	 in the "done" expression used as the content of the INFORM message
	 send back by the DF in response to a REQUEST to perform a register,
	 deregister or modify action.
	 @exception FIPAException If some error occurs while decoding
	 */
	public static DFAgentDescription decodeDone(String s) throws FIPAException {
		// S has the form: 
		// ((done (action (AID...) (register (df-agent-description ....) ) ) ) )
		// We skip until we find "df-agent-description" and start decoding from there.
		try {
			int start = s.indexOf(FIPAManagementVocabulary.DFAGENTDESCRIPTION);
			return parseDfd(new SimpleSLTokenizer(s.substring(start)));
		}
		catch (Exception e) {
			throw new FIPAException("Error decoding INFORM Done. "+e.getMessage());
		}
	}
	
	/**
	 Process the content of the final <code>inform (result)</code> message resulting
	 from a <code>search</code> action requested to a DF agent, extracting the array of
	 <code>df-agent-description</code> contained within.
	 @return The <code>DFAgentDescription</code> objects (as an array) included
	 in the "result" expression used as the content of the INFORM message
	 send back by the DF in response to a REQUEST to perform a search action.
	 @exception FIPAException If some error occurs while decoding
	 */
	public static DFAgentDescription[] decodeResult(String s) throws FIPAException {
		// S has the form: 
		// ((result (action...)  (sequence (DFD...) (DFD...)) ) )
		// We skip until we find "action", skip until the end of (action...) and start decoding from there.
		try {
			int start = s.indexOf(SL0Vocabulary.ACTION);
			start += countUntilEnclosing(s, start);
			return decodeDfdSequence(s.substring(start));
		}
		catch (Exception e) {
			throw new FIPAException("Error decoding INFORM Result. "+e.getMessage());
		}	  	
	}
	
	/**
	 Process the content of the <code>inform</code> message resulting
	 from a subscription with a DF agent, extracting the array of
	 <code>df-agent-description</code> objects contained within.
	 @return The <code>DFAgentDescription</code> objects (as an array) included
	 in the "(= (iota...) ...)" expression used as the content of an INFORM message
	 sent back by the DF as a subscription notification.
	 @exception FIPAException If some error occurs while decoding
	 */
	public static DFAgentDescription[] decodeNotification(String s) throws FIPAException {
		// S has the form:
		// ((= (iota...)  (sequence (DFD...) (DFD...)) ) )
		// We skip until we find "iota", skip until the end of (iota...) and start decoding from there.
		try {
			int start = s.indexOf("iota");
			start += countUntilEnclosing(s, start);
			return decodeDfdSequence(s.substring(start));
		}
		catch (Exception e) {
			throw new FIPAException("Error decoding INFORM Equals. "+e.getMessage());
		}
	}
	
	/**
	 The parser content has the form:
	 df-agent-description ......) <possibly something else>
	 */
	private static DFAgentDescription parseDfd(SimpleSLTokenizer parser) throws Exception {
		DFAgentDescription dfd = new DFAgentDescription();
		// Skip "df-agent-description"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME)) {
				parser.consumeChar('(');
				dfd.setName(parseAID(parser));
			}
			// Lease time
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_LEASE_TIME)) {
				dfd.setLeaseTime(ISO8601.toDate(parser.getElement()));
			}
			// Protocols
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_PROTOCOLS)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					dfd.addProtocols((String) it.next());
				}
			}
			// Languages
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_LANGUAGES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					dfd.addLanguages((String) it.next());
				}
			}
			// Ontologies
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_ONTOLOGIES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					dfd.addOntologies((String) it.next());
				}
			}
			// Services
			else if (slotName.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION_SERVICES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					dfd.addServices((ServiceDescription) it.next());
				}
			}
		}
		parser.consumeChar(')');
		return dfd;
	}
	
	
	/**
	 The parser content has the form:
	 service-description ......) <possibly something else>
	 */
	private static ServiceDescription parseServiceDescription(SimpleSLTokenizer parser) throws Exception {
		ServiceDescription sd = new ServiceDescription();
		// Skip "service-description"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_NAME)) {
				sd.setName(parser.getElement());
			}
			// Type
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_TYPE)) {
				sd.setType(parser.getElement());
			}
			// Ownership
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_OWNERSHIP)) {
				sd.setOwnership(parser.getElement());
			}
			// Protocols
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_PROTOCOLS)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					sd.addProtocols((String) it.next());
				}
			}
			// Languages
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_LANGUAGES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					sd.addLanguages((String) it.next());
				}
			}
			// Ontologies
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_ONTOLOGIES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					sd.addOntologies((String) it.next());
				}
			}
			// Properties
			else if (slotName.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION_PROPERTIES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					sd.addProperties((Property) it.next());
				}
			}
		}
		parser.consumeChar(')');
		return sd;
	}
	
	/**
	 The parser content has the form:
	 property ......) <possibly something else>
	 */
	private static Property parseProperty(SimpleSLTokenizer parser) throws Exception {
		Property p = new Property();
		// Skip "property"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(FIPAManagementVocabulary.PROPERTY_NAME)) {
				p.setName(parser.getElement());
			}
			// Value
			if (slotName.equals(FIPAManagementVocabulary.PROPERTY_VALUE)) {
				p.setValue(parser.getElement());
			}
		}
		parser.consumeChar(')');
		return p;
	}

	/**
	 The parser content has the form:
	 multi-value-property ......) <possibly something else>
	 */
	private static Property parseMultiValueProperty(SimpleSLTokenizer parser) throws Exception {
		MultiValueProperty mvp = new MultiValueProperty();
		// Skip "multi-value-property"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(FIPAManagementVocabulary.PROPERTY_NAME)) {
				mvp.setName(parser.getElement());
			}
			// Values
			if (slotName.equals(FIPAManagementVocabulary.PROPERTY_VALUE)) {
				mvp.setValues(parseAggregate(parser));	
			}
		}
		parser.consumeChar(')');
		return mvp;
	}
	
	/**
	 The parser content has the form:
	 agent-identifier ......) <possibly something else>
	 */
	public static AID parseAID(SimpleSLTokenizer parser) throws Exception {
		AID id = new AID("", AID.ISGUID); // Dummy temporary name
		// Skip "agent-identifier"
		parser.getElement();
		while (parser.nextToken().startsWith(":")) {
			String slotName = parser.getElement();
			// Name
			if (slotName.equals(SL0Vocabulary.AID_NAME)) {
				id.setName(parser.getElement());
			}
			// Addresses
			else if (slotName.equals(SL0Vocabulary.AID_ADDRESSES)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					id.addAddresses((String) it.next());
				}
			}
			// Resolvers
			else if (slotName.equals(SL0Vocabulary.AID_RESOLVERS)) {
				Iterator it = parseAggregate(parser).iterator();
				while (it.hasNext()) {
					id.addResolvers((AID) it.next());
				}
			}
		}
		parser.consumeChar(')');
		return id;
	}

	/**
	 The parser content has the form:
	 (sequence <val> <val> ......) <possibly something else>
	 or 
	 (set <val> <val> ......) <possibly something else>
	 */
	private static List parseAggregate(SimpleSLTokenizer parser) throws Exception {
		List l = new ArrayList();
		// Skip first (
		parser.consumeChar('(');
		// Skip "sequence" or "set" (no matter)
		parser.getElement();
		String next = parser.nextToken();
		while (!next.startsWith(")")) {
			if (!next.startsWith("(")) {
				l.add(parser.getElement());
			}
			else {
				parser.consumeChar('(');
				next = parser.nextToken();
				if (next.equals(FIPAManagementVocabulary.DFAGENTDESCRIPTION)) {
					l.add(parseDfd(parser));
				}
				if (next.equals(SL0Vocabulary.AID)) {
					l.add(parseAID(parser));
				}
				else if (next.equals(FIPAManagementVocabulary.SERVICEDESCRIPTION)) {
					l.add(parseServiceDescription(parser));
				}
				else if (next.equals(FIPAManagementVocabulary.MULTI_VALUE_PROPERTY)) {
					l.add(parseMultiValueProperty(parser));
				}
				else if (next.equals(FIPAManagementVocabulary.PROPERTY)) {
					l.add(parseProperty(parser));
				}
			}
			next = parser.nextToken();
		}
		parser.consumeChar(')');
		return l;
	}
	
	/**
	 S has the form:
	 (sequence (DFD...) (DFD...)) <possibly something else>
	 */
	private static DFAgentDescription[] decodeDfdSequence(String s) throws Exception {
		List l = parseAggregate(new SimpleSLTokenizer(s));
		// Convert the list into an array
		DFAgentDescription[] items = new DFAgentDescription[l.size()];
		for(int i = 0; i < l.size(); i++){
			items[i] = (DFAgentDescription)l.get(i);
		}
		return items;
	}
	
	/**
	 Start indicates the index of the first char after the open parenthesis
	 */
	private static int countUntilEnclosing(String s, int start) {
		int openCnt = 1;
		boolean skipMode = false;
		int cnt = start;
		while (openCnt > 0) {
			char c = s.charAt(cnt++);
			if (!skipMode) {
				if (c == '(') {
					openCnt++;
				}
				else if (c == ')') {
					openCnt--;
				}
				else if (c == '"') {
					skipMode = true;
				}
			}
			else {
				if (c == '\\' && s.charAt(cnt) == '\"') {
					cnt++;
				}
				else if (c == '"') {
					skipMode = false;
				}
			}
		}
		return cnt-start;
	}
	
	///////////////////////////////////
	// Encoding methods
	///////////////////////////////////
	
	/**
	 This is package scoped as it is used by DFUpdateBehaviour and 
	 DFSearchBehaviour.
	 */
	static String encodeAction(AID df, String actionName, DFAgentDescription dfd, SearchConstraints sc) {
		StringBuffer sb = new StringBuffer("((");
		sb.append(SL0Vocabulary.ACTION);
		sb.append(' ');
		sb.append(df.toString());
		sb.append(SPACE_BRACKET);
		sb.append(actionName);
		sb.append(' ');
		encodeDfd(sb, dfd);
		if (actionName.equals(FIPAManagementVocabulary.SEARCH) && sc == null) {
			sc = new SearchConstraints();
			sc.setMaxResults(MINUSONE);
		}
		if (sc != null) {
			sb.append(SPACE_BRACKET);
			sb.append(FIPAManagementVocabulary.SEARCHCONSTRAINTS);
			encodeField(sb, sc.getMaxResults(), FIPAManagementVocabulary.SEARCHCONSTRAINTS_MAX_RESULTS);
			encodeField(sb, sc.getMaxDepth(), FIPAManagementVocabulary.SEARCHCONSTRAINTS_MAX_DEPTH);
			encodeField(sb, sc.getSearchId(), FIPAManagementVocabulary.SEARCHCONSTRAINTS_SEARCH_ID);
			sb.append(')');
		}
		sb.append(")))"); // Close <actionName>, action and content
		return sb.toString();
	}
	
	/**
	 This is package scoped as it is used by DFSearchBehaviour 
	 */
	static String encodeIota(AID df, DFAgentDescription dfd, SearchConstraints sc) {
		StringBuffer sb = new StringBuffer("((iota ?x (");
		sb.append(SL0Vocabulary.RESULT);
		sb.append(' ');
		String tmp = encodeAction(df, FIPAManagementVocabulary.SEARCH, dfd, sc);
		sb.append(tmp.substring(1, tmp.length()-1));
		sb.append(" ?x)))"); // Close Result, iota and content
		return sb.toString();
	}
	
	/**
	 This is package scoped as it is used by DFSearchBehaviour 
	 */
	static String encodeCancel(AID df, ACLMessage msg) {
		StringBuffer sb = new StringBuffer("((");
		sb.append(SL0Vocabulary.ACTION);
		sb.append(' ');
		sb.append(df.toString());
		sb.append(SPACE_BRACKET);
		sb.append(ACLMessage.getPerformative(msg.getPerformative()));
		encodeField(sb, msg.getSender(), SL0Vocabulary.ACLMSG_SENDER);
		encodeAggregate(sb, msg.getAllReceiver(), SL0Vocabulary.SEQUENCE, SL0Vocabulary.ACLMSG_RECEIVERS);
		encodeField(sb, msg.getProtocol(), SL0Vocabulary.ACLMSG_PROTOCOL);
		encodeField(sb, msg.getLanguage(), SL0Vocabulary.ACLMSG_LANGUAGE);
		encodeField(sb, msg.getOntology(), SL0Vocabulary.ACLMSG_ONTOLOGY);
		encodeField(sb, msg.getReplyWith(), SL0Vocabulary.ACLMSG_REPLY_WITH);
		encodeField(sb, msg.getConversationId(), SL0Vocabulary.ACLMSG_CONVERSATION_ID);
		encodeField(sb, msg.getContent(), SL0Vocabulary.ACLMSG_CONTENT);
		sb.append(")))"); // Close msg, action and content
		return sb.toString();
	}
	
	private static void encodeDfd(StringBuffer sb, DFAgentDescription dfd) {
		sb.append('(');
		sb.append(FIPAManagementVocabulary.DFAGENTDESCRIPTION);
		encodeField(sb, dfd.getName(), FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME);
		encodeAggregate(sb, dfd.getAllProtocols(), SL0Vocabulary.SET, FIPAManagementVocabulary.DFAGENTDESCRIPTION_PROTOCOLS);
		encodeAggregate(sb, dfd.getAllLanguages(), SL0Vocabulary.SET, FIPAManagementVocabulary.DFAGENTDESCRIPTION_LANGUAGES);
		encodeAggregate(sb, dfd.getAllOntologies(), SL0Vocabulary.SET, FIPAManagementVocabulary.DFAGENTDESCRIPTION_ONTOLOGIES);
		encodeAggregate(sb, dfd.getAllServices(), SL0Vocabulary.SET, FIPAManagementVocabulary.DFAGENTDESCRIPTION_SERVICES);
		Date lease = dfd.getLeaseTime();
		if (lease != null) {
			sb.append(SPACE_COLON);
			sb.append(FIPAManagementVocabulary.DFAGENTDESCRIPTION_LEASE_TIME);
			sb.append(' ');
			sb.append(ISO8601.toString(lease));
		}
		sb.append(')');
	}
	
	private static void encodeServiceDescription(StringBuffer sb, ServiceDescription sd) {
		sb.append('(');
		sb.append(FIPAManagementVocabulary.SERVICEDESCRIPTION);
		encodeField(sb, sd.getName(), FIPAManagementVocabulary.SERVICEDESCRIPTION_NAME);
		encodeField(sb, sd.getType(), FIPAManagementVocabulary.SERVICEDESCRIPTION_TYPE);
		encodeField(sb, sd.getOwnership(), FIPAManagementVocabulary.SERVICEDESCRIPTION_OWNERSHIP);
		encodeAggregate(sb, sd.getAllProtocols(), SL0Vocabulary.SET, FIPAManagementVocabulary.SERVICEDESCRIPTION_PROTOCOLS);
		encodeAggregate(sb, sd.getAllLanguages(), SL0Vocabulary.SET, FIPAManagementVocabulary.SERVICEDESCRIPTION_LANGUAGES);
		encodeAggregate(sb, sd.getAllOntologies(), SL0Vocabulary.SET, FIPAManagementVocabulary.SERVICEDESCRIPTION_ONTOLOGIES);
		encodeAggregate(sb, sd.getAllProperties(), SL0Vocabulary.SET, FIPAManagementVocabulary.SERVICEDESCRIPTION_PROPERTIES);
		sb.append(')');
	}
	
	private static void encodeProperty(StringBuffer sb, Property p) {
		sb.append('(');
		sb.append(FIPAManagementVocabulary.PROPERTY);
		encodeField(sb, p.getName(), FIPAManagementVocabulary.PROPERTY_NAME);
		encodeField(sb, p.getValue(), FIPAManagementVocabulary.PROPERTY_VALUE);
		sb.append(')');
	}

	private static void encodeMultiValueProperty(StringBuffer sb, MultiValueProperty mvp) {
		sb.append('(');
		sb.append(FIPAManagementVocabulary.MULTI_VALUE_PROPERTY);
		encodeField(sb, mvp.getName(), FIPAManagementVocabulary.PROPERTY_NAME);
		encodeAggregate(sb, mvp.getValues().iterator(), SL0Vocabulary.SEQUENCE, FIPAManagementVocabulary.PROPERTY_VALUE);
		sb.append(')');
	}
	
	private static void encodeField(StringBuffer sb, Object val, String name) {
		if (val != null) {
			sb.append(SPACE_COLON);
			sb.append(name);
			sb.append(' ');
			if (val instanceof String) {
				encodeString(sb, (String) val);
			}
			else {
				sb.append(val);
			}
		}
	}
	
	private static void encodeAggregate(StringBuffer sb, Iterator agg, String aggType, String name) {
		if (agg != null && agg.hasNext()) {
			sb.append(SPACE_COLON);
			sb.append(name);
			sb.append(SPACE_BRACKET);
			sb.append(aggType);
			while (agg.hasNext()) {
				sb.append(' ');
				Object val = agg.next();
				if (val instanceof ServiceDescription) {
					encodeServiceDescription(sb, (ServiceDescription) val);
				}
				else if (val instanceof MultiValueProperty) {
					encodeMultiValueProperty(sb, (MultiValueProperty) val);
				}
				else if (val instanceof Property) {
					encodeProperty(sb, (Property) val);
				}
				else if (val instanceof String) {
					encodeString(sb, (String) val);
				}
				else {
					sb.append(val);
				}
			}
			sb.append(')');
		}
	}
	
	private static void encodeString(StringBuffer sb, String s) {
		if (SimpleSLTokenizer.isAWord(s)) {
			sb.append(s);
		}
		else {
			sb.append(SimpleSLTokenizer.quoteString(s));
		}
	}

}
