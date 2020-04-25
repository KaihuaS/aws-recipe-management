package app.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OrderedList {
    @Id
    private String id;
    private String recipie_id;
    private int position;
    private String items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipie_id() {
        return recipie_id;
    }

    public void setRecipie_id(String recipie_id) {
        this.recipie_id = recipie_id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }
}