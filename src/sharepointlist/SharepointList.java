/**
 * (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package sharepointlist;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sap.hana.dp.adapter.sdk.Adapter;
import com.sap.hana.dp.adapter.sdk.AdapterConstant.AdapterCapability;
import com.sap.hana.dp.adapter.sdk.AdapterConstant.DataType;
import com.sap.hana.dp.adapter.sdk.AdapterException;
import com.sap.hana.dp.adapter.sdk.AdapterRow;
import com.sap.hana.dp.adapter.sdk.AdapterRowSet;
import com.sap.hana.dp.adapter.sdk.BrowseNode;
import com.sap.hana.dp.adapter.sdk.CallableProcedure;
import com.sap.hana.dp.adapter.sdk.Capabilities;
import com.sap.hana.dp.adapter.sdk.Column;
import com.sap.hana.dp.adapter.sdk.CredentialEntry;
import com.sap.hana.dp.adapter.sdk.CredentialProperties;
import com.sap.hana.dp.adapter.sdk.DataDictionary;
import com.sap.hana.dp.adapter.sdk.FunctionMetadata;
import com.sap.hana.dp.adapter.sdk.Metadata;
import com.sap.hana.dp.adapter.sdk.Parameter;
import com.sap.hana.dp.adapter.sdk.ParametersResponse;
import com.sap.hana.dp.adapter.sdk.ProcedureMetadata;
import com.sap.hana.dp.adapter.sdk.PropertyEntry;
import com.sap.hana.dp.adapter.sdk.PropertyGroup;
import com.sap.hana.dp.adapter.sdk.RemoteObjectsFilter;
import com.sap.hana.dp.adapter.sdk.RemoteSourceDescription;
import com.sap.hana.dp.adapter.sdk.StatementInfo;
import com.sap.hana.dp.adapter.sdk.TableMetadata;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * SharepointList Adapter.
 */
public class SharepointList extends Adapter {

	static Logger logger = LogManager.getLogger("SharepointList");
	private String name = null;
	private String hostName = null;
	private String listName = null;
	private boolean alreadySent = false;
	private CloseableHttpClient httpclient;

	@Override
	public void beginTransaction() throws AdapterException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<BrowseNode> browseMetadata() throws AdapterException {

		List<BrowseNode> nodes = new ArrayList<BrowseNode>();
		BrowseNode node = new BrowseNode(listName, listName);
		node.setImportable(true);
		node.setExpandable(false);
		nodes.add(node);
		return nodes;
	}

	@Override
	public void close() throws AdapterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void commitTransaction() throws AdapterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeStatement(String sql, StatementInfo info) throws AdapterException {
		alreadySent = false;
	}

	@Override
	public Capabilities<AdapterCapability> getCapabilities(String version) throws AdapterException {
		Capabilities<AdapterCapability> capbility = new Capabilities<AdapterCapability>();
		capbility.setCapability(AdapterCapability.CAP_SELECT);
		return capbility;
	}

