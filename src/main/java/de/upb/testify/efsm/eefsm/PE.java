package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.JGraphBasedFPALgo;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import pathexpression.Edge;
import pathexpression.IRegEx;
import pathexpression.LabeledGraph;
import pathexpression.PathExpressionComputer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Manuel Benz
 * created on 07.03.18
 */
public class PE<State, Input, ContextObject> extends JGraphBasedFPALgo<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {


  private final PathExpressionComputer<State, Input> peComputer;

  public PE(EEFSM<State, Input, ContextObject> efsm) {
    super(efsm);
    GraphWrapper graphWrapper = new GraphWrapper(efsm);
    graphWrapper.toDot(Paths.get("./target/wrapper.dot"));
    peComputer = new PathExpressionComputer(graphWrapper);
  }

  @Override
  public EEFSMPath<State, Input, ContextObject> getPath(Configuration<State, EEFSMContext<ContextObject>> config, State tgt) {
    IRegEx<Input> expressionBetween = peComputer.getExpressionBetween(config.getState(), tgt);


    // expressionBetween
    return null;
  }

  @Override
  public EEFSMPath<State, Input, ContextObject> getPath(State tgt) {
    return (EEFSMPath<State, Input, ContextObject>) super.getPath(tgt);
  }

  /**
   * Implements the LabeledGraph interface, needed for the path expression computation. Edge labels of the wrapper are actual edges of the eefsm instead of the input, to make mapping the paths back easier.
   */
  private final class GraphWrapper implements LabeledGraph<State, ETransition<State, Input, ContextObject>> {
    private final EEFSM<State, Input, ContextObject> eefsm;

    public GraphWrapper(EEFSM<State, Input, ContextObject> eefsm) {
      this.eefsm = eefsm;
    }

    @Override
    public Set<Edge<State, ETransition<State, Input, ContextObject>>> getEdges() {
      return eefsm.getTransitons().stream().map(e -> new EdgeWrapper(e)).collect(Collectors.toSet());
    }

    @Override
    public Set<State> getNodes() {
      return eefsm.getStates();
    }

    @Override
    public ETransition<State, Input, ContextObject> epsilon() {
      return null;
    }

    public void toDot(Path out) {
      DirectedMultigraph<State, Edge<State, ETransition<State, Input, ContextObject>>> stateEdgeDirectedMultigraph = new DirectedMultigraph<>((state, v1) -> null);
      getNodes().forEach(s -> stateEdgeDirectedMultigraph.addVertex(s));
      getEdges().forEach(e -> stateEdgeDirectedMultigraph.addEdge(e.getStart(), e.getTarget(), e));

      DOTExporter<State, Edge<State, ETransition<State, Input, ContextObject>>> ex = new DOTExporter();
      try {
        ex.exportGraph(stateEdgeDirectedMultigraph, out.toFile());
      } catch (ExportException e) {
        e.printStackTrace();
      }
    }

    private final class EdgeWrapper implements Edge<State, ETransition<State, Input, ContextObject>> {

      private final ETransition<State, Input, ContextObject> e;

      public EdgeWrapper(ETransition<State, Input, ContextObject> e) {
        this.e = e;
      }

      @Override
      public State getStart() {
        return e.getSrc();
      }

      @Override
      public State getTarget() {
        return e.getTgt();
      }

      @Override
      public ETransition<State, Input, ContextObject> getLabel() {
        return e;
      }
    }
  }
}
