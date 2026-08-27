package be.technifutur.erp_finalproject.controllers;

import be.technifutur.erp_finalproject.models.dto_request.CompanyRequest;
import be.technifutur.erp_finalproject.models.dto_response.CompanyResponse;
import be.technifutur.erp_finalproject.services.companyservice.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<CompanyResponse> getCompany() {

        CompanyResponse response = CompanyResponse.from(companyService.find());

        return ResponseEntity.ok().body(response);
    }

    @PutMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyResponse> update(
            @Valid @RequestBody CompanyRequest request
    ) {
        CompanyResponse response = CompanyResponse.from(companyService.update(request.toForm()));

        return ResponseEntity.ok().body(response);
    }
}
