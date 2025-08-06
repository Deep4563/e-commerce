package com.chiragbhisikar.e_commerce.model.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionType {
    USER_MANAGE("user:manage"),// For admin tasks
    CATEGORY_MANAGE("category:manage"); // For admin tasks

    private final String permission;
}
