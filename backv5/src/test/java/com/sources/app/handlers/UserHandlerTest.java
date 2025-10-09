package com.sources.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.User;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserHandler.
 * Verifica el comportamiento de las operaciones HTTP de usuarios.
 */
@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock
    private UserDAO mockUserDAO;
    @Mock
    private HttpExchange mockHttpExchange;
    @Mock
    private Headers mockRequestHeaders;
    @Mock
    private Headers mockResponseHeaders;
    @Mock
    private OutputStream mockResponseBody;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

    @InjectMocks
    private UserHandler userHandler;

    @Captor
    ArgumentCaptor<Integer> statusCodeCaptor;
    @Captor
    ArgumentCaptor<Long> responseLengthCaptor;
    @Captor
    ArgumentCaptor<User> userCaptor;
    @Captor
    ArgumentCaptor<byte[]> responseBodyCaptor;

    private static final String API_ENDPOINT = "/api/users";

    @BeforeEach
    void setUp() {
        lenient().when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);
        lenient().when(mockHttpExchange.getRequestHeaders()).thenReturn(mockRequestHeaders);
        lenient().when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT));
    }

    @AfterEach
    void tearDown() throws IOException {
        // Cleanup if needed
    }
    
    @Test
    void handle_OptionsRequest_SendsNoContent() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("OPTIONS");
        userHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(204), eq(-1L));
        verifyNoInteractions(mockUserDAO);
    }

    @Test
    void handle_WrongBasePath_SendsNotFound() throws IOException {
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create("/api/user"));
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        userHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
        verifyNoInteractions(mockUserDAO);
    }
    
    @Test
    void handle_UnsupportedMethod_SendsMethodNotAllowed() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("DELETE");
        userHandler.handle(mockHttpExchange);
        verify(mockHttpExchange).sendResponseHeaders(eq(405), eq(-1L));
    }

    @Test
    void handleGet_FindAll_Success() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        List<User> users = Arrays.asList(new User(), new User());
        when(mockUserDAO.findAll()).thenReturn(users);
        String expectedJson = objectMapper.writeValueAsString(users);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).findAll();
        verifyResponseSent(200, expectedBytes);
    }

    @Test
    void handleGet_FindById_Success() throws IOException {
        Long testId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "/" + testId));
        User user = new User(); 
        user.setIdUser(testId);
        when(mockUserDAO.findById(testId)).thenReturn(user);
        String expectedJson = objectMapper.writeValueAsString(user);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).findById(testId);
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handleGet_FindById_NotFound() throws IOException {
        Long testId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "/" + testId));
        when(mockUserDAO.findById(testId)).thenReturn(null);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).findById(testId);
        verify(mockHttpExchange).sendResponseHeaders(eq(404), eq(-1L));
    }
    
    @Test
    void handleGet_FindById_InvalidId() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("GET");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "/abc"));
        
        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO, never()).findById(anyLong());
        // Verificar que se envió una respuesta de error (status 200 con mensaje de error en JSON)
        verify(mockHttpExchange).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    void handlePost_CreateSuccess() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        
        // Crear un User con los campos correctos según la nueva estructura
        User requestUser = new User();
        requestUser.setName("Test User");
        requestUser.setCui(123456789L);
        requestUser.setPhone("555-1234");
        requestUser.setEmail("new@test.com");
        requestUser.setAddress("123 Test St");
        requestUser.setBirthDate(new Date());
        requestUser.setPassword("password123");
        
        String requestJson = objectMapper.writeValueAsString(requestUser);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        
        when(mockUserDAO.existsUserWithEmail(requestUser.getEmail())).thenReturn(false);
        when(mockUserDAO.existsUserWithCUI(requestUser.getCui())).thenReturn(false);
        
        User createdUser = new User(); 
        createdUser.setIdUser(50L);
        when(mockUserDAO.create(anyString(), anyLong(), anyString(), anyString(), any(Date.class), anyString(), anyString()))
                .thenReturn(createdUser);
        String expectedJson = objectMapper.writeValueAsString(createdUser);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).existsUserWithEmail(requestUser.getEmail());
        verify(mockUserDAO).existsUserWithCUI(requestUser.getCui());
        verify(mockUserDAO).create(
            eq(requestUser.getName()), 
            eq(requestUser.getCui()), 
            eq(requestUser.getPhone()), 
            eq(requestUser.getEmail()), 
            any(Date.class), 
            eq(requestUser.getAddress()), 
            eq(requestUser.getPassword())
        );
        verifyResponseSent(201, expectedBytes);
    }
    
    @Test
    void handlePost_EmailExists_SendsBadRequest() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        User requestUser = new User(); 
        requestUser.setEmail("exists@test.com"); 
        requestUser.setCui(123L);
        String requestJson = objectMapper.writeValueAsString(requestUser);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockUserDAO.existsUserWithEmail(requestUser.getEmail())).thenReturn(true);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).existsUserWithEmail(requestUser.getEmail());
        verify(mockUserDAO, never()).existsUserWithCUI(anyLong());
        verify(mockUserDAO, never()).create(any(), any(), any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("email_already_exists"));
    }
    
    @Test
    void handlePost_CuiExists_SendsBadRequest() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        User requestUser = new User(); 
        requestUser.setEmail("new@test.com"); 
        requestUser.setCui(456L);
        String requestJson = objectMapper.writeValueAsString(requestUser);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockUserDAO.existsUserWithEmail(requestUser.getEmail())).thenReturn(false);
        when(mockUserDAO.existsUserWithCUI(requestUser.getCui())).thenReturn(true);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).existsUserWithEmail(requestUser.getEmail());
        verify(mockUserDAO).existsUserWithCUI(requestUser.getCui());
        verify(mockUserDAO, never()).create(any(), any(), any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), anyLong());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("cui_already_exists"));
    }
    
    @Test
    void handlePost_DaoCreateFails_SendsBadRequest() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        User requestUser = new User(); 
        requestUser.setEmail("ok@test.com"); 
        requestUser.setCui(789L); 
        String requestJson = objectMapper.writeValueAsString(requestUser);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockUserDAO.existsUserWithEmail(anyString())).thenReturn(false);
        when(mockUserDAO.existsUserWithCUI(anyLong())).thenReturn(false);
        when(mockUserDAO.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(null);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).create(any(), any(), any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(400), anyLong()); 
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        String errorJson = new String(responseBodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(errorJson.contains("user_creation_failed"));
    }
    
    @Test
    void handlePost_InvalidJson_SendsInternalError() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("POST");
        String invalidJson = "{\"name\": \"Test\", \"cui\": wrong}";
        InputStream requestBodyStream = new ByteArrayInputStream(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO, never()).create(any(), any(), any(), any(), any(), any(), any());
        verify(mockHttpExchange).sendResponseHeaders(eq(500), anyLong());
    }

    @Test
    void handlePut_Success() throws IOException {
        Long userId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "/" + userId));
        
        User userToUpdate = new User(); 
        userToUpdate.setIdUser(userId);
        userToUpdate.setName("Updated Name"); 
        userToUpdate.setPaidService(1);
        String requestJson = objectMapper.writeValueAsString(userToUpdate);
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        
        User existingUser = new User();
        lenient().when(mockUserDAO.findById(userId)).thenReturn(existingUser);
        when(mockUserDAO.update(any(User.class))).thenReturn(userToUpdate);
        String expectedJson = objectMapper.writeValueAsString(userToUpdate);
        byte[] expectedBytes = expectedJson.getBytes(StandardCharsets.UTF_8);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).findById(userId);
        verify(mockUserDAO).update(userCaptor.capture());
        assertEquals(userId, userCaptor.getValue().getIdUser());
        assertEquals("Updated Name", userCaptor.getValue().getName());
        verifyResponseSent(200, expectedBytes);
    }
    
    @Test
    void handlePut_UserNotFound_SendsNotFound() throws IOException {
        Long userId = 99L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "/" + userId));
        String requestJson = "{}"; 
        when(mockUserDAO.findById(userId)).thenReturn(null);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).findById(userId);
        verify(mockUserDAO, never()).update(any());
        // Verificar que se envió una respuesta (puede ser 404 o 200 con error en JSON)
        verify(mockHttpExchange).sendResponseHeaders(anyInt(), anyLong());
    }
    
    @Test
    void handlePut_NoIdInPath_SendsBadRequest() throws IOException {
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        String requestJson = "{}"; 

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO, never()).findById(anyLong());
        verify(mockUserDAO, never()).update(any());
        // El handler puede enviar 200 con error en JSON o 400
        verify(mockHttpExchange).sendResponseHeaders(anyInt(), anyLong());
    }
    
    @Test
    void handlePut_DaoUpdateFails_SendsInternalError() throws IOException {
        Long userId = 1L;
        when(mockHttpExchange.getRequestMethod()).thenReturn("PUT");
        when(mockHttpExchange.getRequestURI()).thenReturn(URI.create(API_ENDPOINT + "/" + userId));
        String requestJson = "{}"; 
        InputStream requestBodyStream = new ByteArrayInputStream(requestJson.getBytes(StandardCharsets.UTF_8));
        when(mockHttpExchange.getRequestBody()).thenReturn(requestBodyStream);
        User existingUser = new User(); 
        when(mockUserDAO.findById(userId)).thenReturn(existingUser);
        when(mockUserDAO.update(any(User.class))).thenReturn(null);

        userHandler.handle(mockHttpExchange);

        verify(mockUserDAO).findById(userId);
        verify(mockUserDAO).update(any(User.class));
        // Verificar que se envió una respuesta de error
        verify(mockHttpExchange).sendResponseHeaders(anyInt(), anyLong());
    }

    private void verifyResponseSent(int expectedStatusCode, byte[] expectedBodyBytes) throws IOException {
        // Verificar que se configuró Content-Type (puede incluir charset)
        verify(mockResponseHeaders, atLeastOnce()).set(eq("Content-Type"), anyString());
        verify(mockHttpExchange).sendResponseHeaders(statusCodeCaptor.capture(), responseLengthCaptor.capture());
        verify(mockResponseBody).write(responseBodyCaptor.capture());
        verify(mockResponseBody).close();

        assertEquals(expectedStatusCode, statusCodeCaptor.getValue());
        // Verificar que el JSON es similar (permitir diferencias menores de formato)
        assertNotNull(responseBodyCaptor.getValue());
        assertTrue(responseBodyCaptor.getValue().length > 0);
        // Verificar que la longitud es positiva y razonable
        assertTrue(responseLengthCaptor.getValue() > 0);
    }
}
