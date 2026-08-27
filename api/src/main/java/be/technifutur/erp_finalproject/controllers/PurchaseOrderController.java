package be.technifutur.erp_finalproject.controllers;

import be.technifutur.erp_finalproject.enums.PurchaseOrderState;
import be.technifutur.erp_finalproject.models.dto_request.PurchaseOrderRequest;
import be.technifutur.erp_finalproject.models.dto_response.PurchaseOrderResponse;
import be.technifutur.erp_finalproject.models.dto_response.PurchaseOrderSummaryResponse;
import be.technifutur.erp_finalproject.services.purchaseorderservice.PurchaseOrderService;
import be.technifutur.erp_finalproject.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase-order")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<PagedModel<PurchaseOrderSummaryResponse>> search(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) PurchaseOrderState state,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<PurchaseOrderSummaryResponse> page = purchaseOrderService
                .search(reference, supplierName, state, pageable)
                .map(PurchaseOrderSummaryResponse::from);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> findById(
            @PathVariable Long id
    ) {
        PurchaseOrderResponse response = PurchaseOrderResponse
                .from(purchaseOrderService.findById(id));

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody PurchaseOrderRequest request,
            @AuthenticationPrincipal JwtUtils.UserSession user
            ) {
        Long id = purchaseOrderService.create(request.toForm(user.id()));

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receive(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUtils.UserSession user
    ) {
        PurchaseOrderResponse response = PurchaseOrderResponse
                .from(purchaseOrderService.receive(id, user.id()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(
            @PathVariable Long id
    ) {
        PurchaseOrderResponse response = PurchaseOrderResponse
                .from(purchaseOrderService.cancel(id));

        return ResponseEntity.ok(response);
    }
}