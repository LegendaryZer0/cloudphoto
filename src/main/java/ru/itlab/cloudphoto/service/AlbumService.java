package ru.itlab.cloudphoto.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.itlab.cloudphoto.domain.dto.AlbumDtoList;
import ru.itlab.cloudphoto.domain.model.Album;
import ru.itlab.cloudphoto.helper.ConfigHelper;
import ru.itlab.cloudphoto.util.ObjectMapperUtil;

import java.util.LinkedHashSet;

import static ru.itlab.cloudphoto.constant.INIConstant.OPTION_BUCKET;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class AlbumService {
    private static final String INDEX_ALBUMS_JSON = "albums.json";
    private final AmazonS3 yandexS3;
    private final ObjectMapperUtil objectMapperUtil;

    private final PhotoService photoService;

    private final ConfigHelper configHelper;

    public Album saveAlbum(String name) {
        AlbumDtoList albumDtoList = getAllAlbumsDto();
        Album newAlbum = Album.builder().name(name).build();
        albumDtoList.getAlbumList().add(newAlbum);
        albumDtoList.setAlbumList(new LinkedHashSet<>(albumDtoList.getAlbumList()).stream().toList());
        yandexS3.putObject(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), INDEX_ALBUMS_JSON,
                objectMapperUtil.writeValueAsString(albumDtoList));
        return newAlbum;
    }

    public Album getOrThrow(String albumName) {
        return getAllAlbumsDto().getAlbumList().stream()
                .filter(album -> album.getName().equals(albumName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Album with this name not found"));
    }

    public AlbumDtoList getAllAlbumsDto() {
        AlbumDtoList albumDtoList = AlbumDtoList.builder().build();
        try {
            S3Object s3object = yandexS3.getObject(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), INDEX_ALBUMS_JSON);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            albumDtoList = objectMapperUtil.readValue(inputStream, AlbumDtoList.class);
            return albumDtoList;
        } catch (AmazonS3Exception e) {
            yandexS3.putObject(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), INDEX_ALBUMS_JSON,
                    objectMapperUtil.writeValueAsString(albumDtoList));
            return albumDtoList;
        }
    }

    public void deleteAlbum(String name) {
        String bucketName = configHelper.getParamFromIniDefaultSection(OPTION_BUCKET);
        AlbumDtoList albumDtoList = getAllAlbumsDto();
        Album newAlbum = Album.builder().name(name).build();
        if (!albumDtoList.getAlbumList().remove(newAlbum)) {
            throw new IllegalStateException("there is no album with name " + name);
        }
        photoService.deleteAllPhotosInAlbum(name);
        yandexS3.putObject(bucketName, INDEX_ALBUMS_JSON, objectMapperUtil.writeValueAsString(albumDtoList));
    }
}
