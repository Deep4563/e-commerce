package com.chiragbhisikar.e_commerce.security;


import com.chiragbhisikar.e_commerce.model.auth.PermissionType;
import com.chiragbhisikar.e_commerce.model.auth.RoleType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.chiragbhisikar.e_commerce.model.auth.PermissionType.USER_MANAGE;
import static com.chiragbhisikar.e_commerce.model.auth.RoleType.ADMIN;

public class RolePermissionMapping {

    private static final Map<RoleType, Set<PermissionType>> map = Map.of(
            ADMIN, Set.of(USER_MANAGE)
    );

    public static Set<SimpleGrantedAuthority> getAuthoritiesForRole(RoleType role) {
        return map.get(role).stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
    }
}
