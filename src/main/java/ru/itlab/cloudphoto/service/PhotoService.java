package ru.itlab.cloudphoto.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.itlab.cloudphoto.helper.ConfigHelper;

import java.io.File;
import java.util.List;

import static ru.itlab.cloudphoto.constant.INIConstant.OPTION_BUCKET;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class PhotoService {
    private final AmazonS3 yandexS3;
    private final ConfigHelper configHelper;
    private final TransferManager transferManager;

    public void savePhotoList(List<File> fileList, String albumName) {
        fileList.forEach(file -> {
            try {
                transferManager.upload(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), albumName + "/" + file.getName(), file).waitForCompletion();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
    }


    public List<String> getPhotoKeyListByAlbumName(String albumName) {
        return yandexS3.listObjects(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), albumName).getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList();
    }

    public void downloadPhotoListByAlbumName(String albumName, File destinationDir) {
        MultipleFileDownload download = transferManager.downloadDirectory(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), albumName + "/", destinationDir);
        try {
            download.waitForCompletion();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void deleteAllPhotosInAlbum(String albumName) {
        yandexS3.deleteObjects(new DeleteObjectsRequest(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET)).withKeys(getPhotoKeyListByAlbumName(albumName).toArray(new String[1])));
    }

    public void deletePhotoInAlbum(String albumName, String photoName) {
        getPhotoKeyListByAlbumName(albumName).forEach(x -> log.info("x {}", x));
        String photoKey = getPhotoKeyListByAlbumName(albumName).stream().filter(photoKeys -> {
            log.info("photoKey {}", photoKeys);
            return photoKeys.equals(albumName + "/" + photoName);
        }).findAny().orElseThrow(() -> new IllegalStateException("photo does not exists"));
        yandexS3.deleteObject(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), photoKey);
    }
}
