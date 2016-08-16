package org.smarterbalanced.itemviewerservice.app.Controllers;

import org.smarterbalanced.itemviewerservice.app.Exceptions.ItemNotFoundException;
import org.smarterbalanced.itemviewerservice.core.Models.ItemRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import tds.iris.abstractions.repository.IContentBuilder;


/**
 * REST API controller for rendering items.
 */

@Controller
public class RenderItemController {

  @Autowired
  private IContentBuilder contentBuilder;

  /**
   * Returns content.
   *
   * @param itemId             Item bank and item ID separated by a "-"
   * @param accommodationCodes Feature codes delimited by semicolons.
   * @return content object.
   */
  @RequestMapping(value = "/{item:\\d+[-]\\d+}", method = RequestMethod.GET)
  @ResponseBody
  public ModelAndView getContent(@PathVariable("item") String itemId,
                                 @RequestParam(value = "isaap", required = false,
                                         defaultValue = "")
                                         String accommodationCodes
  ) {
    //Request is in the format
    String[] codes = accommodationCodes.split(";");
    ItemRequestModel item = new ItemRequestModel("I-" + itemId, codes);

    //check if the item exists
    try {
      contentBuilder.getITSDocument("I-" + itemId);
    } catch (Exception e) {
      throw new ItemNotFoundException();
    }

    String token = item.generateJsonToken();
    ModelAndView model = new ModelAndView();
    model.setViewName("item");
    model.addObject("token", token);
    model.addObject("item", itemId);
    return model;
  }

  @RequestMapping(value = "/reload")
  public String reload() {
    contentBuilder.init();
    return "redirect:/Pages/API/content/reload";
  }

}
