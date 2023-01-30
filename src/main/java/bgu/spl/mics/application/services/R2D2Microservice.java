package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.concurrent.CountDownLatch;

/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class R2D2Microservice extends MicroService {

    private long duration;
    private MessageBusImpl msgbus;
    private CountDownLatch latch;

    /**
     * constructor. uses Microservice's  constructor.
     * @param duration the amount of time R2d@ will need to sleep(this is how we simulate the shields deactivation).
     */
    public R2D2Microservice(long duration) {
        super("R2D2");
        this.duration=duration;
        msgbus=MessageBusImpl.getInstance();
        latch=null;//we will update it later using a setter.
    }

    /**
     * set and (actually, initialize since it's null) the latch field.
     * @param latch we use latch in all Microservices so we can let every Microservice subscribe before lea starts to send events.
     */
    public void setLatch(CountDownLatch latch){
        this.latch=latch;
    }

    /**
     * initialize R2D2Microservice with the behavior we want it to have.
     * we subscribe it to the relevant events and Broadcasts and defining the wanted callback functions(using Lambda Expressions) according the
     * required response for each message.
     * in this function we get the instance of the Diary singleton and preforms actions on it via callbacks we defined.
     */
    @Override
    protected void initialize() {
        Diary log = Diary.getInstance();

        subscribeEvent(DeactivationEvent.class,(c)->{
            Thread.sleep(duration);
            log.setR2D2Deactivate(System.currentTimeMillis());
            this.complete(c,true);//letting the messageBus know he can resolve the event's future Object.
        });//subscribe and define the wanted response callback the microservice needs to preform.

        subscribeBroadcast(TerminationBroadcast.class,(c)-> {
            log.setR2D2Terminate(System.currentTimeMillis());
            terminate();}); //termination callback logic's

        latch.countDown();
    }
}
