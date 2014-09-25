package nz.ac.waikato.its.dspace.reporting;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportGeneratorTest {
	@Test
	public void testGetCannedReportConfig() {
		ReportConfiguration config = null;
		try {
			config = new ReportConfigurationService("http://127.0.0.1:8080/solr/search").getCannedReportConfiguration(null, null, "canned-report-1");
		} catch (ReportingConfigurationException e) {
			e.printStackTrace();
			Assert.fail("Caught exception getting canned report config");
		}
		Assert.assertNotNull("Got non-null configuration object", config);
		URL queryURL = null;
		try {
			queryURL = config.getQueryURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Assert.fail("Caught exception getting query URL");
		}
		Assert.assertNotNull("Got non-null query URL object", queryURL);
		Assert.assertEquals("Query string is as expected", "http://127.0.0.1:8080/solr/search/select?q=*%3A*&fq=withdrawn%3Afalse&fq=search.resourcetype%3A2&fq=dc.date.accessioned_dt%3A%5B*+TO+*%5D&wt=csv&csv.mv.separator=%7C&sort=dc.date.accessioned_dt+asc&rows=10000&fl=AgResearch_Group%3Aagresearch.organisation.group%2CAgResearch_Team%3Aagresearch.organisation.team%2COutput_Type%3Adc.type%2COutput_Subtype%3Aagresearch.subtype%2CTitle%3Adc.title%2CDate_Submitted%3Adc.date.accessioned_dt%2CCitation%3Adc.identifier.citation%2CAgScite_Handle%3Ahandle", queryURL.toString());
	}

	@Test
	public void testQueryResultsToFile() {
		ReportConfiguration config = null;
		try {
			config = new ReportConfigurationService("http://127.0.0.1:8080/solr/search").getCannedReportConfiguration(null, null, "canned-report-2");
		} catch (ReportingConfigurationException e) {
			e.printStackTrace();
			Assert.fail("Caught exception getting canned report config");
		}

		Assert.assertNotNull("Got non-null configuration object", config);

		InputStream results;
		try {
			results = ReportGenerator.queryResultsToFile(config);
			Scanner scanner = new Scanner(results);
			Assert.assertTrue(scanner.hasNextLine());
			String firstLine = scanner.nextLine();
			Assert.assertEquals("Header line is as expected", "\"AgResearch_Group\",\"AgResearch_Team\",\"Output_Type\",\"Output_Subtype\",\"Title\",\"Date_Submitted\",\"Citation\",\"AgScite_Handle\"", firstLine);
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			Assert.fail("Caught exception trying to make query / write results to file");
		}
	}

	@Test
	public void testRewriteDates() {
		File input = new File(ClassLoader.getSystemResource("solr-response.csv").getPath());
		File expected = new File(ClassLoader.getSystemResource("rewritten-response.csv").getPath());
		try {
			File rewritten = ReportGenerator.rewriteDates(input);
			System.out.println(rewritten.getAbsolutePath());
			Assert.assertTrue("Rewritten file is not as expected", FileUtils.contentEquals(rewritten, expected));
		} catch (IOException e) {
			Assert.fail("Caught exception trying to rewrite file");
		}
	}
}
