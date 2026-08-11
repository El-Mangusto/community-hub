package com.elmangusto.communityhub.entity.enums;

public enum UserRole {
    USER(0),
    ADMIN(1),
    SUPER_ADMIN(2);

    private final int level;

    UserRole(int level) { this.level = level; }

    public boolean isAtLeast(UserRole other) { return this.level >= other.level; }
}
