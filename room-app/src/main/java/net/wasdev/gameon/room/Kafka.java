package net.wasdev.gameon.room;

import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.kafka.clients.producer.*;

@ApplicationScoped
public class Kafka {

  protected String kafkaUrl;

   private Producer<String,String> producer=null;

   public Kafka(){
     getConfig();
     initProducer();
   }

   private void getConfig() {
       try {
           kafkaUrl = (String) new InitialContext().lookup("kafkaUrl");
       } catch (NamingException e) {
       }
       if (kafkaUrl == null ) {
           throw new IllegalStateException("kafkaUrl("+String.valueOf(kafkaUrl)+") was not found, check server.xml/server.env");
       }
   }

   private void initProducer(){
       System.out.println("Initializing kafka producer for url "+kafkaUrl);
       Properties producerProps = new Properties();
       producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
       producerProps.put("acks","all");
       producerProps.put("retries",0);
       producerProps.put("batch.size",16384);
       producerProps.put("zookeeper.session.timeout.ms",1000);
       producerProps.put("linger.ms",1);
       producerProps.put("buffer.memory",33554432);
       producerProps.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
       producerProps.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
       producer = new KafkaProducer<String, String>(producerProps);
   }

   public void publishMessage(String topic, String key, String message){
     System.out.println("Publishing to kafka, creating record");
     ProducerRecord<String,String> pr = new ProducerRecord<String,String>(topic, key, message);
     System.out.println("Publishing to kafka, sending record");
     producer.send(pr);
     System.out.println("Publishing to kafka, sent record");
   }
}
