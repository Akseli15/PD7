package pd7.foodrutbot.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class OrderList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="chat_id", nullable = false)
    private String chatId;

    @Column(name = "order_number", nullable = false, length = 10, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "is_finalized", nullable = false)
    private boolean isFinalized = false;

    public enum OrderStatus {
        ЗАКАЗ_ВЫДАН("Заказ выдан"),
        ЗАКАЗ_ГОТОВИТСЯ("Заказ готовится"),
        ГОТОВ_К_ВЫДАЧЕ("Готов к выдаче"),
        ВРЕМЯ_ВЫДАЧИ_ВЫШЛО("Время выдачи вышло");

        private final String status;
        OrderStatus(String status) { this.status = status; }
        public String getStatus() { return status; }
    }

    // Методы для управления корзиной
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public BigDecimal calculateTotalPrice() {
        return items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
