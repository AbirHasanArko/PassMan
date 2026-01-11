package com.passman.android.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.passman.android.PassManApp;
import com.passman.android.data.repository.CredentialRepository;

/**
 * Background worker that checks password security and sends notifications
 */
public class SecurityCheckWorker extends Worker {

    public static final String WORK_NAME = "security_check_worker";

    private final NotificationHelper notificationHelper;
    private final CredentialRepository credentialRepository;

    public SecurityCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.notificationHelper = new NotificationHelper(context);
        PassManApp app = (PassManApp) context.getApplicationContext();
        this.credentialRepository = app.getCredentialRepository();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            checkWeakPasswords();
            checkOldPasswords();
            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private void checkWeakPasswords() {
        credentialRepository.getStatistics(new CredentialRepository.RepositoryCallback<CredentialRepository.CredentialStatistics>() {
            @Override
            public void onSuccess(CredentialRepository.CredentialStatistics stats) {
                if (stats.weakCount > 0) {
                    notificationHelper.showWeakPasswordsAlert(stats.weakCount);
                }
            }

            @Override
            public void onError(Exception e) {
                // Silently fail
            }
        });
    }

    private void checkOldPasswords() {
        credentialRepository.getStatistics(new CredentialRepository.RepositoryCallback<CredentialRepository.CredentialStatistics>() {
            @Override
            public void onSuccess(CredentialRepository.CredentialStatistics stats) {
                if (stats.oldCount > 0) {
                    notificationHelper.showOldPasswordsReminder(stats.oldCount, 90);
                }
            }

            @Override
            public void onError(Exception e) {
                // Silently fail
            }
        });
    }
}
