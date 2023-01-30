package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Attack;

public class AttackEvent implements Event<Boolean> {

    private Attack atk;
    public AttackEvent(Attack atk) {
        this.atk=atk;
    }

    public Attack getAtk() {
        return atk;
    }

}
