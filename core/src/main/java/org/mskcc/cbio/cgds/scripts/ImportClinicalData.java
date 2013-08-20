/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center_
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center_
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoClinicalData;
import org.mskcc.cbio.cgds.dao.DaoClinicalAttribute;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.ClinicalData;
import org.mskcc.cbio.cgds.model.ClinicalAttribute;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

import java.io.*;
import java.util.*;
import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;

public class ImportClinicalData {

    public static final String METADATA_PREIX = "#";
    public static final String DELIMITER = "\t";
    public static final String CASE_ID = "CASE_ID";

	private File clinicalDataFile;
	private CancerStudy cancerStudy;
    private ProgressMonitor pMonitor;
	
    /**
     * Constructor.
     *
     * @param cancerStudy   Cancer Study
     * @param clinicalDataFile File
     * @param pMonitor         ProgressMonitor
     */
    public ImportClinicalData(CancerStudy cancerStudy, File clinicalDataFile, ProgressMonitor pMonitor) {
        this.cancerStudy = cancerStudy;
        this.pMonitor = pMonitor;
        this.clinicalDataFile = clinicalDataFile;
    }

    /**
     * Method to import data.
     *
     * @throws java.io.IOException
     * @throws org.mskcc.cbio.cgds.dao.DaoException
     */
    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader =  new FileReader(clinicalDataFile);
        BufferedReader buff = new BufferedReader(reader);

        List<ClinicalAttribute> columnAttrs = grabAttrs(buff);

        String line;
        List<ClinicalData> clinicals = new ArrayList<ClinicalData>();
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            
            if (line.isEmpty() || line.substring(0,1).equals(METADATA_PREIX)) {
                // ignore lines with the METADATA_PREFIX
                continue;
            }

            String[] fields = line.split(DELIMITER);
            if (fields.length > columnAttrs.size()) {
                System.err.println("more attributes than header: "+line);
                continue;
            }
            
            String caseId = null;
            for (int i = 0; i < fields.length; i++) {
                ClinicalData clinical = new ClinicalData();
                clinical.setCancerStudyId(cancerStudy.getInternalId());

                if (columnAttrs.get(i).getAttrId().equals(CASE_ID)) {
                    caseId = fields[i];
                    continue;
                } else {
                    clinical.setCaseId(caseId);
                    clinical.setAttrId(columnAttrs.get(i).getAttrId());
                    clinical.setAttrVal(fields[i]);
                    clinicals.add(clinical);
                }
            }
        }
        
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
        }
    }

    /**
     *
     * Imports clinical data and clinical attributes (from the worksheet)
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        if (args.length != 2) {
            System.out.println("command line usage:  importClinical <clinical.txt> <cancer_study_id>");
            System.exit(1);
        }

		try {
			CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(args[1]);
			if (cancerStudy == null) {
				System.err.println("Unknown cancer study: " + args[1]);
			}
			else {
				File clinical_f = new File(args[0]);
				System.out.println("Reading data from:  " + clinical_f.getAbsolutePath());
				int numLines = FileUtil.getNumLines(clinical_f);
				System.out.println(" --> total number of lines:  " + numLines);
				pMonitor.setMaxValue(numLines);

				ImportClinicalData importClinicalData = new ImportClinicalData(cancerStudy, clinical_f, pMonitor);
                importClinicalData.importData();
                System.out.println("Success!");
			}
		}
		catch (Exception e) {
			System.err.println ("Error:  " + e.getMessage());
        }
		finally {
            ConsoleUtil.showWarnings(pMonitor);
        }
	}

    /**
     * Grabs the metadatas (clinical attributes) from the file, inserts them into the database,
     * and returns them as a list.
     *
     * @param buff
     * @return clinicalAttributes
     */
    private List<ClinicalAttribute> grabAttrs(BufferedReader buff) throws DaoException, IOException {
        List<ClinicalAttribute> attrs = new ArrayList<ClinicalAttribute>();

        String[] displayNames = splitFields(buff);
        String[] descriptions = splitFields(buff);
        String[] datatypes = splitFields(buff);
        String[] colnames = splitFields(buff);

        if (displayNames.length != colnames.length
                ||  descriptions.length != colnames.length
                ||  datatypes.length != colnames.length) {
            throw new DaoException("attribute and metadata mismatch in clinical staging file");
        }

        for (int i = 0; i < colnames.length; i+=1) {
            ClinicalAttribute attr =
                    new ClinicalAttribute(colnames[i], displayNames[i], descriptions[i], datatypes[i]);
            try {
                DaoClinicalAttribute.getDatum(attr.getAttrId());
            } catch (DaoException e) {
                DaoClinicalAttribute.addDatum(attr);
            }
            attrs.add(attr);
        }

        return attrs;
    }

    /**
     * helper function for spliting the *next* line in the reader
     * so, N.B. --  ! it alters the state of the reader
     * @param buff
     * @return
     */
    private String[] splitFields(BufferedReader buff) throws IOException {
        String line = buff.readLine();
        line = line.replaceAll(METADATA_PREIX, "");
        String[] fields = line.split(DELIMITER);

        return fields;
    }
}
