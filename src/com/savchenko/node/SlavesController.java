package com.savchenko.node;

import com.savchenko.data.AppendEntries;
import com.savchenko.data.AppendEntriesResult;
import com.savchenko.data.LogEntry;
import org.apache.commons.lang3.tuple.Pair;

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
        var bound = log.lastIndex();
        var entries = log.lastIndex() >= slaveIndex ? log.get().subList(slaveIndex, bound) : List.<LogEntry>of();
        pending.put(slaveId, Pair.of(bound, matchIndexes.get(slaveId)));
        return new AppendEntries(term, leaderId, 0, null, entries, 0);
    }

    public void processResponse(Integer slaveId, AppendEntriesResult data) {
        var pair = pending.remove(slaveId);
        if (Objects.nonNull(pair)) {
            if(data.success) {
                nextIndexes.put(slaveId, pair.getLeft());
                matchIndexes.put(slaveId, pair.getRight());
            } else {
                nextIndexes.put(slaveId, nextIndexes.get(slaveId) - 1);
            }
        }
    }
}
