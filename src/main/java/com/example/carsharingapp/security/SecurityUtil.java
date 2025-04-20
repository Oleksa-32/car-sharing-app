package com.example.carsharingapp.security;

import com.example.carsharingapp.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static Long getLoggedInUserId() {
        return ((User) getAuthentication().getPrincipal()).getId();
    }
}
