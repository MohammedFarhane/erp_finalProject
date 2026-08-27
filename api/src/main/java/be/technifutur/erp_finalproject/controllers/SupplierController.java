package be.technifutur.erp_finalproject.controllers;

import be.technifutur.erp_finalproject.models.dto_request.SupplierRequest;
import be.technifutur.erp_finalproject.models.dto_response.SupplierResponse;
import be.technifutur.erp_finalproject.services.supplierservice.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<PagedModel<SupplierResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<SupplierResponse> page = supplierService.search(name, email, pageable)
                .map(SupplierResponse::from);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> findById(
            @PathVariable Long id
    ) {
        SupplierResponse response = SupplierResponse.from(supplierService.findById(id));

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> create(
            @Valid @RequestBody SupplierRequest request
    ) {
        Long id = supplierService.create(request.toForm());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request
    ) {
        SupplierResponse response = SupplierResponse.from(supplierService.update(id, request.toForm()));

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        supplierService.delete(id);

        return  ResponseEntity.noContent().build();
    }
}
