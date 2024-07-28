package kr.or.kosa.cmsplusbatch.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.kosa.cmsplusbatch.dto.ExchangeDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ExchangeUtils {

    @Value("${exchange.authKey}")
    private String authKey;

    @Value("${exchange.data}")
    private String data;

    private final String searchdate = getSearchdate();

    WebClient webClient;

    public JsonNode getExchangeDataSync() {

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();

        webClient = WebClient.builder().uriBuilderFactory(factory).build();

        String responseBody = webClient.get()
                .uri(builder -> builder
                        .scheme("https")
                        .host("www.koreaexim.go.kr")
                        .path("/site/program/financial/exchangeJSON")
                        .queryParam("authkey", authKey)
                        .queryParam("searchdate", "20231121")
                        .queryParam("data", data)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return parseJson(responseBody);

    }

    private JsonNode parseJson(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ExchangeDto> getExchangeDataAsDtoList() {
        JsonNode jsonNode = getExchangeDataSync();

        if (jsonNode != null && jsonNode.isArray()) {
            List<ExchangeDto> exchangeDtoList = new ArrayList<>();

            for (JsonNode node : jsonNode) {
                ExchangeDto exchangeDto = convertJsonToExchangeDto(node);
                exchangeDtoList.add(exchangeDto);
            }

            return exchangeDtoList;
        }

        return Collections.emptyList();
    }

    private ExchangeDto convertJsonToExchangeDto(JsonNode jsonNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.treeToValue(jsonNode, ExchangeDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSearchdate() {

        LocalDate currentDate = LocalDate.now();
        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
        // 토요일
        if (dayOfWeek.getValue() == 6)
            return currentDate.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 일요일
        if (dayOfWeek.getValue() == 7)
            return currentDate.minusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}