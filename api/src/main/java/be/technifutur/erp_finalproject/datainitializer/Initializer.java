package be.technifutur.erp_finalproject.datainitializer;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.entities.Category;
import be.technifutur.erp_finalproject.entities.Product;
import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.repositories.CategoryRepository;
import be.technifutur.erp_finalproject.repositories.ProductRepository;
import be.technifutur.erp_finalproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReferenceGenerator referenceGenerator;

    @Override
    public void run(String... args) throws Exception {

        if (categoryRepository.count() == 0) {
            Category electronics = categoryRepository.save(new Category("Electronics"));
            Category clothing = categoryRepository.save(new Category("Clothing"));
            Category books = categoryRepository.save(new Category("Books"));
            Category toys = categoryRepository.save(new Category("Toys"));
            Category homeKitchen = categoryRepository.save(new Category("Home & Kitchen"));

            productRepository.saveAll(List.of(
                    new Product(referenceGenerator.next("PRD"), "Iphone 15", "Smartphone", 299.99, 499.99, 0.21, 10, electronics),
                    new Product(referenceGenerator.next("PRD"), "Samsung Galaxy S23", "Smartphone", 249.99, 449.99, 0.21, 15, electronics),
                    new Product(referenceGenerator.next("PRD"), "Levi's 501 Original Fit Jeans", "Classic straight-leg jeans", 59.99, 79.99, 0.21, 20, clothing),
                    new Product(referenceGenerator.next("PRD"), "Adidas Ultraboost 21", "Running shoes with responsive cushioning", 129.99, 179.99, 0.21, 25, clothing),
                    new Product(referenceGenerator.next("PRD"), "Harry Potter and the Philosopher's Stone", "Fantasy novel for children", 12.99, 19.99, 0.21, 30, books),
                    new Product(referenceGenerator.next("PRD"), "LEGO Creator Expert Titanic", "Detailed model with 6000 pieces", 199.99, 299.99, 0.21, 5, toys),
                    new Product(referenceGenerator.next("PRD"), "Instant Pot Duo 7-in-1", "Multi-cooker for fast and healthy meals", 79.99, 129.99, 0.21, 15, homeKitchen),
                    new Product(referenceGenerator.next("PRD"), "Sony WH-1000XM4", "Wireless noise-canceling headphones", 249.99, 349.99, 0.21, 10, electronics),
                    new Product(referenceGenerator.next("PRD"), "Nike Air Max 270", "Comfortable running shoes with excellent cushioning", 119.99, 169.99, 0.21, 20, clothing)
            ));
        }

        if (userRepository.count() == 0) {
            var users = List.of(
                    new User("Admin", "admin@admin.be", "test123", UserRole.ADMIN),
                    new User("Employee", "employee@employee.be", "test123", UserRole.EMPLOYEE)
            );
            userRepository.saveAll(users);
        }

    }
}
