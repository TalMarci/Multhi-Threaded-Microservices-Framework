package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.concurrent.CountDownLatch;

/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice  extends MicroService {

    private long duration;
    private MessageBusImpl msgbus;
    private CountDownLatch latch;

    /**
     * constructor. uses Microservice's  constructor.
     * @param duration the amount of time Lando will need to sleep(this is how we simulate the Bomb destruction).
     */
    public LandoMicroservice(long duration) {
        super("Lando");
        this.duration=duration;
        msgbus=MessageBusImpl.getInstance();
        latch= null;//we will update it later using a setter.
    }

    /**
     * set and (actually, initialize since it's null) the latch field.
     * @param latch we use latch in all Microservices so we can let every Microservice subscribe before lea starts to send events.
     */
    public void setLatch(CountDownLatch latch){
        this.latch=latch;
    }


    /**
     * initialize LandoMicroservice with the behavior we want it to have.
     * we subscribe it to the relevant events and Broadcasts and defining the wanted callback functions(using Lambda Expressions) according the
     * required response for each message.
     * in this function we get the instance of the Diary singleton and preforms actions on it via call backs we defined.
     */
    @Override
    protected void initialize() {
        Diary log = Diary.getInstance();

        subscribeEvent(BombDestroyerEvent.class,(c)->{
            Thread.sleep(duration);
            sendBroadcast(new TerminationBroadcast());});//Lando finished the final attack and broadcast for every Microservice to terminate.

        subscribeBroadcast(TerminationBroadcast.class,(c)-> {
            log.setLandoTerminate(System.currentTimeMillis());
            terminate();});//termination callback logic.

        latch.countDown();
    }
}
