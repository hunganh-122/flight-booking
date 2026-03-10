package com.final_project.flight_booking.services;

import com.final_project.flight_booking.models.Airport;
import com.final_project.flight_booking.models.Flight;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GroqService {

    private static final Logger logger = LoggerFactory.getLogger(GroqService.class);

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Autowired
    private FlightService flightService;

    @Autowired
    private AirportService airportService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    public String chat(String userMessage) {
        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            systemMessage.put("content", "Bạn là một trợ lý ảo tư vấn chuyến bay thông minh. " +
                    "Hôm nay là ngày " + today + ". " +
                    "Nếu người dùng không nhập ngày, hãy mặc định lấy ngày hôm nay. " +
                    "Nếu người dùng nói 'ngày mai', 'ngày kia', hãy tự tính toán ra ngày chính xác theo định dạng DD-MM-YYYY. " +
                    "Luôn trả lời lịch sự, ngắn gọn và hữu ích bằng tiếng Việt.");
            messages.add(systemMessage);

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            return callGroq(messages);
        } catch (Exception e) {
            logger.error("Error in AI Chat: {}", e.getMessage());
            return "Xin lỗi, hiện tại tôi đang gặp một chút sự cố kỹ thuật. Vui lòng thử lại sau!";
        }
    }

    @SuppressWarnings("unchecked")
    private String callGroq(List<Map<String, Object>> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile"); 
        requestBody.put("messages", messages);
        requestBody.put("tools", getTools());
        requestBody.put("tool_choice", "auto");

        String jsonRequest = gson.toJson(requestBody);
        logger.debug("Request to Groq: {}", jsonRequest);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String body = response.getBody();
            logger.debug("Response from Groq: {}", body);
            
            if (body == null || body.trim().isEmpty() || body.equals("null")) {
                logger.error("Groq API returned an empty or 'null' body");
                return "Xin lỗi, tôi không nhận được phản hồi từ hệ thống AI.";
            }

            JsonObject jsonResponse = gson.fromJson(body, JsonObject.class);
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                return "AI không đưa ra lựa chọn nào.";
            }

            JsonObject choice = choices.get(0).getAsJsonObject();
            JsonObject message = choice.getAsJsonObject("message");
            if (message == null) return "AI trả về tin nhắn trống.";

            if (message.has("tool_calls") && !message.get("tool_calls").isJsonNull()) {
                JsonArray toolCalls = message.getAsJsonArray("tool_calls");
                
                Map<String, Object> assistantMessage = new HashMap<>();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("tool_calls", gson.fromJson(toolCalls, List.class));
                if (message.has("content") && !message.get("content").isJsonNull()) {
                    assistantMessage.put("content", message.get("content").getAsString());
                } else {
                    assistantMessage.put("content", null);
                }
                messages.add(assistantMessage);

                for (int i = 0; i < toolCalls.size(); i++) {
                    JsonObject toolCall = toolCalls.get(i).getAsJsonObject();
                    String functionName = toolCall.getAsJsonObject("function").get("name").getAsString();
                    String arguments = toolCall.getAsJsonObject("function").get("arguments").getAsString();
                    String toolCallId = toolCall.get("id").getAsString();

                    logger.info("AI Calling Tool: {} with args: {}", functionName, arguments);
                    String result = executeFunction(functionName, arguments);
                    
                    Map<String, Object> toolMessage = new HashMap<>();
                    toolMessage.put("role", "tool");
                    toolMessage.put("tool_call_id", toolCallId);
                    toolMessage.put("content", result);
                    messages.add(toolMessage);
                }

                return callGroq(messages);
            }

            return (message.has("content") && !message.get("content").isJsonNull()) 
                   ? message.get("content").getAsString() 
                   : "AI đã thực hiện xong các bước nhưng không có nội dung trả lời.";
        }

        logger.error("Groq API error: {} - {}", response.getStatusCode(), response.getBody());
        return "Lỗi kết nối với Groq AI (" + response.getStatusCode() + ").";
    }

    private List<Map<String, Object>> getTools() {
        List<Map<String, Object>> tools = new ArrayList<>();

        Map<String, Object> getAirports = new HashMap<>();
        getAirports.put("type", "function");
        Map<String, Object> func1 = new HashMap<>();
        func1.put("name", "getAvailableAirports");
        func1.put("description", "Lấy danh sách các sân bay có trong hệ thống (Tên thành phố và Mã IATA).");
        getAirports.put("function", func1);
        tools.add(getAirports);

        Map<String, Object> searchFlights = new HashMap<>();
        searchFlights.put("type", "function");
        Map<String, Object> func2 = new HashMap<>();
        func2.put("name", "searchFlights");
        func2.put("description", "Tìm kiếm chuyến bay thực tế dựa trên mã IATA của sân bay đi, sân bay đến và ngày khởi hành.");
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        
        props.put("departureCode", Map.of("type", "string", "description", "Mã IATA của sân bay khởi hành, ví dụ: HAN, SGN, DAD"));
        props.put("arrivalCode",   Map.of("type", "string", "description", "Mã IATA của sân bay đến, ví dụ: HAN, SGN, DAD"));
        props.put("departureDate", Map.of("type", "string", "description", "Ngày đi định dạng DD-MM-YYYY"));
        
        params.put("properties", props);
        params.put("required", List.of("departureCode", "arrivalCode", "departureDate"));
        func2.put("parameters", params);
        searchFlights.put("function", func2);
        tools.add(searchFlights);

        return tools;
    }

    @SuppressWarnings("unchecked")
    private String executeFunction(String name, String arguments) {
        JsonObject args = gson.fromJson(arguments, JsonObject.class);
        
        if ("getAvailableAirports".equals(name)) {
            List<Airport> airports = airportService.findAll();
            return gson.toJson(airports);
        } else if ("searchFlights".equals(name)) {
            String depCode = args.get("departureCode").getAsString().toUpperCase();
            String arrCode = args.get("arrivalCode").getAsString().toUpperCase();
            String date    = args.get("departureDate").getAsString();

            if (date == null || date.isEmpty() || date.contains("YYYY") || date.contains("DD")) {
                date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }

            Airport depAirport = airportService.findByAirportCode(depCode);
            Airport arrAirport = airportService.findByAirportCode(arrCode);

            if (depAirport == null) {
                return "Không tìm thấy sân bay với mã: " + depCode + ". Hãy dùng getAvailableAirports để xem danh sách.";
            }
            if (arrAirport == null) {
                return "Không tìm thấy sân bay với mã: " + arrCode + ". Hãy dùng getAvailableAirports để xem danh sách.";
            }

            Integer depId = depAirport.getAirportId();
            Integer arrId = arrAirport.getAirportId();
            Map<String, Object> results = flightService.processFlightSearch(depId, arrId, date, "oneway", "");
            List<Flight> flights = (List<Flight>) results.get("flights");
            
            if (flights == null || flights.isEmpty()) {
                return "Không tìm thấy chuyến bay nào cho ngày này.";
            }

            List<Map<String, Object>> simplified = new ArrayList<>();
            for (Flight f : flights) {
                Map<String, Object> m = new HashMap<>();
                m.put("flightId", f.getFlightId());
                m.put("airline", f.getAirline());
                m.put("price", f.getPrice());
                m.put("departure", f.getDepartureTime().toString());
                m.put("arrival", f.getArrivalTime().toString());
                simplified.add(m);
            }
            return gson.toJson(simplified);
        }
        
        return "Lỗi: Không tìm thấy chức năng.";
    }
}
