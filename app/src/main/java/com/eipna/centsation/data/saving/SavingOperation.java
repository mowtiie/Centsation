package com.eipna.centsation.data.saving;

public enum SavingOperation {
    TRANSACTION,
    HISTORY,
    SHARE,
    ARCHIVE,
    UNARCHIVE,
    DELETE;

    private static SavingOperation[] operations;

    static {
        operations = values();
    }
}