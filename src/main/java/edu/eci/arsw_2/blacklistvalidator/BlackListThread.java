package edu.eci.arsw.blacklistvalidator;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class BlackListThread extends Thread{
    private int intA;
    private int intB;
    private String IP;
    private ArrayList<Integer> blackListOcurrences;
    private AtomicInteger checkedListsCount;
    private int alarmCount;

    public BlackListThread(int intA, int intB, String IP, ArrayList<Integer> blackListOcurrences, AtomicInteger checkedListsCount, int alarmCount){
        this.intA = intA;
        this.intB = intB;
        this.IP = IP;
        this.blackListOcurrences = blackListOcurrences;
        this.checkedListsCount = checkedListsCount;
        this.alarmCount = alarmCount;
    }

    @Override
    public void run(){
        blackListFind();
    }

    public void blackListFind(){
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        for (int i = intA; i <= intB; i++){
            if (skds.isInBlackListServer(i, IP)){
                blackListOcurrences.add(i);
            }
            synchronized (blackListOcurrences){
                if (blackListOcurrences.size() >= 5){
                    break;
                }
            checkedListsCount.incrementAndGet();
            }
        }
    }
}
