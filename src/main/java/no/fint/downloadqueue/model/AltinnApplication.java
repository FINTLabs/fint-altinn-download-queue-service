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
    private String countyNumberRequestor;
    private String organisationNumberSubject;
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
        private String reference;
        private String formData;
        private byte[] formDataPdf;
    }

    @Data
    public static class Attachment {
        private Integer attachmentId;
        private byte[] attachmentData;
        private String attachmentType;
        private String attachmentTypeName;
        private String attachmentTypeNameLanguage;
    }
}
