package com.passman.core.repository;

import com.passman.core.model.Backup;

import java.util.List;
import java.util.Optional;

/**
 * Repository for backup metadata
 */
public interface BackupRepository {

    Backup save(Backup backup) throws RepositoryException;

    Optional<Backup> findById(Long id) throws RepositoryException;

    Optional<Backup> findByFileName(String fileName) throws RepositoryException;

    List<Backup> findAll() throws RepositoryException;

    void delete(Long id) throws RepositoryException;
}