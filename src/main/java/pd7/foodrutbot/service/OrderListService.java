package pd7.foodrutbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pd7.foodrutbot.entities.OrderList;
import pd7.foodrutbot.repositories.OrderListRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class OrderListService {

    @Autowired
    private OrderListRepository orderListRepository;

    public List<OrderList> getAllOrderList() {
        return orderListRepository.findAll();
    }

    // Создание нового заказа
    public OrderList createOrder(String orderNumber, String menuItemsJson, String chatId) {
        OrderList orderList = new OrderList();
        orderList.setOrderNumber(orderNumber);
        orderList.setMenuItems(menuItemsJson);
        orderList.setStatus(OrderList.OrderStatus.ЗАКАЗ_В_ОЧЕРЕДИ);
        orderList.setCreatedAt(LocalDateTime.now());
        orderList.setChatId(chatId);
        return orderListRepository.save(orderList);
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
        LocalDateTime oneWeekAgo = LocalDateTime.now().minus(1, ChronoUnit.WEEKS);

        // Получаем все заказы, сделанные за последнюю неделю
        List<OrderList> orders = orderListRepository.findByCreatedAtAfter(oneWeekAgo);

        Map<String, Integer> dishSales = new HashMap<>();  // Карта для хранения количества продаж по блюду

        // Обрабатываем каждый заказ
        for (OrderList order : orders) {
            // Парсим меню из JSON
            List<Map<String, Object>> menuItems = parseMenuItemsJson(order.getMenuItems());

            // Для каждого блюда в заказе увеличиваем количество продаж
            for (Map<String, Object> item : menuItems) {
                String menuItemName = (String) item.get("menuItemName");
                Integer quantity = (Integer) item.get("quantity");

                // Увеличиваем количество заказанных блюд
                dishSales.put(menuItemName, dishSales.getOrDefault(menuItemName, 0) + quantity);
            }
        }

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

