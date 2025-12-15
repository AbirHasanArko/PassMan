package com.passman.core.repository;

import com.passman.core.model.IdentityCard;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for identity cards operations
 */
public interface IdentityCardsRepository {

    IdentityCard save(IdentityCard card) throws RepositoryException;

    Optional<IdentityCard> findById(Long id) throws RepositoryException;

    List<IdentityCard> findAll() throws RepositoryException;

    List<IdentityCard> findByType(IdentityCard.CardType type) throws RepositoryException;

    List<IdentityCard> findExpiringBefore(LocalDate date) throws RepositoryException;

    List<IdentityCard> findExpired() throws RepositoryException;

    List<IdentityCard> search(String query) throws RepositoryException;

    void update(IdentityCard card) throws RepositoryException;

    void delete(Long id) throws RepositoryException;
}