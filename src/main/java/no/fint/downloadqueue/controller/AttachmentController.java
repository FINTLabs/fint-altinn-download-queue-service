package no.fint.downloadqueue.controller;

import no.fint.downloadqueue.client.AttachmentDataStreamedClient;
import no.fint.downloadqueue.exception.AltinnFaultException;
import no.fint.downloadqueue.util.AltinnFaultPretty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentDataStreamedClient attachmentDataStreamedClient;

    public AttachmentController(AttachmentDataStreamedClient attachmentDataStreamedClient) {
        this.attachmentDataStreamedClient = attachmentDataStreamedClient;
    }

    @GetMapping("/{attachmentId}")
    public @ResponseBody byte[] getApplication(@PathVariable Integer attachmentId) {
        return attachmentDataStreamedClient.getAttachmentDataStreamed(attachmentId);
    }

    @ExceptionHandler(AltinnFaultException.class)
    public ResponseEntity<AltinnFaultPretty> altinnFaultException(AltinnFaultException altinnFaultException) {
        AltinnFaultPretty altinnFaultPretty = AltinnFaultPretty.of(altinnFaultException.getAltinnFault());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(altinnFaultPretty);
    }
}