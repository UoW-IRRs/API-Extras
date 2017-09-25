package nz.ac.waikato.its.dspace.events;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.*;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.handle.HandleManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz
 * for the University of Waikato's Institutional Research Repositories
 */
public class NotifyOnArchiveWhenMetadataPresent implements Consumer {
    private static final Logger log = Logger.getLogger(NotifyOnArchiveWhenMetadataPresent.class);

    private String metadataField;
    private List<String> containerHandles = new ArrayList<>();
    private String templateName;
    private List<String> recipients = new ArrayList<>();

    @Override
    public void initialize() throws Exception {
        metadataField = ConfigurationManager.getProperty("api-extras", "notify_archive_with-metadata.field");
        templateName = ConfigurationManager.getProperty("api-extras", "notify_archive_with-metadata.template");
        String recipientsProp = ConfigurationManager.getProperty("api-extras", "notify_archive_with-metadata.recipients");
        if (StringUtils.isNotBlank(recipientsProp)) {
            recipients = Arrays.asList(recipientsProp.split(",\\s*"));
        }
        String containersProp = ConfigurationManager.getProperty("api-extras", "notify_archive_with-metadata.parents");
        if (StringUtils.isNotBlank(containersProp)) {
            containerHandles = Arrays.asList(containersProp.split(",\\s*"));
        }
    }

    @Override
    public void consume(Context ctx, Event event) throws Exception {
        if (event.getSubjectType() != Constants.ITEM || event.getEventType() != Event.INSTALL) {
            // wrong event, don't do anything
            return;
        }
        if (StringUtils.isBlank(metadataField) || StringUtils.isBlank(templateName) || recipients.isEmpty()) {
            log.warn("Metadata field and/or template and/or recipients not configured, not notifying");
            return;
        }
        Item item = (Item) event.getSubject(ctx);
        if (!itemPassesContainerConstraint(item, containerHandles)) {
            // item isn't in a collection we're interested in
            return;
        }

        Metadatum[] metadata = item.getMetadataByMetadataString(metadataField);
        if (metadata == null || metadata.length == 0) {
            // item doesn't have the metadata that we're looking for
            return;
        }

        List<String> values = new ArrayList<>();
        for (Metadatum metadatum : metadata) {
            if (StringUtils.isNotBlank(metadatum.value)) {
                values.add(metadatum.value);
            }
        }

        String itemHandle = HandleManager.getCanonicalForm(item.getHandle());

        if (values.isEmpty() || StringUtils.isBlank(itemHandle)) {
            log.warn("No values for field " + metadataField + " or item has no handle, not notifying");
            return;
        }

        String emailFileName = I18nUtil.getEmailFilename(Locale.getDefault(), templateName);
        if (StringUtils.isBlank(emailFileName)) {
            log.error("Cannot obtain email filename for template " + templateName + " and locale " + Locale.getDefault());
            return;
        }

        try {
            Email email = Email.getEmail(emailFileName);

            for (String recipient : recipients) {
                email.addRecipient(recipient);
            }

            email.addArgument(itemHandle);
            email.addArgument(StringUtils.join(values, "; "));

            email.send();
        } catch (Exception e) {
            log.error("Caught exception while trying to send notification: " + e.getMessage(), e);
        }
    }

    private boolean itemPassesContainerConstraint(Item item, List<String> containerHandles) throws SQLException {
        if (containerHandles.isEmpty()) {
            return true;
        }

        Community[] itemCommunities = item.getCommunities();
        for (Community community : itemCommunities) {
            if (containerHandles.contains(community.getHandle())) {
                return true;
            }
        }
        Collection[] itemCollections = item.getCollections();
        for (Collection collection : itemCollections) {
            if (containerHandles.contains(collection.getHandle())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void end(Context ctx) throws Exception {
        // no-op
    }

    @Override
    public void finish(Context ctx) throws Exception {
        // no-op
    }
}
