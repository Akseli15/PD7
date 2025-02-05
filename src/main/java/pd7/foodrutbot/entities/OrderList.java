package pd7.foodrutbot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class OrderList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name="chat_id", nullable = false)
    private String chatId;

    @Column(name = "order_number", nullable = false, length = 10, unique = true)
    private String orderNumber;

    @Column(name = "menu_items", columnDefinition = "json", nullable = false)
    private String menuItems;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum OrderStatus {
        ЗАКАЗ_В_ОЧЕРЕДИ("Заказ в очереди"),
        ВЫДАН("Выдан");

        private final String status;

        OrderStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}
