package com.passman.core.repository;

import com.passman.core.model.FileVault;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for file vault operations
 */
public interface FileVaultRepository {

    FileVault save(FileVault vault) throws RepositoryException;

    Optional<FileVault> findById(Long id) throws RepositoryException;

    Optional<FileVault> findByType(FileVault.VaultType type) throws RepositoryException;

    List<FileVault> findAll() throws RepositoryException;

    void update(FileVault vault) throws RepositoryException;

    void delete(Long id) throws RepositoryException;
}