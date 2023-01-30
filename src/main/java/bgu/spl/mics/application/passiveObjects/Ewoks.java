package bgu.spl.mics.application.passiveObjects;


import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class Ewoks {
    private Vector<Ewok> EwokList;//Holds the Ewok objects

    /**
     * a setter for the EwokList (because we are not allowed to change the constructor).
     * Will create the Ewok objects and push them to the EwokList
     * @param ewokList Holds the Ewok objects.
     */
    public void setEwokList(int ewokList) {
        EwokList = new Vector<Ewok>();
        for(int i=0;i<ewokList;i++)
            EwokList.add(new Ewok(i+1));
    }

    /**
     * Singleton Class.
     * Holding the single instance of the Ewoks.
     */
    private static class EwoksHolder{
        private static Ewoks instance = new Ewoks();
    }

    /**
     * @return the single Ewoks instance.
     */
    public static Ewoks getInstance()
    {
        return EwoksHolder.instance;
    }

    /**
     * Tries to acquire the requested Ewoks.
     * Will wait on Ewok:Acquire function if the Ewok is unavilable, therefore it is a blocking function.
     * @param EwoksWanted the required Ewoks to aquire.
     * @throws InterruptedException
     */
    public void Acquire (List<Integer> EwoksWanted) throws InterruptedException
    {
        EwoksWanted.sort(Comparator.comparingInt((o) -> o));
        for (Integer a : EwoksWanted)
            EwokList.get(a-1).acquire();//a-1 because we are using a vector that starts on 0 (ewok 1 is on vector[0])
    }

    /**
     * Releases the Ewoks using Ewok.release.
     * @param EwoksToRelease
     */
    public void Release(List<Integer> EwoksToRelease)
    {
        for(Integer a: EwoksToRelease)
            EwokList.get(a-1).release();//a-1 because we are using a vector that starts on 0 (ewok 1 is on vector[0])
    }

}
