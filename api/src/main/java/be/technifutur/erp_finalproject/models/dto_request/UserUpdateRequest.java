package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.services.userservice.UserUpdateForm;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 100) String email,
        @NotNull UserRole role
) {
    public UserUpdateForm toForm() {
        return new UserUpdateForm(
                name,
                email,
                role
        );
    }
}
