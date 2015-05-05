package nz.ac.waikato.its.dspace.reporting.configuration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportsConfiguration {
	@XmlElementWrapper(name = "cannedReports")
	@XmlElement(name = "report")
	private List<Report> cannedReports = new ArrayList<>();

	public Collection<Report> getCannedReports() {
		return cannedReports;
	}

	public void setCannedReports(List<Report> cannedReports) {
		this.cannedReports = cannedReports;
	}

	public Report getCannedReport(String cannedReportName) {
		for (Report report : cannedReports) {
			if (report.getId().equals(cannedReportName)) {
				return report;
			}
		}
		return null;
	}

	public void addCannedReport(Report report) {
		cannedReports.add(report);
	}
}
