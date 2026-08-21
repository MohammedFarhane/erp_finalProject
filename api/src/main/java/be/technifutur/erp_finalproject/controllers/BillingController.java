package be.technifutur.erp_finalproject.controllers;

import be.technifutur.erp_finalproject.enums.BillingState;
import be.technifutur.erp_finalproject.models.dto_request.BillingRequest;
import be.technifutur.erp_finalproject.models.dto_request.PaymentRequest;
import be.technifutur.erp_finalproject.models.dto_response.BillingResponse;
import be.technifutur.erp_finalproject.models.dto_response.BillingSummaryResponse;
import be.technifutur.erp_finalproject.services.billingservice.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping
    public ResponseEntity<PagedModel<BillingSummaryResponse>> search(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) BillingState state,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<BillingSummaryResponse> page = billingService
                .search(reference, clientName, state, from, to, pageable)
                .map(BillingSummaryResponse::from);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillingResponse> findById(
            @PathVariable Long id
    ) {
        BillingResponse response = BillingResponse
                .fromBillingResponse(billingService.findById(id));

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody BillingRequest request
    ) {
        Long id = billingService.create(request.toForm());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PostMapping(value = "/{id}/validate")
    public ResponseEntity<BillingResponse> validate(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        BillingResponse response = BillingResponse
                .fromBillingResponse(billingService.validate(id, userId));

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/pay")
    public ResponseEntity<BillingResponse> pay(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request
    ) {
        BillingResponse response = BillingResponse
                .fromBillingResponse(billingService.pay(id, request.toForm()));

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/cancel")
    public ResponseEntity<BillingResponse> cancel(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        BillingResponse response = BillingResponse
                .fromBillingResponse(billingService.cancel(id, userId));

        return ResponseEntity.ok(response);
    }
}
