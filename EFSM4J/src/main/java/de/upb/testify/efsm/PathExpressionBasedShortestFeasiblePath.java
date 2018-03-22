package de.upb.testify.efsm;

import com.google.common.collect.Maps;
import pathexpression.IRegEx;
import pathexpression.RegEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Manuel Benz
 * created on 07.03.18
 */
public class PathExpressionBasedShortestFeasiblePath<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> extends PEBasedFPAlgo<State, Parameter, Context, Transition> {

  public PathExpressionBasedShortestFeasiblePath(EFSM<State, Parameter, Context, Transition> efsm) {
    super(efsm, Integer.MAX_VALUE);
  }

  @Override
  protected Stream<EFSMPath<State, Parameter, Context, Transition>> expressionToPath(Configuration<State, Context> config, IRegEx<Transition> pathExpression) {
    return expressionToPath(pathExpression, new ContextWithPath(config.getContext())).stream().map(cp -> new EFSMPath<>(efsm, cp.path));
  }

  private Set<ContextWithPath> expressionToPath(IRegEx<Transition> expr, ContextWithPath cp) {
    if (expr instanceof RegEx.Plain) {
      if (cp.addTransition(((RegEx.Plain<Transition>) expr).v)) {
        return Collections.singleton(cp);
      }
      return Collections.emptySet();
    } else if (expr instanceof RegEx.Concatenate) {
      RegEx.Concatenate concat = (RegEx.Concatenate) expr;
      Set<ContextWithPath> lefts = expressionToPath(concat.a, cp);

      // only if the left tree is correct we can procede here
      if (!lefts.isEmpty()) {
        Set<ContextWithPath> res = Collections.emptySet();
        for (ContextWithPath left : lefts) {
          Set<ContextWithPath> rights = expressionToPath(concat.b, left);
          res = merge(res, rights);
        }
        return res;
      } else {
        return Collections.emptySet();
      }
    } else if (expr instanceof RegEx.Star) {
      return merge(Collections.singleton(cp.snapshot()), expressionToPath(((RegEx.Star) expr).a, cp.snapshot()));
    } else if (expr instanceof RegEx.Union) {
      RegEx.Union union = (RegEx.Union) expr;
      Set left = expressionToPath(union.a, cp.snapshot());
      Set right = expressionToPath(union.b, cp.snapshot());
      return merge(left, right);
    } else {
      throw new IllegalArgumentException("Expr of unknown type: " + expr.getClass());
    }
  }

  /**
   * Merges two sets of contexts with path so that for each unique context the shortest path is taken.
   *
   * @param left
   * @param right
   * @return
   */
  private Set<ContextWithPath> merge(Set<ContextWithPath> left, Set<ContextWithPath> right) {
    Map<ContextWithPath, ContextWithPath> resMap = Maps.newHashMapWithExpectedSize(left.size() + right.size());

    for (ContextWithPath cp : left) {
      resMap.put(cp, cp);
    }

    for (ContextWithPath cp : right) {
      ContextWithPath old = resMap.get(cp);
      // overwrite the context only if it has a shorter length
      if (old == null || old.getLength() > cp.getLength()) {
        resMap.put(cp, cp);
      }
    }

    return resMap.keySet();
  }

  private final class ContextWithPath {
    private final Context c;
    private List<Transition> path;

    public ContextWithPath(Context c) {
      this.c = c;
    }

    private ContextWithPath(ContextWithPath cp) {
      this.c = cp.c.snapshot();
      if (cp.path != null) {
        this.path = new ArrayList<>(cp.path);
      }
    }

    public int getLength() {
      return path == null ? 0 : path.size();
    }

    public ContextWithPath snapshot() {
      return new ContextWithPath(this);
    }

    public boolean addTransition(Transition t) {
      if (applyOperationIfFeasible(c, t)) {
        addToPath(t);
        return true;
      }
      return false;
    }

    private void addToPath(Transition t) {
      if (path == null) {
        path = new ArrayList<>();
      }
      path.add(t);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ContextWithPath that = (ContextWithPath) o;
      return com.google.common.base.Objects.equal(c, that.c);
    }

    @Override
    public int hashCode() {
      return com.google.common.base.Objects.hashCode(c);
    }
  }
}
