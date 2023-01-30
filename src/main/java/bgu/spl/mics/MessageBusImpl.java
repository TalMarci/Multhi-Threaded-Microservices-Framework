package bgu.spl.mics;


import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 *
 *
 * @param E used only for syncronization purposes
 * @param B used only for syncronization purposes
 * @param msgQueue a Hashmap that holds seperate LinkedBlockingQueue for each Microservice. Those Queues hold the microservices's messages queue/
 * @param microQueue a Hashmap that holds seperate LinkedBlockingQueue for each Message Type. Those Queues are used to keep track of which microservice is registered to each message type.
 * @param resultsMap a Hashmap that holds the connection between an event and it's future object.
 */
public class MessageBusImpl implements MessageBus {
	private final Object E = new Object();//A and B are to objects being used only for synchronization purposes (SubscribeMessage and SubscribeEvent)
	private final Object B = new Object();
	private ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>> msgQueue;
	private ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<MicroService>> microQueue;
	private ConcurrentHashMap<Event,Future> resultsMap;

	/**
	 * Private Constructor.
	 */
	private MessageBusImpl()
	{
		msgQueue=new ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>>();
		microQueue=new ConcurrentHashMap<Class<? extends Message>,LinkedBlockingQueue<MicroService>>();
		resultsMap=new ConcurrentHashMap<Event,Future>();
	}

	/**
	 * This is a static class, used only for Singleton Purposes.
	 * Holds the single instance of the messageBus.
	 */
	private static class MessageBusHolder{
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	/**
	 * getInstance returns the single instance of the messageBus.
	 * We need it to be a singleton because we want our framework to work on the same Messagebus.
	 * @return returns an instance of MessageBus
	 */
	public static MessageBusImpl getInstance()
	{
		return MessageBusHolder.instance;
	}

	/**
	 * We created a SubscribeMessage function (that being called from subscribeEvent and subscribeEvent to avoid code duplications).
	 * Subscribes {@code m} to receive {@link Message}s of type {@code type}.
	 * There is no need to syncronize this function because the syncronization happens on the subscribeEvent and subscribeMessage functions if needed.
	 * <p>
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service.
	 */
	private <T> void subscribeMessage(Class<? extends Message> type, MicroService m)
	{
		LinkedBlockingQueue<MicroService> newQueue=microQueue.getOrDefault(type,null);
		if (newQueue == null) {
			newQueue = new LinkedBlockingQueue<MicroService>();
			newQueue.add(m);
			microQueue.put(type, newQueue);
		} else
			synchronized (microQueue.get(type))//we want to avoid a case where someone is being added to an event queue while we send an event of the same type (this might mess up the round robin logic)
			{newQueue.add(m);}
	}

	/**
	 * calls the SubscribeMessage with the same parameters.
	 * We are syncronizing the function only where we are given a type of event that does not exits on the Hashmap (to avoid creating more then one queue for this type by mistake)
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service.
	 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m)
	{
		if(!microQueue.containsKey(type))
		synchronized (E) {//in case of two threads trying to subscribe as the first subscribers to the same Event, we need to subscribe so they wouldn't create and insert two queues.
			subscribeMessage(type, m);
		}
		else//The queue already exists so we do not need to worry about subscribeMessage creating another queue for the same type of Message
			subscribeMessage(type, m);
	}

	/**
	 * calls the SubscribeMessage with the same parameters.
	 * We are syncronizing the function only where we are given a type of event that does not exits on the Hashmap (to avoid creating more then one queue for this type by mistake)
	 * @param type 	The type to subscribe to.
	 * @param m    	The subscribing micro-service.
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m)
	{
		if(!microQueue.containsKey(type))
		synchronized (B) {//This part must be syncronized, because otherwise the subscribeMessage might create more then one Queue for this type of event if two threads access it simuliniously.
			subscribeMessage(type, m);
		}
		else//The queue already exists so we do not need to worry about subscribeMessage creating another queue for the same type of Message
			subscribeMessage(type,m);
	}

	/**
	 * Notifies the MessageBus that the event {@code e} is completed and its
	 * result was {@code result}.
	 * When this method is called, the message-bus will resolve the {@link Future}
	 * object associated with {@link Event} {@code e}.
	 * <p>
	 * @param e      The completed event.
	 * @param result The resolved result of the completed event.
	 */
	@Override @SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		Future<T> f=resultsMap.remove(e);//Obtain the matching Future object from the FutureMap we keep. (we do not need to hold the pointer to this Future later on so we remove the key from the map).
										//Also there is no need to synchronize this because we are using concorrentHashMap so two remove calls could not happen simultaneously.
		f.resolve(result);
	}

