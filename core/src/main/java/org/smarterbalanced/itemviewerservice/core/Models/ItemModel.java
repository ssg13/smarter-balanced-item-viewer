package org.smarterbalanced.itemviewerservice.core.Models;

public class ItemModel {
  private final String itemId;
  private final String itemBank;
  private final String[] featureCodes;

  public ItemModel(String id, String bank, String[] featureCodes) {
    this.itemId = id;
    this.itemBank = bank;
    this.featureCodes = featureCodes;
  }

  public String getId() {
    return this.itemId;
  }

  public String getBank() {
    return this.itemBank;
  }

  public String[] getFeatureCodes() {
    return this.featureCodes;
  }
}
