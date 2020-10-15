package no.fint.downloadqueue.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("altinn")
public class AltinnProperties {
    private String serviceCode;
    private String systemUsername;
    private String systemPassword;
    private String downloadQueueUri;
    private String attachmentDataStreamedUri;
}
