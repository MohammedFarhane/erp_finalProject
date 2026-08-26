package be.technifutur.erp_finalproject.services.userservice;

import be.technifutur.erp_finalproject.enums.UserRole;

public record UserForm(
        String name,
        String email,
        String password,
        UserRole role
) {
}
