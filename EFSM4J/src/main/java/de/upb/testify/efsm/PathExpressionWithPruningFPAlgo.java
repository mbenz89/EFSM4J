package de.upb.testify.efsm;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import pathexpression.IRegEx;
import pathexpression.RegEx;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

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
  protected Stream<EFSMPath<State, Parameter, Context, Transition>> expressionToPath(Configuration<State, Context> config, IRegEx<Transition> pathExpression) {
    // TODO check for empty pe
    IRegEx<Transition> prunedPE = new PEPruner().prune(pathExpression, config.getContext());

    if (prunedPE == null || prunedPE instanceof RegEx.EmptySet) {
      // there is no feasible path
      return null;
    }

    Set<EFSMPath<State, Parameter, Context, Transition>> res = generatePaths(prunedPE, new EFSMPath<>(efsm));
    if (res != null) {
      return res.stream().filter(p -> p.isFeasible(config.getContext()));
    }

    return null;
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
      IRegEx<Transition> unique = makeUnique(unpruned);
      Set<ContextHolder> contexts = firstPass(unique, new ContextHolder(initialContext), true);
      return secondPass(unique);
    }


    private Set<ContextHolder> firstPass(IRegEx<Transition> regEx, ContextHolder context, boolean allowPruning) {
      if (regEx instanceof RegEx.Plain) {
        Transition t = ((RegEx.Plain<Transition>) regEx).v;
        if (applyOperationIfFeasible(context.c, t)) {
          return Collections.singleton(context);
        } else {
          return null;
        }
      } else if (regEx instanceof RegEx.Concatenate) {
        RegEx.Concatenate<Transition> concat = (RegEx.Concatenate<Transition>) regEx;
        // left side of the concat can be pruned but only if we are not in a right side of another concat
        Set<ContextHolder> lefts = firstPass(concat.a, context, allowPruning);

        if (lefts == null) {
          if (allowPruning) {
            markForSecondPass(concat);
          }
          return lefts;
        }

        Set<ContextHolder> res = Sets.newHashSet();
        // right side of a concat can only be pruned if we are not already in a right side that cannot be pruned and we have only one element to pass
        int numLefts = lefts.size();
        int numNulls = 0;
        boolean allowPruningHere = allowPruning && numLefts == 1;

        for (ContextHolder left : lefts) {
          Set<ContextHolder> right = firstPass(concat.b, left, allowPruningHere);
          if (right != null) {
            res.addAll(right);
          } else {
            numNulls++;
          }

          // if until the last context none of the lefts returned a feasible context, we can allow pruning for the last iteration
          if (numNulls - 1 == numLefts) {
            allowPruningHere = true;
          }
        }

        // if we haven't found any feasible contexts from concatenating left and right, we can safely return null here
        if (res.isEmpty()) {
          if (allowPruning) {
            markForSecondPass(concat);
          }
          return null;
        }

        return res;
      } else if (regEx instanceof RegEx.Star) {
        RegEx.Star<Transition> star = (RegEx.Star<Transition>) regEx;
        Set<ContextHolder> starRes = null;//firstPass(star.a, context.propagate(), allowPruning);
        if (starRes == null || starRes.equals(context)) {
          if (true || allowPruning) {
            markForSecondPass(star);
          }
          return Collections.singleton(context);
        } else {
          return Sets.union(Collections.singleton(context), starRes);
        }
      } else if (regEx instanceof RegEx.Union) {
        RegEx.Union<Transition> union = (RegEx.Union<Transition>) regEx;
        Set<ContextHolder> left = firstPass(union.getFirst(), context.propagateLeft(union), allowPruning);
        Set<ContextHolder> right = firstPass(union.getSecond(), context.propagateRight(union), allowPruning);

        if (left == null && right == null) {
          if (allowPruning) {
            markForSecondPass(union, true);
            markForSecondPass(union, false);
          }
          return null;
        } else if (left == null) {
          if (allowPruning) {
            markForSecondPass(union, true);
          }
          return right;
        } else if (right == null) {
          if (allowPruning) {
            markForSecondPass(union, false);
          }
          return left;
        } else {
          return Sets.union(left, right);
        }
      } else {
        throw new IllegalArgumentException("Regex unknown " + regEx.getClass());
      }
    }


    private void markForSecondPass(RegEx.Concatenate<Transition> concat) {
      concat.a = null;
      concat.b = null;
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
      private RegEx.Union<Transition> union;
      private boolean left;

      public ContextHolder(Context c) {
        this.c = c;
      }

      private ContextHolder(ContextHolder parent, RegEx.Union<Transition> union, boolean left) {
        this.c = parent.c.snapshot();
        // we always keep the origin of this context
        if (parent.union == null) {
          this.union = union;
        } else {
          this.union = parent.union;
        }
        this.left = left;
      }

      public ContextHolder(ContextHolder parent) {
        this.c = parent.c.snapshot();
        this.union = parent.union;
        this.left = parent.left;
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

      public ContextHolder propagate() {
        return new ContextHolder(this);
      }
    }
  }

}
