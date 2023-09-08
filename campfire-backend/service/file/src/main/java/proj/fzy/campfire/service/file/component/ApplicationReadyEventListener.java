package proj.fzy.campfire.service.file.component;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import proj.fzy.campfire.service.file.config.properties.FileStorageProperties;
import proj.fzy.campfire.service.file.exception.StorageDirectoryInitializationException;

import java.io.File;

@Component
public class ApplicationReadyEventListener {

    private final FileStorageProperties fileStorageProperties;

    public ApplicationReadyEventListener(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void handleApplicationReadyEvent() {
        File baseDirectory = new File(fileStorageProperties.getBucketLocation());
        if (!baseDirectory.exists() && !baseDirectory.mkdirs()) {
            throw new StorageDirectoryInitializationException("Create Base Directory Error");
        }
        String basePath = baseDirectory.getAbsolutePath() + File.separator;

        // mkdir base directory storage
        File storageDirectory = new File(basePath + "bucket");
        if (!storageDirectory.exists() && !storageDirectory.mkdirs()) {
            throw new StorageDirectoryInitializationException("Create Base Storage Directory Error");
        }
        String storageBasePath = storageDirectory.getAbsolutePath() + File.separator;

        // mkdir directory avatar
        File avatarDirectory = new File(storageBasePath + "avatar");
        if (!avatarDirectory.exists() && !avatarDirectory.mkdirs()) {
            throw new StorageDirectoryInitializationException("Create Avatar Directory Error");
        }

        // mkdir directory group
        File groupDirectory = new File(storageBasePath + "group");
        if (!groupDirectory.exists() && !groupDirectory.mkdirs()) {
            throw new StorageDirectoryInitializationException("Create Group Directory Error");
        }
    }
}
