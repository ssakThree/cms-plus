package kr.or.kosa.cmspluspayment.service;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import kr.or.kosa.cmspluspayment.dto.PaymentDto;
import kr.or.kosa.cmspluspayment.dto.PaymentResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {




    @KafkaListener(topics = "payment-topic", groupId = "payment-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeMessage(ConsumerRecord<String, PaymentDto> consumerRecord) {
        PaymentDto paymentDto = consumerRecord.value();
        log.error("[테스트 ㄱㄱ]: {}", paymentDto.toString());

        if (checkResult(paymentDto).equals("SUCCESS")) {
            try {

                PaymentResultDto paymentResultDto = new PaymentResultDto();
                paymentResultDto.setBillingId(1L);
                paymentResultDto.setResult("SUCCESS");
                log.error(paymentResultDto.toString());
                kafkaTemplate.send(resultTopic, paymentResultDto);

            } catch (Exception e) {
                log.error(e.getMessage());
            }



        }


    }


    public String checkResult(PaymentDto paymentDto) {

        return "SUCCESS";
    }

    private final KafkaTemplate<String, PaymentResultDto> kafkaTemplate;

    String resultTopic = "payment-result-topic";

    public void sendResult(String topic, PaymentResultDto paymentResult) {
        log.error("[토픽]={} [페이로드]={}", topic, paymentResult);

    }


//    private final FirebaseMessaging firebaseMessaging;
//
//
//    private void sendFcmNotification(PaymentDto paymentDto) throws FirebaseMessagingException {
//        Notification notification = Notification.builder()
//                .setTitle("PAYMENT SUCCESS")
//                .setBody(paymentDto.getMessage())
//                .build();
//
//        Message message = Message.builder()
//                .setNotification(notification)
//                .setTopic("payment-topic")
//                .build();
//
//        String response = firebaseMessaging.send(message);
//        log.error("[결과물]: {}", response);
//    }

}