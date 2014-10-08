package nz.ac.waikato.its.dspace.reporting.postprocess;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.configuration.Field;
import nz.ac.waikato.its.dspace.reporting.configuration.PostProcess;
import nz.ac.waikato.its.dspace.reporting.configuration.Report;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the LCoNZ Institutional Research Repositories
 */
public class PostProcessingHelper {
	public static PostProcessor getInstance(PostProcess config) throws ConfigurationException {
		if (config == null) {
			return null;
		}
		try {
			PostProcessor instance = (PostProcessor) Class.forName(config.getClassName()).newInstance();
			if (instance != null) {
				instance.setConfig(config);
			}
			return instance;
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace(System.err);
		}
		return null;
	}

	public static File runPostProcessors(Report config, File file) throws IOException {
		PostProcessor[] processors = initPostProcessors(config);

		File rewrittenFile = File.createTempFile("report", ".csv");
		CSVWriter writer = new CSVWriter(new FileWriter(rewrittenFile));
		try (CSVReader reader = new CSVReader(new FileReader(file))) {
			boolean firstLine = true;
			String[] line = reader.readNext();
			while (line != null) {
				if (firstLine) {
					firstLine = false;
				} else {
					for (int i = 0; i < line.length; i++) {
						if (processors[i] != null) {
							try {
								line = processors[i].processLine(line, i);
							} catch (ConfigurationException | PostProcessingException e) {
								e.printStackTrace(System.err);
							}
						}
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

	static PostProcessor[] initPostProcessors(Report config) {
		List<Field> fields = config.getFields();
		PostProcessor[] processors = new PostProcessor[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			if (field.getPostProcess() != null) {
				try {
					processors[i] = getInstance(field.getPostProcess());
				} catch (ConfigurationException e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return processors;
	}
}
