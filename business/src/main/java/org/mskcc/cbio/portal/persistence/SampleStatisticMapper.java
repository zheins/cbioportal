/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.persistence;

/**
 *
 * @author heinsz
 */

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBSampleStatistic;

public interface SampleStatisticMapper {
    List<DBSampleStatistic> getMutationSampleStatisticsByProfile(@Param("genetic_profile_ids") List<String> genetic_profile_ids);
    List<DBSampleStatistic> getCNASampleStatisticsByProfile(@Param("genetic_profile_ids") List<String> genetic_profile_ids);
}
