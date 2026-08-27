package be.technifutur.erp_finalproject.services.companyservice;

import be.technifutur.erp_finalproject.entities.Company;

import java.util.Optional;

public interface CompanyService {

    Company find();

    Company update(CompanyForm form);
}
