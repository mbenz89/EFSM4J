package de.upb.mb.efsm.eefsm;

import de.upb.mb.efsm.EFSMDotExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
class EEFSMTest {

  private WhiteBoardExample example;

  @BeforeEach
  void setUp() {
    example = new WhiteBoardExample();
  }

  @Test
  void toDot() throws IOException {
    EFSMDotExporter exporter = new EFSMDotExporter(example.eefsm);
    exporter.writeOut(Paths.get("./target/eefsm.dot"));
  }
}