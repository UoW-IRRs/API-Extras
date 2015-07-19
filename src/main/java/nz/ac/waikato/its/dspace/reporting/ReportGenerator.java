package nz.ac.waikato.its.dspace.reporting;

import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.configuration.Field;
import nz.ac.waikato.its.dspace.reporting.configuration.Report;
import nz.ac.waikato.its.dspace.reporting.postprocess.PostProcessingHelper;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;

import javax.mail.MessagingException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for AgResearch
 */
public class ReportGenerator {

	public static final String EMAIL_TEMPLATE_NAME = "uow_reporting_email";

	public static void emailReport(Date start, Date end, String cannedReportName, String recipient, Map<String, List<String>> pickedValues) throws IOException, MessagingException, ReportingException, ConfigurationException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		sdf.setTimeZone(TimeZone.getDefault());

		String configDir = ConfigurationManager.getProperty("dspace.dir") + "/config/modules/reporting";
		String solrServer = ConfigurationManager.getProperty("discovery", "search.server");
		ReportConfigurationService configurationService = new ReportConfigurationService(configDir);
		Report config = configurationService.getCannedReportConfiguration(cannedReportName);
		String reportTitle = config.getTitle();
		InputStream reportDataStream;
		try {
			reportDataStream = queryResultsToFile(config, solrServer, start, end, pickedValues);
		} catch (SolrServerException e) {
			throw new ReportingException("Problem obtaining report data", e);
		}
		Email email;
		try {
			email = Email.getEmail(I18nUtil.getEmailFilename(I18nUtil.getDefaultLocale(), "uow_report_email"));
		} catch (IOException e) {
			throw new ConfigurationException("Cannot find e-mail template " + EMAIL_TEMPLATE_NAME, e);
		}
		String fileName = createFilename(config, start, end);
		email.addAttachment(reportDataStream, fileName+".csv", "text/csv;charset=UTF-8");
		email.addRecipient(recipient);
		email.addArgument(reportTitle);
		email.addArgument(sdf.format(start));
		email.addArgument(sdf.format(end));

		email.send();
	}

	public static void emailReport(Date start, Date end, String cannedReportName, String recipient) throws IOException, MessagingException, ReportingException, ConfigurationException {
		emailReport(start, end, cannedReportName, recipient, null);
	}

	public static InputStream queryResultsToFile(Report config, String solrServer, Date start, Date end, Map<String, List<String>> pickedValues) throws SolrServerException, IOException {
		File tempFile = File.createTempFile("report-solr", ".csv");
		tempFile.deleteOnExit();
		FileUtils.copyURLToFile(config.toQueryURL(solrServer, start, end, pickedValues), tempFile);
		File result = PostProcessingHelper.runPostProcessors(config, tempFile);
		result.deleteOnExit();
		return new BufferedInputStream(new FileInputStream(result));
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Need canned report name, e-mail address as arguments");
			return;
		}
		String reportName = args[0];
		String recipient = args[1];

		try {
			emailReport(null, null, reportName, recipient);
		} catch (IOException | MessagingException | ReportingException | ConfigurationException e) {
			e.printStackTrace(System.err);
		}
	}

    private static String createFilename(Report config, Date start, Date end) {
        if(config == null || start == null || end == null){
            return "agscite-report";
        }
        String reportName = config.getTitle();
        if(reportName == null || reportName.equals("")){
            return "agscite-report";
        }
	    reportName = reportName.toLowerCase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        reportName = toTitleCase(reportName) + sdf.format(start) + "To" + sdf.format(end);
        reportName = reportName.replaceAll("\\s","");
        return reportName;
    }

    private static String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuilder sb = new StringBuilder();

	    for (String anArr : arr) {
		    sb.append(Character.toUpperCase(anArr.charAt(0)))
				    .append(anArr.substring(1)).append(" ");
	    }
        return sb.toString().trim();
    }

	public static List<String> getPickableValues(Report config, Field field) throws ReportingException {
		String solrServer = ConfigurationManager.getProperty("discovery", "search.server");
		return config.getPickableValues(field, solrServer);
	}
}
