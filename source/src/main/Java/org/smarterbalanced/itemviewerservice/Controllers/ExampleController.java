package org.smarterbalanced.itemviewerservice.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExampleController {

  @RequestMapping("/example")
  public String example(Model model) {
    model.addAttribute("attributeName", "Attribute Value");
    return "example";
  }

}