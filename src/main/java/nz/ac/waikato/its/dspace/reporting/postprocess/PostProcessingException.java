package nz.ac.waikato.its.dspace.reporting.postprocess;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class PostProcessingException extends Exception {
	public PostProcessingException(String message) {
		super(message);
	}

	public PostProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
