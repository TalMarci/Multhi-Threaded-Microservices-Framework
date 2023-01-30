package bgu.spl.mics.application.passiveObjects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EwokTest {

    private Ewok ewok;

    @BeforeEach
    void setUp() {
        ewok=new Ewok(0);
    }

    @Test
    void testAcquire() {
        assertTrue(ewok.isAvailable());
        try{
            ewok.acquire();}
        catch (Exception e)
        {}
        assertFalse(ewok.isAvailable());
    }

    @Test
    void testRelease() {
        try{
            ewok.acquire();}
        catch (Exception e)
        {}
        assertFalse(ewok.isAvailable());
        ewok.release();
        assertTrue(ewok.isAvailable());
    }

    @Test
    void testIsAvailable(){
        try{
        ewok.acquire();}
        catch (Exception e)
        {}
        assertFalse(ewok.isAvailable());
        ewok.release();
        assertTrue(ewok.isAvailable());
    }
}