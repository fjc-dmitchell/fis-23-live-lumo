package gov.fjc.fis.job.jifms;

public sealed interface ProcessingResult
        permits ProcessingResult.Inserted,
        ProcessingResult.Updated,
        ProcessingResult.Ignored,
        ProcessingResult.Continue {

    final class Inserted implements ProcessingResult {
        public static final Inserted INSTANCE = new Inserted();
        private Inserted() {}
    }

    final class Updated implements ProcessingResult {
        public static final Updated INSTANCE = new Updated();
        private Updated() {}
    }

    final class Ignored implements ProcessingResult {
        public static final Ignored INSTANCE = new Ignored();
        private Ignored() {}
    }

    final class Continue implements ProcessingResult {
        public static final Continue INSTANCE = new Continue();
        private Continue() {}
    }
}