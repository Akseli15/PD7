package pd7.foodrutbot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pd7.foodrutbot.entities.MenuItems;

import java.util.List;

@Repository
public interface MenuItemsRepository extends JpaRepository<MenuItems, Integer> {

    // Поиск доступных блюд
    List<MenuItems> findByAvailableTrue();

    // Поиск блюд по названию (можно частично)
    List<MenuItems> findByNameContainingIgnoreCase(String name);
}
