package ru.itlab.cloudphoto.helper;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.itlab.cloudphoto.domain.dto.ConfigMDTO;

import java.io.*;

import static ru.itlab.cloudphoto.constant.AWSConstant.*;
import static ru.itlab.cloudphoto.constant.INIConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class ConfigHelper {

    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.config/cloudphoto/cloudphotorc";
    public static final String CONFIG_INI = "/config.ini";
    private Ini ini;

    @Lazy
    private final AmazonS3 yandexS3;

    public void updateConfigFile(ConfigMDTO configMDTO){
        log.info("config MDTO {}",configMDTO);
        try {
            FileUtils.forceMkdir(new File(CONFIG_PATH));
            File file =new File(CONFIG_PATH+ CONFIG_INI);
            file.createNewFile();
            ini = new Ini(file);
            ini.put(SECTION_DEFAULT,OPTION_ACCESS_KEY,configMDTO.getAccessKey());
            ini.put(SECTION_DEFAULT,OPTION_SECRET_KEY,configMDTO.getSecretKey());
            ini.put(SECTION_DEFAULT, OPTION_BUCKET,configMDTO.getBucketName());
            ini.put(SECTION_DEFAULT,OPTION_REGION,SIGNING_REGION);
            ini.put(SECTION_DEFAULT,OPTION_ENDPOINT_URL,SERVICE_ENDPOINT);
            ini.store(new FileOutputStream(CONFIG_PATH+CONFIG_INI));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if(!yandexS3.doesBucketExistV2(configMDTO.getBucketName())){
            yandexS3.createBucket(configMDTO.getBucketName());
        }

    }
    public String getParamFromIniDefaultSection(String optionName){
        return getParamFromIni(SECTION_DEFAULT,optionName);
    }

    public String getParamFromIni(String sectionName,String optionName){
        try(InputStream inputStream = new FileInputStream(CONFIG_PATH+CONFIG_INI)){
            return new Ini(inputStream).get(sectionName,optionName);
        }catch (IOException e){
            throw new IllegalStateException("config not found, please, execute init command first, make sure you have administration rights",e);
        }
    }


}
