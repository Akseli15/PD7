package pd7.foodrutbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pd7.foodrutbot.entities.MenuItems;
import pd7.foodrutbot.repositories.MenuItemsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    @Autowired
    private MenuItemsRepository menuItemsRepository;

    // Добавление нового блюда
    public MenuItems addMenuItem(MenuItems menuItem) {
        return menuItemsRepository.save(menuItem);
    }

    // Обновление существующего блюда
    public MenuItems updateMenuItem(Integer id, MenuItems updatedMenuItem) {
        Optional<MenuItems> existingMenuItem = menuItemsRepository.findById(id);
        if (existingMenuItem.isPresent()) {
            MenuItems menuItem = existingMenuItem.get();
            menuItem.setName(updatedMenuItem.getName());
            menuItem.setCategory(updatedMenuItem.getCategory());
            menuItem.setPrice(updatedMenuItem.getPrice());
            menuItem.setAvailable(updatedMenuItem.getAvailable());
            menuItem.setStock(updatedMenuItem.getStock());
            menuItem.setImageUrl(updatedMenuItem.getImageUrl());
            return menuItemsRepository.save(menuItem);
        }
        throw new RuntimeException("Menu item not found with id: " + id);
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


