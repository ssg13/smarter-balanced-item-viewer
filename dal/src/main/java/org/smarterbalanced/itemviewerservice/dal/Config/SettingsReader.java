package org.smarterbalanced.itemviewerservice.dal.Config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * The settings reader use for reading the item viewer service properties file.
 */
public class SettingsReader {
  private static final Logger logger = LoggerFactory.getLogger(SettingsReader.class);

  /**
   * Get the string value associated with the given property key.
   *
   * @param key the key
   * @return the value associated with the given key
   */
  public static String get(String key) {
    String configLocation = SettingsReader.class.getResource("/itemviewerservice.properties")
            .getPath();
    Properties properties = new Properties();
    FileInputStream configInput;
    try {
      configInput = new FileInputStream(configLocation);
      properties.load(configInput);
      configInput.close();
    } catch (IOException e) {
      logger.warn("Unable to load config file");
    }
    return properties.getProperty(key);
  }

  /**
   * Read content path string.
   *
   * @return the iris content path as a string
   * @throws IOException                  io exception loading config files
   * @throws ParserConfigurationException parser configuration exception
   * @throws URISyntaxException           uri syntax exception
   * @throws XPathExpressionException     xpath expression exception
   * @throws SAXException                 sax exception
   */
  public static String readIrisContentPath() throws IOException, ParserConfigurationException,
          URISyntaxException, XPathExpressionException, SAXException {
    URL resource = SettingsReader.class.getResource("/settings-mysql.xml");
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(resource.toURI().toString());
    XPathFactory pathFactory = XPathFactory.newInstance();
    XPath path = pathFactory.newXPath();
    XPathExpression contentPathExpr = path.compile("//entry[@key=\"iris.ContentPath\"]");
    NodeList nodeList = (NodeList) contentPathExpr.evaluate(doc, XPathConstants.NODESET);
    return nodeList.item(0).getTextContent();
  }

}
