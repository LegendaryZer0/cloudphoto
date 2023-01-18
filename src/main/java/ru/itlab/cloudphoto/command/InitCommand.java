package ru.itlab.cloudphoto.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import ru.itlab.cloudphoto.domain.dto.ConfigMDTO;
import ru.itlab.cloudphoto.helper.ConfigHelper;

import java.util.Scanner;

@Component
@Command(name = "init", description = "cloudphoto init command")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class InitCommand implements Runnable {

    private final ConfigHelper configHelper;
    private final Scanner sc;

    @Override
    public void run() {
        System.out.println("Enter the access key: ");
        String accessKey = sc.nextLine();
        System.out.println();
        System.out.println("Enter the secret key: ");
        String secretKey = sc.nextLine();
        System.out.println();
        System.out.println("Enter the bucketName: ");
        String bucketName = sc.nextLine();
        configHelper.updateConfigFile(ConfigMDTO.builder()
                .accessKey(accessKey)
                .secretKey(secretKey)
                .bucketName(bucketName)
                .build());
        System.out.println("Done");

    }
}
