package be.technifutur.erp_finalproject.services.userservice;

import be.technifutur.erp_finalproject.enums.UserRole;

public record UserUpdateForm(
        String name,
        String email,
        UserRole role
) {
}
