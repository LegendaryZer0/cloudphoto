package ru.itlab.cloudphoto.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.amazonaws.services.s3.transfer.TransferManager;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.itlab.cloudphoto.domain.model.Album;
import ru.itlab.cloudphoto.helper.ConfigHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static ru.itlab.cloudphoto.constant.INIConstant.OPTION_BUCKET;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class SiteService {
    private final AlbumService albumService;
    private final PhotoService photoService;
    private final TransferManager transferManager;
    private final AmazonS3 yandexS3;
    private final Configuration configuration;

    private final ConfigHelper configHelper;


    public String getAlbumWebsiteUrl() {
        List<Album> albumList = albumService.getAllAlbumsDto().getAlbumList();
        File file = generateAlbumSitePage(albumList);
        File errorPage = generateErrorSitePage();
        List<File> albumPhotoPageList = generateAlbumContentPageList(albumList);
        try {

            transferManager.upload(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), file.getName(), file)
                    .waitForCompletion();
            transferManager.upload(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), errorPage.getName(), errorPage)
                    .waitForCompletion();
            albumPhotoPageList.forEach(albumPhotosPage -> {
                try {
                    transferManager.upload(configHelper
                            .getParamFromIniDefaultSection(OPTION_BUCKET), albumPhotosPage.getName(), albumPhotosPage)
                            .waitForCompletion();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }

        BucketWebsiteConfiguration websiteConfiguration = new BucketWebsiteConfiguration();
        websiteConfiguration.setIndexDocumentSuffix("index.html");
        yandexS3.setBucketWebsiteConfiguration(configHelper.getParamFromIniDefaultSection(OPTION_BUCKET), websiteConfiguration);

        return "https://" + configHelper.getParamFromIniDefaultSection(OPTION_BUCKET) + ".website.yandexcloud.net";

    }

    private File generateErrorSitePage() {

        try {
            Template template = configuration.getTemplate("error.ftlh", "UTF-8");
            FileWriter fileWriter = new FileWriter("error.html");
            template.process(Collections.emptyMap(), fileWriter);
            fileWriter.flush();
            fileWriter.close();
            return new File("error.html");
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException(e);
        }

    }

    private List<File> generateAlbumContentPageList(List<Album> albumList) {
        List<File> pagesList = new ArrayList<>();
        albumList.forEach(album -> {
            List<String> photoList = photoService.getPhotoKeyListByAlbumName(album.getName());
            try {
                Template template = configuration.getTemplate("album_photos_page.ftlh", "UTF-8");
                FileWriter fileWriter = new FileWriter("album" + albumList.indexOf(album) + ".html");

                Map<String, Object> input = new HashMap<>();
                input.put("photo_content", photoList);
                template.process(input, fileWriter);
                fileWriter.flush();
                fileWriter.close();
                log.info("album name {}", album);

                pagesList.add(new File("album" + albumList.indexOf(album) + ".html"));
            } catch (IOException | TemplateException e) {
                throw new IllegalStateException(e);
            }
        });
        return pagesList;
    }

    private File generateAlbumSitePage(List<Album> albumList) {

        try {
            Map<String, Object> input = new HashMap<>();
            log.info("albums to generate {}", albumList);
            input.put("albums", albumList);
            Template template = configuration.getTemplate("albums_page.ftlh", "UTF-8");
            FileWriter fileWriter = new FileWriter("index.html");
            template.process(input, fileWriter);
            fileWriter.flush();
            fileWriter.close();
            log.info("html done");
            return new File("index.html");
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException(e);
        }
    }

}
