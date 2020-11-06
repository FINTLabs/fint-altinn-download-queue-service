package no.fint.downloadqueue.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class AltinnForm {
    @JacksonXmlProperty(localName = "Innsender")
    private Innsender innsender;

    @Data
    public static class Innsender {
        @JacksonXmlProperty(localName = "language")
        private String language;

        @JacksonXmlProperty(localName = "Organisasjon")
        private Organisasjon organisasjon;
    }

    @Data
    public static class Organisasjon {
        @JacksonXmlProperty(localName = "organisasjonsnummer")
        private String organisasjonsnummer;

        @JacksonXmlProperty(localName = "navn")
        private String navn;

        @JacksonXmlProperty(localName = "Forretningsadresse")
        private Adresse forretningsadresse;

        @JacksonXmlProperty(localName = "telefonnummer")
        private String telefonnummer;

        @JacksonXmlProperty(localName = "epost")
        private String epost;

        @JacksonXmlProperty(localName = "fylke")
        private String fylke;

        @JacksonXmlProperty(localName = "fylkenummer")
        private String fylkenummer;
    }

    @Data
    public static class Adresse {
        @JacksonXmlProperty(localName = "adresse")
        private String adresse;

        @JacksonXmlProperty(localName = "postnummer")
        private String postnummer;

        @JacksonXmlProperty(localName = "poststed")
        private String poststed;
    }
}
