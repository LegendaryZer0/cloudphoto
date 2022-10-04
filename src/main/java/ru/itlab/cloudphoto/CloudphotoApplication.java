package ru.itlab.cloudphoto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import ru.itlab.cloudphoto.command.CommandResolver;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
public class CloudphotoApplication implements CommandLineRunner {

    @Autowired
    private IFactory factory;
    @Autowired
    private CommandResolver commandResolver;

    public static void main(String[] args) {
        SpringApplication.run(CloudphotoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println(Arrays.toString(args)); //fixme
        int exitCode = new CommandLine(commandResolver, factory).execute(args);
        System.exit(exitCode); //fixme
    }
}