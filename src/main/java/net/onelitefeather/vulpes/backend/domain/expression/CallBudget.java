package net.onelitefeather.vulpes.backend.domain.expression;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.parser.Token;

/**
 * Limits the project function calls a single evaluation may make.
 *
 * <p>A depth limit alone does not stop a function that calls itself twice per level, because the number of calls
 * doubles with every level. The budget therefore also counts every call of one evaluation. It is bound to the
 * current thread: the outermost {@link #open()} creates it, nested ones share it, and closing the outermost scope
 * removes it again.
 */
final class CallBudget {

    /**
     * The number of nested project function calls a single evaluation may make.
     */
    static final int MAX_DEPTH = 32;

    /**
     * The number of project function calls a single evaluation may make in total.
     */
    static final int MAX_CALLS = 10_000;

    private static final ThreadLocal<CallBudget> CURRENT = new ThreadLocal<>();

    private int depth;
    private int calls;

    private CallBudget() {
    }

    /**
     * Opens a budget for the current thread, or joins the one that is already open.
     *
     * @return the scope to close when the evaluation is done
     */
    static Scope open() {
        if (CURRENT.get() != null) {
            return () -> { };
        }
        CURRENT.set(new CallBudget());
        return CURRENT::remove;
    }

    /**
     * Returns the budget of the current thread.
     *
     * @return the open budget
     * @throws IllegalStateException if no budget is open
     */
    static CallBudget current() {
        CallBudget budget = CURRENT.get();
        if (budget == null) {
            throw new IllegalStateException("No call budget is open on this thread.");
        }
        return budget;
    }

    /**
     * Counts a call and steps one level deeper.
     *
     * @param functionToken the token of the call, used to report errors
     * @throws EvaluationException if the call chain is too deep or the evaluation made too many calls
     */
    void enter(Token functionToken) throws EvaluationException {
        if (depth >= MAX_DEPTH) {
            throw new EvaluationException(functionToken, "Function call depth exceeded (recursive function?)");
        }
        if (calls >= MAX_CALLS) {
            throw new EvaluationException(functionToken,
                    "Function call limit of %d calls per evaluation exceeded.".formatted(MAX_CALLS));
        }
        depth++;
        calls++;
    }

    /**
     * Steps one level back up after a call returned or failed.
     */
    void leave() {
        depth--;
    }

    /**
     * The scope of an open budget, closing it never throws.
     */
    @FunctionalInterface
    interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
