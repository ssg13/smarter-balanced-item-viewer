package org.smarterbalanced.itemviewerservice.core.Models;

/**
 * Models an item request.
 */
public class ItemRequestModel {
  private final String itemId;
  private final String itemBank;
  private final String[] featureCodes;

  /**
   * Instantiates a new Item model.
   *
   * @param itemId           item id
   * @param itemBank         item bank
   * @param featureCodes Accessibility feature codes
   */
  public ItemRequestModel(String itemId, String itemBank, String[] featureCodes) {
    this.itemId = itemId;
    this.itemBank = itemBank;
    this.featureCodes = featureCodes;
  }

  /**
   * Gets id.
   *
   * @return the item id for the request
   */
  public String getId() {
    return this.itemId;
  }

  /**
   * Gets bank.
   *
   * @return the item bank
   */
  public String getBank() {
    return this.itemBank;
  }

  /**
   * Get accessibility feature codes.
   *
   * @return the feature codes for the request.
   */
  public String[] getFeatureCodes() {
    return this.featureCodes;
  }
}
