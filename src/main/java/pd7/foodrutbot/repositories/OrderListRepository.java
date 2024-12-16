package pd7.foodrutbot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pd7.foodrutbot.entities.OrderList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderListRepository extends JpaRepository<OrderList, Integer> {

    // Найти заказ по номеру
    Optional<OrderList> findByOrderNumber(String orderNumber);

    // Найти заказы по статусу
    List<OrderList> findByStatus(OrderList.OrderStatus status);

    // Найти заказы по chatId
    List<OrderList> findByChatId(String chatId);

    // Найти заказы, созданные после заданной даты
    List<OrderList> findByCreatedAtAfter(LocalDateTime date);
}
