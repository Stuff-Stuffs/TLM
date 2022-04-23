package io.github.stuff_stuffs.tlm.common.api.storage;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractDeltaTransactionParticipant<Info, Delta> implements Transaction.CloseCallback {
    private final List<Delta> deltas = new ArrayList<>();

    protected abstract Delta createBlankDelta();

    protected abstract Delta mergeDeltas(Delta first, Delta second);

    public void save(final Info info, final TransactionContext transaction) {
        final int depth = transaction.nestingDepth();
        while (deltas.size() <= depth) {
            deltas.add(null);
        }
        Delta delta = deltas.get(depth);
        if (delta == null) {
            delta = createBlankDelta();
            deltas.set(depth, delta);
            transaction.addCloseCallback(this);
        }
        final Delta updatedDelta = updateDelta(info, delta);
        if (updatedDelta != delta) {
            deltas.set(depth, delta);
        }
    }

    @Override
    public void onClose(final TransactionContext transaction, final Transaction.Result result) {
        final Delta delta = deltas.set(transaction.nestingDepth(), null);

        if (result.wasAborted()) {
            readDelta(delta);
        } else if (transaction.nestingDepth() > 0) {
            final Delta first = deltas.get(transaction.nestingDepth() - 1);
            if (first == null) {
                deltas.set(transaction.nestingDepth() - 1, delta);
                transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
            } else {
                deltas.set(transaction.nestingDepth()-1, mergeDeltas(first, delta));
            }
        }
    }

    protected abstract void readDelta(Delta delta);

    protected abstract Delta updateDelta(Info info, Delta delta);
}
