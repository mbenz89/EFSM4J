package de.upb.mb.efsm;

import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.IntegerComponentNameProvider;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Manuel Benz
 * created on 24.02.18
 */
public class EFSMDotExporter {
  private final EFSM efsm;

  public EFSMDotExporter(EFSM efsm) {
    this.efsm = efsm;
  }

  public void writeOut(Path outFile) throws IOException {
    DOTExporter exporter = new DOTExporter<>(
        new IntegerComponentNameProvider<>(),
        state -> state.toString(),
        edge -> edge.toString()
    );

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile.toFile()))) {
      exporter.exportGraph(efsm.getBaseGraph(), writer);
    }
  }
}
