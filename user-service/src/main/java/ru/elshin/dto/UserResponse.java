package ru.elshin.dto;

import ru.elshin.entity.Role;

import java.io.Serializable;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        Role role
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
