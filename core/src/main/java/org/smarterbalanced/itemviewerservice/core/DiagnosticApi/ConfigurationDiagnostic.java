package org.smarterbalanced.itemviewerservice.core.DiagnosticApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarterbalanced.itemviewerservice.dal.Config.SettingsReader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Diagnostics for the system configuration.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "configuration")
class ConfigurationDiagnostic extends BaseDiagnostic {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationDiagnostic.class);

  @XmlElement(name = "content-path")
  private String contentPath = null;

  @XmlElement(name = "S3-content-bucket")
  private String s3ContentBucket;

  @XmlElement(name = "AWS-region")
  private String awsRegion = null;

  /**
   * Instantiates a new Configuration diagnostic.
   */
  ConfigurationDiagnostic() {
    this.errors = new ArrayList<>();
  }

  /**
   * Run diagnostics.
   * Validate the iris content path and application properties.
   */
  void runDiagnostics() {
    validateContentPath();
    validateAppProperties();
    generateStatus();
  }

  private void validateContentPath() {
    try {
      String path = SettingsReader.readIrisContentPath();
      if (contentPath != null) {
        this.contentPath = path;
      }
    } catch (IOException e) {
      addError("Unable to load settings-mysql.xml to validate settings");
      logger.error("Unable to load settings-mysql.xml to validate settings. Reason: "
              + e.getMessage());
    } catch (ParserConfigurationException e) {
      addError("Unable to create parser to read settings-mysql.xml");
      logger.error("Unable to create parser to read settings-mysql.xml. Reason "
              + e.getMessage());
    } catch (URISyntaxException e) {
      addError("Unable to generate URI required to load the settings-mysql.xml");
      logger.error("Unable to generate URI required to load the settings-mysql.xml. Reason: "
              + e.getMessage());
    } catch (XPathExpressionException e) {
      addError("Unable to find content path setting in settings-mysql.xml");
      logger.error("Unable to find content path setting in settings-mysql.xml. Reason: "
              + e.getMessage());
    } catch (SAXException e) {
      addError("Unable to parse xml document.");
      logger.error("Unable to parse xml document. Reason: " + e.getMessage());
    }
  }

  private void validateAppProperties() {
    String contentBucket = SettingsReader.get("S3bucket");
    String s3region = SettingsReader.get("S3region");
    if (contentBucket == null) {
      addError("Amazon S3 bucket to pull content packages is not specified in"
              + " itemviewerservice.properties.");
      logger.error("Amazon S3 bucket to pull content packages is not specified in"
              + " itemviewerservice.properties.");
      this.s3ContentBucket = "Not configured";
    } else {
      this.s3ContentBucket = contentBucket;
    }
    if (s3region == null) {
      addError("Amazon S3 region is not specified in itemviewerservice.properties");
      logger.error("Amazon S3 region is not specified in itemviewerservice.properties");
      this.awsRegion = "Not configured";
    } else {
      this.awsRegion = s3region;
    }
  }

  /**
   * Sets content path.
   *
   * @param contentPath the content path
   */
  public void setContentPath(String contentPath) {
    this.contentPath = contentPath;
  }

  /**
   * Gets content path.
   *
   * @return the content path
   */
  public String getContentPath() {
    return this.contentPath;
  }

  /**
   * Gets the Amazon s3 content bucket.
   *
   * @return the Amazon s3 content bucket
   */
  public String getS3ContentBucket() {
    return s3ContentBucket;
  }

  /**
   * Sets s 3 content bucket.
   *
   * @param s3ContentBucket the s 3 content bucket
   */
  public void setS3ContentBucket(String s3ContentBucket) {
    this.s3ContentBucket = s3ContentBucket;
  }

  /**
   * Gets aws region.
   *
   * @return the aws region
   */
  public String getAwsRegion() {
    return awsRegion;
  }

  /**
   * Sets aws region.
   *
   * @param awsRegion the aws region
   */
  public void setAwsRegion(String awsRegion) {
    this.awsRegion = awsRegion;
  }

}
