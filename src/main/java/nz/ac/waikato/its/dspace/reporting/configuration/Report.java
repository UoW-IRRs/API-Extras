package nz.ac.waikato.its.dspace.reporting.configuration;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.util.DateUtil;

import javax.xml.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
@XmlRootElement(namespace = "nz.ac.waikato.its.dspace.reporting.configuration.ReportsConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"sorting", "dateField", "maxResults", "fields"})
public class Report {
	@XmlAttribute(required = true)
	private String id;
	@XmlElementWrapper(name = "fields")
	@XmlElement(name = "field")
	private List<Field> fields = new ArrayList<>();
	@XmlElement
	private Sorting sorting;
	@XmlElement
	private String dateField;
	@XmlElement(defaultValue = "10000")
	private int maxResults = 10000;

	private static final DateFormat solrDateFormat = DateUtil.getThreadLocalDateFormat();

	static {
		solrDateFormat.setTimeZone(TimeZone.getDefault());
	}

	public Report() { } // no-arg constructor needed by JAXB

	public Report(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
	    this.fields = fields;
	}

	public void addField(Field field) {
		fields.add(field);
	}

	public String getDateField() {
		return dateField;
	}

	public void setDateField(String dateField) {
		this.dateField = dateField;
	}

	public Sorting getSorting() {
		return sorting;
	}

	public void setSorting(Sorting sorting) {
		this.sorting = sorting;
	}

	public URL toQueryURL(String solrServer, Date start, Date end) throws MalformedURLException, UnsupportedEncodingException {
		SolrQuery query = new SolrQuery("*:*");
		// only include live items
		query.addFilterQuery("withdrawn:false");
		query.addFilterQuery("search.resourcetype:2");

		// csv settings
		query.add("wt", "csv");
		query.add("csv.mv.separator", "|");

		if (StringUtils.isNotBlank(dateField)) {
			query.addFilterQuery(dateField + ":[" + dateToSolr(start, false) + " TO " + dateToSolr(end, true) + "]");
		}

		if (sorting != null) {
			query.addSort(sorting.getFieldName(), sorting.getOrder() == Sorting.Order.ASC ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
		}

		for (Field field : fields) {
			String fieldSpec;
			if (StringUtils.isNotBlank(field.getHeader())) {
				fieldSpec = URLEncoder.encode(field.getHeader(), "UTF-8") + ":" + field.getName();
			} else {
				fieldSpec = field.getName();
			}
			query.addField(fieldSpec);
		}

		query.setRows(maxResults);

		return new URL(solrServer + "/select?" + query.toString());
	}

	public URL toQueryURL(String solrServer) throws MalformedURLException, UnsupportedEncodingException {
		return toQueryURL(solrServer, null, null);
	}

	private String dateToSolr(Date date, boolean pushForward) {
		if (date == null) {
			return "*";
		} else if(pushForward){
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.DATE, 1);
			date = c.getTime();
		}
		return solrDateFormat.format(date);
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public int getMaxResults() {
		return maxResults;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Report report = (Report) o;

		if (maxResults != report.maxResults) return false;
		if (dateField != null ? !dateField.equals(report.dateField) : report.dateField != null) return false;
		if (!fields.equals(report.fields)) return false;
		if (!id.equals(report.id)) return false;
		if (sorting != null ? !sorting.equals(report.sorting) : report.sorting != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + fields.hashCode();
		result = 31 * result + (sorting != null ? sorting.hashCode() : 0);
		result = 31 * result + (dateField != null ? dateField.hashCode() : 0);
		result = 31 * result + maxResults;
		return result;
	}

	@Override
	public String toString() {
		return "Report{" +
				       "id='" + id + '\'' +
				       ", fields=" + fields +
				       ", sorting=" + sorting +
				       ", dateField='" + dateField + '\'' +
				       ", maxResults=" + maxResults +
				       '}';
	}
}
