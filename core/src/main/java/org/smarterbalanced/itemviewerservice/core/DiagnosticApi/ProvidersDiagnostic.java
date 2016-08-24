package org.smarterbalanced.itemviewerservice.core.DiagnosticApi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarterbalanced.itemviewerservice.dal.Config.SettingsReader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * The Providers diagnostic.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "providers")
class ProvidersDiagnostic extends BaseDiagnostic {

  private static final Logger logger = LoggerFactory.getLogger(ProvidersDiagnostic.class);

  @XmlElement(name = "S3-connection")
  private String s3connection = null;

  @XmlElementWrapper(name = "content-packages")
  @XmlElement(name = "content-package")
  private List<String> contentPackages = null;

  @XmlElement(name = "Itemviewerservice-API-HTTP-status")
  private Integer irisStatus = null;

  @XmlElement(name = "Itemviewerservice-blackbox-HTTP-status")
  private Integer blackBoxStatus = null;

  @XmlElement(name = "Itemviewerservice-wordlisthandler-HTTP-status")
  private Integer wordListHandlerStatus = null;

  @XmlTransient
  private String baseUrl = null;

  /**
   * Instantiates a new Providers diagnostic.
   *
   * @param baseUrl the base application url
   */
  ProvidersDiagnostic(String baseUrl) {
    this.errors = new ArrayList<>();
    this.baseUrl = baseUrl;
  }

  /**
   * Instantiates a new Providers diagnostic.
   */
  /* The empty constructor is here for the XML serializer.
  Your IDE may claim it is not used. It is.
  DO NOT REMOVE! */
  ProvidersDiagnostic() {
  }

  /**
   * Run diagnostics.
   * <p>
   * Validates the Amazon S3 connection, iris is running, the blackbox is running,
   * and the word list handler is running.
   * </p>
   */
  void runDiagnostics() {
    String baseUrl = this.baseUrl;
    validateS3();
    validateIris(baseUrl);
    validateBlackbox(baseUrl);
    validateWordListHandler(baseUrl);
    generateStatus();
  }

  private void validateS3() {
    AmazonS3 s3Client = new AmazonS3Client();
    String contentBucket = SettingsReader.get("S3bucket");
    String region = SettingsReader.get("S3region");
    s3Client.setRegion(RegionUtils.getRegion(region));
    if ((contentBucket == null) || region == null) {
      addError("Configuration errors are preventing the diagnostic tool from connecting"
              + " to the Amazon S3 API.");
      logger.error("Configuration errors are preventing the diagnostic tool from connection"
              + " to the Amazon S3 API.");
      return;
    }

    try {
      ObjectListing listing = s3Client.listObjects(contentBucket);
      List<S3ObjectSummary> summaries = listing.getObjectSummaries();

      if (!s3Client.doesBucketExist(contentBucket)) {
        addError("Unable to locate the configured Amazon S3 bucket.");
        logger.error("Unable to locate the configured Amazon S3 bucket.");
        return;
      } else {
        this.s3connection = "Connected to Amazon S3.";
      }
      if (summaries.size() > 0) {
        this.contentPackages = new ArrayList<>();
        for (S3ObjectSummary summary : summaries) {
          this.contentPackages.add(summary.getKey());
        }
      } else {
        addError("The Amazon S3 does not contain any content packages.");
        logger.warn("The Amazon S3 does not contain any content packages.");
      }
    } catch (AmazonServiceException e) {
      addError("Request rejected by Amazon S3. Error Message: "
              + e.getMessage());
      logger.error("Request rejected by Amazon S3. Error Message: "
              + e.getMessage());
    } catch (AmazonClientException e) {
      addError("Internal error connecting to Amazon S3."
              + "Error message: " + e.getMessage());
      logger.error("Internal error connecting to Amazon S3."
              + "Error message: " + e.getMessage());
    }
  }

  private Integer getHttpStatus(URL url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    Integer responseCode = connection.getResponseCode();
    connection.disconnect();
    return responseCode;
  }

