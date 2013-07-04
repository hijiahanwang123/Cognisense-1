package servicediscovery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import multicast.MulticastLayer;
import multicast.MulticastReceive;
import multicast.RecvMessageEvent;

public class ServiceDiscoveryLayer implements MulticastReceive {

	MulticastLayer multicastLayer = null;
	// the serviceid and the service objects
	Map<String, Service> services;

	Object appObject = null;
	boolean DEBUG = false;

	public ServiceDiscoveryLayer(boolean bool) {
		multicastLayer = new MulticastLayer();
		multicastLayer.DEBUG = bool;
		DEBUG = bool;
		System.out.println("Started the Multicast layer");
		multicastLayer.addEventListener(this);
		services = new HashMap<String, Service>(2);
	}

	/**
	 * this method will add a service to the service set of the SDL It will
	 * perform all the checks for a valid service that need to be done.
	 * 
	 * @param service
	 */
	public String registerNewService(String serviceType) {

		Service service = new Service();
		service.setServiceType(serviceType);
		String serviceId = getNewServiceId();
		service.setServiceid(serviceId);
		services.put(serviceId, service);
		return serviceId;

	}

	/**
	 * Gives a new service id everytime
	 * 
	 * @return
	 */
	public String getNewServiceId() {

		UUID a = UUID.randomUUID();
		String message = "" + a;
		return message;
	}

	/**
	 * this method generates a string from the message object and then passes it
	 * to the multicast layer reliable multicast layer.
	 * 
	 * @param message
	 */
	public void sendMessage(Message message) {

		String msg = message.generateMessage();

		if (msg != null)
			multicastLayer.sendAll(msg);
		else
			throw new IllegalArgumentException("Message is not valid");
	}

	public void registerApp(Object appObject) {
		this.appObject = appObject;
	}

	public void registerActions(String serviceID, String methodName, String actionName, 
			Class appClass) throws Exception {
		Service service = services.get(serviceID);
		Method appMethod = appClass.getMethod(methodName, Object.class,
				Object.class);
		// sets the name and Method of the action
		Action action = new Action(appMethod, actionName!=null?actionName:methodName);
		service.addAction(action);
	}

	public void registerTriggers(String serviceID, String methodName, String triggerName,
			Class appClass) throws Exception {
		Service service = services.get(serviceID);
		Method appMethod = appClass.getMethod(methodName, Object.class,
				Object.class);
		// sets the name and Method of the trigger
		Trigger trigger = new Trigger(appMethod, triggerName!=null?triggerName:methodName);
		service.addTrigger(trigger);
	}

	public void addProperty(String serviceId, Property property) {
		Service service = services.get(serviceId);
		if (property.name == null)
			property.name = property.getClass().getName();
		service.addProperties(property);
	}

	public void addLocation(String serviceId) {
		Service service = services.get(serviceId);
		Location location = new Location();
		location.addLocation("MyHome", "one", "Bedroom", "Top", "onDoor");
		service.addProperties(location);
	}

	public Map<String, Property> getProperties(String serviceId) {
		return services.get(serviceId).getProperties();
	}

	public List<String> getActions(String serviceId) {
		Service service = services.get(serviceId);
		List<String> list = new LinkedList<String>();

		if (service.getActions() != null) {
			for (Action action : service.getActions().values())
				list.add(action.getActionTag());
		}
		return list;
	}

	public List<String> getTriggers(String serviceId) {
		Service service = services.get(serviceId);
		List<String> list = new LinkedList<String>();

		if (service.getTrigger() != null) {
			for (Trigger trig : service.getTrigger().values())
				list.add(trig.getTriggerTag());
		}
		return list;
	}

	
	private void checkServiceIds(List<String> idList, Set<Service> set) {
		for (Service service : services.values()) {
			for (String incomingSId : idList) {
				if (service.getServiceid().equals(incomingSId)) {
					set.add(service);
				}
			}
		}
	}
	
	private void checkServiceTypes(List<String> typeList, Set<Service> set) {

		for (Service service : services.values()) {
			for (String type : typeList) {
				if (service.getServiceType().equals(type)) {
					set.add(service);
				}
			}
		}
	}
	
	private void performActions(Collection<Service>set,Message message ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		for (Service service : set) 
		{
				Action action = service.isActionPresent(message.getAction());
				if(action!=null)action.getMethod().invoke(appObject,message.getActionInput(),message.getSrcServiceID());
		}
	}
	
	private void performTriggers(Collection<Service>set,Message message ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		for (Service service : set) 
		{
				Trigger trigger = service.isTriggerPresent(message.getTriggerName());
				if(trigger!=null)trigger.getMethod().invoke(appObject,message.getTriggerData(),message.getSrcServiceID());
		}
	}

	
	/**
	 * this is a test method for the matching scheme for jiahan This method
	 * should print out the method names to be called and the services they
	 * should be called on Looking at the message object
	 * 
	 * @param message
	 */
	public void testOnReceiveMessage(Message message) {
		List<String> idList = message.getServiceIds();
		List<String> typeList = message.getServiceTypes();
		Set<Service> set = new HashSet<Service>(); 
		try {
			
			checkServiceIds(idList, set);
			checkServiceTypes(typeList, set);

			if (set.size()>0 && message.getProperties().size() == 0) 
				if(message.getAction()!=null)
					performActions(set, message);
				else
					performTriggers(set, message);
			
			else if (set.size()>0 && message.getProperties().size() >0)
			{
				Set<Service> list = new HashSet<Service>();
				for (Service service : set) 
				{
					for(String propertyName : message.getProperties())
					{
						if(service.isPropertyMatching(propertyName, message.getPropertyAttributes(propertyName)))
							list.add(service);
						else
							break;
						
					}
				}
				// not the set only contains services that have matching properties. 
				if(message.getAction()!=null)
					performActions(list, message);
				else
					performTriggers(list, message);
			}
			else if(set.size()==0)
				if(message.getAction()!=null)
					performActions(services.values(), message);
				else
					performTriggers(services.values(), message);
				
				
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	@Override
	public void onReceiveMessage(RecvMessageEvent e) {
		System.out.println("Message Received: " + e.getMessage());
		Message message = Message.parseMessage(e.getMessage());
		String actionName = message.getAction();
		String triggerName = message.getTriggerName();
		try {
			if (actionName != null) {
				for (Service service : services.values()) {
					for (Action action : service.getActions().values()) {
						// TODO this has to be generic as i do not know what
						// argument to give.
						// at the same time the service requires the message
						// object to get the
						// service src id and the action and the trigger data.
						action.getMethod().invoke(appObject,
								message.getActionInput(),
								message.getSrcServiceID());
					}
				}
			} else if (triggerName != null) {
				for (Service service : services.values()) {
					for (Trigger trigger : service.getTrigger().values()) {
						// TODO this has to be generic as i do not know what
						// argument to give.
						// at the same time the service requires the message
						// object to get the
						// service src id and the action and the trigger data.
						trigger.getMethod().invoke(appObject,
								message.getTriggerData(),
								message.getSrcServiceID());
					}
				}
			}
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public static void main(String args[]) {
		ServiceDiscoveryLayer sdl = new ServiceDiscoveryLayer(true);

	}

}
