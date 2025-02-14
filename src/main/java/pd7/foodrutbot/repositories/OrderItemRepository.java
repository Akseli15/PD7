package pd7.foodrutbot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pd7.foodrutbot.entities.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.createdAt > :createdAt")
    List<OrderItem> findByOrderCreatedAtAfter(@Param("createdAt") LocalDateTime createdAt);
}
