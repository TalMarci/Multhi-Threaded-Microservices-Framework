package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * C3POMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {

    private MessageBusImpl msgbus;
    private CountDownLatch latch;

    /**
     * constructor. uses Microservice's  constructor.
     */
    public C3POMicroservice() {
        super("C3PO");
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
     * initialize C3POMicroservice with the behavior we want it to have.
     * we subscribe it to the relevant events and Broadcasts and defining the wanted callback functions(using Lambda Expressions) according the
     * required response for each message.
     * in this function we get the instance of the Ewoks and Diary singletons and preforms actions on them via call backs we defined.
     */
    @Override
    protected void initialize() {
        Diary log = Diary.getInstance();

        subscribeEvent(AttackEvent.class,(c)->{
            Attack atkInfo=c.getAtk();//extract the attack Object.
            List<Integer> requiredEwoks = atkInfo.getSerials();//extract the specific attack details.
            Ewoks EwoksManager = Ewoks.getInstance();
            EwoksManager.Acquire(requiredEwoks);//acquire the relevant resources.
            Thread.sleep(atkInfo.getDuration());
            EwoksManager.Release(requiredEwoks);//releasing the resources used.
            log.setC3POFinish(System.currentTimeMillis());
            log.setTotalAttacks();
            complete(c,true);//letting the messageBus know he can resolve the event's future Object.
        });//subscribe and define the wanted response callback the microservice needs to preform.

        subscribeBroadcast(TerminationBroadcast.class,(c ->
        {
            log.setC3POTerminate(System.currentTimeMillis());
            terminate();
        }));//termination's callback logic.
        latch.countDown();
    }
}
