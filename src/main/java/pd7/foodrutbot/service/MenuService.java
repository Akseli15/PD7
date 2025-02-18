package pd7.foodrutbot.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pd7.foodrutbot.entities.Category;
import pd7.foodrutbot.entities.MenuItems;
import pd7.foodrutbot.repositories.MenuItemsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    @Autowired
    private MenuItemsRepository menuItemsRepository;

    @Autowired
    private CategoryService categoryService;

    // Добавление нового блюда
    public MenuItems addMenuItem(MenuItems menuItem) {
        return menuItemsRepository.save(menuItem);
    }

    // Обновление существующего блюда
    public MenuItems updateMenuItem(MenuItems updatedMenuItem) {
        if (updatedMenuItem.getId() == null) {
            throw new IllegalArgumentException("ID cannot be null for update operation");
        }

        MenuItems existingMenuItem = getMenuItemById(updatedMenuItem.getId());

        existingMenuItem.setName(updatedMenuItem.getName());
        existingMenuItem.setPrice(updatedMenuItem.getPrice());
        existingMenuItem.setStock(updatedMenuItem.getStock());
        existingMenuItem.setAvailable(updatedMenuItem.getAvailable());
        existingMenuItem.setImageUrl(updatedMenuItem.getImageUrl());

        if (updatedMenuItem.getCategory() != null) {
            Category updatedCategory = categoryService.getCategoryById(updatedMenuItem.getCategory().getId());
            if (updatedCategory != null) {
                existingMenuItem.setCategory(updatedCategory);
            }
        }

        return menuItemsRepository.save(existingMenuItem);
    }


    // Удаление блюда по id
    public void deleteMenuItem(Integer id) {
        menuItemsRepository.deleteById(id);
    }

    // Получение блюда по id
    public MenuItems getMenuItemById(Integer id) {
        return menuItemsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));
    }

    // Получение всех блюд
    public List<MenuItems> getAllMenuItems() {
        return menuItemsRepository.findAll();
    }

    // Получение всех доступных блюд
    public List<MenuItems> getAvailableMenuItems() {
        return menuItemsRepository.findByAvailableTrue();
    }

    // Поиск блюд по названию
    public List<MenuItems> searchMenuItemsByName(String name) {
        return menuItemsRepository.findByNameContainingIgnoreCase(name);
    }
}


