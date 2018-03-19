package de.upb.testify.efsm;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pathexpression.Edge;
import pathexpression.IRegEx;
import pathexpression.LabeledGraph;
import pathexpression.PathExpressionComputer;
import pathexpression.RegEx;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Manuel Benz
 * created on 13.03.18
 */
public abstract class PEBasedFPAlgo<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> implements IFeasiblePathAlgo<State, Parameter, Context, Transition> {
  private final static Logger logger = LoggerFactory.getLogger(PEBasedFPAlgo.class);

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
    List<EFSMPath<State, Parameter, Context, Transition>> paths = getPaths(config, tgt);
    if (paths == null || paths.isEmpty()) {
      return null;
    }
    return paths.stream().min(Comparator.comparing(EFSMPath::getLength)).orElse(null);
  }

  @Override
  public EFSMPath<State, Parameter, Context, Transition> getPath(State tgt) {
    return getPath(efsm.getConfiguration(), tgt);
  }

  /**
   * Returns a list of feasible paths in ascending order of length. Note: These can be all or a subset of all feasible paths depending on the implementation!
   */
  public List<EFSMPath<State, Parameter, Context, Transition>> getPaths(State tgt) {
    return getPaths(efsm.getConfiguration(), tgt);
  }

  /**
   * Returns a list of feasible paths in ascending order of length. Note: These can be all or a subset of all feasible paths depending on the implementation!
   */
  public List<EFSMPath<State, Parameter, Context, Transition>> getPaths(Configuration<State, Context> config, State tgt) {
    Stopwatch sw = Stopwatch.createStarted();
    IRegEx<Transition> pathExpression = peComputer.getExpressionBetween(config.getState(), tgt);
    logger.trace("Path-expression building took {}", sw);
    return expressionToPath(config, pathExpression).sorted(Comparator.comparing(EFSMPath::getLength)).collect(Collectors.toList());
  }

  /**
   * Returns a list of feasible paths based on the given path-expression and configuration in ascending order of length. Note: These can be all or a subset of all feasible paths depending on the implementation!
   */
  protected abstract Stream<EFSMPath<State, Parameter, Context, Transition>> expressionToPath(Configuration<State, Context> config, IRegEx<Transition> pathExpression);

  /**
   * Computes if a path exists from the given configuration to the given target state.
   * <p>
   * Note: This is still a expensive method and should be called only if no specific path needs to be found but only the existence of one.
   * It might still make sense to call this in an algorithm if it is very expensive when no path exists.
   *
   * @param config
   * @param tgt
   * @return
   */
  public boolean pathExists(Configuration<State, Context> config, State tgt) {
    return pathExists(config, peComputer.getExpressionBetween(config.getState(), tgt));
  }

  /**
   * Computes if a path exists from the current configuration to the given target state.
   * <p>
   * Note: This is still a expensive method and should be called only if no specific path needs to be found but only the existence of one.
   * It might still make sense to call this in an algorithm if it is very expensive when no path exists.
   *
   * @param tgt
   * @return
   */
  public boolean pathExists(State tgt) {
    return pathExists(efsm.getConfiguration(), tgt);
  }

  protected boolean pathExists(Configuration<State, Context> config, IRegEx<Transition> pathExpression) {
    return !pathExists(pathExpression, config.getContext()).isEmpty();
  }

  private Set<Context> pathExists(IRegEx<Transition> expr, Context c) {
    if (expr == null || expr instanceof RegEx.EmptySet) {
      return Collections.emptySet();
    }

    if (expr instanceof RegEx.Plain) {
      if (applyOperationIfFeasible(c, ((RegEx.Plain<Transition>) expr).v)) {
        return Collections.singleton(c);
      }
      return Collections.emptySet();
    } else if (expr instanceof RegEx.Concatenate) {
      Set<Context> lefts = pathExists(((RegEx.Concatenate) expr).a, c);

      // only if the left tree is correct we can procede here
      if (!lefts.isEmpty()) {
        Set<Context> res = Sets.newHashSet();
        for (Context left : lefts) {
          Set<Context> rights = pathExists(((RegEx.Concatenate) expr).b, left);
          res.addAll(rights);
        }

        return res;
      } else {
        return Collections.emptySet();
      }
    } else if (expr instanceof RegEx.Star) {
      return Sets.union(Collections.singleton(c.snapshot()), pathExists(((RegEx.Star) expr).a, c.snapshot()));
      // return Collections.singleton(c);
    } else if (expr instanceof RegEx.Union) {
      Set<Context> left = pathExists(((RegEx.Union) expr).a, c.snapshot());
      Set<Context> right = pathExists(((RegEx.Union) expr).b, c.snapshot());
      return Sets.union(left, right);
    } else {
      throw new IllegalArgumentException("Expr of unknown type: " + expr.getClass());
    }
  }

  /**
   * Checks if the transition if feasible in the given context and fires it so that the context is modified according to edge's operation if so.
   *
   * @param c
   * @param t
   * @return True, if the transition is feasible in the given context and the operation was applied.
   */
  protected boolean applyOperationIfFeasible(Context c, Transition t) {
    if (t.domainGuard(c)) {
      t.operation(t.getExpectedInput(), c);
      return true;
    }
    return false;
  }

  protected IRegEx<Transition> makeUnique(IRegEx<Transition> regEx) {
    if (regEx instanceof RegEx.Plain) {
      return new RegEx.Plain(((RegEx.Plain) regEx).v);
    } else if (regEx instanceof RegEx.Concatenate) {
      RegEx.Concatenate concat = (RegEx.Concatenate) regEx;
      return new RegEx.Concatenate<>(makeUnique(concat.a), makeUnique(concat.b));
    } else if (regEx instanceof RegEx.Star) {
      RegEx.Star star = (RegEx.Star) regEx;
      return new RegEx.Star<>(makeUnique(star.a));
    } else if (regEx instanceof RegEx.Union) {
      RegEx.Union union = (RegEx.Union) regEx;
      return new RegEx.Union<>(makeUnique(union.a), makeUnique(union.b));
    } else {
      throw new IllegalArgumentException("Regex unknown " + regEx.getClass());
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
