package no.vebb.f1.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class CategoryEntity {
    @Id
    @Column(name = "category_name")
    private String categoryName;

    protected CategoryEntity() {}

    public CategoryEntity(String categoryName) {
        this.categoryName = categoryName;
    }

    public String categoryName() {
        return categoryName;
    }
}
