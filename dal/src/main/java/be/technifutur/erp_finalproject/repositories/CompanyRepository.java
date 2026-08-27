package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Company findByName(String name);
}
