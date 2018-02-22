package de.upb.mb.efsm.eefsm;

import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
    DOTExporter exporter = new DOTExporter<>(
        new IntegerComponentNameProvider<>(),
        state -> state.toString(),
        edge -> edge.toString()
    );

    try (BufferedWriter writer = new BufferedWriter(new FileWriter("./target/eefsm.dot"))) {
      exporter.exportGraph(example.eefsm.getBaseGraph(), writer);
    }
  }
}