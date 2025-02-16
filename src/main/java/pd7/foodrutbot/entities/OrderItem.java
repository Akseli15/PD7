package pd7.foodrutbot.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderList order;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn(name = "menu_item_id", referencedColumnName = "id", nullable = false)
    private MenuItems menuItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @PrePersist
    @PreUpdate
    private void calculatePrice() {
        if (menuItem != null && quantity != null) {
            this.price = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
