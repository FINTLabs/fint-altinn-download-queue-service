package no.fint.downloadqueue.controller;

import no.fint.downloadqueue.client.DownloadQueueClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {
    private final DownloadQueueClient downloadQueueClient;

    public ApplicationController(DownloadQueueClient downloadQueueClient) {
        this.downloadQueueClient = downloadQueueClient;
    }
}
