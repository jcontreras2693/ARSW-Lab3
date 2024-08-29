/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;

    public List<Integer> checkHost(String ipaddress, int numThreads){
        ArrayList<BlackListThread> threads = new ArrayList<BlackListThread>();
        ArrayList<Integer> blackListOcurrences= new ArrayList<Integer>();
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int serverCount = skds.getRegisteredServersCount();
        int inicio = 0;
        int delta = serverCount / numThreads;
        int fin = delta;
        /*
        El proceso cambia un poco dependiendo de que la cantidad de hilos sea par o impar.
         */
        if (numThreads % 2 == 1){
            for (int i = 0; i < numThreads - 1; i++){
                threads.add(new BlackListThread(inicio, fin, ipaddress));
                threads.get(i).start();
                inicio = fin;
                fin += delta;
            }
            fin += serverCount - fin;
            threads.add(new BlackListThread(inicio, fin, ipaddress));
            threads.get(numThreads - 1).start();
        }else{
            for (int i = 0; i < numThreads; i++){
                threads.add(new BlackListThread(inicio, fin, ipaddress));
                threads.get(i).start();
                inicio = fin;
                fin += delta;
            }
        }
        int checkedListsCount = 0;
        for (int i = 0; i < numThreads; i++) {
            BlackListThread obj = threads.get(i);
            checkedListsCount += obj.getCheckedListsCount();
            try{
                obj.join();
                blackListOcurrences.addAll(obj.getBlackListOcurrences());
                /*
               En caso de que se compruebe que la IP no es confiable se termina el proceso.
                 */
                if (blackListOcurrences.size()>=BLACK_LIST_ALARM_COUNT){
                    skds.reportAsNotTrustworthy(ipaddress);
                    LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
                    return blackListOcurrences;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
            skds.reportAsTrustworthy(ipaddress);

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});

        return blackListOcurrences;
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

}
