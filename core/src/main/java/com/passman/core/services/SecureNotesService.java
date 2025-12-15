package com.passman.core.services;

import com.google.gson.Gson;
import com. passman.core.crypto.AESCipher;
import com.passman.core.crypto.CipherFactory;
import com.passman.core.model.NoteAttachment;
import com.passman.core.model.SecureNote;
import com.passman.core.repository.SecureNotesRepository;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java. util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing secure encrypted notes
 */
public class SecureNotesService {

    private final SecureNotesRepository notesRepository;
    private final AESCipher aesCipher;
    private final Path attachmentStoragePath;
    private final Gson gson;

    public SecureNotesService(SecureNotesRepository notesRepository, String storagePath) {
        this.notesRepository = notesRepository;
        this.aesCipher = CipherFactory.createAESCipher();
        this.attachmentStoragePath = Paths.get(storagePath, "note_attachments");
        this.gson = new Gson();
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(attachmentStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize note attachment storage", e);
        }
    }

    public SecureNote saveNote(SecureNote note, SecretKey masterKey) throws Exception {
        String content = note.getContent();
        if (content != null && !content.isEmpty()) {
            String encryptedContent = aesCipher.encrypt(content, masterKey);
            byte[] combined = Base64.getDecoder().decode(encryptedContent);

            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

            note.setEncryptionIV(iv);
            note.setEncryptedContent(encrypted);
        }

        note.setLastModified(LocalDateTime.now());

        if (note.getId() == null) {
            return notesRepository.save(note);
        } else {
            notesRepository.update(note);
            return note;
        }
    }

    public Optional<SecureNote> getNote(Long id, SecretKey masterKey) throws Exception {
        Optional<SecureNote> noteOpt = notesRepository.findById(id);

        if (noteOpt.isPresent()) {
            SecureNote note = noteOpt.get();
            decryptNote(note, masterKey);
            note.setAttachments(notesRepository.findAttachmentsByNoteId(id));
            return Optional.of(note);
        }

        return Optional.empty();
    }

    private void decryptNote(SecureNote note, SecretKey masterKey) throws Exception {
        if (note.getEncryptedContent() != null && note.getEncryptionIV() != null) {
            byte[] combined = new byte[note.getEncryptionIV().length + note.getEncryptedContent().length];
            System.arraycopy(note.getEncryptionIV(), 0, combined, 0, note.getEncryptionIV().length);
            System.arraycopy(note. getEncryptedContent(), 0, combined, note.getEncryptionIV().length, note.getEncryptedContent().length);

            String encryptedContent = Base64.getEncoder().encodeToString(combined);
            String decryptedContent = aesCipher.decrypt(encryptedContent, masterKey);
            note.setContent(decryptedContent);
        }
    }

    public List<SecureNote> getAllNotes() throws Exception {
        return notesRepository.findAll();
    }

    public List<SecureNote> searchNotes(String query) throws Exception {
        return notesRepository.searchByTitle(query);
    }

    public List<SecureNote> getNotesByCategory(SecureNote.NoteCategory category) throws Exception {
        return notesRepository.findByCategory(category);
    }

    public List<SecureNote> getFavoriteNotes() throws Exception {
        return notesRepository.findFavorites();
    }

    public void deleteNote(Long id) throws Exception {
        List<NoteAttachment> attachments = notesRepository.findAttachmentsByNoteId(id);
        for (NoteAttachment attachment : attachments) {
            deleteAttachmentFile(attachment);
        }
        notesRepository.delete(id);
    }

    public NoteAttachment addAttachment(Long noteId, File file, SecretKey masterKey) throws Exception {
        byte[] fileData = Files.readAllBytes(file.toPath());
        byte[] encryptedData = aesCipher.encryptBytes(fileData, masterKey);

        String encryptedFileName = UUID.randomUUID().toString() + ". enc";
        Path encryptedFilePath = attachmentStoragePath.resolve(noteId. toString())
                .resolve(encryptedFileName);

        Files.createDirectories(encryptedFilePath.getParent());
        Files.write(encryptedFilePath, encryptedData);

        String checksum = calculateSHA256(fileData);

        NoteAttachment attachment = new NoteAttachment();
        attachment.setNoteId(noteId);
        attachment.setOriginalFileName(file.getName());
        attachment.setEncryptedFileName(encryptedFileName);
        attachment.setFileSize(fileData.length);
        attachment.setMimeType(Files.probeContentType(file.toPath()));
        attachment. setChecksum(checksum);

        attachment = notesRepository.saveAttachment(attachment);

        Optional<SecureNote> noteOpt = notesRepository.findById(noteId);
        if (noteOpt.isPresent()) {
            SecureNote note = noteOpt.get();
            note.setHasAttachments(true);
            notesRepository.update(note);
        }

        return attachment;
    }

    public File downloadAttachment(NoteAttachment attachment, File destinationFile, SecretKey masterKey) throws Exception {
        Path encryptedFilePath = attachmentStoragePath.resolve(attachment.getNoteId().toString())
                .resolve(attachment.getEncryptedFileName());

        byte[] encryptedData = Files.readAllBytes(encryptedFilePath);
        byte[] decryptedData = aesCipher.decryptBytes(encryptedData, masterKey);

        String checksum = calculateSHA256(decryptedData);
        if (!checksum.equals(attachment. getChecksum())) {
            throw new SecurityException("Attachment integrity check failed");
        }

        Files.write(destinationFile. toPath(), decryptedData);
        return destinationFile;
    }

    public void deleteAttachment(Long attachmentId) throws Exception {
        Optional<NoteAttachment> attachmentOpt = notesRepository.findAttachmentById(attachmentId);

        if (attachmentOpt. isPresent()) {
            NoteAttachment attachment = attachmentOpt.get();
            deleteAttachmentFile(attachment);
            notesRepository.deleteAttachment(attachmentId);

            List<NoteAttachment> remaining = notesRepository.findAttachmentsByNoteId(attachment.getNoteId());
            if (remaining.size() <= 1) {
                Optional<SecureNote> noteOpt = notesRepository.findById(attachment.getNoteId());
                if (noteOpt.isPresent()) {
                    SecureNote note = noteOpt.get();
                    note.setHasAttachments(false);
                    notesRepository.update(note);
                }
            }
        }
    }

    private void deleteAttachmentFile(NoteAttachment attachment) throws IOException {
        Path filePath = attachmentStoragePath.resolve(attachment.getNoteId().toString())
                .resolve(attachment.getEncryptedFileName());
        Files.deleteIfExists(filePath);
    }

    private String calculateSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
    }
}