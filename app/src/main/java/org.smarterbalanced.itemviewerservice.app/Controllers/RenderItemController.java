package org.smarterbalanced.itemviewerservice.app.Controllers;

import org.smarterbalanced.itemviewerservice.dal.Models.ItemModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RenderItemController {

  @RequestMapping(value="/render", method= RequestMethod.GET)
  public ItemModel getContent() {
    return new ItemModel();
  }
}
