package be.technifutur.erp_finalproject.datainitializer;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.entities.*;
import be.technifutur.erp_finalproject.enums.AddressType;
import be.technifutur.erp_finalproject.enums.MovementType;
import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReferenceGenerator referenceGenerator;
    private final StockMovementRepository stockMovementRepository;
    private final SupplierRepository supplierRepository;
    private final Clock clock;
    private final ClientRepository clientRepository;

    @Override
    public void run(String... args) throws Exception {

        if (categoryRepository.count() == 0) {
            Category electronics = categoryRepository.save(new Category("Electronics"));
            Category clothing = categoryRepository.save(new Category("Clothing"));
            Category books = categoryRepository.save(new Category("Books"));
            Category toys = categoryRepository.save(new Category("Toys"));
            Category homeKitchen = categoryRepository.save(new Category("Home & Kitchen"));

            Product iphone = productRepository.save(new Product(referenceGenerator.next("PRD"), "Iphone 15", "Smartphone", new BigDecimal("299.99"), new BigDecimal("499.99"), 0.21, 10, electronics));
            Product samsung = productRepository.save(new Product(referenceGenerator.next("PRD"), "Samsung Galaxy S23", "Smartphone", new BigDecimal("249.99"), new BigDecimal("449.99"), 0.21, 15, electronics));
            Product jeans = productRepository.save(new Product(referenceGenerator.next("PRD"), "Levi's 501 Original Fit Jeans", "Classic straight-leg jeans", new BigDecimal("59.99"), new BigDecimal("79.99"), 0.21, 20, clothing));
            Product adidas = productRepository.save(new Product(referenceGenerator.next("PRD"), "Adidas Ultraboost 21", "Running shoes with responsive cushioning", new BigDecimal("129.99"), new BigDecimal("179.99"), 0.21, 25, clothing));
            Product harryPotter = productRepository.save(new Product(referenceGenerator.next("PRD"), "Harry Potter and the Philosopher's Stone", "Fantasy novel for children", new BigDecimal("12.99"), new BigDecimal("19.99"), 0.21, 30, books));
            Product lego = productRepository.save(new Product(referenceGenerator.next("PRD"), "LEGO Creator Expert Titanic", "Detailed model with 6000 pieces", new BigDecimal("199.99"), new BigDecimal("299.99"), 0.21, 5, toys));
            Product instantPot = productRepository.save(new Product(referenceGenerator.next("PRD"), "Instant Pot Duo 7-in-1", "Multi-cooker for fast and healthy meals", new BigDecimal("79.99"), new BigDecimal("129.99"), 0.21, 15, homeKitchen));
            Product sonyHeadphones = productRepository.save(new Product(referenceGenerator.next("PRD"), "Sony WH-1000XM4", "Wireless noise-canceling headphones", new BigDecimal("249.99"), new BigDecimal("349.99"), 0.21, 10, electronics));
            Product nikeShoes = productRepository.save(new Product(referenceGenerator.next("PRD"), "Nike Air Max 270", "Comfortable running shoes with excellent cushioning", new BigDecimal("119.99"), new BigDecimal("169.99"), 0.21, 20, clothing));

            User admin = userRepository.save(new User("Admin", "admin@admin.be", "test123", UserRole.ADMIN));
            User employee = userRepository.save(new User("Employee", "employee@employee.be", "test123", UserRole.EMPLOYEE));

            stockMovementRepository.saveAll(List.of(
                new StockMovement(MovementType.ENTREE, 50, LocalDateTime.now(clock), iphone, admin),
                new StockMovement(MovementType.AJUSTEMENT_NEGATIF, 15, LocalDateTime.now(clock), samsung, admin),
                new StockMovement(MovementType.AJUSTEMENT_POSITIF, 0, LocalDateTime.now(clock), jeans, admin)
            ));

            Supplier fournisseurBelge =  supplierRepository.save(new Supplier("Fournitures Belges SA",
                            "contact@fournitures.be", "042345678",
                            new Address("Rue de l'Industrie", "45", "4000", "Liège")));
            Supplier materiauxDuNord = supplierRepository.save(new Supplier("Matériaux du Nord SPRL",
                    "info@materiauxdunord.be", "051987654",
                    new Address("Avenue des Artisans", "12", "7000", "Mons")));

            Supplier textilesWallons = supplierRepository.save(new Supplier("Textiles Wallons SA",
                    "contact@textileswallons.be", "081456789",
                    new Address("Chaussée de Namur", "78", "5000", "Namur")));

            Supplier electroDistribution = supplierRepository.save(new Supplier("Électro Distribution Belgique",
                    "commercial@electrodistrib.be", "064321098",
                    new Address("Rue de la Station", "23", "6000", "Charleroi")));

            Supplier boisEtCie = supplierRepository.save(new Supplier("Bois & Cie SPRL",
                    "vente@boisetcie.be", "071654321",
                    new Address("Rue des Forestiers", "5", "6001", "Marcinelle")));

            Client dupont = new Client("Dupont SPRL", "contact@dupont.be", "042111111");
            dupont.getAddresses().add(new TypeAddress(
                    AddressType.LIVRAISON,
                    new Address("Rue Neuve", "12", "4000", "Liège")));
            clientRepository.save(dupont);

            Client martin = new Client("Martin & Fils", "info@martin.be", "022222222");
            martin.getAddresses().add(new TypeAddress(
                    AddressType.LIVRAISON,
                    new Address("Chaussée de Wavre", "230", "1050", "Bruxelles")));
            martin.getAddresses().add(new TypeAddress(
                    AddressType.FACTURATION,
                    new Address("Avenue Louise", "500", "1050", "Bruxelles")));
            clientRepository.save(martin);
        }
    }
}
