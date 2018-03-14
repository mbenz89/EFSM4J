package de.upb.testify.efsm;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import pathexpression.IRegEx;
import pathexpression.RegEx;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 13.03.18
 */
public class PathExpressionWithPruningFPAlgo<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> extends PEBasedFPAlgo<State, Parameter, Context, Transition> {
  public PathExpressionWithPruningFPAlgo(EFSM<State, Parameter, Context, Transition> efsm) {
    super(efsm);
  }

  public PathExpressionWithPruningFPAlgo(EFSM<State, Parameter, Context, Transition> efsm, int maxDepth) {
    super(efsm, maxDepth);
  }

  @Override
  protected Collection<EFSMPath<State, Parameter, Context, Transition>> expressionToPath(Configuration<State, Context> config, IRegEx<Transition> pathExpression) {
    // TODO check for empty pe
    IRegEx<Transition> prunedPE = new PEPruner().prune(pathExpression, config.getContext());

    if (prunedPE == null) {
      // there is no feasible path
      return null;
    }

    Set<EFSMPath<State, Parameter, Context, Transition>> res = generatePaths(prunedPE, new EFSMPath<>(efsm));
    if (res != null) {
      assert res.stream().allMatch(p -> p.isFeasible(config.getContext()));
    }

    return res;
  }

  private Set<EFSMPath<State, Parameter, Context, Transition>> generatePaths(IRegEx<Transition> expr, EFSMPath<State, Parameter, Context, Transition> pred) {
    if (expr instanceof RegEx.EmptySet) {
      return Collections.singleton(pred);
    } else if (expr instanceof RegEx.Plain) {
      return Collections.singleton(new EFSMPath<>(efsm, pred, ((RegEx.Plain<Transition>) expr).v));
    } else if (expr instanceof RegEx.Concatenate) {
      Set<EFSMPath<State, Parameter, Context, Transition>> lefts = generatePaths(((RegEx.Concatenate<Transition>) expr).a, pred);

      // only if the left tree is correct we can procede here
      if (!lefts.isEmpty()) {
        Set<EFSMPath<State, Parameter, Context, Transition>> res = Sets.newHashSet();
        for (EFSMPath<State, Parameter, Context, Transition> left : lefts) {
          Set<EFSMPath<State, Parameter, Context, Transition>> rights = generatePaths(((RegEx.Concatenate<Transition>) expr).b, left);
          res.addAll(rights);
        }

        return res;
      } else {
        return Collections.emptySet();
      }
    } else if (expr instanceof RegEx.Star) {
      throw new IllegalStateException("Star should have been removed in the second pass of the pruning step!");
    } else if (expr instanceof RegEx.Union) {
      Set<EFSMPath<State, Parameter, Context, Transition>> lefts = generatePaths(((RegEx.Union) expr).a, pred);
      Set<EFSMPath<State, Parameter, Context, Transition>> rights = generatePaths(((RegEx.Union) expr).b, pred);
      return Sets.union(lefts, rights);
    } else {
      throw new IllegalArgumentException("Expr of unknown type: " + expr.getClass());
    }
  }

  private class PEPruner {

    /**
     * Prunes the Regex to contain only feasible paths
     *
     * @param unpruned
     * @param initialContext
     * @return
     */
    public IRegEx<Transition> prune(IRegEx<Transition> unpruned, Context initialContext) {
      firstPass(unpruned, new ContextHolder(initialContext));
      return secondPass(unpruned);
    }

