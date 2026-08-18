package com.swappy.dto;

import java.util.Set;

import com.swappy.entities.User;
import com.swappy.entities.enums.Role;

public record UserProfileDto(Long id, String name, String email, Set<Role> roles) {

    public static UserProfileDto from(User user) {
        return new UserProfileDto(user.getId(), user.getName(), user.getEmail(), Set.copyOf(user.getRoles()));
    }
}
