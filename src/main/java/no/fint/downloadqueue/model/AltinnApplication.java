package no.fint.downloadqueue.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Document
public class AltinnApplication {
    @Id
    private String archiveReference;

    @Version
    private long version;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @CreatedDate
    private LocalDateTime createdDate;
}
