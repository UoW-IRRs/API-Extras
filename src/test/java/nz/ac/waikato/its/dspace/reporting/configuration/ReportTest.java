package nz.ac.waikato.its.dspace.reporting.configuration;

import nz.ac.waikato.its.dspace.reporting.ReportingException;
import nz.ac.waikato.its.dspace.reporting.TestUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportTest {
	@Test
	public void toQueryUrlTest() {

		Report config = TestUtils.makeReport1();
		Assert.assertNotNull("Report config non-null", config);
		URL queryURL = null;
		try {
			queryURL = config.toQueryURL("http://127.0.0.1:8080/solr/search");
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			e.printStackTrace();
			Assert.fail("Caught exception getting query URL");
		}
		Assert.assertNotNull("Got non-null query URL object", queryURL);
		Assert.assertEquals("Query string is as expected", "http://127.0.0.1:8080/solr/search/select?q=*%3A*&fq=withdrawn%3Afalse&fq=search.resourcetype%3A2&fq=dc.date.accessioned_dt%3A%5B*+TO+*%5D&wt=csv&csv.mv.separator=%7C&sort=dc.date.accessioned_dt+asc&fl=AgResearch_Group%3Aagresearch.organisation.group%2CAgResearch_Team%3Aagresearch.organisation.team%2COutput_Type%3Adc.type%2COutput_Subtype%3Aagresearch.subtype%2CTitle%3Adc.title%2CDate_Submitted%3Adc.date.accessioned_dt%2CCitation%3Adc.identifier.citation%2CAgScite_Handle%3Ahandle&rows=10000", queryURL.toString());
	}


	@Test
	public void testToPickableValuesQuery() {
		Report config = new Report("test");
		Field field = new Field("group_keyword", "AgResearch Group", Field.ValuesMode.PICK);
		config.addField(field);
		SolrQuery query = config.toPickableValuesQuery(field.getName());
		Assert.assertNotNull("Got non-null query object", query);
		Assert.assertEquals("Query is as expected", "q=*%3A*&fq=withdrawn%3Afalse&fq=search.resourcetype%3A2&facet=true&facet.mincount=1&facet.limit=10000&facet.field=group_keyword", query.toString());
	}
}
