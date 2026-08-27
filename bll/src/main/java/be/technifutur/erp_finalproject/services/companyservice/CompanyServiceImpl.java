package be.technifutur.erp_finalproject.services.companyservice;

import be.technifutur.erp_finalproject.entities.Company;
import be.technifutur.erp_finalproject.exceptions.Entities;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public Company find() {
        return companyRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(Entities.COMPANY));
    }

    @Override
    @Transactional
    public Company update(CompanyForm form) {

        Company company = companyRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(Entities.COMPANY));

        company.setName(form.name());
        company.setTvaNumber(form.tvaNumber());
        company.setIban(form.iban());
        company.setEmail(form.email());
        company.setPhone(form.phone());
        company.setAddress(form.address());

        return companyRepository.save(company);
    }
}
