package com.savchenko;

public class Constants {
    public static final String HOST = "127.0.0.1";
    public static final Long RENEW_CONNECTION_TIMEOUT = 1500L;
    public static final Long APPEND_ENTRIES_TIMEOUT = 1500L;
    public static final Integer APPEND_ENTRIES_BATCH_SIZE = 5;
    public static final Integer APPEND_ENTRIES_ROLLBACK_BATCH_SIZE = 5;
    public static final Long ELECTION_TIMEOUT = 2000L;
    public static final Long CANDIDATE_TIMEOUT_DURATION = 2000L;
    public static final Long CLIENT_CAS_CHECK_TIMEOUT = 4000L;
}
