package com.passman.core.repository;

import com.passman.core.model.Credential;

import java.util.List;
import java.util.Optional;

/**
 * Repository for credential CRUD operations
 */
public interface CredentialRepository {

    Credential save(Credential credential) throws RepositoryException;

    Optional<Credential> findById(Long id) throws RepositoryException;

    List<Credential> findAll() throws RepositoryException;

    List<Credential> searchByTitle(String query) throws RepositoryException;

    List<Credential> findFavorites() throws RepositoryException;

    void update(Credential credential) throws RepositoryException;

    void delete(Long id) throws RepositoryException;

    int count() throws RepositoryException;
}