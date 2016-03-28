/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.service;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.model.DBGeneticProfile;
import org.mskcc.cbio.portal.model.DBSampleStatistic;
import org.mskcc.cbio.portal.persistence.SampleStatisticMapper;
import org.mskcc.cbio.portal.persistence.GeneticProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 *
 * @author heinsz
 */
@Service
public class ApiStatisticService {
    @Autowired
    private SampleStatisticMapper sampleStatisticMapper;
    @Autowired
    private GeneticProfileMapper geneticProfileMapper;
    
    @Transactional
    public List<DBGeneticProfile> getGeneticProfiles(List<String> genetic_profile_ids) {
        return geneticProfileMapper.getGeneticProfiles(genetic_profile_ids);
    }
    
    @Transactional
    public List<DBSampleStatistic> getSampleStatistics(List<String> genetic_profile_ids) {
        List<DBSampleStatistic> ret = new ArrayList<>();
        List<DBGeneticProfile> genetic_profiles = getGeneticProfiles(genetic_profile_ids);
        List<String> mutation_profiles = new ArrayList<>();
        List<String> non_mutation_profiles = new ArrayList<>();
        
        for (DBGeneticProfile profile : genetic_profiles) {
            if (profile.genetic_alteration_type.equals("MUTATION_EXTENDED")) {
                mutation_profiles.add(profile.id);
            } else {
                non_mutation_profiles.add(profile.id);
            }
        }
        if (!mutation_profiles.isEmpty()) {
            List<DBSampleStatistic> result  = sampleStatisticMapper.getMutationSampleStatisticsByProfile(mutation_profiles);
            for (DBSampleStatistic dbss : result) {
                ret.add(dbss);
            }
        }
        if (!non_mutation_profiles.isEmpty()) {
            List<DBSampleStatistic> result = sampleStatisticMapper.getCNASampleStatisticsByProfile(non_mutation_profiles);
            for (DBSampleStatistic dbss : result) {
                ret.add(dbss);
            }
        }
        
        return ret;
    }
}
