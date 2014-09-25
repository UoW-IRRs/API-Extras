package nz.ac.waikato.its.dspace.reporting;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.util.DateUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportConfiguration {
	private SolrQuery query;
	private String baseURL;
	private static final DateFormat solrDateFormat = DateUtil.getThreadLocalDateFormat();

	static {
		solrDateFormat.setTimeZone(TimeZone.getDefault());
	}

	public ReportConfiguration() {
		query = new SolrQuery("*:*");

		// only include live items
		query.addFilterQuery("withdrawn:false");
		query.addFilterQuery("search.resourcetype:2");

		// csv settings
		query.add("wt", "csv");
		query.add("csv.mv.separator", "|");
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public void setMaxResults(int maxResults) {
		query.setRows(maxResults);
	}

	public void addResultField(String field, String heading) throws UnsupportedEncodingException {
		if (StringUtils.isNotBlank(field)) {
			String fieldSpec;
			if (StringUtils.isNotBlank(heading)) {
				fieldSpec = URLEncoder.encode(heading, "UTF-8") + ":" + field;
			} else {
				fieldSpec = field;
			}
			query.addField(fieldSpec);
		}
	}

	public void setDateRange(String dateField, Date start, Date end) {
		if (StringUtils.isNotBlank(dateField)) {
			query.addFilterQuery(dateField + ":[" + dateToSolr(start) + " TO " + dateToSolr(end) + "]");
		}
	}

	public void setSortField(String field, boolean asc) {
		if (StringUtils.isNotBlank(field)) {
			query.addSort(field, asc ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
		}
	}

	public URL getQueryURL() throws MalformedURLException {
		return new URL(baseURL + "/select?" + query.toString());
	}

	private String dateToSolr(Date date) {
		if (date == null) {
			return "*";
		}
		return solrDateFormat.format(date);
	}
}
