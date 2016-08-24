package org.smarterbalanced.itemviewerservice.core.DiagnosticApi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigurationDiagnosticNoPropertiesTest {

  @Test
  public void noConfig() {
    ConfigurationDiagnostic configurationDiagnostic = new ConfigurationDiagnostic();
    configurationDiagnostic.runDiagnostics();
    assertTrue(configurationDiagnostic.getErrors().size() > 0);
    assertEquals(configurationDiagnostic.getStatusRating(), (Integer)0);
    assertEquals(configurationDiagnostic.getStatusText(), "failed");
  }

}
