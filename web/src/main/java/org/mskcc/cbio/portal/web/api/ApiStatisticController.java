/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web.api;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.service.ApiStatisticService;
import org.mskcc.cbio.portal.model.DBSampleStatistic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;


/**
 *
 * @author heinsz
 */

@Controller
public class ApiStatisticController {
    @Autowired
    private ApiStatisticService service;
    
    @ApiOperation(value = "Get sample statistics data",
            nickname = "getSampleStatistics",
            notes="")
    @Transactional
    @RequestMapping(value="/samplestatistics", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBSampleStatistic> getSampleStatistics(
            @ApiParam(required = true, value = "List of genetic profiles (example: studyid_mutations).")
            @RequestParam(required = true)
            List<String> genetic_profile_ids) {
        return service.getSampleStatistics(genetic_profile_ids);
    }
}
