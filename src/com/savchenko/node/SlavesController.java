package com.savchenko.node;

import com.savchenko.Constants;
import com.savchenko.data.communication.AppendEntries;
import com.savchenko.data.communication.AppendEntriesResult;
import com.savchenko.suportive.Entry;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SlavesController {
    private Integer leaderId;
    private List<Integer> slaveIds;
    private Log log;
    private Map<Integer, Integer> nextIndexes;
    private Map<Integer, Integer> matchIndexes;
    private Map<Integer, Pair<Integer, Integer>> pending;

    public SlavesController(Log log, List<Integer> slaveIds, Integer leaderId) {
        this.log = log;
        this.leaderId = leaderId;
        this.slaveIds = slaveIds;
        var initialIndex = log.lastIndex() + 1;
        nextIndexes = slaveIds.stream().collect(Collectors.toMap(Function.identity(), id -> initialIndex));
        matchIndexes = slaveIds.stream().collect(Collectors.toMap(Function.identity(), id -> 0));
        pending = slaveIds.stream().collect(Collectors.toMap(Function.identity(), id -> Pair.of(initialIndex, 0)));
    }

    public AppendEntries newAppendEntries(Integer slaveId, Integer term) {
        var slaveIndex = nextIndexes.get(slaveId);
        var batch = Math.min(Constants.APPEND_ENTRIES_BATCH_MAX_SIZE, log.get().size() - slaveIndex);
        var entries = log.lastIndex() >= slaveIndex ? log.get().subList(slaveIndex, slaveIndex + batch) : List.<Entry>of();
        var prevLogTerm = log.getByIndex(slaveIndex - 1).map(Entry::term).orElse(null);
        pending.put(slaveId, Pair.of(slaveIndex + batch, matchIndexes.get(slaveId) + batch));
        return new AppendEntries(term, leaderId, slaveIndex - 1, prevLogTerm, entries, 0);
    }

    public void processResponse(Integer slaveId, AppendEntriesResult data) {
        var pair = pending.remove(slaveId);
        if (Objects.nonNull(pair) && nextIndexes.get(slaveId) < pair.getLeft()) {
            if(data.success) {
                nextIndexes.put(slaveId, pair.getLeft());
                matchIndexes.put(slaveId, pair.getRight());
            } else {
                nextIndexes.put(slaveId, nextIndexes.get(slaveId) - 1);
            }
        }
    }

}
