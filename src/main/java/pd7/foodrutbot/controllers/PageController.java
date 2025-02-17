package pd7.foodrutbot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pd7.foodrutbot.entities.Category;
import pd7.foodrutbot.entities.MenuItems;
import pd7.foodrutbot.entities.OrderItem;
import pd7.foodrutbot.entities.OrderList;
import pd7.foodrutbot.repositories.CategoryRepository;
import pd7.foodrutbot.repositories.MenuItemsRepository;
import pd7.foodrutbot.repositories.OrderItemRepository;
import pd7.foodrutbot.repositories.OrderListRepository;
import pd7.foodrutbot.service.MenuService;
import pd7.foodrutbot.service.OrderListService;
import pd7.foodrutbot.service.TelegramBot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderListRepository orderListRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private TelegramBot telegramBot;

    //Общие контроллеры

    // Получить все блюдаx
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
    public ResponseEntity<?> addMenuItem(@RequestBody MenuItems menuItem) {
        try {
            if (menuItem.getCategory() == null || menuItem.getCategory().getId() == null) {
                return ResponseEntity.badRequest().body("Категория не указана");
            }

            Category category = categoryRepository.findById(menuItem.getCategory().getId()).orElse(null);
            if (category == null) {
                return ResponseEntity.badRequest().body("Категория с таким ID не найдена");
            }

            menuItem.setCategory(category);

            MenuItems createdItem = menuService.addMenuItem(menuItem);

            return ResponseEntity.ok(createdItem);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при добавлении блюда");
        }
    }


    // Обновить блюдо по ID
    @PutMapping("/menu/{id}")
    public ResponseEntity<MenuItems> updateMenuItem(@PathVariable Integer id, @RequestBody MenuItems updatedMenuItem) {
        try {
            MenuItems updatedItem = menuService.updateMenuItem(id, updatedMenuItem);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
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

    /**
     * Создание новой корзины (заказа)
     */
    @PostMapping("/order/create")
    public ResponseEntity<OrderList> createOrder(@RequestParam String chatId) {
        OrderList order = new OrderList();
        order.setChatId(chatId);
        order.setOrderNumber("ORD" + String.format("%06d", new Random().nextInt(99999)));
        order.setStatus(OrderList.OrderStatus.ЗАКАЗ_ГОТОВИТСЯ);
        order.setCreatedAt(LocalDateTime.now());
        order.setFinalized(false);

        orderListRepository.save(order);
        return ResponseEntity.ok(order);
    }

    /**
     * Добавление товара в корзину
     */
    @PostMapping("/order/{orderId}/addItem")
    public ResponseEntity<OrderItem> addItemToOrder(@PathVariable Integer orderId,
                                                    @RequestParam Integer menuItemId,
                                                    @RequestParam Integer quantity) {
        Optional<OrderList> orderOpt = orderListRepository.findById(orderId);
        Optional<MenuItems> menuItemOpt = menuItemsRepository.findById(menuItemId);

        if (orderOpt.isEmpty() || menuItemOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        OrderList order = orderOpt.get();
        MenuItems menuItem = menuItemOpt.get();

        if (order.isFinalized()) {
            return ResponseEntity.badRequest().body(null);
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setMenuItem(menuItem);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)));

        orderItemRepository.save(orderItem);
        return ResponseEntity.ok(orderItem);
    }

    /**
     * Удаление товара из корзины
     */
    @DeleteMapping("/order/{orderId}/removeItem/{itemId}")
    public ResponseEntity<Void> removeItemFromOrder(@PathVariable Integer orderId,
                                                    @PathVariable Integer itemId) {
        Optional<OrderItem> itemOpt = orderItemRepository.findById(itemId);

        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrderItem item = itemOpt.get();
        if (!item.getOrder().getId().equals(orderId) || item.getOrder().isFinalized()) {
            return ResponseEntity.badRequest().build();
        }

        orderItemRepository.delete(item);
        return ResponseEntity.ok().build();
    }

    /**
     * Получение всех товаров в корзине
     */
    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable Integer orderId) {
        Optional<OrderList> orderOpt = orderListRepository.findById(orderId);
        return orderOpt.map(order -> ResponseEntity.ok(order.getItems()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Финализация заказа (оформление)
     */
    @PostMapping("/{orderId}/finalize")
    public ResponseEntity<OrderList> finalizeOrder(@PathVariable Integer orderId) {
        Optional<OrderList> orderOpt = orderListRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrderList order = orderOpt.get();
        if (order.isFinalized() || order.getItems().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        order.setFinalized(true);
        order.setStatus(OrderList.OrderStatus.ГОТОВ_К_ВЫДАЧЕ);
        orderListRepository.save(order);

        return ResponseEntity.ok(order);
    }

    // Получить заказ по номеру
    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<OrderList> getOrderByNumber(@PathVariable String orderNumber) {
        Optional<OrderList> order = orderListService.getOrderByNumber(orderNumber);
        return order.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    //Изменение статуса заказа
    @PutMapping("/order/{id}/status")
    public ResponseEntity<String> updateOrderStatus(@PathVariable Integer id, @RequestParam OrderList.OrderStatus status) {
        Optional<OrderList> orderOptional = orderListRepository.findById(id);
        if (orderOptional.isPresent()) {
            OrderList order = orderOptional.get();
            order.setStatus(status);
            orderListRepository.save(order);
            return ResponseEntity.ok("Статус заказа обновлён на: " + status);
        } else {
            return ResponseEntity.badRequest().body("Заказ с ID " + id + " не найден");
        }
    }

    @GetMapping("order/by-status")
    public ResponseEntity<List<OrderList>> getOrdersByStatus(@RequestParam OrderList.OrderStatus status) {
        List<OrderList> orders = orderListRepository.findByStatus(status);
        return ResponseEntity.ok(orders);
    }
}
