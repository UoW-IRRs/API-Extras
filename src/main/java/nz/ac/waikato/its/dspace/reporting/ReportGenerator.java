package nz.ac.waikato.its.dspace.reporting;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.DateUtil;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;

import javax.mail.MessagingException;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class ReportGenerator {

	public static final String EMAIL_TEMPLATE_NAME = "uow_reporting_email";

	public static void emailReport(Date start, Date end, String cannedReportName, String recipient) throws IOException, MessagingException, ReportingException, ReportingConfigurationException {
		ReportConfigurationService configurationService = new ReportConfigurationService(ConfigurationManager.getProperty("discovery", "search.server"));
		ReportConfiguration config = configurationService.getCannedReportConfiguration(start, end, cannedReportName);
		InputStream reportDataStream;
		try {
			reportDataStream = queryResultsToFile(config);
		} catch (SolrServerException e) {
			throw new ReportingException("Problem obtaining report data", e);
		}
		Email email = Email.getEmail(I18nUtil.getEmailFilename(I18nUtil.getDefaultLocale(), "uow_report_email"));
		if (email == null) {
			throw new ReportingConfigurationException("Cannot find e-mail template " + EMAIL_TEMPLATE_NAME);
		}
		email.addAttachment(reportDataStream, "agscite-report.csv", "text/csv;charset=UTF-8");
		email.addRecipient(recipient);
		email.addArgument(cannedReportName);
		email.addArgument(start);
		email.addArgument(end);

		email.send();
	}

	public static InputStream queryResultsToFile(ReportConfiguration config) throws SolrServerException, IOException {
		File tempFile = File.createTempFile("report-solr", ".csv");
		tempFile.deleteOnExit();
		FileUtils.copyURLToFile(config.getQueryURL(), tempFile);
		tempFile = rewriteDates(tempFile);
		return new BufferedInputStream(new FileInputStream(tempFile));
	}

	static File rewriteDates(File file) throws IOException {
		DateFormat solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		solrDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		DateFormat excelDateFormat = new SimpleDateFormat("MMMMM yyyy");//SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
		excelDateFormat.setTimeZone(TimeZone.getDefault());

		File rewrittenFile = File.createTempFile("report", ".csv");
		rewrittenFile.deleteOnExit();
		CSVWriter writer = new CSVWriter(new FileWriter(rewrittenFile));

		try (CSVReader reader = new CSVReader(new FileReader(file))) {
			String[] line = reader.readNext();
			while (line != null) {
				for (int i = 0; i < line.length; i++) {
					String field = line[i];
					try {
						Date date = solrDateFormat.parse(field);
						line[i] = excelDateFormat.format(date);
					} catch (ParseException e) {
						// not a date, ignore
					}
				}
				writer.writeNext(line);
				line = reader.readNext();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		return rewrittenFile;
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
		} catch (IOException | MessagingException | ReportingException | ReportingConfigurationException e) {
			e.printStackTrace(System.err);
		}
	}
}
