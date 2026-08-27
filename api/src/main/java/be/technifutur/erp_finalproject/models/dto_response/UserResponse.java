package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.UserRole;

public record UserResponse(
        Long id,
        String email,
        String name,
        UserRole role,
        boolean archived
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isArchived()
        );
    }
}