  private void validateIris(String baseUrl) {
    try {
      URL url = new URL(baseUrl + "/item/0-0");
      Integer status = getHttpStatus(url);
      if (status == 200 || status == 404) {
        this.irisStatus = 200;
      } else {
        this.irisStatus = status;
        addError("Item viewer service API ");
        logger.error("Item viewer service API returned a failing HTTP status code. HTTP code: "
                + status.toString());
      }
    } catch (IOException e) {
      addError("An I/O error occurred when trying to connect to the item viewer service API. "
              + "Please review the system logs.");
      logger.error("Unable to connect to item viewer service API. Exception: " + e.getMessage());
    }
  }

  private void validateBlackbox(String baseUrl) {
    try {
      URL url = new URL(baseUrl + "/");
      Integer status = getHttpStatus(url);
      if (status != 200) {
        this.blackBoxStatus = status;
        addError("Item viewer service BlackBox dependency returned a non 200 HTTP status code."
                + " HTTP code: " + status.toString());
        logger.error("Item viewer service BlackBox dependency returned a non 200 HTTP status code."
                + " HTTP code: " + status.toString());
      } else {
        this.blackBoxStatus = status;
      }
    } catch (IOException e) {
      addError("An internal I/O error occurred when trying to connect to the BlackBox API "
              + "the item viewer service depends on. Please review the system logs.");
      logger.error("Unable to connect to the blackbox API. Exception: " + e.getMessage());
    }
  }

  private void validateWordListHandler(String baseUrl) {
    try {
      String urlParams = "?bankKey=0&itemKey=0&index=1&TDS_ACCS=TDS_WL_Glossary";
      URL url = new URL(baseUrl + "/Pages/API/WordList.axd/resolve" + urlParams);
      Integer status = getHttpStatus(url);
      if (status == 200 || status == 500) {
        this.wordListHandlerStatus = 200;
      } else {

        this.wordListHandlerStatus = status;
        addError("Item viewer service word list handler dependency returned"
                + " a failing http status code. HTTP code: " + status.toString());
        logger.error("Item viewer service word list handler dependency returned"
                + " a failing http status code. HTTP code: " + status.toString());
      }
    } catch (IOException e) {
      addError("An I/O error occurred when trying to connect to the word list handler API "
              + "the item viewer service depends on. Please review the system logs.");
      logger.error("Unable to connect to the word list handler API. Exception: " + e.getMessage());
    }
  }

  /**
   * Gets s3 connection.
   *
   * @return the s3 connection
   */
  public String getS3connection() {
    return s3connection;
  }

  /**
   * Sets s3 connection.
   *
   * @param s3connection the s3 connection
   */
  public void setS3connection(String s3connection) {
    this.s3connection = s3connection;
  }

  /**
   * Gets content packages.
   *
   * @return the content packages
   */
  public List<String> getContentPackages() {
    return contentPackages;
  }

  /**
   * Sets content packages.
   *
   * @param contentPackages the content packages
   */
  public void setContentPackages(List<String> contentPackages) {
    this.contentPackages = contentPackages;
  }

  /**
   * Gets iris status.
   *
   * @return the iris status
   */
  public Integer getIrisStatus() {
    return irisStatus;
  }

  /**
   * Sets iris status.
   *
   * @param irisStatus the iris status
   */
  public void setIrisStatus(Integer irisStatus) {
    this.irisStatus = irisStatus;
  }

  /**
   * Gets blackbox status.
   *
   * @return the blackbox status
   */
  public Integer getBlackBoxStatus() {
    return blackBoxStatus;
  }

  /**
   * Sets blackbox status.
   *
   * @param blackBoxStatus the blackbox status
   */
  public void setBlackBoxStatus(Integer blackBoxStatus) {
    this.blackBoxStatus = blackBoxStatus;
  }

  /**
   * Gets word list handler status.
   *
   * @return the word list handler status
   */
  public Integer getWordListHandlerStatus() {
    return wordListHandlerStatus;
  }

  /**
   * Sets word list handler status.
   *
   * @param wordListHandlerStatus the word list handler status
   */
  public void setWordListHandlerStatus(Integer wordListHandlerStatus) {
    this.wordListHandlerStatus = wordListHandlerStatus;
  }

  /**
   * Gets base application url.
   *
   * @return the base application url
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Sets base application url.
   *
   * @param baseUrl the base application url
   */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
