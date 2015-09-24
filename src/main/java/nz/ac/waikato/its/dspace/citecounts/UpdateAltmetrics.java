package nz.ac.waikato.its.dspace.citecounts;

import org.apache.commons.lang.StringUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.SQLException;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the UoW Institutional Research Repositories
 */
public class UpdateAltmetrics {
	private static final String DEFAULT_PROVIDER = "scopus";
	private static final int TIMEOUT = 1000;

	private static Logger log = Logger.getLogger(UpdateAltmetrics.class);

	public static void main(String[] args) {
		String provider = DEFAULT_PROVIDER;
		if (args.length > 0 && StringUtils.isNotBlank(args[0])) {
			provider = args[0];
		}

		String url = ConfigurationManager.getProperty("api-extras", String.format("citecounts.%s.url", provider));
		if (StringUtils.isBlank(url)) {
			System.err.println("No URL found in api-extras config file for provider " + provider);
			return;
		}

		Context context = null;
		try {
			context = new Context(Context.READ_ONLY);
			context.ignoreAuthorization();

			CloseableHttpClient client = HttpClients.createDefault();
			RequestConfig config = RequestConfig.custom().setConnectTimeout(TIMEOUT).build();

			ItemIterator items = Item.findByMetadataField(context, "dc", "identifier", "doi", Item.ANY);

			while (items.hasNext()) {
				Item item = items.next();

				String handle = item.getHandle();
				if (StringUtils.isBlank(handle)) {
					item.decache();
					continue;
				}

				String doi = getFirstNonBlankValue(item.getMetadata("dc", "identifier", "doi", Item.ANY));
				if (StringUtils.isBlank(doi)) {
					item.decache();
					continue;
				}

				HttpGet get = new HttpGet();
				get.setConfig(config);
				URI uri = null;
				try {
					uri = new URIBuilder(url).addParameter("provider", provider).addParameter("handle", handle).addParameter("param_doi", doi).build();
				} catch (URISyntaxException e) {
					log.fatal("Cannot construct query url when retrieving altmetrics for item handle=" + handle + ", provider=" + provider + ": " + e.getMessage(), e);
				}
				get.setURI(uri);

				try (CloseableHttpResponse response = client.execute(get)) {
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() == 200) {
						log.info("Retrieved altmetrics for item handle=" + handle + ", provider=" + provider);
					} else {
						log.warn("Non-OK status code (" + status.getStatusCode() + "; " + status.getReasonPhrase() + ") when retrieving altmetrics for item handle=" + handle + ", provider=" + provider);
					}
				}

				item.decache();
			}
		} catch (SQLException | AuthorizeException | IOException e) {
			log.error("Cannot fetch/process altmetrics: " + e.getMessage(), e);
		} finally {
			if (context != null && context.isValid()) {
				context.abort();
			}
		}
	}

	private static String getFirstNonBlankValue(Metadatum[] values) {
		for (Metadatum value : values) {
			if (value != null && StringUtils.isNotBlank(value.value)) {
				return value.value;
			}
		}
		return null;
	}
}
