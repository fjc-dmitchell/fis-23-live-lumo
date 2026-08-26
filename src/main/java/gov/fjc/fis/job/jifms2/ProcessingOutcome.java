package gov.fjc.fis.job.jifms;

import java.util.List;

sealed interface ProcessingOutcome permits ProcessingOutcome.Rejected, ProcessingOutcome.Ignored,
        ProcessingOutcome.Inserted, ProcessingOutcome.Updated {

    String summary();

    record Rejected(List<String> reasons) implements ProcessingOutcome {
        public String summary() {
            return String.join("; ", reasons);
        }
    }

    record Ignored(String reason) implements ProcessingOutcome {
        public String summary() {
            return reason;
        }
    }

    record Inserted(String documentNumber, FieldChanges changes) implements ProcessingOutcome {
        public String summary() {
            return "INSERTED " + documentNumber + " " + changes.summary();
        }
    }

    record Updated(String documentNumber, FieldChanges changes) implements ProcessingOutcome {
        public String summary() {
            return "UPDATED " + documentNumber + " " + changes.summary();
        }
    }
}

/**
 * Structured change set instead of a bare StringBuilder.
 */
record FieldChange(String field, Object from, Object to) {
}

final class FieldChanges {
    private final java.util.List<FieldChange> list = new java.util.ArrayList<>();

    void add(String field, Object from, Object to) {
        list.add(new FieldChange(field, from, to));
    }

    boolean isEmpty() {
        return list.isEmpty();
    }

    String summary() {
        return list.stream()
                .map(fc -> "-" + fc.field() + "[" + fc.from() + "→" + fc.to() + "]")
                .collect(java.util.stream.Collectors.joining(" "));
    }

    List<FieldChange> all() {
        return List.copyOf(list);
    }
}