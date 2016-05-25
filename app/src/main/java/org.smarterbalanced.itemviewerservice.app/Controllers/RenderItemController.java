package org.smarterbalanced.itemviewerservice.app.Controllers;

import org.smarterbalanced.itemviewerservice.core.Models.ItemRequestModel;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for rendering items.
 */
@RestController
@EnableAutoConfiguration
public class RenderItemController {

  /**
   * Returns content.
   *
   * @param id    Item and bank ID separated by a "-"
   * @param codes Feature codes delimited by semicolons.
   * @return content object.
   */
  @RequestMapping(value = "/item/{itemID}", method = RequestMethod.GET)
  public ItemRequestModel getContent(@PathVariable("itemID") String id,
                                     @RequestParam(value = "isaap", required = false)
                                     final String codes) {
    String[] parsedIds = id.split("-");
    String bank = parsedIds[0];
    String itemId = parsedIds[1];
    String[] codesArray = {};
    if ( codes != null ) {
      codesArray = codes.split(";");
    }
    ItemRequestModel request = new ItemRequestModel(itemId, bank, codesArray);
    System.out.println("Bank ID: " + request.getBank());
    System.out.println("Item ID: " + request.getId());
    System.out.println("Codes:");
    for (String code : request.getFeatureCodes()) {
      System.out.println(code);
    }
    return request;
  }
}
