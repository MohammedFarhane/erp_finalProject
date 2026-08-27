package be.technifutur.erp_finalproject.services.categoryservice;

import be.technifutur.erp_finalproject.entities.Category;
import be.technifutur.erp_finalproject.exceptions.Entities;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.category.CategoryAlreadyExistsException;
import be.technifutur.erp_finalproject.exceptions.category.CategoryNotEmptyException;
import be.technifutur.erp_finalproject.repositories.CategoryRepository;
import be.technifutur.erp_finalproject.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException(Entities.CATEGORY, id));
    }

    @Override
    @Transactional
    public Long save(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new CategoryAlreadyExistsException(category.getName());
        }
        return categoryRepository.save(category).getId();
    }

    @Override
    @Transactional
    public Category update(Long id, Category category) {
        Category exitsingCategory = categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException(Entities.CATEGORY, id));
        if (!exitsingCategory.getName().equals(category.getName())
                && categoryRepository.existsByName(category.getName())) {
            throw new CategoryAlreadyExistsException(category.getName());
        }

        exitsingCategory.setName(category.getName());
        return categoryRepository.save(exitsingCategory);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException(Entities.CATEGORY, id);
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryNotEmptyException(id);
        }
        categoryRepository.deleteById(id);
    }
}