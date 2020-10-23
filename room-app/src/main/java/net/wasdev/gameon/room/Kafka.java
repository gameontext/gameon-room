package net.wasdev.gameon.room;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

@ApplicationScoped
public class Kafka {

   @Resource(lookup="kafkaUrl")
   protected String kafkaUrl;

   private Producer<String,String> producer=null;

   public Kafka(){
   }

   @PostConstruct
   public void init(){
     try{
         //Kafka client expects this property to be set and pointing at the
         //jaas config file.. except when running in liberty, we don't need
         //one of those.. thankfully, neither does kafka client, it just doesn't
         //know that.. so we'll set this to an empty string to bypass the check.
         if(System.getProperty("java.security.auth.login.config")==null){
           System.setProperty("java.security.auth.login.config", "");
         }

         Log.log(Level.INFO, this, "Initializing kafka producer for url {0}", kafkaUrl);
         Properties producerProps = new Properties();
         producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
         producerProps.put(ProducerConfig.ACKS_CONFIG,"-1");
         producerProps.put(ProducerConfig.CLIENT_ID_CONFIG,"gameon-map");
         producerProps.put(ProducerConfig.RETRIES_CONFIG,0);
         producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG,16384);
         producerProps.put(ProducerConfig.LINGER_MS_CONFIG,1);
         producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG,33554432);
         producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
         producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");

         producer = new KafkaProducer<String, String>(producerProps);
     }catch(Exception e){
         System.out.println("KAFKA INIT FAILED");
         e.printStackTrace(System.out);
         throw e;
     }
   }

   public void publishMessage(String topic, String key, String message){
     Log.log(Level.FINER, this, "Publishing Event {0} {1} {2}",topic,key,message);
     ProducerRecord<String,String> pr = new ProducerRecord<String,String>(topic, key, message);
     producer.send(pr);
     Log.log(Level.FINER, this, "Published Event");
   }

}
