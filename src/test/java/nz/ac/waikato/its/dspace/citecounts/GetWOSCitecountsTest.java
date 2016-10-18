package nz.ac.waikato.its.dspace.citecounts;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.madgag.hamcrest.FileExistenceMatcher.exists;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.xmlmatchers.XmlMatchers.isEquivalentTo;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.xpath.XpathReturnType.returningANumber;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the UoW Institutional Research Repositories
 */
public class GetWOSCitecountsTest {
	@Test public void verifyResponse() throws Exception {
		InputStream response = postXMLRequest(ClassLoader.getSystemResourceAsStream("citecounts/TestRequest.xml"), GetWOSCitecounts.WOS_SERVICE_URL);
		assertThat(response, is(notNullValue()));

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(response);
		Node responseNode = document.getDocumentElement();

		NamespaceContext namespace = new SimpleNamespaceContext().withBinding("i", "http://www.isinet.com/xrpc42");

		// check overall structure & response code are ok
		assertThat(responseNode,
				hasXPath("/i:response/i:fn[@name='LinksAMR.retrieve' and @rc='OK']",
						namespace));

		// check that there are responses for 50 handles
		assertThat(responseNode,
				hasXPath("count(/i:response/i:fn/i:map/i:map[starts-with(@name, '10289_')])",
						namespace,
						equalTo("50")));

		// check that there is at least one response with the expected elements - sourceURL, timesCited and citingArticlesURL
		assertThat(responseNode,
				hasXPath("/i:response/i:fn/i:map/i:map[starts-with(@name, '10289_')]/i:map[@name='WOS']/i:val[@name='sourceURL']/text()",
						namespace,
						not(isEmptyString())));
		assertThat(responseNode,
				hasXPath("/i:response/i:fn/i:map/i:map[starts-with(@name, '10289_')]/i:map[@name='WOS']/i:val[@name='timesCited']/text()",
						namespace,
						not(isEmptyString())));
		assertThat(responseNode,
				hasXPath("/i:response/i:fn/i:map/i:map[starts-with(@name, '10289_')]/i:map[@name='WOS']/i:val[@name='citingArticlesURL']/text()",
						namespace,
						not(isEmptyString())));
	}

	private InputStream postXMLRequest(InputStream data, String serviceUrl) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5_000).build();
		HttpPost method = new HttpPost(serviceUrl);
		method.setConfig(config);
		method.setEntity(new InputStreamEntity(data));
		method.setHeader("Content-type", "text/xml; charset=UTF-8");

		String resultString = null;
		try (CloseableHttpResponse response = client.execute(method)) {
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		}
		return resultString != null ? IOUtils.toInputStream(resultString, "UTF-8") : null;
	}

	@Test public void verifyConversion() throws Exception {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getDefault());

		InputStream response = ClassLoader.getSystemResourceAsStream("citecounts/TestResponse.xml");
		File transformedResponse = File.createTempFile("transformed", "xml");
		transformedResponse.deleteOnExit();

		FileWriter writer = new FileWriter(transformedResponse);
		GetWOSCitecounts.processResponse(response, writer);
		writer.flush();
		writer.close();
		assertThat(transformedResponse, exists());

		String generatedXml = IOUtils.toString(new FileInputStream(transformedResponse));
		assertThat(generatedXml, not(isEmptyString()));

		String storedTransformed = IOUtils.toString(ClassLoader.getSystemResourceAsStream("citecounts/TestResponseTransformed.xml"));

		// update last updated date to today's date
		storedTransformed = StringUtils.replaceOnce(storedTransformed, " last-updated=\"2016-10-11\"", " last-updated=\"" + sdf.format(date) + "\"");
		assertThat(the(generatedXml), isEquivalentTo(the(storedTransformed)));

		transformedResponse.delete();
	}


}
