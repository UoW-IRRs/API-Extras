package nz.ac.waikato.its.dspace.reporting.configuration;

import junit.framework.Assert;
import nz.ac.waikato.its.dspace.reporting.TestUtils;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch
 */
public class ReportsConfigurationTest {
	@Test
	public void testUnmarshalConfiguration() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ReportsConfiguration.class);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		File xml = new File(ClassLoader.getSystemResource("reports-configuration.xml").getPath());

		ReportsConfiguration configuration = (ReportsConfiguration) jaxbUnmarshaller.unmarshal(xml);
		ReportsConfiguration reference = TestUtils.makeFirstConfig();
		Assert.assertEquals("Canned report size", configuration.getCannedReports().size(), reference.getCannedReports().size());
	}

	@Test
	public void testUnmarshalConfigurationWithParent() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ReportsConfiguration.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		File xml = new File(ClassLoader.getSystemResource("reports-configuration-with-parent.xml").getPath());

		ReportsConfiguration configuration = (ReportsConfiguration) jaxbUnmarshaller.unmarshal(xml);

		Assert.assertEquals(configuration.getCannedReports().size(), 2);
		Report report = configuration.getCannedReports().iterator().next();
		Assert.assertNotNull(report.getParent());
	}

}
