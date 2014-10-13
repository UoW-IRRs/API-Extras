package nz.ac.waikato.its.dspace.reporting.postprocess;

import nz.ac.waikato.its.dspace.reporting.configuration.ConfigurationException;
import nz.ac.waikato.its.dspace.reporting.configuration.PostProcess;
import org.dspace.handle.HandleManager;

/**
 * @author Stefan Mutter (stefanm@waikato.ac.nz) for University of Waikato, ITS
 */
public class HandleToCanonicalForm implements PostProcessor {

    @Override
    public void setConfig(PostProcess config) throws ConfigurationException {
    }

    @Override
    public String[] processLine(String[] inputLine, int index) throws ConfigurationException, PostProcessingException {
        String[] result = inputLine.clone();
        result[index] = HandleManager.getCanonicalForm(inputLine[index]);
        return result;
    }
}
