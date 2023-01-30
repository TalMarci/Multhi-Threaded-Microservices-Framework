package bgu.spl.mics.application.passiveObjects;

/**
 * Passive data-object representing a forest creature summoned when HanSolo and C3PO receive AttackEvents.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Ewok {
	private int serialNumber;
	private boolean available;

    /**
     * Constructor
     * @param serialNumber the ewok's serial number
     */
	Ewok(int serialNumber){
	    this.serialNumber=serialNumber;
	    available=true;
    }

    /**
     * @return availablity status
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Acquires an Ewok
     * We syncronize this function because we want to avoid a case where two microservices aquire the same Ewok.
     * If an Ewok is unavailable, it will force wait untill notifyAll is being called on release.
     */
    public synchronized void acquire() throws InterruptedException {
        while(!available)
            this.wait();
        available=false;
    }

    /**
     * release an Ewok, and notifies all sleeping threads that are waiting on acquire.
     */
    public synchronized void release() {
        available=true;
        notifyAll();
    }
}
