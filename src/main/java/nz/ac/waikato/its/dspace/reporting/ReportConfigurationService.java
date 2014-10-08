package nz.ac.waikato.its.dspace.reporting;

import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.configuration.Report;
import nz.ac.waikato.its.dspace.reporting.configuration.ReportsConfiguration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportConfigurationService {
	private ReportsConfiguration configuration;

	public ReportConfigurationService(String configDir) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ReportsConfiguration.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			File xml = new File(configDir, "reports-configuration.xml");

			configuration = (ReportsConfiguration) jaxbUnmarshaller.unmarshal(xml);
		} catch (JAXBException e) {
			System.err.println("Could not initialise from config: " + e.getMessage());
			e.printStackTrace(System.err);
		}

	}

	public List<String> getCannedReportNames()  throws ConfigurationException {
		if (configuration == null) {
			throw new ConfigurationException("Configuration service isn't initialised");
		}
		Collection<Report> reports = configuration.getCannedReports();
		List<String> result = new ArrayList<>();
		for (Report report : reports) {
			result.add(report.getId());
		}
		return result;
	}

	public Report getCannedReportConfiguration(String cannedReportName) throws ConfigurationException {
		if (configuration == null) {
			throw new ConfigurationException("Configuration service isn't initialised");
		}
		return configuration.getCannedReport(cannedReportName);
	}
}
