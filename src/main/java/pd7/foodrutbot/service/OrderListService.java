package pd7.foodrutbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pd7.foodrutbot.entities.OrderItem;
import pd7.foodrutbot.entities.OrderList;
import pd7.foodrutbot.repositories.OrderItemRepository;
import pd7.foodrutbot.repositories.OrderListRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderListService {

    @Autowired
    private OrderListRepository orderListRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public List<OrderList> getAllOrderList() {
        return orderListRepository.findAll();
    }

    // Создание нового заказа
    @Transactional
    public OrderList createOrder(String orderNumber,  String chatId) {
        // Создаем новый заказ
        OrderList orderList = new OrderList();
        orderList.setOrderNumber(orderNumber);
        orderList.setStatus(OrderList.OrderStatus.ЗАКАЗ_ГОТОВИТСЯ);
        orderList.setCreatedAt(LocalDateTime.now());
        orderList.setChatId(chatId);

        // Сохраняем заказ в базе
        OrderList savedOrder = orderListRepository.save(orderList);

        return savedOrder;
    }


    // Получить заказ по номеру
    public Optional<OrderList> getOrderByNumber(String orderNumber) {
        return orderListRepository.findByOrderNumber(orderNumber);
    }

    // Получить все заказы по статусу
    public List<OrderList> getOrdersByStatus(OrderList.OrderStatus status) {
        return orderListRepository.findByStatus(status);
    }

    // Получить все заказы по chatId
    public List<OrderList> getOrdersByChatId(String chatId) {
        return orderListRepository.findByChatId(chatId);
    }

    // Обновить статус заказа
    public Optional<OrderList> updateOrderStatus(Integer orderId, OrderList.OrderStatus newStatus) {
        Optional<OrderList> orderList = orderListRepository.findById(orderId);
        if (orderList.isPresent()) {
            OrderList order = orderList.get();
            order.setStatus(newStatus);
            return Optional.of(orderListRepository.save(order));
        }
        return Optional.empty();
    }

    // Удалить заказ
    public void deleteOrder(Integer orderId) {
        orderListRepository.deleteById(orderId);
    }


    // Метод для получения статистики по продажам блюд за последнюю неделю
    public Map<String, Object> getWeeklyDishSales() {
        // Получаем дату 7 дней назад
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        // Получаем все заказанные позиции за последнюю неделю
        List<OrderItem> orderItems = orderItemRepository.findByOrderCreatedAtAfter(oneWeekAgo);

        // Группируем данные по названию блюда и суммируем количество продаж
        Map<String, Integer> dishSales = orderItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getMenuItem().getName(),
                        Collectors.summingInt(OrderItem::getQuantity)
                ));

        // Формируем результат в виде нужного формата
        Map<String, Object> result = new HashMap<>();
        result.put("наименование товара", new ArrayList<>(dishSales.keySet()));
        result.put("количество", new ArrayList<>(dishSales.values()));

        return result;
    }

    // Метод для парсинга JSON из строки menuItems
    private List<Map<String, Object>> parseMenuItemsJson(String menuItemsJson) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(menuItemsJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

