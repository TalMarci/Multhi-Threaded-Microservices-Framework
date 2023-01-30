package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {

    private Attack[] attacks;
    private MessageBusImpl msgbus;//singleton
    private Future[] futureArray;//since Liea is the one sending Events for all Microservices, she will need to save all
                                 // futures for future tracking on finished missions
    /**
     * constructor. uses "Microservice"  constructor.
     * this constructor will be invoked only after all Microservices finished to initialize and used the countdownLatch functions.
     * @param attacks List of Attack objects we receive from input file,each of them will be sent as AttackEvents and executed by HanSolo and C3PO.
     */
    public LeiaMicroservice(Attack[] attacks) {
        super("Leia");
		this.attacks = attacks;
        msgbus=MessageBusImpl.getInstance();
        futureArray=new Future[attacks.length];
    }
    /**
     * initialize LeiaMicroservice with the behavior we want it to have.
     * we subscribe it to the relevant events and Broadcasts and defining the wanted callback functions(using Lambda Expressions) according the
     * required response for each message.
     * in this function we get the instance of the  Diary singleton and preforms actions on it via call backs we defined.
     */
    @Override
    protected void initialize() {
        Diary log = Diary.getInstance();
        subscribeBroadcast(TerminationBroadcast.class,(e)-> {
            log.setLeiaTerminate(System.currentTimeMillis());
            terminate();});//termination callback's logic.

        for(int i=0;i<attacks.length;i++)
        {
            futureArray[i]=sendEvent(new AttackEvent(attacks[i]));//sends all attackEvents we received from the input file.
        }

        for(int i=0;i<futureArray.length;i++) {
                futureArray[i].get();//making sure all attacks are finished
        }
        Future<Boolean> R2D2Result =this.sendEvent(new DeactivationEvent());//signals R2D2 to deactivate shields.
        R2D2Result.get();
        sendEvent(new BombDestroyerEvent());//ordeing Lando to destroy bomb.
    }
}
