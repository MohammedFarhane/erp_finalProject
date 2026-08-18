package be.technifutur.erp_finalproject.services.clientservice;

import be.technifutur.erp_finalproject.entities.Client;
import be.technifutur.erp_finalproject.entities.TypeAddress;
import be.technifutur.erp_finalproject.enums.AddressType;
import be.technifutur.erp_finalproject.exceptions.client.ClientAlreadyExistsException;
import be.technifutur.erp_finalproject.exceptions.client.ClientNotFoundException;
import be.technifutur.erp_finalproject.exceptions.client.InvalidClientAddressesException;
import be.technifutur.erp_finalproject.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional( readOnly = true)
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    public Page<Client> search(String name, String email, Pageable pageable) {

        String namePattern = (name == null || name.isBlank())
                ? null
                : "%" + name.toLowerCase() + "%";

        String emailPattern = (email == null || email.isBlank())
                ? null
                : "%" + email.toLowerCase() + "%";

        return clientRepository.search(namePattern, emailPattern, pageable);
    }

    @Override
    public Client findById(Long id) {
        return clientRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Override
    @Transactional
    public Long create(ClientForm form) {

        if (clientRepository.existsByEmail(form.email())){
            throw new ClientAlreadyExistsException(form.email());
        }

        Client client = new Client(
                form.name(),
                form.email(),
                form.phone()
        );

        client.getAddresses().add(new TypeAddress(AddressType.LIVRAISON, form.address()));

        return clientRepository.save(client).getId();

    }

    @Transactional
    @Override
    public Client update(Long id, ClientForm form) {

        Client client = clientRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        if (!client.getEmail().equals(form.email()) && clientRepository.existsByEmail(form.email())){
            throw new ClientAlreadyExistsException(form.email());
        }

        client.setName(form.name());
        client.setEmail(form.email());
        client.setPhone(form.phone());

        return clientRepository.save(client);
    }

    @Transactional
    @Override
    public void delete(Long id) {

        Client client = clientRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        client.setArchived(true);

        clientRepository.save(client);
    }

    @Transactional
    @Override
    public Client addAddress(Long clientId, TypeAddress address) {

        Client client = clientRepository.findByIdAndArchivedFalse(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        boolean hasBillingAddress = client.getAddresses()
                .stream()
                .anyMatch(a -> a.getType() == AddressType.FACTURATION);

        if (AddressType.FACTURATION == address.getType() && hasBillingAddress) {
            throw InvalidClientAddressesException.multipleBilling(clientId);
        }

        client.getAddresses().add(address);

        return clientRepository.save(client);
    }

    @Transactional
    @Override
    public Client replaceAddress(Long clientId, Set<TypeAddress> addresses) {

        Client client = clientRepository.findByIdAndArchivedFalse(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        long billingCount = addresses.stream()
                .filter(a -> a.getType() == AddressType.FACTURATION)
                .count();

        if (billingCount > 1) {
            throw InvalidClientAddressesException.multipleBilling(clientId);
        }

        boolean hasDelivery = addresses.stream()
                        .anyMatch(a -> a.getType() == AddressType.LIVRAISON);

        if (!hasDelivery) {
            throw InvalidClientAddressesException.missingDelivery(clientId);
        }

        client.getAddresses().clear();
        client.getAddresses().addAll(addresses);

        return clientRepository.save(client);
    }
}