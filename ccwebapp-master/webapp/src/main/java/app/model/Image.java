package app.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Image {
    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String url;
    private String recipeId;
    private String md5;
    private long contentLength;

    public Image() {
        super();
    }

    public Image(String url, String md5, long contentLength) {
        id = UUID.randomUUID().toString();
        this.url = url;
        this.md5 = md5;
        this.contentLength = contentLength;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
}
