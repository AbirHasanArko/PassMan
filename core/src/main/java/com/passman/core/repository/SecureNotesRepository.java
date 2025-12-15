package com.passman.core.repository;

import com.passman.core.model.NoteAttachment;
import com.passman.core.model.SecureNote;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for secure notes operations
 */
public interface SecureNotesRepository {

    SecureNote save(SecureNote note) throws RepositoryException;

    Optional<SecureNote> findById(Long id) throws RepositoryException;

    List<SecureNote> findAll() throws RepositoryException;

    List<SecureNote> searchByTitle(String query) throws RepositoryException;

    List<SecureNote> findByCategory(SecureNote.NoteCategory category) throws RepositoryException;

    List<SecureNote> findFavorites() throws RepositoryException;

    void update(SecureNote note) throws RepositoryException;

    void delete(Long id) throws RepositoryException;

    // Attachment operations
    NoteAttachment saveAttachment(NoteAttachment attachment) throws RepositoryException;

    Optional<NoteAttachment> findAttachmentById(Long id) throws RepositoryException;

    List<NoteAttachment> findAttachmentsByNoteId(Long noteId) throws RepositoryException;

    void deleteAttachment(Long id) throws RepositoryException;
}