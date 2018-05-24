package de.upb.testify.efsm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.IntegerComponentNameProvider;

/** @author Manuel Benz created on 24.02.18 */
public class EFSMDotExporter<State, Transition> {
  private final EFSM<State, Transition, ?, ?> efsm;
  private final Function<State, String> stateLabeler;
  private final Function<Transition, String> edgeLabeler;

  public EFSMDotExporter(EFSM<State, Transition, ?, ?> efsm) {
    this(efsm, Object::toString, Object::toString);
  }

  public EFSMDotExporter(EFSM<State, Transition, ?, ?> efsm, Function<State, String> stateLabeler,
      Function<Transition, String> edgeLabeler) {
    this.efsm = efsm;
    this.stateLabeler = stateLabeler;
    this.edgeLabeler = edgeLabeler;
  }

  public void writeOut(Path outFile) throws IOException {
    DOTExporter exporter = new DOTExporter<State, Transition>(new IntegerComponentNameProvider<>(),
        s -> stateLabeler.apply(s), t -> edgeLabeler.apply(t));

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile.toFile()))) {
      exporter.exportGraph(efsm.getBaseGraph(), writer);
    }
  }
}
