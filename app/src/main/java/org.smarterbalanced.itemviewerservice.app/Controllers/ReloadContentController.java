package org.smarterbalanced.itemviewerservice.app.Controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ReloadContentController {

  @RequestMapping(value = "reload")
  public static String reload() {
    return "redirect:/Pages/API/content/reload";
  }
}
