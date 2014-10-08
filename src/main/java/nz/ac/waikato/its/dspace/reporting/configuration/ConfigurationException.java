package nz.ac.waikato.its.dspace.reporting.configuration;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ConfigurationException extends Exception {
	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
