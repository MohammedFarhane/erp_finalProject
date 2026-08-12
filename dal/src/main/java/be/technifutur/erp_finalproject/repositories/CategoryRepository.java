package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("select count(c) > 0 from Category c where c.name ilike :name")
    boolean existsByName(String name);
}
