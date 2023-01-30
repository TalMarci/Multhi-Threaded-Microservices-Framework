package bgu.spl.mics.application.passiveObjects;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a Diary - in which the flow of the battle is recorded.
 * We are going to compare your recordings with the expected recordings, and make sure that your output makes sense.
 * <p>
 * Do not add to this class nothing but a single constructor, getters and setters.
 */
public class Diary
{
    private AtomicInteger totalAttacks=new AtomicInteger(0);//we use AtmoicInteger because multiple threads are updating this field simultiniously.
    private long HanSoloFinish,C3POFinish,R2D2Deactivate,LeiaTerminate,HanSoloTerminate,C3POTerminate,R2D2Terminate,LandoTerminate;//the Diary fields.

    /**
     * A Class that holds the singleton instance of the diary.
     */
    private static class singleDiary {
        private static Diary instance = new Diary();
    }

    /**
     * @return the singleton Diary instance.
     */
    public static Diary getInstance() { return Diary.singleDiary.instance; }

    /*
    Simple getters and setters.
     */
    public void setC3POFinish(long c3POFinish) {
        C3POFinish = c3POFinish;
    }

    public void setC3POTerminate(long c3POTerminate) {
        C3POTerminate = c3POTerminate;
    }

    public void setHanSoloFinish(long hanSoloFinish) {
        HanSoloFinish = hanSoloFinish;
    }

    public void setHanSoloTerminate(long hanSoloTerminate) {
        HanSoloTerminate = hanSoloTerminate;
    }

    public void setLandoTerminate(long landoTerminate) {
        LandoTerminate = landoTerminate;
    }

    public void setLeiaTerminate(long leiaTerminate) {
        LeiaTerminate = leiaTerminate;
    }

    public void setR2D2Deactivate(long r2D2Deactivate) {
        R2D2Deactivate = r2D2Deactivate;
    }

    public void setR2D2Terminate(long r2D2Terminate) {
        R2D2Terminate = r2D2Terminate;
    }

    public void setTotalAttacks() {
        totalAttacks.incrementAndGet();//Increses the totalAttacks by 1.
    }

    public int getTotalAttacks() {
        return totalAttacks.intValue();
    }

    public long getC3POFinish() {
        return C3POFinish;
    }

    public long getC3POTerminate() {
        return C3POTerminate;
    }

    public long getHanSoloFinish() {
        return HanSoloFinish;
    }

    public long getHanSoloTerminate() {
        return HanSoloTerminate;
    }

    public long getLandoTerminate() {
        return LandoTerminate;
    }

    public long getLeiaTerminate() {
        return LeiaTerminate;
    }

    public long getR2D2Deactivate() {
        return R2D2Deactivate;
    }

    public long getR2D2Terminate() {
        return R2D2Terminate;
    }
}
