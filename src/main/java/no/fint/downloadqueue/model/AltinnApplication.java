package no.fint.downloadqueue.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document
public class AltinnApplication {
    @Id
    private String archiveReference;
    private LocalDateTime archivedDate;
    private String requestor;
    private String requestorName;
    private String subject;
    private String subjectName;
    private String serviceCode;
    private Integer languageCode;
    private Form form;
    private List<Attachment> attachments = new ArrayList<>();
    private AltinnApplicationStatus status;

    @Version
    private long version;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @CreatedDate
    private LocalDateTime createdDate;

    @Data
    public static class Form {
        private String formData;
    }

    @Data
    public static class Attachment {
        private Integer attachmentId;
        private String attachmentType;
        private String attachmentTypeName;
        private String attachmentTypeNameLanguage;
        private String fileName;
    }
}
