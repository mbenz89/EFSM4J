package de.upb.testify.efsm;

import com.google.common.collect.Sets;
import pathexpression.IRegEx;
import pathexpression.RegEx;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Manuel Benz
 * created on 07.03.18
 */
public class PEAllPath<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> extends PEBasedFPAlgo<State, Parameter, Context, Transition> {

  public PEAllPath(EFSM<State, Parameter, Context, Transition> efsm) {
    super(efsm, Integer.MAX_VALUE);
  }


  public PEAllPath(EFSM<State, Parameter, Context, Transition> efsm, int maxDepth) {
    super(efsm, maxDepth);
  }

  @Override
  protected Stream<EFSMPath<State, Parameter, Context, Transition>> expressionToPath(Configuration<State, Context> config, IRegEx<Transition> pathExpression) {
    return expressionToPath(new EFSMPath<>(efsm), pathExpression, config, 0).stream().map(ps -> ps.path);
  }

  private Set<PathState> expressionToPath(EFSMPath<State, Parameter, Context, Transition> pred, IRegEx<Transition> expr, Configuration<State, Context> config, int depth) {
    if (expr == null || expr instanceof RegEx.EmptySet || depth >= maxDepth) {
      return Collections.emptySet();
    }

    if (expr instanceof RegEx.Plain) {
      return validate(pred, ((RegEx.Plain<Transition>) expr).v, config);
    } else if (expr instanceof RegEx.Concatenate) {
      Set<PathState> lefts = expressionToPath(pred, ((RegEx.Concatenate<Transition>) expr).a, config, depth + 1);

      // only if the left tree is correct we can procede here
      if (!lefts.isEmpty()) {
        Set<PathState> res = Sets.newHashSet();
        for (PathState left : lefts) {
          Set<PathState> rights = expressionToPath(left.path, ((RegEx.Concatenate<Transition>) expr).b, left.config, depth + 1);
          res.addAll(rights);
        }

        return res;
      } else {
        return Collections.emptySet();
      }
    } else if (expr instanceof RegEx.Star) {
      // we decide to take the loop once and not at all
      Set<PathState> a = expressionToPath(pred, ((RegEx.Star<Transition>) expr).a, config, depth + 1);
      // the previous path for not having an of the star typed input
      return Sets.union(Collections.singleton(new PathState(pred, config)), a);
      //return Collections.singleton(new PathState(pred, config));
    } else if (expr instanceof RegEx.Union) {
      Set<PathState> lefts = expressionToPath(pred, ((RegEx.Union<Transition>) expr).getFirst(), config, depth + 1);
      Set<PathState> rights = expressionToPath(pred, ((RegEx.Union<Transition>) expr).getSecond(), config, depth + 1);
      return Sets.union(lefts, rights);
    } else {
      throw new IllegalArgumentException("Expr of unknown type: " + expr.getClass());
    }
  }

  private Set<PathState> validate(EFSMPath<State, Parameter, Context, Transition> pred, Transition t, Configuration<State, Context> config) {
    // this should be much faster then evaluating the whole path all the time
    if (t.getSrc() == config.getState() && t.domainGuard(config.getContext())) {
      EFSMPath res = new EFSMPath(efsm, pred);
      res.append(t);
      Context snapshot = config.getContext().snapshot();
      t.operation(t.getExpectedInput(), snapshot);
      return Collections.singleton(new PathState(res, new Configuration(t.getTgt(), snapshot)));
    } else {
      return Collections.emptySet();
    }
  }

  private final class PathState {
    EFSMPath<State, Parameter, Context, Transition> path;
    Configuration<State, Context> config;

    public PathState(EFSMPath<State, Parameter, Context, Transition> path, Configuration<State, Context> config) {
      this.path = path;
      this.config = config;
    }
  }

}
