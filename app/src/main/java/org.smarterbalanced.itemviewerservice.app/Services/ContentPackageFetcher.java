package org.smarterbalanced.itemviewerservice.app.Services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarterbalanced.itemviewerservice.core.S3UpdateChecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import tds.iris.abstractions.repository.IContentBuilder;

import javax.annotation.PostConstruct;

public class ContentPackageFetcher {
  private static final Logger logger = LoggerFactory.getLogger(ContentPackageFetcher.class);

  @Autowired
  private IContentBuilder contentBuilder;

  //Check for a content paage when the application is started, then refresh context
  @PostConstruct
  public void checkForUpdates() {
    S3UpdateChecker updateChecker = new S3UpdateChecker();
    updateChecker.checkForUpdates();
    contentBuilder.init();
  }
}
