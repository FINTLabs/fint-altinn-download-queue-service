package no.fint.downloadqueue.repository;

import no.fint.downloadqueue.model.AltinnApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AltinnApplicationRepository extends MongoRepository<AltinnApplication, String> {
}
