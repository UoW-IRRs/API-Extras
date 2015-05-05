package nz.ac.waikato.its.dspace.reporting.configuration;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch
 */
public class ConfigurationException extends Exception {
	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
