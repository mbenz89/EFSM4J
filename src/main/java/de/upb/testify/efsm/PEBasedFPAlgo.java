package de.upb.testify.efsm;

import com.google.common.base.Stopwatch;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import pathexpression.Edge;
import pathexpression.IRegEx;
import pathexpression.LabeledGraph;
import pathexpression.PathExpressionComputer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Manuel Benz
 * created on 13.03.18
 */
public abstract class PEBasedFPAlgo<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> implements IFeasiblePathAlgo<State, Parameter, Context, Transition> {
  protected final PathExpressionComputer<State, Transition> peComputer;
  protected final EFSM<State, Parameter, Context, Transition> efsm;
  protected final int maxDepth;

  public PEBasedFPAlgo(EFSM<State, Parameter, Context, Transition> efsm) {
    this(efsm, Integer.MAX_VALUE);
  }


  public PEBasedFPAlgo(EFSM<State, Parameter, Context, Transition> efsm, int maxDepth) {
    GraphWrapper graphWrapper = new GraphWrapper(efsm);
    peComputer = new PathExpressionComputer(graphWrapper);
    this.maxDepth = maxDepth;
    this.efsm = efsm;
  }


  /**
   * Returns the shortest path
   */
  @Override
  public EFSMPath<State, Parameter, Context, Transition> getPath(Configuration<State, Context> config, State tgt) {
    return getPaths(config, tgt).stream().min(Comparator.comparing(EFSMPath::getLength)).orElse(null);
  }

  @Override
  public EFSMPath<State, Parameter, Context, Transition> getPath(State tgt) {
    return getPath(efsm.getConfiguration(), tgt);
  }

  /**
   * Returns all feasible paths in ascending order of length
   */
  public Collection<EFSMPath<State, Parameter, Context, Transition>> getPaths(State tgt) {
    return getPaths(efsm.getConfiguration(), tgt);
  }

  /**
   * Returns all feasible paths in ascending order of length
   */
  public Collection<EFSMPath<State, Parameter, Context, Transition>> getPaths(Configuration<State, Context> config, State tgt) {
    Stopwatch sw = Stopwatch.createStarted();
    IRegEx<Transition> pathExpression = peComputer.getExpressionBetween(config.getState(), tgt);
    System.out.println("expression building took " + sw);
    return expressionToPath(config, pathExpression);
  }

  protected abstract Collection<EFSMPath<State, Parameter, Context, Transition>> expressionToPath(Configuration<State, Context> config, IRegEx<Transition> pathExpression);

  protected final class PathState {
    EFSMPath<State, Parameter, Context, Transition> path;
    Configuration<State, Context> config;

    public PathState(EFSMPath<State, Parameter, Context, Transition> path, Configuration<State, Context> config) {
      this.path = path;
      this.config = config;
    }
  }

  /**
   * Implements the LabeledGraph interface, needed for the path expression computation. Edge labels of the wrapper are actual edges of the eefsm instead of the input, to make mapping the paths back easier.
   */
  protected final class GraphWrapper implements LabeledGraph<State, Transition> {
    private final EFSM<State, Parameter, Context, Transition> efsm;

    public GraphWrapper(EFSM<State, Parameter, Context, Transition> efsm) {
      this.efsm = efsm;
    }

    @Override
    public Set<Edge<State, Transition>> getEdges() {
      return efsm.getTransitons().stream().map(e -> new PEAllPath.GraphWrapper.EdgeWrapper(e)).collect(Collectors.toSet());
    }

    @Override
    public Set<State> getNodes() {
      return efsm.getStates();
    }

    @Override
    public Transition epsilon() {
      return null;
    }

    public void toDot(Path out) {
      DirectedMultigraph<State, Edge<State, Transition>> stateEdgeDirectedMultigraph = new DirectedMultigraph<>((state, v1) -> null);
      getNodes().forEach(s -> stateEdgeDirectedMultigraph.addVertex(s));
      getEdges().forEach(e -> stateEdgeDirectedMultigraph.addEdge(e.getStart(), e.getTarget(), e));

      DOTExporter<State, Edge<State, Transition>> ex = new DOTExporter();
      try {
        ex.exportGraph(stateEdgeDirectedMultigraph, out.toFile());
      } catch (ExportException e) {
        e.printStackTrace();
      }
    }

    private final class EdgeWrapper implements Edge<State, Transition> {

      private final Transition e;

      public EdgeWrapper(Transition e) {
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
      public Transition getLabel() {
        return e;
      }
    }
  }
}
