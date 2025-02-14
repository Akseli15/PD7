package pd7.foodrutbot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pd7.foodrutbot.entities.Favorites;
import pd7.foodrutbot.entities.MenuItems;

import java.util.List;

public interface FavoritesRepository extends JpaRepository<Favorites, Integer> {

    @Query("SELECT m FROM MenuItems m WHERE m.id IN (SELECT f.menuItem.id FROM Favorites f WHERE f.chatID = :chatID)")
    List<MenuItems> findFavoriteMenuItemsByChatID(@Param("chatID") String chatID);
}
