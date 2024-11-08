package org.example.common.crypto.repository;

import org.example.common.crypto.entity.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CryptoRepository extends JpaRepository<Crypto, Long> {

    List<Crypto> findAll();
}
