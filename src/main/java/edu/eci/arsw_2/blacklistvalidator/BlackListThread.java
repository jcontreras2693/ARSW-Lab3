package edu.eci.arsw.blacklistvalidator;

import java.util.ArrayList;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class BlackListThread extends Thread{
    private int intA;
    private int intB;
    private String IP;
    private ArrayList<Integer> blackListOcurrences;
    private int checkedListsCount;

    public BlackListThread(int intA, int intB, String IP){
        this.intA = intA;
        this.intB = intB;
        this.IP = IP;
        this.blackListOcurrences = new ArrayList<Integer>();
        this.checkedListsCount = 0;
    }

    @Override
    public void run(){
        blackListFind();
    }

    public void blackListFind(){
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        for (int i = intA; i <= intB; i++){
            synchronized (blackListOcurrences){
                if (blackListOcurrences.size() >= 5){
                    break;
                }
            }
            checkedListsCount++;
            if (skds.isInBlackListServer(i, IP)){
                blackListOcurrences.add(i);
            }
        }

    }

    public ArrayList<Integer> getBlackListOcurrences(){
        return blackListOcurrences;
    }

    public int getCheckedListsCount(){
        return checkedListsCount;
    }

}
