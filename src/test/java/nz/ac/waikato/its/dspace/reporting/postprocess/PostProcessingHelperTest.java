package nz.ac.waikato.its.dspace.reporting.postprocess;

import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.TestUtils;
import nz.ac.waikato.its.dspace.reporting.configuration.*;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class PostProcessingHelperTest {
	@Test
	public void testRewriteDatesInFile() throws ConfigurationException, IOException {
		File input = new File(ClassLoader.getSystemResource("solr-response.csv").getPath());
		File expected = new File(ClassLoader.getSystemResource("rewritten-response.csv").getPath());

		Report config = TestUtils.makeReport1();

		File result = PostProcessingHelper.runPostProcessors(config, input);
		result.deleteOnExit();

		System.out.println(FileUtils.readFileToString(expected, "UTF-8"));
		System.out.println(FileUtils.readFileToString(result, "UTF-8"));

		Assert.assertTrue("Post processing as expected", FileUtils.contentEquals(expected, result));
	}

	@Test
	public void testInitProcessors() {
		Report config = TestUtils.makeReport1();
		PostProcessor[] processors = PostProcessingHelper.initPostProcessors(config);
		int[] expectedNulls = new int[]{0, 1, 2, 3, 4, 6, 7};
		for (int index : expectedNulls) {
			Assert.assertNull("No postprocessor for index " + index, processors[0]);
		}
		PostProcessor actualProcessor = processors[5];
		Assert.assertNotNull("Postprocessor for index 5", actualProcessor);
		Assert.assertEquals("Postprocessor has right class", ReformatDates.class, actualProcessor.getClass());
	}
}
