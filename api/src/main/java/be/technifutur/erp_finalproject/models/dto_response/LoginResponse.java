package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.enums.UserRole;

public record LoginResponse(
        String token,
        String email,
        UserRole role
) {}
