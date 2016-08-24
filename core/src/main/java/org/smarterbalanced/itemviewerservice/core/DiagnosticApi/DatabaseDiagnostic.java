package org.smarterbalanced.itemviewerservice.core.DiagnosticApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarterbalanced.itemviewerservice.dal.Config.SettingsReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * The Class use for database diagnostics.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "database")
class DatabaseDiagnostic extends BaseDiagnostic {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseDiagnostic.class);

  @XmlElement(name = "db-exists")
  private Boolean dbExists = false;

  @XmlElement(name = "db-readable")
  private Boolean contentReadable = null;

  @XmlElement(name = "db-writable")
  private Boolean contentWriteable = null;

  @XmlElement(name = "db-create-file")
  private String createExampleFile = null;

  @XmlElement(name = "db-remove-file")
  private String removeExampleFile = null;

  @XmlTransient
  private String contentPath = null;

  /**
   * Instantiates a new Database diagnostic object.
   */
  DatabaseDiagnostic() {
    this.errors = new ArrayList<>();
    try {
      this.contentPath = SettingsReader.readIrisContentPath();
    } catch (Exception e) {
      addError("Unable to load configuration file required to connect to content database.");
      addError("No further diagnostics can be run on the content database until the configuration"
              + " is corrected.");
      logger.error("Unable to load configuration file required to connect to content database.");
    }
  }

  /**
   * Validate that the specified content directory exists, is readable,
   * and contains content.
   */
  void dbReadDiagnostics() {
    if (this.contentPath == null) {
      return;
    }

    try {
      File dir = new File(this.contentPath);

      if (!dir.exists()) {
        addError("The content directory specified in settings-mysql.xml does not exist.");
        logger.error("The content directory specified in settings-mysql.xml does not exist.");
        return;
      }

      if (!dir.isDirectory()) {
        this.dbExists = false;
        addError("The content directory specified in settings-mysql.xml "
                + "is not a directory.");
        logger.error("The content directory specified in settings-mysql.xml "
                + "is not a directory.");
      } else {
        this.dbExists = true;
      }

      if (!dir.canRead()) {
        this.contentReadable = false;
        addError("The content directory specified in settings-mysql.xml is not readable");
        logger.error("The content directory specified in settings-mysql.xml is not readable");
        return;
      } else {
        this.contentReadable = true;
      }

      File[] dirContents = dir.listFiles();
      if (dirContents != null && dirContents.length <= 0) {
        addError("The content directory specified in settings-mysql.xml is empty");
        logger.error("The content directory specified in settings-mysql.xml is empty");
      }
    } catch (NullPointerException e) {
      addError("Unable to open content directory specified in settings-mysql.xml");
      logger.error("Unable to open content directory specified in settings-mysql.xml");
    } catch (Exception e) {
      addError("The content path specified in settings-mysql.xml is invalid.");
      logger.error("The content path specified in settings-mysql.xml is invalid.");
    }
    generateStatus();
  }

  /**
   * Diagnostic tests to determine if the content directory exists, is writable, and
   * to create and remove files from the content directory.
   */
  void dbWriteDiagnostics() {
    if (this.contentPath == null) {
      return;
    }

    //Validate the context folder exists
    try {
      File dir = new File(this.contentPath);

      if (!dir.exists()) {
        this.dbExists = false;
        addError("The content directory specified in settings-mysql.xml does not exist.");
        logger.error("The content directory specified in settings-mysql.xml does not exist.");
        return;
      } else {
        this.dbExists = true;
      }

      if (!dir.isDirectory()) {
        addError("The content directory specified in settings-mysql.xml "
                + "is not a directory.");
        logger.error("The content directory specified in settings-mysql.xml "
                + "is not a directory.");
      }
      if (!dir.canWrite()) {
        this.contentWriteable = false;
        addError("The content directory specified in settings-mysql.xml is not readable.");
        logger.error("The content directory specified in settings-mysql.xml is not readable.");
      } else {
        this.contentWriteable = true;
      }
    } catch (Exception e) {
      addError("The content path specified in settings-mysql.xml is invalid.");
      logger.error("The content path specified in settings-mysql.xml is invalid.");
      return;
    }
    //make a directory for our diagnostic writes if it doesn't exist
    try {
      File directory = new File(this.contentPath + "/diagnostics");
      directory.mkdir();
    } catch (SecurityException e) {
      addError("Failed to make new directory in content location.");
      logger.error("Unable to create test directory for level four diagnostic file write.");
      return;
    }
    String randomFileName = contentPath + "/diagnostics/"
            + UUID.randomUUID().toString() + "-diagnostic-test";
    File randomFile = new File(randomFileName);

    try {
      if (randomFile.createNewFile()) {
        this.createExampleFile = "Diagnostic test file written to content package";
      }
    } catch (SecurityException e) {
      addError("File permissions prevented new files from being written to the content location.");
      logger.error(
              "File permissions prevented new files from being written to the content location.");
      return;
    } catch (IOException e) {
      addError("I/O failure when writing to the content location.");
      logger.error("I/O failure when writing to the content location.");
      return;
    }

    try {
      if (randomFile.delete()) {
        this.removeExampleFile = "Diagnostic test file deleted from content package.";
      } else {
        addError("Unable to delete diagnostic test file from content location.");
        logger.error("Unable to delete diagnostic test file from content location., Filename: "
                + randomFileName);
      }
    } catch (SecurityException e) {
      addError("File permissions prevented files from being deleted from the content location.");
      logger.error(
              "File permissions prevented files from being deleted from the content location.");
    }

  }

  /**
   * Gets db exists.
   *
   * @return the db exists
   */
  public boolean getDbExists() {
    return dbExists;
  }

  /**
   * Sets if the db exists.
   *
   * @param dbExists the db exists
   */
  public void setDbExists(boolean dbExists) {
    this.dbExists = dbExists;
  }

  /**
   * Gets if the content is readable.
   *
   * @return is the content readable
   */
  public Boolean getContentReadable() {
    return contentReadable;
  }

  /**
   * Sets if the content is readable.
   *
   * @param contentReadable is the content readable
   */
  public void setContentReadable(Boolean contentReadable) {
    this.contentReadable = contentReadable;
  }

  /**
   * Gets if the content is writeable.
   *
   * @return the is the content writeable
   */
  public Boolean getContentWriteable() {
    return contentWriteable;
  }

  /**
   * Sets if the content is writeable.
   *
   * @param contentWriteable the content writeable
   */
  public void setContentWriteable(Boolean contentWriteable) {
    this.contentWriteable = contentWriteable;
  }

  /**
   * Gets if the diagnostic was able to create an example file.
   *
   * @return the result of attempting to create an example file
   */
  public String getCreateExampleFile() {
    return createExampleFile;
  }

  /**
   * Sets the result of attempting to create an example file.
   *
   * @param createExampleFile the result of attempting to create an example file
   */
  public void setCreateExampleFile(String createExampleFile) {
    this.createExampleFile = createExampleFile;
  }

  /**
   * Gets the result of attempting to remove an example file.
   *
   * @return the result of attempting to remove an example file
   */
  public String getRemoveExampleFile() {
    return removeExampleFile;
  }

  /**
   * Sets the result of attempting to remove example file.
   *
   * @param removeExampleFile the result of attempting to remove example file
   */
  public void setRemoveExampleFile(String removeExampleFile) {
    this.removeExampleFile = removeExampleFile;
  }

  /**
   * Gets the content path.
   *
   * @return the content path
   */
  public String getContentPath() {
    return contentPath;
  }

  /**
   * Sets the content path.
   *
   * @param contentPath the content path
   */
  public void setContentPath(String contentPath) {
    this.contentPath = contentPath;
  }

}
