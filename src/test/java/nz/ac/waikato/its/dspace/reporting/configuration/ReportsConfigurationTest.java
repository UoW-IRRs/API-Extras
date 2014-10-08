package nz.ac.waikato.its.dspace.reporting.configuration;

import junit.framework.Assert;
import nz.ac.waikato.its.dspace.reporting.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
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
	public void testMarshalConfiguration() throws JAXBException, IOException {
		ReportsConfiguration configuration = TestUtils.makeFirstConfig();

		JAXBContext jaxbContext = JAXBContext.newInstance(ReportsConfiguration.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		File tempFile = File.createTempFile("config-marshal", ".xml");
		tempFile.deleteOnExit();
		jaxbMarshaller.marshal(configuration, tempFile);

		Assert.assertTrue("Marshalled file", FileUtils.contentEquals(tempFile, new File(ClassLoader.getSystemResource("reports-configuration.xml").getPath())));
	}

}
