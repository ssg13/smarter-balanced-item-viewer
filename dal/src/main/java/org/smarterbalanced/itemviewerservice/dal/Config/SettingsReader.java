package org.smarterbalanced.itemviewerservice.dal.Config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
}
