package be.technifutur.erp_finalproject.services;

import be.technifutur.erp_finalproject.entities.Category;

import java.util.List;

public interface CategoryService {

    List<Category> findAll();

    Category findById(Long id);

    Long save(Category category);

    Category update(Long id, Category category);

    void delete(Long id);
}