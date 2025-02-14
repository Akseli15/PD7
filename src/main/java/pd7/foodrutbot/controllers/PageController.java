package pd7.foodrutbot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pd7.foodrutbot.entities.MenuItems;
import pd7.foodrutbot.entities.OrderItem;
import pd7.foodrutbot.entities.OrderList;
import pd7.foodrutbot.repositories.MenuItemsRepository;
import pd7.foodrutbot.service.MenuService;
import pd7.foodrutbot.service.OrderListService;
import pd7.foodrutbot.service.TelegramBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://bot-techsupport.rightscan.ru")
public class PageController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private OrderListService orderListService;

    @Autowired
    private MenuItemsRepository menuItemsRepository;

    @Autowired
    private TelegramBot telegramBot;

    //Общие контроллеры

    // Получить все блюда
    @GetMapping("/menu/all")
    public ResponseEntity<List<MenuItems>> getAllMenuItems() {
        List<MenuItems> availableItems = menuService.getAllMenuItems();
        return ResponseEntity.ok(availableItems);
    }

    // Получить все блюда
    @GetMapping("/order/all")
    public ResponseEntity<List<OrderList>> getAllOrders() {
        List<OrderList> availableItems = orderListService.getAllOrderList();
        return ResponseEntity.ok(availableItems);
    }

    //Админские контроллеры

    // Добавить новое блюдо
    @PostMapping("/menu")
    public ResponseEntity<MenuItems> addMenuItem(@RequestBody MenuItems menuItem) {
        MenuItems createdItem = menuService.addMenuItem(menuItem);
        return ResponseEntity.ok(createdItem);
    }

    // Обновить блюдо по ID
    @PutMapping("/menu/{id}")
    public ResponseEntity<MenuItems> updateMenuItem(@PathVariable Integer id, @RequestBody MenuItems updatedMenuItem) {
        MenuItems updatedItem = menuService.updateMenuItem(id, updatedMenuItem);
        return ResponseEntity.ok(updatedItem);
    }

    // Удалить блюдо по ID
    @DeleteMapping("/menu/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Integer id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    // Найти блюдо по ID
    @GetMapping("/menu/{id}")
    public ResponseEntity<MenuItems> getMenuItemById(@PathVariable Integer id) {
        MenuItems menuItem = menuService.getMenuItemById(id);
        return ResponseEntity.ok(menuItem);
    }

    // Обновить статус заказа
    @PatchMapping("/order/{orderId}/status")
    public ResponseEntity<OrderList> updateOrderStatus(@PathVariable Integer orderId, @RequestBody Map<String, String> request) {
        OrderList.OrderStatus newStatus = OrderList.OrderStatus.valueOf(request.get("status"));
        String chatId = request.get("chatId");
        Optional<OrderList> updatedOrder = orderListService.updateOrderStatus(orderId, newStatus);

        if (updatedOrder.isPresent() && chatId != null) {
            String notificationText = "Ваш заказ №" + updatedOrder.get().getOrderNumber() + " был обновлён. Новый статус: " + newStatus;
            telegramBot.sendUserNotification(chatId, notificationText);
        }

        return updatedOrder.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Получить статистику продаж за неделю
    @GetMapping("/order/stats/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyDishSales() {
        Map<String, Object> salesStats = orderListService.getWeeklyDishSales();
        return ResponseEntity.ok(salesStats);
    }

    // Удалить заказ
    @DeleteMapping("/order/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderId) {
        orderListService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    //Клиентские контроллеры

    // Получить все доступные блюда
    @GetMapping("/menu/available")
    public ResponseEntity<List<MenuItems>> getAvailableMenuItems() {
        List<MenuItems> availableItems = menuService.getAvailableMenuItems();
        return ResponseEntity.ok(availableItems);
    }

    @PostMapping("/order")
    public ResponseEntity<OrderList> createOrder(@RequestBody Map<String, Object> request) {
        String orderNumber = (String) request.get("orderNumber");
        String chatId = (String) request.get("chatId");

        // Получаем список товаров из запроса
        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) request.get("orderItems");
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Object> itemData : itemsList) {
            Integer menuItemId = (Integer) itemData.get("menuItemId");
            Integer quantity = (Integer) itemData.get("quantity");

            // Находим MenuItem в базе данных
            MenuItems menuItem = menuItemsRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Товар с ID " + menuItemId + " не найден"));

            // Создаем OrderItem
            OrderItem item = new OrderItem();
            item.setMenuItem(menuItem);
            item.setQuantity(quantity);
            orderItems.add(item);
        }

        // Создаем заказ с товарами
        OrderList newOrder = orderListService.createOrder(orderNumber, orderItems, chatId);
        return ResponseEntity.ok(newOrder);
    }

    // Получить заказ по номеру
    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<OrderList> getOrderByNumber(@PathVariable String orderNumber) {
        Optional<OrderList> order = orderListService.getOrderByNumber(orderNumber);
        return order.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
}
