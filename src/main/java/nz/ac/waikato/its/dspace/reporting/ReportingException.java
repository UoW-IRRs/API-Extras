package nz.ac.waikato.its.dspace.reporting;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportingException extends Exception {
	public ReportingException(String message) {
		super(message);
	}

	public ReportingException(String message, Throwable cause) {
		super(message, cause);
	}
}
