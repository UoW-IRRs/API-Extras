package nz.ac.waikato.its.dspace.reporting.postprocess;

import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.configuration.PostProcess;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch
 */
public interface PostProcessor {
	/**
	 * Set the configuration for this post processor
	 * @param config The configuration object. Must not be null.
	 */
	public void setConfig(PostProcess config) throws ConfigurationException;

	/**
	 * Process the fields indicated in the line provided and return the result.
	 * @param inputLine The line to process. Must not be null.
	 * @param index The index of the field to work on.
	 * @return The processed line as a non-null (but potentially empty) String array of the same length as inputLine.
	 * @throws nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException if no configuration has been set.
	 */
	public String[] processLine(String[] inputLine, int index) throws ConfigurationException, PostProcessingException;
}
