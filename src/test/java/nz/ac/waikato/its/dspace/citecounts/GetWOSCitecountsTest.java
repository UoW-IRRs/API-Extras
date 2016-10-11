package nz.ac.waikato.its.dspace.citecounts;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.*;

import static com.madgag.hamcrest.FileExistenceMatcher.exists;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.xmlmatchers.XmlMatchers.isEquivalentTo;
import static org.xmlmatchers.transform.XmlConverters.the;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the UoW Institutional Research Repositories
 */
public class GetWOSCitecountsTest {
	@Test public void verifyResponse() throws Exception {
		InputStream response = postXMLRequest(ClassLoader.getSystemResourceAsStream("citecounts/TestRequest.xml"), GetWOSCitecounts.WOS_SERVICE_URL);
		assertThat(response, is(notNullValue()));

		String responseStream = IOUtils.toString(response);
		String storedResponse = IOUtils.toString(ClassLoader.getSystemResourceAsStream("citecounts/TestResponse.xml"));

		assertThat(the(responseStream), isEquivalentTo(the(storedResponse)));
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
		assertThat(the(generatedXml), isEquivalentTo(the(storedTransformed)));

		transformedResponse.delete();
	}


}
