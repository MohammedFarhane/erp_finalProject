package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.entities.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank @Size(max = 100)
        String name
) {
    public Category toCategory() {
        return new Category(name);
    }
}
