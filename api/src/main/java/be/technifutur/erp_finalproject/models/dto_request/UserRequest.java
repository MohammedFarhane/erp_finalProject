package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.services.userservice.UserForm;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull UserRole role
) {
    public UserForm toForm() {
        return new UserForm(
                name,
                email,
                password,
                role
        );
    }
}
