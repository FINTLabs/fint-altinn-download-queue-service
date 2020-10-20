package no.fint.downloadqueue.repository;

import no.fint.downloadqueue.model.AltinnApplication;
import no.fint.downloadqueue.model.AltinnApplicationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AltinnApplicationRepository extends MongoRepository<AltinnApplication, String> {

    List<AltinnApplication> findByStatus(AltinnApplicationStatus status);
}
