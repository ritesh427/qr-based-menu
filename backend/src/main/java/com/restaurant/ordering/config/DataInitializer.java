package com.restaurant.ordering.config;

import java.math.BigDecimal;
import java.util.List;

import com.restaurant.ordering.entity.AppUser;
import com.restaurant.ordering.entity.Category;
import com.restaurant.ordering.entity.DiningTable;
import com.restaurant.ordering.entity.MenuItem;
import com.restaurant.ordering.entity.Restaurant;
import com.restaurant.ordering.enums.Role;
import com.restaurant.ordering.repository.CategoryRepository;
import com.restaurant.ordering.repository.DiningTableRepository;
import com.restaurant.ordering.repository.MenuItemRepository;
import com.restaurant.ordering.repository.RestaurantRepository;
import com.restaurant.ordering.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(RestaurantRepository restaurantRepository,
                               DiningTableRepository diningTableRepository,
                               CategoryRepository categoryRepository,
                               MenuItemRepository menuItemRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (restaurantRepository.count() > 0) {
                return;
            }

            Restaurant restaurant = new Restaurant();
            restaurant.setName("Saffron Table");
            restaurant.setSlug("saffron-table");
            restaurant.setDescription("Modern Indian dining with fast QR ordering.");
            restaurant.setLogoUrl("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4");
            restaurant = restaurantRepository.save(restaurant);

            for (int index = 1; index <= 6; index++) {
                DiningTable table = new DiningTable();
                table.setRestaurant(restaurant);
                table.setTableNumber(index);
                table.setQrCodeToken("saffron-table-t" + index);
                diningTableRepository.save(table);
            }

            Category veg = createCategory(categoryRepository, restaurant, "Veg", "Fresh vegetarian mains");
            Category nonVeg = createCategory(categoryRepository, restaurant, "Non-Veg", "Chef specials with meat");
            Category drinks = createCategory(categoryRepository, restaurant, "Drinks", "Cold, hot, and sparkling");

            menuItemRepository.saveAll(List.of(
                    createItem("Paneer Tikka Bowl", "Charred paneer, saffron rice, mint chutney", new BigDecimal("249"), true, true, 14, veg, "https://images.unsplash.com/photo-1546833999-b9f581a1996d"),
                    createItem("Truffle Malai Kofta", "Silky gravy with herb naan pairing", new BigDecimal("299"), true, true, 18, veg, "https://images.unsplash.com/photo-1601050690597-df0568f70950"),
                    createItem("Chicken Ghee Roast", "Spiced coastal roast with onion salad", new BigDecimal("329"), true, false, 16, nonVeg, "https://images.unsplash.com/photo-1604908176997-431e7d7d8f17"),
                    createItem("Smoked Butter Chicken", "Slow simmered tomato sauce and makhani foam", new BigDecimal("349"), true, false, 20, nonVeg, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398"),
                    createItem("Masala Soda", "Citrus soda with black salt", new BigDecimal("89"), true, true, 4, drinks, "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd"),
                    createItem("Cold Brew Tonic", "Coffee tonic with orange zest", new BigDecimal("129"), true, true, 5, drinks, "https://images.unsplash.com/photo-1461023058943-07fcbe16d735")
            ));

            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setRestaurantId(restaurant.getId());
            userRepository.save(admin);
        };
    }

    private Category createCategory(CategoryRepository repository, Restaurant restaurant, String name, String description) {
        Category category = new Category();
        category.setRestaurant(restaurant);
        category.setName(name);
        category.setDescription(description);
        return repository.save(category);
    }

    private MenuItem createItem(String name,
                                String description,
                                BigDecimal price,
                                boolean available,
                                boolean vegetarian,
                                int estimatedTime,
                                Category category,
                                String imageUrl) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setAvailable(available);
        item.setVegetarian(vegetarian);
        item.setEstimatedPreparationTime(estimatedTime);
        item.setCategory(category);
        item.setImageUrl(imageUrl);
        return item;
    }
}
