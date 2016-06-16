package com.mapr.opc.streams;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIUnsigned;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.common.NotConnectedException;
import org.openscada.opc.lib.da.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.LogManager;

@Service
public class OpcStreamService {
    private static final Logger logger = LoggerFactory.getLogger(OpcStreamService.class);
    @Autowired
    private OpcConfig opcConfig;
    private AtomicLong counter = new AtomicLong(1L);

    public void start() {
        Properties properties = new Properties();
        Map<String, String> kafka = opcConfig.getKafka();
        for (String kafkaKey : kafka.keySet()) {
            properties.setProperty(kafkaKey, kafka.get(kafkaKey));
        }
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());
        KafkaProducer<String, String> producer = null;

        ConnectionInformation ci = new ConnectionInformation();
        ci.setHost(getValueOrDefault(opcConfig.getHostname(), "localhost"));
        if(StringUtils.isNotBlank(opcConfig.getDomain())) {
            ci.setDomain(opcConfig.getDomain());
        }
        if(StringUtils.isNotBlank(opcConfig.getUser())) {
            ci.setUser(opcConfig.getUser());
        }
        if(StringUtils.isNotBlank(opcConfig.getPassword())) {
            ci.setPassword(opcConfig.getPassword());
        }
        if(StringUtils.isNotBlank(opcConfig.getProgId())) {
            ci.setProgId(opcConfig.getProgId());
        }
        if(StringUtils.isNotBlank(opcConfig.getClsId())) {
            ci.setClsid(opcConfig.getClsId());
        }
        // create a new server
        int corePoolSize = getValueOrDefault(opcConfig.getThreads(), 1);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(corePoolSize);
        Server server = new Server(ci, scheduler);

        try {
            producer = new KafkaProducer<>(properties);
            server.connect();
            SyncAccess access = new SyncAccess(server, getValueOrDefault(opcConfig.getFetchIntervalInMs(), 1000));
            for (OpcItem item : opcConfig.getItems()) {
                DataCallback itemCallback = createItemCallback(producer, item, item.getTopic());
                access.addItem(item.getItemId(), itemCallback);
            }

            access.bind();

        } catch (JIException | AlreadyConnectedException | DuplicateGroupException | UnknownHostException | AddFailedException | NotConnectedException e) {
            throw new OpcException("Error during start", e);
        }
    }

    private <T> T getValueOrDefault(T value, T defaultValue) {
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    private DataCallback createItemCallback(KafkaProducer<String, String> producer, OpcItem opcItem, String topic) {
        return new DataCallback() {
            private long lastTime = 0;
            private String lastValue = "";

            @Override
            public void changed(Item item, ItemState state) {
                long currentTime = state.getTimestamp().getTimeInMillis();
                if (opcItem.getDistinctTimeStamp() && currentTime == lastTime) {
                    // equal time and distinctTime, do not track
                    return;
                }
                lastTime = currentTime;
                try {
                    JIVariant value = state.getValue();
                    Object object = value.getObject();
                    String converted = convert(object);
                    if (opcItem.getDistinctValue() && converted.equals(lastValue)) {
                        // equal value and distinctValue, do not track
                        return;
                    }
                    lastValue = converted;
                    String message = opcItem.getLineFormat();
                    message = message.replace("{ITEM_ID}", item.getId());
                    if ("millis".equalsIgnoreCase(opcItem.getTimeFormat())) {
                        message = message.replace("{TIME}", Long.toString(state.getTimestamp().getTimeInMillis()));
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat(opcItem.getTimeFormat());
                        message = message.replace("{TIME}", sdf.format(state.getTimestamp().getTime()));
                    }
                    message = message.replace("{VALUE}", converted);
                    message = message.replace("{QUALITY}", Short.toString(state.getQuality()));
                    message = message.replace("{ERROR}", Integer.toString(state.getErrorCode()));
                    producer.send(new ProducerRecord<>(topic, message));
//                    producer.flush();
//                    System.out.println(message);
                    long count = counter.incrementAndGet();
                    if (count % 100 == 0) {
                        logger.info("Processed messages: " + count);
                    }
                    logger.debug("Event processed: " + message);

                } catch (JIException e) {
                    logger.error("During item evaluation", e);
                }
            }
        };
    }


    private String convert(Object object) {
        if (object == null) {
            return "[empty]";
        }
        if (object instanceof JIString) {
            return ((JIString) object).getString();
        } else if (object instanceof IJIUnsigned) {
            return ((IJIUnsigned) object).getValue().toString();
        } else if (object instanceof JIArray) {
            JIArray array = (JIArray) object;
            Object[] arrayInstance = (Object[]) array.getArrayInstance();
            StringBuilder buf = new StringBuilder();
            boolean firstRun = true;
            for (Object o : arrayInstance) {
                if (!firstRun) {
                    buf.append(',');
                }
                buf.append(convert(o));
                firstRun = false;
            }
            return buf.toString();
        } else if (object instanceof Character) {
            char o = (Character) object;
            return Integer.toString((int) o);
        } else {
            return object.toString();
        }
    }

}