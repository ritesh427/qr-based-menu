package com.restaurant.ordering.config;

import java.math.BigDecimal;
import java.util.List;

import com.restaurant.ordering.entity.AppUser;
import com.restaurant.ordering.entity.Category;
import com.restaurant.ordering.entity.Coupon;
import com.restaurant.ordering.entity.DiningTable;
import com.restaurant.ordering.entity.MenuItemAddon;
import com.restaurant.ordering.entity.MenuItem;
import com.restaurant.ordering.entity.MenuItemVariant;
import com.restaurant.ordering.entity.Restaurant;
import com.restaurant.ordering.enums.Role;
import com.restaurant.ordering.repository.CategoryRepository;
import com.restaurant.ordering.repository.CouponRepository;
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
                               CouponRepository couponRepository,
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
                    withOptions(
                            createItem("Paneer Tikka Bowl", "Charred paneer, saffron rice, mint chutney", new BigDecimal("249"), true, true, 25, 14, veg, "https://images.unsplash.com/photo-1546833999-b9f581a1996d"),
                            List.of(
                                    createVariant("Regular", new BigDecimal("0"), 18, true, 14),
                                    createVariant("Large", new BigDecimal("70"), 10, true, 16)
                            ),
                            List.of(
                                    createAddon("Extra Paneer", new BigDecimal("55"), 14, true, 15),
                                    createAddon("Mint Yogurt", new BigDecimal("25"), 25, true, 14)
                            )
                    ),
                    withOptions(
                            createItem("Truffle Malai Kofta", "Silky gravy with herb naan pairing", new BigDecimal("299"), true, true, 20, 18, veg, "https://images.unsplash.com/photo-1601050690597-df0568f70950"),
                            List.of(
                                    createVariant("Classic", new BigDecimal("0"), 12, true, 18),
                                    createVariant("Chef Feast", new BigDecimal("90"), 8, true, 21)
                            ),
                            List.of(
                                    createAddon("Butter Naan", new BigDecimal("35"), 18, true, 18),
                                    createAddon("Truffle Drizzle", new BigDecimal("45"), 10, true, 19)
                            )
                    ),
                    withOptions(
                            createItem("Chicken Ghee Roast", "Spiced coastal roast with onion salad", new BigDecimal("329"), true, false, 18, 16, nonVeg, "https://images.unsplash.com/photo-1604908176997-431e7d7d8f17"),
                            List.of(
                                    createVariant("Half", new BigDecimal("0"), 10, true, 16),
                                    createVariant("Full", new BigDecimal("120"), 6, true, 20)
                            ),
                            List.of(
                                    createAddon("Malabar Parotta", new BigDecimal("40"), 16, true, 17),
                                    createAddon("Egg Fry", new BigDecimal("30"), 12, true, 16)
                            )
                    ),
                    withOptions(
                            createItem("Smoked Butter Chicken", "Slow simmered tomato sauce and makhani foam", new BigDecimal("349"), true, false, 15, 20, nonVeg, "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398"),
                            List.of(
                                    createVariant("Classic", new BigDecimal("0"), 9, true, 20),
                                    createVariant("Family Bowl", new BigDecimal("160"), 4, true, 24)
                            ),
                            List.of(
                                    createAddon("Garlic Naan", new BigDecimal("45"), 14, true, 21),
                                    createAddon("Jeera Rice", new BigDecimal("60"), 10, true, 20)
                            )
                    ),
                    withOptions(
                            createItem("Masala Soda", "Citrus soda with black salt", new BigDecimal("89"), true, true, 40, 4, drinks, "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd"),
                            List.of(
                                    createVariant("Regular", new BigDecimal("0"), 25, true, 4),
                                    createVariant("Pitcher", new BigDecimal("120"), 8, true, 6)
                            ),
                            List.of(
                                    createAddon("Fresh Lime Boost", new BigDecimal("20"), 20, true, 4)
                            )
                    ),
                    withOptions(
                            createItem("Cold Brew Tonic", "Coffee tonic with orange zest", new BigDecimal("129"), true, true, 30, 5, drinks, "https://images.unsplash.com/photo-1461023058943-07fcbe16d735"),
                            List.of(
                                    createVariant("Regular", new BigDecimal("0"), 18, true, 5),
                                    createVariant("Large", new BigDecimal("35"), 10, true, 6)
                            ),
                            List.of(
                                    createAddon("Vanilla Cream", new BigDecimal("25"), 14, true, 5),
                                    createAddon("Orange Zest Shot", new BigDecimal("18"), 20, true, 5)
                            )
                    )
            ));

            couponRepository.saveAll(List.of(
                    createCoupon(restaurant, "WELCOME10", "10% off on first table order", new BigDecimal("10"), true, new BigDecimal("300"), new BigDecimal("150")),
                    createCoupon(restaurant, "CHEF50", "Flat Rs. 50 off on chef specials", new BigDecimal("50"), false, new BigDecimal("500"), BigDecimal.ZERO)
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
                                int stockQuantity,
                                int estimatedTime,
                                Category category,
                                String imageUrl) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setAvailable(available);
        item.setVegetarian(vegetarian);
        item.setStockQuantity(stockQuantity);
        item.setEstimatedPreparationTime(estimatedTime);
        item.setCategory(category);
        item.setImageUrl(imageUrl);
        return item;
    }

    private MenuItem withOptions(MenuItem item, List<MenuItemVariant> variants, List<MenuItemAddon> addons) {
        for (MenuItemVariant variant : variants) {
            variant.setMenuItem(item);
            variant.setAvailable(variant.isAvailable() && variant.getStockQuantity() > 0);
            item.getVariants().add(variant);
        }
        for (MenuItemAddon addon : addons) {
            addon.setMenuItem(item);
            addon.setAvailable(addon.isAvailable() && addon.getStockQuantity() > 0);
            item.getAddons().add(addon);
        }
        return item;
    }

    private MenuItemVariant createVariant(String name,
                                          BigDecimal priceAdjustment,
                                          int stockQuantity,
                                          boolean available,
                                          int estimatedTime) {
        MenuItemVariant variant = new MenuItemVariant();
        variant.setName(name);
        variant.setPriceAdjustment(priceAdjustment);
        variant.setStockQuantity(stockQuantity);
        variant.setAvailable(available);
        variant.setEstimatedPreparationTime(estimatedTime);
        return variant;
    }

    private MenuItemAddon createAddon(String name,
                                      BigDecimal price,
                                      int stockQuantity,
                                      boolean available,
                                      int estimatedTime) {
        MenuItemAddon addon = new MenuItemAddon();
        addon.setName(name);
        addon.setPrice(price);
        addon.setStockQuantity(stockQuantity);
        addon.setAvailable(available);
        addon.setEstimatedPreparationTime(estimatedTime);
        return addon;
    }

    private Coupon createCoupon(Restaurant restaurant,
                                String code,
                                String description,
                                BigDecimal discountValue,
                                boolean percentage,
                                BigDecimal minimumOrderAmount,
                                BigDecimal maxDiscountAmount) {
        Coupon coupon = new Coupon();
        coupon.setRestaurant(restaurant);
        coupon.setCode(code);
        coupon.setDescription(description);
        coupon.setDiscountValue(discountValue);
        coupon.setPercentage(percentage);
        coupon.setActive(true);
        coupon.setMinimumOrderAmount(minimumOrderAmount);
        coupon.setMaxDiscountAmount(maxDiscountAmount);
        return coupon;
    }
}
