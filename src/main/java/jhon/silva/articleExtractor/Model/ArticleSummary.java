package jhon.silva.articleExtractor.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("article_summaries")
public class ArticleSummary {

    @Id
    private Long id;

    private String url;
    private String summary;
    private String language;
    private Integer length;
    private String status;

    @Column("error_message")
    private String errorMessage;

    @Column("created_at")
    private LocalDateTime createdAt;

    // Constructor sin id
    public ArticleSummary(String url, String summary, String language,
                          Integer length, String status, String errorMessage,
                          LocalDateTime createdAt) {
        this.url = url;
        this.summary = summary;
        this.language = language;
        this.length = length;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }
}