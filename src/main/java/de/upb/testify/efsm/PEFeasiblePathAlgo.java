package de.upb.testify.efsm;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import pathexpression.Edge;
import pathexpression.IRegEx;
import pathexpression.LabeledGraph;
import pathexpression.PathExpressionComputer;
import pathexpression.RegEx;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Manuel Benz
 * created on 07.03.18
 */
public class PEFeasiblePathAlgo<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> implements IFeasiblePathAlgo<State, Parameter, Context, Transition> {

  private final PathExpressionComputer<State, Transition> peComputer;
  private final EFSM<State, Parameter, Context, Transition> efsm;

  public PEFeasiblePathAlgo(EFSM<State, Parameter, Context, Transition> efsm) {
    this.efsm = efsm;
    GraphWrapper graphWrapper = new GraphWrapper(efsm);
    peComputer = new PathExpressionComputer(graphWrapper);
  }

  /**
   * Returns the shortest path
   */
  @Override
  public EFSMPath<State, Parameter, Context, Transition> getPath(Configuration<State, Context> config, State tgt) {
    Stopwatch sw = Stopwatch.createStarted();
    IRegEx<Transition> expressionBetween = peComputer.getExpressionBetween(config.getState(), tgt);
    System.out.println("expression building took " + sw);
    return expressionToPath(new EFSMPath<>(efsm), expressionBetween, config.getContext()).stream().min(Comparator.comparing(EFSMPath::getLength)).orElse(null);
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
    IRegEx<Transition> expressionBetween = peComputer.getExpressionBetween(config.getState(), tgt);

    return expressionToPath(new EFSMPath<>(efsm), expressionBetween, config.getContext()).stream().sorted(Comparator.comparing(EFSMPath::getLength)).collect(Collectors.toList());
  }


  @Override
  public EFSMPath<State, Parameter, Context, Transition> getPath(State tgt) {
    return getPath(efsm.getConfiguration(), tgt);
  }

  private Set<EFSMPath<State, Parameter, Context, Transition>> expressionToPath(EFSMPath<State, Parameter, Context, Transition> pred, IRegEx<Transition> expr, Context context) {
    if (expr == null || expr instanceof RegEx.EmptySet) {
      return Collections.emptySet();
    }

    if (expr instanceof RegEx.Plain) {
      EFSMPath res = new EFSMPath(efsm, pred);
      res.append(((RegEx.Plain<Transition>) expr).v);
      if (res.isFeasible(context)) {
        return Collections.singleton(res);
      } else {
        return Collections.emptySet();
      }
    } else if (expr instanceof RegEx.Concatenate) {
      Set<EFSMPath<State, Parameter, Context, Transition>> lefts = expressionToPath(pred, ((RegEx.Concatenate<Transition>) expr).a, context);

      // only if the left tree is correct we can procede here
      if (!lefts.isEmpty()) {
        Set<EFSMPath<State, Parameter, Context, Transition>> res = Sets.newHashSetWithExpectedSize(lefts.size());
        for (EFSMPath<State, Parameter, Context, Transition> left : lefts) {
          Set<EFSMPath<State, Parameter, Context, Transition>> rights = expressionToPath(left, ((RegEx.Concatenate<Transition>) expr).b, context);
          res.addAll(rights);
        }
        return res;
      } else {
        return Collections.emptySet();
      }
    } else if (expr instanceof RegEx.Star) {
      // we decide to take the loop once and not at all
      Set<EFSMPath<State, Parameter, Context, Transition>> a = expressionToPath(pred, ((RegEx.Star<Transition>) expr).a, context);
      // the empty path for not taking this path
      return Sets.union(a, Collections.singleton(pred));
    } else if (expr instanceof RegEx.Union) {
      Set<EFSMPath<State, Parameter, Context, Transition>> lefts = expressionToPath(pred, ((RegEx.Union<Transition>) expr).getFirst(), context);
      Set<EFSMPath<State, Parameter, Context, Transition>> rights = expressionToPath(pred, ((RegEx.Union<Transition>) expr).getSecond(), context);
      return Sets.union(lefts, rights);
    } else {
      throw new IllegalArgumentException("Expr of unknown type: " + expr.getClass());
    }
  }

  /**
   * Implements the LabeledGraph interface, needed for the path expression computation. Edge labels of the wrapper are actual edges of the eefsm instead of the input, to make mapping the paths back easier.
   */
  private final class GraphWrapper implements LabeledGraph<State, Transition> {
    private final EFSM<State, Parameter, Context, Transition> efsm;

    public GraphWrapper(EFSM<State, Parameter, Context, Transition> efsm) {
      this.efsm = efsm;
    }

    @Override
    public Set<Edge<State, Transition>> getEdges() {
      return efsm.getTransitons().stream().map(e -> new EdgeWrapper(e)).collect(Collectors.toSet());
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
