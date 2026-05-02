package jhon.silva.cartoon.Model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "cartoon")
public class CartoonResult {

    @Id
    private String id;

    private String imageName;
    private Integer cartoonIndex;
    private String taskId;
    private String taskType;
    private String requestId;
    private String logId;
    private Integer errorCode;
    private String errorMsg;
    private String resultUrl;
    private Integer taskStatus;   //  0=queued, 1=processing, 2=success
    private String status;        // pending, completed, failed
    private Boolean deleted;      // false=activo, true=eliminado (borrado lógico)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartoonResult() {
        this.createdAt = LocalDateTime.now();
        this.deleted = false;
    }
}