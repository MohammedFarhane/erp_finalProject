package be.technifutur.erp_finalproject.controllers;

import be.technifutur.erp_finalproject.enums.QuoteState;
import be.technifutur.erp_finalproject.models.dto_request.QuoteRequest;
import be.technifutur.erp_finalproject.models.dto_response.QuoteResponse;
import be.technifutur.erp_finalproject.models.dto_response.QuoteSummaryResponse;
import be.technifutur.erp_finalproject.services.quoteservice.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/quote")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping
    public ResponseEntity<PagedModel<QuoteSummaryResponse>> search(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) QuoteState state,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<QuoteSummaryResponse> page = quoteService
                .search(reference, clientName, state, pageable)
                .map(QuoteSummaryResponse::from);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponse> findById(
            @PathVariable Long id
    ) {
        QuoteResponse response = QuoteResponse
                .fromQuoteResponse(quoteService.findById(id));

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody QuoteRequest request
    ) {
        Long id = quoteService.create(request.toForm());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<QuoteResponse> send(
            @PathVariable Long id
    ) {
        QuoteResponse response = QuoteResponse
                .fromQuoteResponse(quoteService.send(id));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<QuoteResponse> accept(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        QuoteResponse response = QuoteResponse
                .fromQuoteResponse(quoteService.accept(id, userId));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refuse")
    public ResponseEntity<QuoteResponse> refuse(
            @PathVariable Long id
    ) {
        QuoteResponse response = QuoteResponse
                .fromQuoteResponse(quoteService.refuse(id));

        return ResponseEntity.ok(response);
    }

}
