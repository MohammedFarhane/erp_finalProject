package be.technifutur.erp_finalproject.controllers;

import be.technifutur.erp_finalproject.entities.Client;
import be.technifutur.erp_finalproject.entities.TypeAddress;
import be.technifutur.erp_finalproject.models.dto_request.ClientRequest;
import be.technifutur.erp_finalproject.models.dto_request.TypeAddressRequest;
import be.technifutur.erp_finalproject.models.dto_response.ClientResponse;
import be.technifutur.erp_finalproject.services.clientservice.ClientService;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<PagedModel<ClientResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ClientResponse> page = clientService.search(name, email, pageable)
                .map(ClientResponse::fromClient);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> findById(
            @PathVariable Long id
    ) {
        ClientResponse response = ClientResponse.fromClient(clientService.findById(id));

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody ClientRequest request
    ) {
        Long id = clientService.create(request.toForm());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request
    ) {
        ClientResponse response = ClientResponse.fromClient(clientService.update(id, request.toForm()));

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        clientService.delete(id);

        return  ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<ClientResponse> addAddress(
            @PathVariable Long id,
            @Valid @RequestBody TypeAddressRequest request
    ) {
        Client client = clientService.addAddress(id, request.toTypeAddress());

        return ResponseEntity.ok().body(ClientResponse.fromClient(client));
    }

    @PutMapping("/{id}/addresses")
    public ResponseEntity<ClientResponse> replaceAddresses(
            @PathVariable Long id,
            @Valid @RequestBody List<TypeAddressRequest> requests
    ) {
        Set<TypeAddress> addresses = requests
                .stream()
                .map(TypeAddressRequest::toTypeAddress)
                .collect(Collectors.toSet());

        Client client = clientService.replaceAddress(id, addresses);

        return ResponseEntity.ok().body(ClientResponse.fromClient(client));
    }
}