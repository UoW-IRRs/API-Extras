package nz.ac.waikato.its.dspace.reporting;

import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.configuration.Report;
import nz.ac.waikato.its.dspace.reporting.configuration.ReportsConfiguration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportConfigurationService {
	private final String configDir;

	public ReportConfigurationService(String configDir) {
		this.configDir = configDir;
	}

	public Report getCannedReportConfiguration(String cannedReportName) throws ConfigurationException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ReportsConfiguration.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			File xml = new File(configDir, "reports-configuration.xml");

			ReportsConfiguration configuration = (ReportsConfiguration) jaxbUnmarshaller.unmarshal(xml);
			return configuration.getCannedReport(cannedReportName);
		} catch (JAXBException e) {
			throw new ConfigurationException("Could not load report configuration", e);
		}
	}
}
