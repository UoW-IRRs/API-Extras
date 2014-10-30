package nz.ac.waikato.its.dspace.reporting;

import nz.ac.waikato.its.dspace.reporting.configuration.*;

import java.io.File;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class TestUtils {
	public static ReportsConfiguration makeFirstConfig() {
		ReportsConfiguration configuration = new ReportsConfiguration();
		configuration.addCannedReport(makeReport1());
		return configuration;
	}

	public static Report makeReport1() {
		Report report1 = new Report("report1");
		report1.setSorting(new Sorting("dc.date.accessioned_dt", Sorting.Order.ASC));
		report1.setDateField("dc.date.accessioned_dt");
		report1.setMaxResults(10000);
        report1.setTitle("All outputs by Group and Team");
		report1.addField(new Field("group_keyword", "AgResearch_Group"));
		report1.addField(new Field("team_keyword", "AgResearch_Team"));
		report1.addField(new Field("dc.type", "Output_Type"));
		report1.addField(new Field("agresearch.subtype", "Output_Subtype"));
		report1.addField(new Field("dc.title", "Title"));

		Field dateField = new Field("dc.date.accessioned_dt", "Date_Submitted");
		PostProcess postProc = new PostProcess("nz.ac.waikato.its.dspace.reporting.postprocess.ReformatDates");
		postProc.addParam(new PostProcess.Param("format", "MMMM YYYY"));
		dateField.setPostProcess(postProc);
		report1.addField(dateField);

		report1.addField(new Field("dc.identifier.citation", "Citation"));
		report1.addField(new Field("handle", "AgScite_Handle"));

		return report1;
	}

	static String getTestConfigDir() {
		return new File(ClassLoader.getSystemResource("reports-configuration.xml").getPath()).getParent();
	}

	static String getTestSolrServer() {
		return "http://127.0.0.1:8080/solr/search";
	}
}
