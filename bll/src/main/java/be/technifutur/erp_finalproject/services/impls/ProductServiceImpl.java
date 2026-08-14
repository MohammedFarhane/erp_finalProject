package be.technifutur.erp_finalproject.services.impls;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.entities.Category;
import be.technifutur.erp_finalproject.entities.Product;
import be.technifutur.erp_finalproject.exceptions.category.CategoryNotFoundException;
import be.technifutur.erp_finalproject.exceptions.product.ProductNotFoundException;
import be.technifutur.erp_finalproject.projections.ProductStock;
import be.technifutur.erp_finalproject.repositories.CategoryRepository;
import be.technifutur.erp_finalproject.repositories.ProductRepository;
import be.technifutur.erp_finalproject.repositories.StockMovementRepository;
import be.technifutur.erp_finalproject.services.productService.ProductForm;
import be.technifutur.erp_finalproject.services.productService.ProductService;
import be.technifutur.erp_finalproject.services.productService.ProductWithStock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ReferenceGenerator referenceGenerator;

    @Override
    public Page<ProductWithStock> search(Long categoryId, String name, Pageable pageable) {
        String pattern = (name == null || name.isBlank())
                ? null
                : "%" + name.toLowerCase() + "%";

        Page<Product> page = productRepository.search(categoryId, pattern, pageable);

        List<Long> ids = page.getContent()
                .stream()
                .map(Product::getId)
                .toList();

        if (ids.isEmpty()) {
            return page.map(p -> new ProductWithStock(p, 0));
        }

        Map<Long, Integer> stocks = stockMovementRepository.computeStocksFor(ids)
                .stream()
                .collect(
                        Collectors
                                .toMap(
                                        ProductStock::getProductId, ProductStock::getStock
                                )
                );

        return page.map(p -> new ProductWithStock(p, stocks.getOrDefault(p.getId(), 0)));
    }

    @Override
    public ProductWithStock findById(Long id) {
        Product product = productRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return new ProductWithStock(product, stockMovementRepository.computeStockForProduct(id));
    }

    @Override
    @Transactional
    public Long create(ProductForm form) {
        Category category = categoryRepository.findById(form.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(form.categoryId()));
        Product product = new Product(
                referenceGenerator.next("PRD"),
                form.name(),
                form.description(),
                form.purchasePrice(),
                form.sellingPrice(),
                form.tvaRate(),
                form.minStockQuantity(),
                category);

        return productRepository.save(product).getId();
    }

    @Transactional
    @Override
    public ProductWithStock update(Long id, ProductForm form) {
        Product product = productRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        Category category = categoryRepository.findById(form.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(form.categoryId()));

        product.setName(form.name());
        product.setDescription(form.description());
        product.setPurchasePrice(form.purchasePrice());
        product.setSellingPrice(form.sellingPrice());
        product.setTvaRate(form.tvaRate());
        product.setMinStockQuantity(form.minStockQuantity());
        product.setCategory(category);

        return new ProductWithStock(productRepository.save(product), stockMovementRepository.computeStockForProduct(id));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Product product = productRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setArchived(true);
        productRepository.save(product);
    }
}
