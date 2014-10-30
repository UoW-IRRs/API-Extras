package nz.ac.waikato.its.dspace.reporting;

import edu.emory.mathcs.backport.java.util.Arrays;
import junit.framework.Assert;
import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.configuration.Field;
import nz.ac.waikato.its.dspace.reporting.configuration.Report;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportConfigurationServiceTest {

	private ReportConfigurationService service;

	@Before
	public void initService() {
		service = new ReportConfigurationService(TestUtils.getTestConfigDir());
	}

	@Test
	public void testGetReport1FromConfig() throws ConfigurationException {
		Report report1FromService = service.getCannedReportConfiguration("report1");
		Report expected = TestUtils.makeReport1();
		Assert.assertEquals("Report 1 from configuration service", expected, report1FromService);
	}

	@Test
	public void testGetReport2FromConfig() throws ConfigurationException {
		Report report2FromService = service.getCannedReportConfiguration("report2");
		Report expected = TestUtils.makeReport2();
		Assert.assertEquals("Report 2 from configuration service", expected, report2FromService);
		Field pickField = TestUtils.findPickField(report2FromService);
		Assert.assertNotNull("Pick field", pickField);
	}

	@Test
	public void testGetReportNamesFromConfig() throws ConfigurationException {
		List<String> namesFromService = service.getCannedReportNames();
		List<String> expected = Arrays.asList(new String[] {"report1", "report2"});
		Assert.assertEquals("Report names from configuration service", expected, namesFromService);
	}
}
