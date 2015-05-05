package nz.ac.waikato.its.dspace.reporting.configuration;

import nz.ac.waikato.its.dspace.reporting.ReportingException;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.DateUtil;

import javax.xml.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch
 */
@XmlRootElement(namespace = "nz.ac.waikato.its.dspace.reporting.configuration.ReportsConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"title", "sorting", "dateField", "maxResults", "fields"})
public class Report {
	@XmlAttribute(required = true)
	private String id;
	@XmlElementWrapper(name = "fields")
	@XmlElement(name = "field")
	private List<Field> fields = new ArrayList<>();
    @XmlElement
    private String title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

	public URL toQueryURL(String solrServer, Date start, Date end) throws MalformedURLException, UnsupportedEncodingException {
		return toQueryURL(solrServer, start, end, null);
	}

	public URL toQueryURL(String solrServer, Date start, Date end, Map<String, List<String>> pickedValues) throws MalformedURLException, UnsupportedEncodingException {
		SolrQuery query = getBasicSolrQuery();

		if (pickedValues != null && !pickedValues.isEmpty()) {
			for (String field : pickedValues.keySet()) {
				List<String> values = pickedValues.get(field);
				if (values == null || values.isEmpty()) {
					continue; // skip this field
				}
				StringBuilder filterBuilder = new StringBuilder("(");
				boolean first = true;
				for (String value : values) {
					if (first) {
						first = false;
					} else {
						filterBuilder.append(" OR ");
					}
					filterBuilder.append("\"");
					filterBuilder.append(value);
					filterBuilder.append("\"");
				}
				filterBuilder.append(")");
				query.addFilterQuery(field + ":" + filterBuilder.toString());
			}
		}

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

	private SolrQuery getBasicSolrQuery() {
		SolrQuery query = new SolrQuery("*:*");
		// only include live items
		query.addFilterQuery("withdrawn:false");
		query.addFilterQuery("search.resourcetype:2");
		return query;
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
        if (!title.equals(report.title)) return false;
		if (sorting != null ? !sorting.equals(report.sorting) : report.sorting != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + fields.hashCode();
		result = 31 * result + (sorting != null ? sorting.hashCode() : 0);
		result = 31 * result + (dateField != null ? dateField.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + maxResults;
		return result;
	}

	@Override
	public String toString() {
		return "Report{" +
				       "id='" + id + '\'' +
                       ", title=" + title +
				       ", fields=" + fields +
				       ", sorting=" + sorting +
				       ", dateField='" + dateField + '\'' +
				       ", maxResults=" + maxResults +
				       '}';
	}

	public List<String> getPickableValues(Field field, String solrServer) throws ReportingException {
		if (!fields.contains(field)) {
			throw new IllegalArgumentException("This report doesn't contain the specified field " + field);
		}
		if (field.getValuesMode() != Field.ValuesMode.PICK) {
			throw new IllegalArgumentException("This field isn't pickable " + field);
		}

		List<String> result = new ArrayList<>();

		HttpSolrServer solr = new HttpSolrServer(solrServer);
		SolrQuery query = toPickableValuesQuery(field.getName());
		try {
			QueryResponse response = solr.query(query);
			FacetField facetField = response.getFacetField(field.getName());
			List<FacetField.Count> values = facetField.getValues();
			for (FacetField.Count value : values) {
				result.add(value.getName());
			}
			Collections.sort(result);
		} catch (SolrServerException e) {
			throw new ReportingException("Could not determine pickable values", e);
		}
		return result;
	}

	SolrQuery toPickableValuesQuery(String name) {
		SolrQuery query = getBasicSolrQuery();
		query.setFacet(true);
		query.setFacetMinCount(1);
		query.setFacetLimit(maxResults);
		query.addFacetField(name);
		return query;
	}

}
