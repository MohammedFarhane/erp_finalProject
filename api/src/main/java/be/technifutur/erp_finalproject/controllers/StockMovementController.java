package be.technifutur.erp_finalproject.controllers;

import be.technifutur.erp_finalproject.models.dto_request.StockMovementRequest;
import be.technifutur.erp_finalproject.models.dto_response.StockMovementResponse;
import be.technifutur.erp_finalproject.services.stockmovementservice.StockMovementService;
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
@RequestMapping("/stock-movement")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<PagedModel<StockMovementResponse>> history(
            @RequestParam Long productId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<StockMovementResponse> page = stockMovementService.history(productId, pageable)
                .map(StockMovementResponse::from);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody StockMovementRequest request,
            @AuthenticationPrincipal JwtUtils.UserSession user
            ) {
        Long id = stockMovementService.record(request.toForm(user.id()));

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).build();
    }
}
