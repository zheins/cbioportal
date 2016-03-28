/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

var StudyViewDashboardTabController = (function() {
    var init = function (study_id, case_ids, mutation_profile_id, cna_profile_id) {
        var sample_clinical_data;
        $.when(window.cbioportal_client.getSampleClinicalData({study_id:[study_id], attribute_ids: ["DATE_ADDED"], sample_ids: case_ids})).then(function(_sample_clinical_data) {
            sample_clinical_data  = _sample_clinical_data;
            $.when(window.cbioportal_client.getSampleStatisticsByProfile({genetic_profile_ids:[mutation_profile_id, cna_profile_id]})).then(function(genetic_profile_data) {
                StudyViewInitDashboardTab.init(sample_clinical_data, genetic_profile_data);
                $('#dashboard-loading-wait').hide();
                $('#dashboard-main').show();
            });
            
        });
        

    };
     
    return {
        init: function(study_id, case_ids, mutation_profile_id, cna_profile_id) {
            init(study_id, case_ids, mutation_profile_id, cna_profile_id);
        }
    };

})();