package com.mapr.opc.streams;

import org.apache.commons.lang.StringUtils;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.LogManager;

@SpringBootApplication
public class OpcStreamApplication implements CommandLineRunner {
    private static String configFile;
    @Autowired
    private OpcStreamService opcStreamService;

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        if (args.length != 1) {
            System.out.println("Usage: mapr-opc-streams <config file>");
            return;
        }
        configFile = args[0];
        SpringApplication.run(OpcStreamApplication.class, args);
    }

    @Bean
    public OpcConfig opcConfig() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        OpcConfig opcConfig = yaml.loadAs(new FileInputStream(new File(configFile)), OpcConfig.class);
        writeDefaultsToItemIfEmpty(opcConfig);
        return opcConfig;
    }

    private void writeDefaultsToItemIfEmpty(OpcConfig opcConfig) {
        for (OpcItem opcItem : opcConfig.getItems()) {
            if(StringUtils.isBlank(opcItem.getLineFormat())) {
                opcItem.setLineFormat(opcConfig.getLineFormat());
            }
            if(StringUtils.isBlank(opcItem.getTimeFormat())) {
                opcItem.setTimeFormat(opcConfig.getTimeFormat());
            }
            if(opcItem.getDistinctValue() == null) {
                opcItem.setDistinctValue(opcConfig.getDistinctValue());
            }
            if(opcItem.getDistinctTimeStamp() == null) {
                opcItem.setDistinctTimeStamp(opcConfig.getDistinctTimeStamp());
            }
        }
    }


    @Override
    public void run(String... args) throws Exception {
        opcStreamService.start();
    }
}