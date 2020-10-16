package no.fint.downloadqueue.controller;

import no.fint.downloadqueue.client.AttachmentDataStreamedClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentDataStreamedClient attachmentDataStreamedClient;

    public AttachmentController(AttachmentDataStreamedClient attachmentDataStreamedClient) {
        this.attachmentDataStreamedClient = attachmentDataStreamedClient;
    }
}