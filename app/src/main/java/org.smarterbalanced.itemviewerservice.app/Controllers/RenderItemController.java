package org.smarterbalanced.itemviewerservice.app.Controllers;

import org.smarterbalanced.itemviewerservice.core.Models.ItemModel;
import org.springframework.web.bind.annotation.*;

@RestController
public class RenderItemController {

  @RequestMapping(value="/item/{itemID}", method= RequestMethod.GET)
  public ItemModel getContent(@PathVariable("itemID") String id,
                              @RequestParam(value = "isaap", required = false) final String codes) {
    String[] parsedIds = id.split("-");
    String bank = parsedIds[0];
    String itemId = parsedIds[1];
    String[] codesArray = {};
    if (codes != null) {
      codesArray = codes.split(";");
    }
    ItemModel request = new ItemModel(itemId, bank, codesArray);
    System.out.println("Bank ID: " + request.getBank());
    System.out.println("Item ID: " + request.getId());
    System.out.println("Codes:");
    for(String code : request.getFeatureCodes()) {
      System.out.println(code);
    }
    return request;
  }
}
