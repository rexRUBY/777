package org.example.common.crypto.repository;

import org.example.common.crypto.entity.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CryptoRepository extends JpaRepository<Crypto, Long> {

    List<Crypto> findAll();
}
