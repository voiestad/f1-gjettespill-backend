package no.vebb.f1.guessing.category;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class CategoryEntity {
    @Id
    @Column(name = "category_name")
    @Enumerated(EnumType.STRING)
    private Category categoryName;

    protected CategoryEntity() {}

    public Category categoryName() {
        return categoryName;
    }
}
