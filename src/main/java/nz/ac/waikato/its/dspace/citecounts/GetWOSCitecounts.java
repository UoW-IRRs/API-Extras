package nz.ac.waikato.its.dspace.citecounts;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Andrea Schweer schweer@waikato.ac.nz for the UoW Institutional Research Repositories
 */
public class GetWOSCitecounts {

	public static final int CHUNK_SIZE = 50;
	public static final String WOS_SERVICE_URL = "https://ws.isiknowledge.com/cps/xrpc";


	private static Logger log = Logger.getLogger(GetWOSCitecounts.class);


	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: GetWOSCitecounts outfile");
			return;
		}
		
		File destination = new File(args[0]);
		if (!destination.canWrite()) {
			log.error("Cannot write to destination file " + args[0]);
			return;
		}
		if (destination.exists()) {
			log.warn("Destination file " + args[0] + " exists already; overwriting");
		} else {
			try {
				//noinspection ResultOfMethodCallIgnored
				destination.createNewFile();
			} catch (IOException e) {
				log.error("Cannot create output file " + args[0], e);
				return;
			}
		}
		
		Context context = null;
		PrintWriter outputWriter = null;

		try {
			context = new Context(Context.READ_ONLY);
			context.ignoreAuthorization();
			ItemIterator items = Item.findByMetadataField(context, "dc", "identifier", "doi", Item.ANY);

			File tmpFile = File.createTempFile("wos-lookup", null);
			tmpFile.deleteOnExit();

			outputWriter = new PrintWriter(new BufferedWriter(new FileWriter(tmpFile)));

			final PrintWriter finalOutputWriter = outputWriter;
			finalOutputWriter.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
			finalOutputWriter.println("<results>");

			List<Item> chunkList = new ArrayList<Item>(CHUNK_SIZE);
			int chunkNo = 1;
			while (items.hasNext()) {
				if (chunkList.size() < CHUNK_SIZE) {
					Item item = items.next();
					if (!item.isWithdrawn() || item.getHandle() == null || "".equals(item.getHandle())) {
						chunkList.add(item);
					}
				} else {
					final List<Item> chunkCopy = chunkList;
					final int finalChunkNo = chunkNo;
					try {
						processChunk(finalChunkNo, chunkCopy, WOS_SERVICE_URL, finalOutputWriter);
					} catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
						log.error("Problem processing chunk: " + e, e);
					}
					chunkList = new ArrayList<>(CHUNK_SIZE);
					chunkNo++;
				}
			}
			if (!chunkList.isEmpty()) {
				try {
					processChunk(chunkNo, chunkList, WOS_SERVICE_URL, outputWriter);
				} catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
					log.error("Problem processing chunk: " + e, e);
				}
			}

			outputWriter.println("</results>");
			outputWriter.flush();
			outputWriter.close();
			outputWriter = null;

			//noinspection ResultOfMethodCallIgnored
			tmpFile.renameTo(destination);
		} catch (SQLException | AuthorizeException | IOException e) {
			log.error("Cannot fetch/process cite counts: " + e.getMessage(), e);
		} finally {
			if (context != null) {
				context.abort();
			}
			if (outputWriter != null) {
				outputWriter.flush();
				outputWriter.close();
			}
		}
	}

	private static void processChunk(int chunkNo, List<Item> chunk, String url, PrintWriter outputWriter) throws IOException, ParserConfigurationException, TransformerException, SAXException {
		String requestXML = buildRequestXML(chunk);

		CloseableHttpClient client = HttpClients.createDefault();
		RequestConfig config = RequestConfig.custom().setConnectTimeout(5_000).build();
		HttpPost method = new HttpPost(url);
		method.setConfig(config);

		byte[] xmlBytes = requestXML.getBytes("UTF-8");

		method.setEntity(new InputStreamEntity(new ByteArrayInputStream(xmlBytes), xmlBytes.length));
		method.setHeader("Content-type", "text/xml; charset=UTF-8");


		log.info("chunk " + chunkNo + ": sending request");
		try (CloseableHttpResponse response = client.execute(method)) {
			int result = response.getStatusLine().getStatusCode();
			log.info("chunk " + chunkNo + ": response code is " + result);

			if (result == 200) {
				InputStream stream = new ByteArrayInputStream(EntityUtils.toByteArray(response.getEntity()));
				processResponse(stream, outputWriter);
				log.info("wrote chunk no " + chunkNo + " to output");
			}
		}

	}

	static String buildRequestXML(List<Item> items) throws ParserConfigurationException, TransformerException {
		String ns = "http://www.isinet.com/xrpc42";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element request = doc.createElementNS(ns, "request");
		doc.appendChild(request);
		Element fn = doc.createElementNS(ns, "fn");
		fn.setAttribute("name", "LinksAMR.retrieve");
		request.appendChild(fn);
		Element list = doc.createElementNS(ns, "list");
		fn.appendChild(list);

		Element whoMap = doc.createElementNS(ns, "map");
		list.appendChild(whoMap);

		Element whatMap = doc.createElementNS(ns, "map");
		list.appendChild(whatMap);
		Element whatMapList = doc.createElementNS(ns, "list");
		whatMapList.setAttribute("name", "WOS");
		whatMap.appendChild(whatMapList);
		String[] vals = new String[] { "timesCited", "sourceURL", "citingArticlesURL"};
		for (String val : vals) {
			Element valNode = doc.createElementNS(ns, "val");
			valNode.setTextContent(val);
			whatMapList.appendChild(valNode);
		}

		Element dataMap = doc.createElementNS(ns, "map");
		list.appendChild(dataMap);
		for (Item item : items) {
			String doi = null;
			Metadatum[] doiValues = item.getMetadata("dc", "identifier", "doi", Item.ANY);
			if (doiValues != null && doiValues.length > 0) {
				doi = doiValues[0].value;
			}
			if (doi == null || "".equals(doi.trim())) {
				continue;
			}

			Element citeID = doc.createElementNS(ns, "map");
			citeID.setAttribute("name", item.getHandle().replace('/', '_'));
			dataMap.appendChild(citeID);
			
			Element doiNode = doc.createElementNS(ns, "val");
			doiNode.setAttribute("name", "doi");
			doiNode.setTextContent(doi);
			citeID.appendChild(doiNode);
		}

		DOMSource source = new DOMSource(doc);
		StringWriter xmlAsWriter = new StringWriter();

		StreamResult result = new StreamResult(xmlAsWriter);

		TransformerFactory.newInstance().newTransformer().transform(source, result);

		return xmlAsWriter.toString();
	}

	static void processResponse(InputStream input, Writer outputWriter) throws ParserConfigurationException, IOException, SAXException, TransformerException {
		Source xmlSource = new StreamSource(input);
		Source xsltSource = new StreamSource(ClassLoader.getSystemResourceAsStream("xsl/WOSResponse.xsl"));

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer(xsltSource);
		transformer.setParameter("service", "WOS");
		transformer.setParameter("last-updated", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

		synchronized (outputWriter) {
			transformer.transform(xmlSource, new StreamResult(outputWriter));
			outputWriter.flush();
		}
	}
}