	/**
	 * Adds the {@link Broadcast} {@code b} to the message queues of all the
	 * micro-services subscribed to {@code b.getClass()}.
	 * <p>
	 * @param b 	The message to added to the queues.
	 */
	@Override
	public void sendBroadcast(Broadcast b)
	{
		LinkedBlockingQueue<MicroService> broadcastDistribute = microQueue.get(b.getClass());
		if(broadcastDistribute!=null)
			synchronized (B)//Syncronizing this part because we want to make sure that people do not register to a broadcast type while we send the Broadcast to the currently registered memmbers.
			{
				broadcastDistribute.forEach((m) -> { msgQueue.get(m).add(b); });
			}
	}

	/**
	 * Adds the {@link Event} {@code e} to the message queue of one of the
	 * micro-services subscribed to {@code e.getClass()} in a round-robin
	 * fashion.
	 * <p>
	 * @param e     	The event to add to the queue.
	 * @return {@link Future<T>} object to be resolved once the processing is complete,
	 * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
			Future<T> future = new Future<>();
			resultsMap.put(e, future);

			LinkedBlockingQueue<MicroService> nextMicroQ = microQueue.get(e.getClass());//nextMicroQ holds the queue that holds the microservices registered to this Event type.
			if(nextMicroQ!=null){//if its null, we do not send the event to anyone, and return null.
					synchronized (nextMicroQ) {//We syncronize on this object because we want to avoid messing up the round robin logic.
						                       // this could happend when two different MicroServices send the same event or when a Microservice tries to subscribe while we are using the queue.
						MicroService micro = nextMicroQ.poll();//Pull the microservice on the top of the queue
						if(micro!=null) {
							LinkedBlockingQueue<Message> queue = msgQueue.get(micro);//obtain the match messageQueue for the microservice we are sending the event to.
							queue.add(e);//add the event
							nextMicroQ.add(micro);//return the microservice to the end of the queue to keep the Round Robin logic.
							return future;
						}
					}
			}
		return null;
	}

	/**
	 * Allocates a message-queue for the {@link MicroService} {@code m}.
	 * @param m the micro-service to create a queue for.
	 */
	@Override
	public void register(MicroService m) {
		msgQueue.put(m,new LinkedBlockingQueue<>());
	}

	/**
	 * Removes the message queue allocated to {@code m} via the call to
	 * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
	 * related to {@code m} in this message-bus. If {@code m} was not
	 * registered, nothing should happen.
	 * <p>
	 * @param m the micro-service to unregister.
	 */
	@Override
	public void unregister(MicroService m) {
		microQueue.forEach((key,value)->{
			LinkedBlockingQueue<MicroService> nextMicroQ = microQueue.get(key);
			synchronized (nextMicroQ){//We need to syncronize the Queue because if two microservices try to unregister at the same time, it might mess up the remove function of the queue.
			value.remove(m);}});
		msgQueue.remove(m);
	}

	/**
	 * Using this method, a <b>registered</b> micro-service can take message
	 * from its allocated queue.
	 * This method is blocking meaning that if no messages
	 * are available in the micro-service queue it
	 * should wait until a message becomes available.
	 * The method should throw the {@link IllegalStateException} in the case
	 * where {@code m} was never registered.
	 * <p>
	 * @param m The micro-service requesting to take a message from its message
	 *          queue.
	 * @return The next message in the {@code m}'s queue (blocking).
	 * @throws InterruptedException if interrupted while waiting for a message
	 *                              to became available.
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if(!msgQueue.containsKey(m))
			throw new IllegalStateException();
		return msgQueue.get(m).take();//Take is a blocking function, if the queue is empty it waits until something is being inserted (because we are using LinkedBlockingQueue).
	}
}
