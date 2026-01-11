package com.passman.android.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.passman.android.data.dao.CardDao;
import com.passman.android.data.dao.CredentialDao;
import com.passman.android.data.dao.EncryptedFileDao;
import com.passman.android.data.dao.FileVaultDao;
import com.passman.android.data.dao.SecureNoteDao;
import com.passman.android.data.dao.UserDao;
import com.passman.android.data.entity.CardEntity;
import com.passman.android.data.entity.CredentialEntity;
import com.passman.android.data.entity.EncryptedFileEntity;
import com.passman.android.data.entity.FileVaultEntity;
import com.passman.android.data.entity.SecureNoteEntity;
import com.passman.android.data.entity.UserEntity;

/**
 * Room Database for PassMan
 */
@Database(
    entities = {
        CredentialEntity.class,
        UserEntity.class,
        FileVaultEntity.class,
        EncryptedFileEntity.class,
        SecureNoteEntity.class,
        CardEntity.class
    },
    version = 3,
    exportSchema = false
)
public abstract class PassManDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "passman_vault.db";
    private static volatile PassManDatabase INSTANCE;

    public abstract CredentialDao credentialDao();
    public abstract UserDao userDao();
    public abstract FileVaultDao fileVaultDao();
    public abstract EncryptedFileDao encryptedFileDao();
    public abstract SecureNoteDao secureNoteDao();
    public abstract CardDao cardDao();

    public static PassManDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PassManDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            PassManDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Close the database and clear the singleton instance
     */
    public static void destroyInstance() {
        if (INSTANCE != null) {
            if (INSTANCE.isOpen()) {
                INSTANCE.close();
            }
            INSTANCE = null;
        }
    }
}
