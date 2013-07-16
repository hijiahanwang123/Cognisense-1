/**
 * A service has 2 things. Actions it can perform and triggers it can generate.
 * Actions behavior is specified and known to all other services. 
 * While what the service will do on getting a trigger is totally up to the implementation o
 * of the service. The configuration services listen on triggers that other service generate. 
 * They tell other service to perform actions. The behavior of these actions that it tells
 * other service to perform is specified before hand and know to everybody. 
 * Trigger generated is the list of triggers that are generated by the service. 
 * Other services who care about these trigger can register listeners on these triggers. 
 * which is stored in  Set<Trigger> triggers. 
 */

package com.servicediscovery;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Service {
		private String serviceid;
		private String serviceType;
		private Map<String,Action> actions;
		private Map<String,Trigger> triggers;
		private List<String> triggersGenerated;
		// property name and the property object
		private Map<String,Property> properties;
		private boolean DEBUG = true;
		
		
		public Service() {
			actions = new HashMap<String,Action>();
			triggers = new HashMap<String,Trigger>();
			triggersGenerated = new LinkedList<String>();
			setProperties(new HashMap<String, Property>());
		}
		
		public String getServiceid() {
			return serviceid;
		}
		public void setServiceid(String serviceid) {
			this.serviceid = serviceid;
		}
		public String getServiceType() {
			return serviceType;
		}
		public void setServiceType(String serviceType) {
			this.serviceType = serviceType;
		}
		public Map<String,Action> getActions() {
			return actions;
		}
		
		public Map<String,Trigger> getTriggers() {
			return triggers;
		}

		public Action getAction(String actionName) {
			return actions.get(actionName);
		}
		
		public Trigger getTrigger(String triggerName) {
			return triggers.get(triggerName);
		}
		
		public void addAction(Action action) {
			actions.put(action.getActionTag(),action);
		}

		public Action isActionPresent(String actionTag) {
			return actions.get(actionTag);
		}
		
		public Map<String,Property> getProperties() {
			return properties;
		}

		public void setProperties(Map<String,Property> properties) {
			this.properties = properties;
		}
		
		public void addProperties(Property property) {
			properties.put(property.getName(), property);
		}
		
		public void addTrigger(Trigger trigger) {
			triggers.put(trigger.getTriggerTag(),trigger);
		}
		
		public void addTriggerGenerated(String trigger) {
			triggersGenerated.add(trigger);
		}

		public List<String> getTriggerGenerated() {
			return triggersGenerated;
		}
		
		public boolean isPropertyMatching(String propertyName, Map<String, String> propertyAttributes) {
			
			if(properties.get(propertyName) != null)
			{
				if(properties.get(propertyName).match(propertyAttributes))
					return true;
				else
					return false;
			}
			return false;
		}

		public Trigger isTriggerPresent(String triggerName) {
			return triggers.get(triggerName);
		}

		public Property getProperty(String propertyName) {
			return properties.get(propertyName);
		}
		
		public void setProperty(String propertyName, Property property) {
			properties.put(propertyName, property);
		}
		
		public static void main(String[] args) throws SecurityException, NoSuchMethodException {
			Service service = new Service();
			service.setServiceType("Speaker");
			String serviceId = "12-323-42343-343-232";
			service.setServiceid(serviceId);
			Method appMethod = Service.class.getMethod("isTriggerPresent", String.class);
			Action action = new Action(appMethod, "isTriggerPresent");
			service.addAction(action);
			Location location = new Location();
			service.addProperties(location);
			
			
		}		
	}