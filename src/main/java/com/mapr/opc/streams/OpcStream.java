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
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.LogManager;

public class OpcStream {
    private static final Logger logger = LoggerFactory.getLogger(OpcStream.class);
    private final OpcConfig opcConfig;
    private AtomicLong counter = new AtomicLong(1L);


    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        if (args.length != 1) {
            System.out.println("Usage: mapr-opc-streams <config file>");
            return;
        }
        OpcStream opcStream = new OpcStream(args[0]);
        opcStream.start();
    }

    private OpcStream(String configFile) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        opcConfig = yaml.loadAs(new FileInputStream(new File(configFile)), OpcConfig.class);
    }

    private void start() {
        Properties properties = new Properties();
        properties.setProperty("batch.size", "16384");
        properties.setProperty("block.on.buffer.full", "true");
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
                DataCallback itemCallback = createItemCallback(producer, item.getItemId(), item.getTopic());
                access.addItem(item.getItemId(), itemCallback);
            }

            access.bind();

        } catch (JIException | AlreadyConnectedException | DuplicateGroupException | UnknownHostException | AddFailedException | NotConnectedException e) {
            throw new OpcException("Error during start", e);
        } finally {
            if(producer != null) {
                producer.close();
            }
        }

    }

    private <T> T getValueOrDefault(T value, T defaultValue) {
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    private DataCallback createItemCallback(KafkaProducer<String, String> producer, String itemId, String topic) {
        return new DataCallback() {
            private long lastTime = 0;
            private String lastValue = "";

            @Override
            public void changed(Item item, ItemState state) {
                long currentTime = state.getTimestamp().getTimeInMillis();
                if (opcConfig.isDistinctTimeStamp() && currentTime == lastTime) {
                    // equal time and distinctTime, do not track
                    return;
                }
                lastTime = currentTime;
                try {
                    JIVariant value = state.getValue();
                    Object object = value.getObject();
                    String converted = convert(object);
                    if (opcConfig.isDistinctValue() && converted.equals(lastValue)) {
                        // equal value and distinctValue, do not track
                        return;
                    }
                    lastValue = converted;
                    String message = opcConfig.getLineFormat();
                    message = message.replace("{ITEM_ID}", item.getId());
                    if ("millis".equalsIgnoreCase(opcConfig.getTimeFormat())) {
                        message = message.replace("{TIME}", Long.toString(state.getTimestamp().getTimeInMillis()));
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat(opcConfig.getTimeFormat());
                        message = message.replace("{TIME}", sdf.format(state.getTimestamp().getTime()));
                    }
                    message = message.replace("{VALUE}", converted);
                    message = message.replace("{QUALITY}", Short.toString(state.getQuality()));
                    message = message.replace("{ERROR}", Integer.toString(state.getErrorCode()));
                    producer.send(new ProducerRecord<>(topic, message));
                    producer.flush();
                    long count = counter.incrementAndGet();
                    if (count % 100 == 0) {
                        logger.info("Processed messages");
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