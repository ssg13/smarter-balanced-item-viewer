package org.smarterbalanced.itemviewerservice.app.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExampleController {

  @RequestMapping("/")
  public String index() {
    return "public/index.html";
  }

}