	@Override
	public int getLob(long lobId, byte[] bytes, int bufferSize) throws AdapterException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void getNext(AdapterRowSet rows) throws AdapterException {
		/**
		 * Currently this function gets until it returns a empty row set.
		 */
		if (alreadySent)
			return;
		/**
		 * Lets return a single row with 1, hello user data.
		 */

		HttpGet httpget = new HttpGet("http://"+ hostName +"/_api/web/lists/GetByTitle('"+listName+"')/items");
		TableMetadata table = new TableMetadata();
		try {
			CloseableHttpResponse response = httpclient.execute(httpget);

			String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			StringBuilder xmlStringBuilder = new StringBuilder();
			xmlStringBuilder.append(responseBody);

			ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			Document doc = null;
			try {
				doc = builder.parse(input);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Element root = doc.getDocumentElement();

			NodeList flowList = doc.getElementsByTagName("m:properties");
			for (int i = 0; i < flowList.getLength(); i++) {
				NodeList childList = flowList.item(i).getChildNodes();
				AdapterRow row = rows.newRow();
				for (int j = 0; j < childList.getLength(); j++) {
					Node childNode = childList.item(j);
					row.setColumnValue(j, childList.item(j).getTextContent());

				}
			}
		} catch (ClientProtocolException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} finally {

		}
		alreadySent = true;
	}

	@Override
	public RemoteSourceDescription getRemoteSourceDescription() throws AdapterException {
		RemoteSourceDescription rs = new RemoteSourceDescription();

		PropertyGroup connectionInfo = new PropertyGroup("testParam", "Connection Parameters", "Connection Parameters");
		PropertyEntry hostNameProperty = new PropertyEntry("hostName", "Host name");
		hostNameProperty.setIsRequired(true);
		connectionInfo.addProperty(hostNameProperty);
		PropertyEntry listNameProperty = new PropertyEntry("listName", "List title");
		listNameProperty.setIsRequired(true);
		connectionInfo.addProperty(listNameProperty);
		connectionInfo.addProperty(new PropertyEntry("worksation", "Worksation"));
		connectionInfo.addProperty(new PropertyEntry("domain", "Domain"));
		

		CredentialProperties credentialProperties = new CredentialProperties();
		CredentialEntry credential = new CredentialEntry("credential", "Sharepoint user credentials");
		credential.getUser().setDisplayName("Username");
		credential.getPassword().setDisplayName("Password");
		credentialProperties.addCredentialEntry(credential);

		rs.setCredentialProperties(credentialProperties);
		rs.setConnectionProperties(connectionInfo);
		return rs;
	}

	@Override
	public String getSourceVersion(RemoteSourceDescription remoteSourceDescription) throws AdapterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metadata importMetadata(String nodeId) throws AdapterException {

		HttpGet httpget = new HttpGet("http://"+ hostName +"/_api/web/lists/GetByTitle('"+listName+"')/items");
		TableMetadata table = new TableMetadata();
		try {
			CloseableHttpResponse response = httpclient.execute(httpget);

			String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			StringBuilder xmlStringBuilder = new StringBuilder();
			xmlStringBuilder.append(responseBody);

			ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
			Document doc = null;
			try {
				doc = builder.parse(input);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Element root = doc.getDocumentElement();

			NodeList flowList = doc.getElementsByTagName("m:properties");
			NodeList childList2 = flowList.item(1).getChildNodes();
			List<Column> schema = new ArrayList<Column>();

			for (int j = 0; j < childList2.getLength(); j++) {

				schema.add(new Column(childList2.item(j).getNodeName().substring(2), DataType.VARCHAR, 256));
			}

			table.setName(nodeId);
			table.setColumns(schema);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return table;

	}

	@Override
	public void open(RemoteSourceDescription connectionInfo, boolean isCDC) throws AdapterException {

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		System.out.println(credsProvider.toString());

		String username = "";
		String password = "";
		try {
			username = new String(
					connectionInfo.getCredentialProperties().getCredentialEntry("credential").getUser().getValue(),
					"UTF-8");
			password = new String(
					connectionInfo.getCredentialProperties().getCredentialEntry("credential").getPassword().getValue(),
					"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			throw new AdapterException(e1, e1.getMessage());
		}

		credsProvider.setCredentials(new AuthScope(AuthScope.ANY), new NTCredentials(username, password, "", ""));

		httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		hostName = connectionInfo.getConnectionProperties().getPropertyEntry("hostName").getValue();
		listName = connectionInfo.getConnectionProperties().getPropertyEntry("listName").getValue();

		//name = connectionInfo.getConnectionProperties().getPropertyEntry("name").getValue();

	}

	@Override
	public int putNext(AdapterRowSet rows) throws AdapterException {
		return 0;
	}

	@Override
	public void rollbackTransaction() throws AdapterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBrowseNodeId(String nodeId) throws AdapterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFetchSize(int fetchSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAutoCommit(boolean autocommit) throws AdapterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void executePreparedInsert(String arg0, StatementInfo arg1) throws AdapterException {
		// TODO Auto-generated method stub

	}

	@Override
	public void executePreparedUpdate(String arg0, StatementInfo arg1) throws AdapterException {
		// TODO Auto-generated method stub

	}

	@Override
	public int executeUpdate(String sql, StatementInfo info) throws AdapterException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Metadata importMetadata(String nodeId, List<Parameter> dataprovisioningParameters) throws AdapterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParametersResponse queryParameters(String nodeId, List<Parameter> parametersValues) throws AdapterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BrowseNode> loadTableDictionary(String lastUniqueName) throws AdapterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataDictionary loadColumnsDictionary() throws AdapterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeCall(FunctionMetadata metadata) throws AdapterException {
		// TODO Auto-generated method stub
	}

	@Override
	public void validateCall(FunctionMetadata metadata) throws AdapterException {
		// TODO Auto-generated method stub
	}

	@Override
	public void setNodesListFilter(RemoteObjectsFilter remoteObjectsFilter) throws AdapterException {
		// TODO Auto-generated method stub
	}

	@Override
	public Metadata getMetadataDetail(String nodeId) throws AdapterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CallableProcedure prepareCall(ProcedureMetadata metadata) throws AdapterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeResultSet() throws AdapterException {
		// TODO Auto-generated method stub

	}

}
