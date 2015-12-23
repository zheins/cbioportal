package org.mskcc.cbio.portal.util;


import java.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import static java.util.concurrent.TimeUnit.*;
import java.text.ParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by heinsz on 12/22/15.
 */
public class CacheCheck implements Runnable {


    private static synchronized void reCacheAll(long time) {

        System.out.println("Recaching... ");
        DaoCancerStudy.reCache();
        DaoGeneticProfile.reCache();
        DaoPatient.reCache();
        DaoSample.reCache();
        DaoClinicalData.reCache();
        System.out.println("Finished recaching... ");
    }

    private static boolean studyNeedsRecaching(String stableId, Integer ... internalId) {
        if (cacheOutOfSyncWithDb()) {
            return true;
        }

        try {
            java.util.Date importDate = null;
            java.util.Date cacheDate = null;
            if (internalId.length > 0) {
                importDate = DaoCancerStudy.getImportDate(stableId, internalId[0]);
                cacheDate = DaoCancerStudy.cacheDateByInternalId.get(internalId[0]);
            } else {
                if (stableId.equals(org.mskcc.cbio.portal.util.AccessControl.ALL_CANCER_STUDIES_ID)) {
                    return false;
                }
                importDate = DaoCancerStudy.getImportDate(stableId);
                cacheDate = DaoCancerStudy.cacheDateByStableId.get(stableId);
            }

            return (importDate == null || cacheDate == null) ? false : cacheDate.before(importDate);
        } catch (ParseException e) {
            return false;
        }
        catch (DaoException e) {
            return false;
        }
    }

    private static boolean cacheOutOfSyncWithDb()
    {
        try {
            return DaoCancerStudy.getStudyCount() != DaoCancerStudy.byStableId.size();
        }
        catch (DaoException e) {}
        return false;
    }

    private static void checkCaching() {
        ArrayList<CancerStudy> studies = DaoCancerStudy.getAllCancerStudies();
        boolean recacheNeeded = false;
        for(CancerStudy study : studies) {
            if(studyNeedsRecaching(study.getCancerStudyStableId(),study.getInternalId())) {
                recacheNeeded = true;
            }
        }
        if(recacheNeeded) {
            reCacheAll(System.currentTimeMillis());
        }
    }

    public void run() {
        boolean alive = true;
        while (alive) {
            try {
                checkCaching();
                Thread.sleep(1 * 60 * 1000);
            }
            catch (InterruptedException e) {
                alive = false;
            }
        }
    }
}
