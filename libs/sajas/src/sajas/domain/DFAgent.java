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

import jade.content.abs.AbsAgentAction;
import jade.content.abs.AbsIRE;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.lang.sl.SLVocabulary;
import jade.content.onto.BasicOntology;
import jade.domain.DFMemKB;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.DFGUIManagement.DFAppletOntology;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.Search;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

import java.util.HashMap;
import java.util.Map;

import sajas.core.Agent;
import sajas.proto.SubscriptionResponder;
import sajas.proto.SubscriptionResponder.Subscription;
import sajas.proto.SubscriptionResponder.SubscriptionManager;

/**
 * SAJaS implementation of the DF agent.
 * Subscriptions are supported.
 * For now, we assume that register, deregister and search operations are handled using DFService static methods
 * (no support for protocol-based operations is included yet).
 * 
 * @see jade.domain.df
 * @author hlc
 *
 */
public class DFAgent extends Agent {
	
	private Codec codec = new SLCodec();

	private DFSubscManager subManager;
	
	@Override
	public void setup() {
		// set default DF AID
		DFService.setDFAID(getAID());
		
		// register languages and ontologies
		getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);	
		getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL1);	
		getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL2);	
		getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);	
		getContentManager().registerOntology(FIPAManagementOntology.getInstance());
		getContentManager().registerOntology(JADEManagementOntology.getInstance());
		getContentManager().registerOntology(DFAppletOntology.getInstance());
		
		// subscription responder behaviour
		subManager = new DFSubscManager(this);
		addBehaviour(new SubscriptionResponder(this,
				MessageTemplate.and(SubscriptionResponder.createMessageTemplate(ACLMessage.SUBSCRIBE),
						MessageTemplate.MatchOntology(FIPAManagementOntology.getInstance().getName())),
						subManager));
	}
	
	/**
	 * Proxy for DFSubscManager#handleChange(DFAgentDescription, DFAgentDescription)
	 * @param dfd
	 * @param oldDfd
	 */
	void handleChange(DFAgentDescription dfd, DFAgentDescription oldDfd) {
		subManager.handleChange(dfd, oldDfd);
	}
	
	
	/**
	 * @see jade.domain.KBSubscriptionManager
	 * @author hlc
	 *
	 */
	private static class DFSubscManager implements SubscriptionManager {
		
		private Map<String, SubscriptionInfo> subscriptions = new HashMap<String, SubscriptionInfo>();

		private Agent myAgent;
		
		public DFSubscManager(Agent a) {
			myAgent = a;
		}
		
		/**
		 * @see jade.domain.KBSubscriptionManager#register(jade.proto.SubscriptionResponder.Subscription)
		 */
		public boolean register(Subscription sub) throws RefuseException, NotUnderstoodException{
			
			DFAgentDescription dfdTemplate = null;
			SearchConstraints constraints = null;
			AbsIRE absIota = null;
			
			try {
				// Get DFD template and search constraints from the subscription message 
				ACLMessage subMessage = sub.getMessage();
				
				absIota = (AbsIRE) myAgent.getContentManager().extractAbsContent(subMessage);
				AbsPredicate absResult = absIota.getProposition();
				AbsAgentAction absAction = (AbsAgentAction) absResult.getAbsObject(BasicOntology.RESULT_ACTION);
				AbsAgentAction absSearch = (AbsAgentAction) absAction.getAbsObject(BasicOntology.ACTION_ACTION);
				Search search = (Search) FIPAManagementOntology.getInstance().toObject(absSearch);
				
				dfdTemplate = (DFAgentDescription) search.getDescription();
				constraints = search.getConstraints();
				
				subscriptions.put(subMessage.getConversationId(), new SubscriptionInfo(sub, dfdTemplate, absIota));
			}
			catch(Exception e) {
				throw new NotUnderstoodException(e.getMessage());
			}
			
			DFAgentDescription[] results;
			try {
				results = DFService.search(myAgent, dfdTemplate, constraints);
				// If some DFD matches the template, notify the subscribed agent 
				if(results.length > 0) {
					List resultsList = new ArrayList();
					for(DFAgentDescription dfd : results) {
						resultsList.add(dfd);
					}
					notify(sub, resultsList, absIota);
					return true;
				}
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;
		}

		/**
		 * @see jade.domain.KBSubscriptionManager#deregister(jade.proto.SubscriptionResponder.Subscription)
		 */
		public boolean deregister(Subscription sub ) throws FailureException {
			subscriptions.remove(sub.getMessage().getConversationId());
			return false;
		}
		
		/**
		 * @see jade.domain.KBSubscriptionManager#handleChange(jade.domain.FIPAAgentManagement.DFAgentDescription, jade.domain.FIPAAgentManagement.DFAgentDescription)
		 */
		void handleChange(DFAgentDescription dfd, DFAgentDescription oldDfd) {
			for(SubscriptionInfo info : subscriptions.values()) {
				DFAgentDescription template = info.getTemplate();
				if(DFMemKB.compare(template, dfd) || ((oldDfd != null) && DFMemKB.compare(template, oldDfd))) {
					// This subscriber must be notified
					List results = new ArrayList();
					results.add(dfd);
					notify(info.getSubscription(), results, info.getAbsIota());
				}
			}
		}

		/**
		 * @see jade.domain.KBSubscriptionManager#notify(jade.proto.SubscriptionResponder.Subscription, jade.domain.FIPAAgentManagement.DFAgentDescription, jade.content.abs.AbsIRE)
		 */
		private void notify(SubscriptionResponder.Subscription sub, List results, AbsIRE absIota) {
			try {
				ACLMessage notification = sub.getMessage().createReply();
				notification.addUserDefinedParameter(ACLMessage.IGNORE_FAILURE, "true");
				notification.setPerformative(ACLMessage.INFORM);
				AbsPredicate absEquals = new AbsPredicate(SLVocabulary.EQUALS);
				absEquals.set(SLVocabulary.EQUALS_LEFT, absIota);
				absEquals.set(SLVocabulary.EQUALS_RIGHT, FIPAManagementOntology.getInstance().fromObject(results));
				
				myAgent.getContentManager().fillContent(notification, absEquals);
				sub.notify(notification);
			}
			catch (Exception e) {
				e.printStackTrace();
				//FIXME: Check whether a FAILURE message should be sent back.       
			}
		}
		

		/**
		 * @see jade.domain.KBSubscriptionManager.SubscriptionInfo
		 * @author hlc
		 *
		 */
		private class SubscriptionInfo {
			private Subscription subscription;
			private DFAgentDescription template;
			private AbsIRE absIota;
			
			private SubscriptionInfo(Subscription subscription, DFAgentDescription template, AbsIRE absIota) {
				this.subscription = subscription;
				this.template = template;
				this.absIota = absIota;
			}
			
			public SubscriptionResponder.Subscription getSubscription() {
				return subscription;
			}
			
			public DFAgentDescription getTemplate() {
				return template;
			}

			public AbsIRE getAbsIota() {
				return absIota;
			}

		}

	}


}
