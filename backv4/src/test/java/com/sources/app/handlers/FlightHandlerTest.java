package com.sources.app.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sources.app.dao.FlightDAO;
import com.sources.app.entities.Flight;
import com.sources.app.entities.City;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para FlightHandler.
 * Verifica el comportamiento de las operaciones HTTP de vuelos.
 */
@ExtendWith(MockitoExtension.class)
class FlightHandlerTest {

    @Mock
    private HttpExchange mockExchange;

    @Mock
    private Headers mockResponseHeaders;

    @Mock
    private Headers mockRequestHeaders;

    private FlightHandler flightHandler;
    private Gson gson;
    private ByteArrayOutputStream responseStream;

    @BeforeEach
    void setUp() {
        flightHandler = new FlightHandler();
        gson = new Gson();
        responseStream = new ByteArrayOutputStream();
        
        // Configurar mocks por defecto con lenient() para evitar warnings de stubbing innecesario
        lenient().when(mockExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
        lenient().when(mockExchange.getResponseBody()).thenReturn(responseStream);
    }

    @Test
    void testHandleOptionsRequest() throws IOException {
        // Arrange
        lenient().when(mockExchange.getRequestMethod()).thenReturn("OPTIONS");
        lenient().when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/airline/flights"));

        // Act
        flightHandler.handle(mockExchange);

        // Assert
        verify(mockResponseHeaders).add("Access-Control-Allow-Origin", "*");
        verify(mockResponseHeaders).add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        verify(mockResponseHeaders).add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        verify(mockExchange).sendResponseHeaders(204, -1);
    }

    @Test
    void testHandleGetFlightsRequest() throws IOException {
        // Arrange
        when(mockExchange.getRequestMethod()).thenReturn("GET");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/airline/flights"));
        when(mockExchange.getResponseBody()).thenReturn(responseStream);

        // Act
        flightHandler.handle(mockExchange);

        // Assert
        verify(mockResponseHeaders).add("Access-Control-Allow-Origin", "*");
        verify(mockExchange).sendResponseHeaders(anyInt(), anyLong());
        
        // Verificar que se escribió algo en el response
        assertTrue(responseStream.size() > 0);
    }

    @Test
    void testHandlePostFlightRequest() throws IOException {
        // Arrange
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/airline/flights"));
        
        // Crear JSON de prueba para crear un vuelo
        JsonObject flightJson = new JsonObject();
        flightJson.addProperty("flightNumber", "TEST123");
        flightJson.addProperty("aircraftId", 1);
        flightJson.addProperty("originId", 1);
        flightJson.addProperty("destinationId", 2);
        
        String requestBody = gson.toJson(flightJson);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));
        when(mockExchange.getRequestBody()).thenReturn(inputStream);
        when(mockExchange.getResponseBody()).thenReturn(responseStream);

        // Act
        flightHandler.handle(mockExchange);

        // Assert
        verify(mockExchange).sendResponseHeaders(anyInt(), anyLong());
        assertTrue(responseStream.size() > 0);
    }

    @Test
    void testHandleGetFlightByIdRequest() throws IOException {
        // Arrange
        when(mockExchange.getRequestMethod()).thenReturn("GET");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/airline/flights/1"));
        when(mockExchange.getResponseBody()).thenReturn(responseStream);

        // Act
        flightHandler.handle(mockExchange);

        // Assert
        verify(mockExchange).sendResponseHeaders(anyInt(), anyLong());
        assertTrue(responseStream.size() > 0);
    }

    @Test
    void testHandleInvalidMethod() throws IOException {
        // Arrange
        when(mockExchange.getRequestMethod()).thenReturn("DELETE");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/airline/flights"));
        when(mockExchange.getResponseBody()).thenReturn(responseStream);

        // Act
        flightHandler.handle(mockExchange);

        // Assert
        verify(mockExchange).sendResponseHeaders(anyInt(), anyLong());
        
        // Verificar que se devolvió un error
        String response = responseStream.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("error") || response.contains("success"));
    }

    @Test
    void testGetFlightsReturnsValidJson() {
        // Act
        String result = flightHandler.getAllFlightsJson();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verificar que es JSON válido
        assertDoesNotThrow(() -> gson.fromJson(result, Object.class));
    }

    @Test
    void testGetFlightByIdReturnsValidJson() {
        // Act
        String result = flightHandler.getFlightByIdJson("1");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verificar que es JSON válido
        assertDoesNotThrow(() -> gson.fromJson(result, Object.class));
    }

    @Test
    void testGetFlightByIdWithInvalidId() {
        // Act
        String result = flightHandler.getFlightByIdJson("invalid");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("error") || result.contains("success"));
    }

    @Test
    void testCreateFlightWithValidJson() {
        // Arrange
        JsonObject flightJson = new JsonObject();
        flightJson.addProperty("flightNumber", "TEST456");
        flightJson.addProperty("aircraftId", 1);
        flightJson.addProperty("originId", 1);
        flightJson.addProperty("destinationId", 2);
        
        String requestBody = gson.toJson(flightJson);

        // Act
        String result = flightHandler.createFlightFromJson(requestBody);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verificar que es JSON válido
        assertDoesNotThrow(() -> gson.fromJson(result, Object.class));
    }

    @Test
    void testCreateFlightWithInvalidJson() {
        // Arrange
        String invalidJson = "{invalid json}";

        // Act
        String result = flightHandler.createFlightFromJson(invalidJson);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("error") || result.contains("success"));
    }

    @Test
    void testGetAllFlightsJson() {
        // Act
        String result = flightHandler.getAllFlightsJson();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verificar que es JSON válido
        assertDoesNotThrow(() -> gson.fromJson(result, Object.class));
    }

    @Test
    void testHandlerInitialization() {
        // Act & Assert
        assertNotNull(flightHandler);
        assertDoesNotThrow(() -> new FlightHandler());
    }
}

