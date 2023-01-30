package bgu.spl.mics.application;
import bgu.spl.mics.application.passiveObjects.Attack;

/**
 * A class that helps us to extract the information from the json file.
 */
public class InputJson {
    //Fields:
    private Attack[] attacks;
    private long R2D2;
    private long Lando;
    private int Ewoks;

    //Constructor:
    public InputJson(Attack[] attacks, long r2d2, long lando, int ewoks)
    {
        this.attacks=attacks;
        this.R2D2=r2d2;
        this.Lando=lando;
        this.Ewoks=ewoks;
    }

    //Getters
    public Attack[] getAttacks()
    {
        return attacks;
    }

    public long getR2D2()
    {
        return R2D2;
    }

    public long getLando()
    {
        return Lando;
    }

    public int getEwoks()
    {
        return Ewoks;
    }
}
