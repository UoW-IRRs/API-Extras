package nz.ac.waikato.its.dspace.reporting;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportingConfigurationException extends Exception {
	public ReportingConfigurationException(String message) {
		super(message);
	}

	public ReportingConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
