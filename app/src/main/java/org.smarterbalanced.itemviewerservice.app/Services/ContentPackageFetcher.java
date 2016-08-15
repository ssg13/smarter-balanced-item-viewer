package org.smarterbalanced.itemviewerservice.app.Services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarterbalanced.itemviewerservice.core.S3UpdateChecker;

import org.springframework.scheduling.annotation.Scheduled;

public class ContentPackageFetcher {
  private static final Logger logger = LoggerFactory.getLogger(ContentPackageFetcher.class);

  //Run every 5 minutes
  @Scheduled(fixedDelay = 300000)
  public void checkForUpdates() {
    S3UpdateChecker updateChecker = new S3UpdateChecker();
    updateChecker.checkForUpdates();
  }
}
