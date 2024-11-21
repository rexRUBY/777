package org.example.common.user.mongo;

import org.example.common.user.entity.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserQueryRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);
}