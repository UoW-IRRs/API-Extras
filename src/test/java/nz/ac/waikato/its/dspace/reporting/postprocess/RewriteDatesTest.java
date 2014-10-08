package nz.ac.waikato.its.dspace.reporting.postprocess;

import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.configuration.PostProcess;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class RewriteDatesTest {

	@Test
	public void testBasicRewrite() throws ConfigurationException, PostProcessingException {
		ReformatDates postproc = new ReformatDates();
		PostProcess config = new PostProcess();
		config.setClassName(ReformatDates.class.getName());
		config.addParam(new PostProcess.Param("format", "MMMM YYYY"));
		postproc.setConfig(config);

		String[] input = new String[] { "2014-09-08T02:48:22Z" };
		String[] rewritten = postproc.processLine(input, 0);
		Assert.assertEquals("Rewritten line length", input.length, rewritten.length);
		Assert.assertEquals("Rewritten values", new String[] { "September 2014" }, rewritten);
	}

}
