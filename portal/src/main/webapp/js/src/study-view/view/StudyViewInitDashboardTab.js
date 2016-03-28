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

var StudyViewInitDashboardTab = (function(){
     var init = function(_clinical_data, _genetic_profile_data){
         var dashboard_data = {};
         initData(_clinical_data, _genetic_profile_data);
         render(dashboard_data);
        
        // Combine the clinical date added to genetic alterations, basically need number of events per date per type of event.
        function initData(clinical_data, genetic_profile_data) {
            for (var i = 0; i < clinical_data.length; i++) {
                var sid = clinical_data[i].sample_id;
                var date_added = clinical_data[i].attr_val;
                var sample_added = false;
                for(var j = 0; j < genetic_profile_data.length; j++) {
                    if (genetic_profile_data[j].sample_id === clinical_data[i].sample_id) {
                        sample_added = true;
                        // if the date is already in the structure, we want to add to it
                        if(dashboard_data.hasOwnProperty(date_added)) {
                            var found = false;
                            for (var k = 0; k < dashboard_data[date_added].length; k++) {
                                if (genetic_profile_data[j].event_type === dashboard_data[date_added][k].event_type) {
                                    dashboard_data[date_added][k].number_of_events = dashboard_data[date_added][k].number_of_events + parseInt(genetic_profile_data[j].number_of_events);
                                    dashboard_data[date_added][k].number_of_samples = dashboard_data[date_added][k].number_of_samples + 1;
                                    found = true;
                                }
                                if (dashboard_data[date_added][k].event_type === "GENERAL" && dashboard_data[date_added][k].samples.indexOf(sid) < 0) {
                                    dashboard_data[date_added][k].number_of_samples = dashboard_data[date_added][k].number_of_samples + 1;
                                    dashboard_data[date_added][k].samples.push(sid); 
                                }
                            }
                            if (!found) {
                                // we didn't find the event type of this sample, so add it as a new one
                                dashboard_data[date_added].push({'event_type':genetic_profile_data[j].event_type, 'number_of_events':parseInt(genetic_profile_data[j].number_of_events), 'number_of_samples':1});
                            }
                        } else {
                            // this is a new date, so we need to add a new list to it with the event type and the general event type (for overall counts)
                            dashboard_data[date_added] = [{'event_type':genetic_profile_data[j].event_type, 'number_of_events':parseInt(genetic_profile_data[j].number_of_events), 'number_of_samples':1}, {'event_type':"GENERAL", 'number_of_samples':1, 'samples':[sid]}];
                        }
                    }
                }
                
                // If the sample doesn't have any genomic profile data associated with it, it stil needs to go into the general event type count.
                if (!sample_added) {
                    if (dashboard_data.hasOwnProperty(date_added)) {
                        for (var k = 0; k < dashboard_data[date_added].length; k++) {
                            if (dashboard_data[date_added][k].event_type === "GENERAL" && dashboard_data[date_added][k].samples.indexOf(sid) < 0) {
                                dashboard_data[date_added][k].number_of_samples = dashboard_data[date_added][k].number_of_samples + 1;
                                dashboard_data[date_added][k].samples.push(sid);
                            }
                        }
                    } else {
                        dashboard_data[date_added] = [{'event_type':"GENERAL", 'number_of_samples':1, 'samples': [sid]}];
                    }
                }
            }            
        }
        
        function render(dashboard_data) {
            var _x = [];
            var _y_samples = [];
            var _y_mutations = [];
            var _y_cna = [];
            
            var sorted_dates = Object.keys(dashboard_data).sort();
            
            for (var i = 0; i < sorted_dates.length; i++) {
                var date = sorted_dates[i];
                var split_date = date.split("/");
                var d = new Date(split_date[0], split_date[1], split_date[2]);
                _x.push(d);
                for (var j = 0; j < dashboard_data[date].length; j++) {
                    if (dashboard_data[date][j].event_type === "GENERAL") {
                        if (_y_samples.length === 0) {
                            _y_samples.push(dashboard_data[date][j].number_of_samples);
                        } else {
                            _y_samples.push(dashboard_data[date][j].number_of_samples + _y_samples[_y_samples.length - 1]);
                        }
                    }
                    if (dashboard_data[date][j].event_type === "MUTATION_EXTENDED") {
                        if (_y_mutations.length === 0) {
                            _y_mutations.push(dashboard_data[date][j].number_of_events);
                        } else {
                            _y_mutations.push(dashboard_data[date][j].number_of_events + _y_mutations[_y_mutations.length - 1]);
                        }
                    }
                    if (dashboard_data[date][j].event_type === "COPY_NUMBER_ALTERATION") {
                        if (_y_cna.length === 0) {
                            _y_cna.push(dashboard_data[date][j].number_of_events);
                        } else {
                            _y_cna.push(dashboard_data[date][j].number_of_events + _y_cna[_y_cna.length - 1]);
                        }
                    }
                }
            }
            
            render_samples_over_time(_x, _y_samples);
            render_mutations_over_time(_x, _y_mutations);
            render_cna_over_time(_x, _y_cna);
            
            function render_samples_over_time(_x, _y) {
                var data = [{
                    y: _y,
                    x: _x,
                    line: {width: 1}
                }];
                var layout = {
                        title: 'Samples Added Over Time',
                        yaxis: {title: 'Number of Samples'},
                        xaxis: {
                            tickformat: "%B, %Y"
                        }
                };
                window.Plotly.plot('dashboard-samples-over-time', data, layout);
            };
            function render_mutations_over_time(_x, _y) {
                var data = [{
                    y: _y,
                    x: _x,
                    line: {width: 1}
                }];
                var layout = {
                        title: 'Mutations Added Over Time',
                        yaxis: {title: 'Number of Mutations'},
                        xaxis: {
                            tickformat: "%B, %Y"
                        }
                };
                window.Plotly.plot('dashboard-mutations-over-time', data, layout);
            };
            function render_cna_over_time(_x, _y) {
                var data = [{
                    y: _y,
                    x: _x,
                    line: {width: 1}
                }];
                var layout = {
                        title: 'CNA Events Added Over Time',
                        yaxis: {title: 'Number of CNA events'},
                        xaxis: {
                            tickformat: "%B, %Y"
                        }
                };
                window.Plotly.plot('dashboard-cna-over-time', data, layout);
            };
        };
    };
    
    return {
        init: init
    };
})();