package be.technifutur.erp_finalproject.services.clientservice;

import be.technifutur.erp_finalproject.entities.Client;
import be.technifutur.erp_finalproject.entities.TypeAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ClientService {

    Page<Client> search(String name, String email, Pageable pageable);

    Client findById(Long id);

    Long create(ClientForm form);

    Client update(Long id, ClientForm form);

    void delete(Long id);

    Client addAddress(Long clientId, TypeAddress address);

    Client replaceAddress(Long clientId, Set<TypeAddress> addresses);
}