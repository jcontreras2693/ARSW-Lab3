package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean shouldPause = false;

    private Object lock;

    private boolean keepRunning = true;

    private AtomicInteger healthTotal;


    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, Object lock, AtomicInteger healthTotal) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.lock = lock;
        this.healthTotal = healthTotal;
    }

    public void run() {
        while (keepRunning) {
            if (this.health <= 0 ){
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

            System.out.println("I am " + this.getName() + " | Immortals population: " + immortalsPopulation.size());
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
        int comparation = this.getName().compareTo(i2.getName());
        if (comparation < 0){
            synchronized (this){
                synchronized (i2){
                    fightNow(i2);
                }
            }
        }else if (comparation > 0){
            synchronized (i2){
                synchronized (this){
                    fightNow(i2);
                }
            }
        }
    }

    public void fightNow(Immortal i2){
        if (i2.getHealth() > 0) {
            i2.changeHealth(i2.getHealth() - defaultDamageValue);
            this.health += defaultDamageValue;
            updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
        }
        if (i2.getHealth() <= 0) {
            updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
            i2.stopFight();
            immortalsPopulation.remove(i2);
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
        healthTotal.addAndGet(health);
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
