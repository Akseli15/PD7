package pd7.foodrutbot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pd7.foodrutbot.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
