package nz.ac.waikato.its.dspace.reporting;

import org.apache.commons.lang.StringUtils;
import org.dspace.core.ConfigurationManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportConfigurationService {
	private String solrServer;

	public ReportConfigurationService(String solrServer) {
		this.solrServer = solrServer;
	}

	public ReportConfiguration getCannedReportConfiguration(Date start, Date end, String cannedReportName) throws ReportingConfigurationException {
		if (StringUtils.isBlank(solrServer)) {
			throw new ReportingConfigurationException("No solr server found");
		}

		ReportConfiguration config = new ReportConfiguration();

		// hard code for now -- TODO read from config file
		config.setBaseURL(solrServer);
		config.setDateRange("dc.date.accessioned_dt", start, end);
		config.setSortField("dc.date.accessioned_dt", true);
		config.setMaxResults(10000);
		try {
			config.addResultField("agresearch.organisation.group", "AgResearch_Group");
			config.addResultField("agresearch.organisation.team", "AgResearch_Team");
			config.addResultField("dc.type", "Output_Type");
			config.addResultField("agresearch.subtype", "Output_Subtype");
			config.addResultField("dc.title", "Title");
			config.addResultField("dc.date.accessioned_dt", "Date_Submitted");
			config.addResultField("dc.identifier.citation", "Citation");
			config.addResultField("handle", "AgScite_Handle");
		} catch (UnsupportedEncodingException e) {
			throw new ReportingConfigurationException("Problem adding header fields", e);
		}
		return config;
	}
}
