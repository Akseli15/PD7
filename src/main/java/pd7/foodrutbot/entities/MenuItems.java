package pd7.foodrutbot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "menu_items")
public class MenuItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "category", columnDefinition = "TEXT")
    private String category;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available", nullable = false)
    private Boolean available;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "imageUrl", columnDefinition = "LONGTEXT")
    private String imageUrl;
}
