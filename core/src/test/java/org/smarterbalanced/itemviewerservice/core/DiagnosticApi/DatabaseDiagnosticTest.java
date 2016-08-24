package org.smarterbalanced.itemviewerservice.core.DiagnosticApi;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class DatabaseDiagnosticTest {

  private DatabaseDiagnostic databaseDiagnostic;
  private String readOnlyDirectoryPath;
  private File readOnlyDirectory;

  @Before
  public void setup() {
    databaseDiagnostic = new DatabaseDiagnostic();
    String testContentPath = DatabaseDiagnosticTest.class.getResource("/testContent").getFile();
    databaseDiagnostic.setContentPath(testContentPath);
    readOnlyDirectoryPath = testContentPath + "/readOnly";
    readOnlyDirectory = new File(readOnlyDirectoryPath);
  }

  @Test
  public void testDbWrite() {
    databaseDiagnostic.dbWriteDiagnostics();
    databaseDiagnostic.generateStatus();
    assertEquals((Integer)4, databaseDiagnostic.getStatusRating());
    assertEquals(databaseDiagnostic.getStatusText(), BaseDiagnostic.convertToStatusText(4));
    assertTrue(databaseDiagnostic.getContentWriteable());
    assertNotEquals(databaseDiagnostic.getCreateExampleFile(), null);
    assertNotEquals(databaseDiagnostic.getRemoveExampleFile(), null);
  }

  @Test
  public void testDbRead() {
    databaseDiagnostic.dbReadDiagnostics();
    assertEquals((Integer)4, databaseDiagnostic.getStatusRating());
    assertEquals(BaseDiagnostic.convertToStatusText(4), databaseDiagnostic.getStatusText());
    assertTrue(databaseDiagnostic.getContentReadable());
    assertTrue(databaseDiagnostic.getDbExists());
  }

  @Test
  public void testNoContentDirectory() {
    DatabaseDiagnostic invalidDatabaseDiagnostic = new DatabaseDiagnostic();
    invalidDatabaseDiagnostic.setContentPath(readOnlyDirectoryPath + "/someInvalidPathThatDoesNotExist");
    invalidDatabaseDiagnostic.dbReadDiagnostics();
    invalidDatabaseDiagnostic.dbWriteDiagnostics();
    invalidDatabaseDiagnostic.generateStatus();
    assertEquals((Integer)0, invalidDatabaseDiagnostic.getStatusRating());
    assertEquals(BaseDiagnostic.convertToStatusText(0), invalidDatabaseDiagnostic.getStatusText());
  }

}
