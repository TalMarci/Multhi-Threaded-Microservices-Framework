package bgu.spl.mics;

import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.services.C3POMicroservice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {

    private MessageBus msgbus;
    private MicroService micro1;
    private MicroService micro2;
    @BeforeEach
    void setUp() {
         msgbus= MessageBusImpl.getInstance();
         micro1= new C3POMicroservice();
         micro2= new C3POMicroservice();
         msgbus.register(micro1);//This line tests the register function (if the register function does not works properly, the other tests would fail).
         msgbus.register(micro2);//This line tests the register function (if the register function does not works properly, the other tests would fail).
    }

    /*
     SubscribeEvent is being tested on SendEvent (Since we subscribe micro2 to the msgbus in order to implement it).
     */
    @Test
    void testSubscribeEvent() {
    }
    /*
     SubscribeEvent is being tested on SendEvent (Since we subscribe micro1 to the msgbus in order to implement it).
    */
    @Test
    void testSubscribeBroadcast() {
    }

    @Test
    void testComplete() throws InterruptedException {
        DeactivationEvent attack = new DeactivationEvent();
        msgbus.subscribeEvent(DeactivationEvent.class,micro1);//Subscribing micro1 to handle DeactivationEvent.
        Future<Boolean> result = micro2.sendEvent(attack);//Creating a future object that will hold the result of the event that micro2 sends (to micro1 since he is registered to handle those events).
        assertFalse(result.isDone());//we expect the event not to be resolved at this point
        micro1.complete(attack,true);//complete the event with the results held in the Future object.
        assertTrue(result.isDone());//we expect the event to be resolved at this point
        assertNotEquals(null,result.get());//we expect the result not to be null.
    }

    @Test
    void testSendBroadcast() throws InterruptedException {
        Broadcast br = new TerminationBroadcast();//Create a new broadcast that will be sent to all subscribed services (in this case, sent to micro1)
        micro1.subscribeBroadcast(br.getClass(),(c)->{});// internal call to msg.bus.subscribeBroadCast
        micro2.sendBroadcast(br);//send the broadcast to micro1
        Message br2=msgbus.awaitMessage(micro1);//pull the arrived broadcast.
        assertEquals(br,br2);//check if the broadcast arrived matches to the original broadcast.
    }

    @Test
    void testSendEvent() throws InterruptedException {
        Event attack1 = new DeactivationEvent();
        micro2.subscribeEvent(DeactivationEvent.class,(c)->{});//This line also checks if SubscribeEvent works.
        micro1.sendEvent(attack1);//Send the event (to micro2 since he is subscribed to this event type)
        Message attack2 = msgbus.awaitMessage(micro2);
        assertEquals(attack1,attack2);//make sure that micro2 got the exact event.
    }

    /*
    Register is being tested through all the other tests (as the Micro1 and Micro2 are being registered before each and every test).
     */
    @Test
    void testRegister() {
    }
    /*
    We were told on the forum not to implement it.
     */
    @Test
    void testUnregister() {
    }

    @Test
    void testAwaitMessage() throws InterruptedException {
        Event attack1 = new DeactivationEvent();
        micro2.subscribeEvent(DeactivationEvent.class,(c)->{});//This line also checks if SubscribeEvent works.
        micro1.sendEvent(attack1);//send attack1 to micro 2 (since he is the one that is registered to handle such events)
        Message attack2 = msgbus.awaitMessage(micro2);//The test fails if the AwaitMessage does not work properly.
        assertEquals(attack1,attack2);
    }

    @AfterEach
    void tearDown() {
        msgbus.unregister(micro1);
        msgbus.unregister(micro2);
    }
}