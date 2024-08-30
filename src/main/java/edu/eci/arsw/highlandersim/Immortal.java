package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean shouldPause = false;

    private Object lock;

    private boolean keepRunning = true;


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, Object lock) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.lock = lock;
    }

    public void run() {

        while (keepRunning) {
            if (health <= 0 ){
                break;
            }
            synchronized (lock) {
                while (shouldPause) {
                    try{
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);
            /*
            if (immortalsPopulation.size() <= 1){
                updateCallback.processReport(this + " says: I am the Winner!!" + "\n");
                break;
            }
            */
            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void fight(Immortal i2) {
        synchronized (lock){
            if (i2.getHealth() > 0) {
                i2.changeHealth(i2.getHealth() - defaultDamageValue);
                this.health += defaultDamageValue;
                updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                if (i2.getHealth() <= 0) {
                    updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
                    immortalsPopulation.remove(i2);
                }
            } else {
                updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                immortalsPopulation.remove(i2);
            }
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void pause() {
        synchronized (lock) {
            shouldPause = true;
        }
    }

    public void resumeFight() {
        synchronized (lock) {
            shouldPause = false;
            lock.notifyAll();
        }
    }

    public void stopFight(){
        keepRunning =  false;
    }
}