    private Set<ContextHolder> firstPass(IRegEx<Transition> regEx, ContextHolder context) {
      if (regEx == null || regEx instanceof RegEx.EmptySet) {
        return null;
      }

      if (regEx instanceof RegEx.Plain) {
        Transition t = ((RegEx.Plain<Transition>) regEx).v;
        Context cc = context.c;
        if (t.domainGuard(cc)) {
          t.operation(t.getExpectedInput(), cc);
          return Collections.singleton(context);
        } else {
          // we can mark this context since it is not feasible anymore
          markForSecondPass(context);
          return null;
        }
      } else if (regEx instanceof RegEx.Concatenate) {
        RegEx.Concatenate<Transition> concat = (RegEx.Concatenate<Transition>) regEx;
        Set<ContextHolder> lefts = firstPass(concat.a, context);

        if (lefts == null) {
          return lefts;
        }

        Set<ContextHolder> res = Sets.newHashSet();
        for (ContextHolder left : lefts) {
          Set<ContextHolder> right = firstPass(concat.b, left);
          if (right != null) {
            res.addAll(right);
          }
        }

        // if we haven't found any feasible contexts from concatenating left and right, we can savely return null here
        if (res.isEmpty()) {
          return null;
        }

        return res;
      } else if (regEx instanceof RegEx.Star) {
        RegEx.Star<Transition> star = (RegEx.Star<Transition>) regEx;
        Set<ContextHolder> starRes = firstPass(star.a, context.propagate(star));
        if (starRes == null) {
          markForSecondPass(star);
          return Collections.singleton(context);
        } else {
          return Sets.union(Collections.singleton(context), starRes);
        }
      } else if (regEx instanceof RegEx.Union) {
        RegEx.Union<Transition> union = (RegEx.Union<Transition>) regEx;
        Set<ContextHolder> left = firstPass(union.getFirst(), context.propagateLeft(union));
        Set<ContextHolder> right = firstPass(union.getSecond(), context.propagateRight(union));

        if (left == null && right == null) {
          return null;
        } else if (left == null) {
          markForSecondPass(union, true);
          return right;
        } else if (right == null) {
          markForSecondPass(union, false);
          return left;
        } else {
          return Sets.union(left, right);
        }
      } else {
        throw new IllegalArgumentException("Regex unknown " + regEx.getClass());
      }
    }

    private void markForSecondPass(RegEx.Star<Transition> star) {
      star.a = null;
    }

    private void markForSecondPass(RegEx.Union<Transition> union, boolean left) {
      if (left) {
        union.a = null;
      } else {
        union.b = null;
      }
    }

    private void markForSecondPass(ContextHolder context) {
      if (context.union != null) {
        markForSecondPass(context.union, context.left);
        context.union = null;
      } else if (context.star != null) {
        markForSecondPass(context.star);
        context.star = null;
      } else {
        throw new IllegalStateException("Neither star nor union registered to this context");
      }

    }


    private IRegEx<Transition> secondPass(IRegEx<Transition> regEx) {
      if (regEx == null || regEx instanceof RegEx.Plain) {
        return regEx;
      } else if (regEx instanceof RegEx.Concatenate) {
        RegEx.Concatenate<Transition> concat = (RegEx.Concatenate<Transition>) regEx;
        IRegEx<Transition> left = secondPass(concat.a);
        if (left == null) {
          return null;
        }
        IRegEx<Transition> right = secondPass(concat.b);
        if (right == null) {
          return null;
        }
        return RegEx.concatenate(left, right);
      } else if (regEx instanceof RegEx.Star) {
        RegEx.Star<Transition> star = (RegEx.Star<Transition>) regEx;
        IRegEx<Transition> res = secondPass(star.a);
        if (res == null) {
          // if the content of the star operator is not feasible we can still just take it 0 times!
          return new RegEx.EmptySet<>();
        }
        // we replace the star operator by an or'ing the taken and the not taken path
        return new RegEx.Union<>(res, new RegEx.EmptySet<>());
      } else if (regEx instanceof RegEx.Union) {
        RegEx.Union<Transition> union = (RegEx.Union<Transition>) regEx;
        IRegEx<Transition> first = secondPass(union.a);
        IRegEx<Transition> second = secondPass(union.b);

        if (first == null && second == null) {
          return null;
        } else if (first == null) {
          return second;
        } else if (second == null) {
          return first;
        } else {
          return RegEx.union(first, second);
        }
      } else {
        throw new IllegalArgumentException("Regex unknown " + regEx.getClass());
      }
    }

    private class ContextHolder {
      private final Context c;
      private RegEx.Star<Transition> star;
      private RegEx.Union<Transition> union;
      private boolean left;

      public ContextHolder(Context c) {
        this.c = c;
      }

      private ContextHolder(ContextHolder parent, RegEx.Union<Transition> union, boolean left) {
        this.c = parent.c.snapshot();
        this.union = union;
        this.left = left;
      }

      public ContextHolder(ContextHolder parent, RegEx.Star<Transition> star) {
        this.c = parent.c.snapshot();
        this.star = star;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }
        ContextHolder that = (ContextHolder) o;
        return Objects.equal(c, that.c);
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(c);
      }

      public ContextHolder propagateLeft(RegEx.Union<Transition> union) {
        return new ContextHolder(this, union, true);
      }

      public ContextHolder propagateRight(RegEx.Union<Transition> union) {
        return new ContextHolder(this, union, false);
      }

      public ContextHolder propagate(RegEx.Star<Transition> star) {
        return new ContextHolder(this, star);
      }
    }
  }

}
