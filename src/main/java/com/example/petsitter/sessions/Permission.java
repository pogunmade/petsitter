package com.example.petsitter.sessions;

import java.util.Optional;

public class Permission {

    public static final Permission IS_GRANTED = new Permission(Decision.GRANTED);
    public static final Permission IS_DENIED = new Permission(Decision.DENIED);

    private final Decision decision;

    private final String reason;

    private Permission(Decision decision) {
        this(decision, null);
    }

    Permission(Decision decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }

    public boolean isDenied() {
        return decision == Decision.DENIED;
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    enum Decision { GRANTED, DENIED }

    public enum Resource { USER, JOB, JOB_APPLICATION }

    public enum Action { CREATE, VIEW, MODIFY, DELETE }

    public enum Attribute {

        JOB_APPLICATION_ATT,
        JOB_APPLICATION_DTO_ATT,
        JOB_APPLICATION_OWNER_ID_ATT,
        JOB_DTO_ATT,
        JOB_ID_ATT,
        JOB_OWNER_ID_ATT,
        USER_DTO_ATT,
        USER_ID_ATT
    }
}
