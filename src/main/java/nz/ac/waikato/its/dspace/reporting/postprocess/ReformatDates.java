package nz.ac.waikato.its.dspace.reporting.postprocess;

import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.configuration.PostProcess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReformatDates implements PostProcessor {

	private SimpleDateFormat sourceFormat;
	private SimpleDateFormat targetFormat;

	@Override
	public void setConfig(PostProcess config) throws ConfigurationException {
		sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		PostProcess.Param formatParam = config.getParam("format");
		if (formatParam == null) {
			throw new ConfigurationException("Format parameter is required but doesn't exist");
		}
		targetFormat = new SimpleDateFormat(formatParam.getValue());
		targetFormat.setTimeZone(TimeZone.getDefault());
	}

	@Override
	public String[] processLine(String[] inputLine, int index) throws ConfigurationException, PostProcessingException {
		if (targetFormat == null) {
			throw new ConfigurationException("This post processor hasn't been configured yet");
		}
		String[] result = inputLine.clone();
		try {
			Date date = sourceFormat.parse(inputLine[index]);
			result[index] = targetFormat.format(date);
		} catch (ParseException e) {
			throw new PostProcessingException("Could not reformat index " + index + " of line: " + e.getMessage(), e);
		}
		return result;
	}
}
