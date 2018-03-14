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
public class PEContextPropagation<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> extends PEBasedFPAlgo<State, Parameter, Context, Transition> {
  public PEContextPropagation(EFSM<State, Parameter, Context, Transition> efsm) {
    super(efsm);
  }

  public PEContextPropagation(EFSM<State, Parameter, Context, Transition> efsm, int maxDepth) {
    super(efsm, maxDepth);
  }

  @Override
  protected Collection<EFSMPath<State, Parameter, Context, Transition>> expressionToPath(Configuration<State, Context> config, IRegEx<Transition> pathExpression) {
    // TODO check for empty pe
    IRegEx<Transition> prunedPE = new PEPruner().prune(pathExpression, config.getContext());
    Set<EFSMPath<State, Parameter, Context, Transition>> res = generatePaths(prunedPE, new EFSMPath<>(efsm));
    if (res != null) {
      assert res.stream().allMatch(p -> p.isFeasible(config.getContext()));
    }
    return res;
  }

  private Set<EFSMPath<State, Parameter, Context, Transition>> generatePaths(IRegEx<Transition> expr, EFSMPath<State, Parameter, Context, Transition> pred) {
    if (expr == null || expr instanceof RegEx.EmptySet) {
      return Collections.emptySet();
    }

    if (expr instanceof RegEx.Plain) {
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
      return Collections.singleton(pred);
    } else if (expr instanceof RegEx.Union) {
      Set<EFSMPath<State, Parameter, Context, Transition>> lefts = generatePaths(((RegEx.Union) expr).a, pred);
      Set<EFSMPath<State, Parameter, Context, Transition>> rights = generatePaths(((RegEx.Union) expr).b, pred);
      return Sets.union(lefts, rights);
    } else {
      throw new IllegalArgumentException("Expr of unknown type: " + expr.getClass());
    }
  }


  private class PEPruner {
    IRegEx<Transition> root;

    /**
     * Prunes the Regex to contain only feasible paths
     *
     * @param unpruned
     * @param initialContext
     * @return
     */
    public IRegEx<Transition> prune(IRegEx<Transition> unpruned, Context initialContext) {
      root = unpruned;
      firstPass(root, null, new ContextHolder(initialContext));
      return secondPass(root, null);
    }

    private IRegEx<Transition> secondPass(IRegEx<Transition> regEx, IRegEx<Transition> parentRegEx) {
      if (regEx == null || regEx instanceof RegEx.Plain) {
        return regEx;
      } else if (regEx instanceof RegEx.Concatenate) {
        RegEx.Concatenate<Transition> concat = (RegEx.Concatenate<Transition>) regEx;
        if (secondPass(concat.a, concat) == null) {
          return null;
        }
        if (secondPass(concat.b, concat) == null) {
          return null;
        }
        return regEx;
      } else if (regEx instanceof RegEx.Star) {
        RegEx.Star<Transition> star = (RegEx.Star<Transition>) regEx;
        return secondPass(star.a, regEx);
      } else if (regEx instanceof RegEx.Union) {
        RegEx.Union<Transition> union = (RegEx.Union<Transition>) regEx;
        IRegEx<Transition> first = secondPass(union.getFirst(), union);
        IRegEx<Transition> second = secondPass(union.getSecond(), union);

        if (first == null && second == null) {
          return null;
        } else if (first == null) {
          replaceRegChild(regEx, parentRegEx, union.b);
          return second;
        } else if (second == null) {
          replaceRegChild(regEx, parentRegEx, union.a);
          return first;
        } else {
          return regEx;
        }
      } else {
        throw new IllegalArgumentException("Regex unknown " + regEx.getClass());
      }
    }

    private Set<ContextHolder> firstPass(IRegEx<Transition> regEx, IRegEx<Transition> parentRegEx, ContextHolder context) {
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
        Set<ContextHolder> lefts = firstPass(concat.a, concat, context);

        if (lefts == null) {
          return lefts;
        }

        Set<ContextHolder> res = Sets.newHashSet();
        for (ContextHolder left : lefts) {
          Set<ContextHolder> right = firstPass(concat.b, concat, left);
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
        //     RegEx.Star<Transition> star = (RegEx.Star<Transition>) regEx;
        //   Set<ContextHolder> starRes = markForSecondPass(star.a, context);
        // FIXME same problem here as well if we take the star we never terminate. if we fix this, we need to also implement pruning here
        return Collections.singleton(context);
      } else if (regEx instanceof RegEx.Union) {
        RegEx.Union<Transition> union = (RegEx.Union<Transition>) regEx;
        Set<ContextHolder> first = firstPass(union.getFirst(), union, context.propagateLeft(union));
        // FIXME it should be ok to take the original context here to prevent cloning too much
        Set<ContextHolder> second = firstPass(union.getSecond(), union, context.propagateRight(union));

        if (first == null && second == null) {
          return null;
        } else if (first == null) {
          replaceRegChild(regEx, parentRegEx, union.b);
          return second;
        } else if (second == null) {
          replaceRegChild(regEx, parentRegEx, union.a);
          return first;
        } else {
          return Sets.union(first, second);
        }
      } else {
        throw new IllegalArgumentException("Regex unknown " + regEx.getClass());
      }
    }

    private void markForSecondPass(ContextHolder context) {
      if (context.left) {
        context.union.a = null;
      } else {
        context.union.b = null;
      }
    }

    private void replaceRegChild(IRegEx<Transition> oldVal, IRegEx<Transition> parentRegEx, IRegEx<Transition> newVal) {
      if (parentRegEx instanceof RegEx.Union) {
        RegEx.Union pUnion = (RegEx.Union) parentRegEx;
        // is this the left or right side of the union?
        if (oldVal == pUnion.a) {
          pUnion.a = newVal;
        } else {
          pUnion.b = newVal;
        }
      } else if (parentRegEx instanceof RegEx.Star) {
        ((RegEx.Star) parentRegEx).a = newVal;
      } else if (parentRegEx instanceof RegEx.Concatenate) {
        RegEx.Concatenate pConcat = (RegEx.Concatenate) parentRegEx;
        if (pConcat.a == oldVal) {
          pConcat.a = newVal;
        } else {
          pConcat.b = newVal;
        }
      } else if (parentRegEx == null) {
        root = newVal;
      } else {
        throw new IllegalArgumentException("Parent should't be something else! " + parentRegEx.getClass());
      }
    }

    private class ContextHolder {
      private final Context c;
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
    }
  }

}
