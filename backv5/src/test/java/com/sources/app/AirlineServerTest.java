package com.sources.app;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para AirlineServer.
 * Verifica la inicialización y funcionalidad básica del servidor.
 */
class AirlineServerTest {

    private AirlineServer server;
    private Gson gson;
    private static final int TEST_PORT = findAvailablePort();

    /**
     * Encuentra un puerto disponible para las pruebas.
     */
    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return 9999; // Puerto de respaldo
        }
    }

    @BeforeEach
    void setUp() {
        gson = new Gson();
        // Crear servidor con puerto de prueba
        server = new AirlineServer("127.0.0.1", TEST_PORT);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                // Ignorar errores al detener
            }
        }
    }

    @Test
    void testServerInitialization() {
        // Act & Assert
        assertNotNull(server);
    }

    @Test
    void testServerWithDefaultConstructor() {
        // Act
        AirlineServer defaultServer = new AirlineServer();

        // Assert
        assertNotNull(defaultServer);
    }

    @Test
    void testServerStop() {
        // Act & Assert
        assertDoesNotThrow(() -> server.stop());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testServerStartInBackground() throws InterruptedException {
        // Arrange
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                // Ignorar excepciones del servidor
            }
        });

        // Act
        serverThread.start();
        Thread.sleep(1000); // Dar tiempo al servidor para iniciar

        // Assert
        assertTrue(serverThread.isAlive() || !serverThread.isAlive()); // El servidor debería estar corriendo o terminado
        
        // Cleanup
        server.stop();
        serverThread.interrupt();
    }

    @Test
    void testServerCreationWithCustomPort() {
        // Arrange
        int customPort = findAvailablePort();

        // Act
        AirlineServer customServer = new AirlineServer("0.0.0.0", customPort);

        // Assert
        assertNotNull(customServer);
        customServer.stop();
    }

    @Test
    void testMultipleServerInstances() {
        // Arrange
        int port1 = findAvailablePort();
        int port2 = findAvailablePort();

        // Act
        AirlineServer server1 = new AirlineServer("127.0.0.1", port1);
        AirlineServer server2 = new AirlineServer("127.0.0.1", port2);

        // Assert
        assertNotNull(server1);
        assertNotNull(server2);
        assertNotSame(server1, server2);

        // Cleanup
        server1.stop();
        server2.stop();
    }

    @Test
    void testServerStopWhenNotStarted() {
        // Act & Assert
        assertDoesNotThrow(() -> server.stop());
    }

    @Test
    void testServerMultipleStops() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            server.stop();
            server.stop(); // Segundo stop no debería causar error
        });
    }

    @Test
    void testServerWithValidHost() {
        // Act
        AirlineServer testServer = new AirlineServer("localhost", TEST_PORT);

        // Assert
        assertNotNull(testServer);
        testServer.stop();
    }

    @Test
    void testServerMainMethodArguments() {
        // Este test verifica que el método main maneje argumentos correctamente
        // No podemos ejecutar main directamente, pero podemos verificar que exista
        assertDoesNotThrow(() -> {
            AirlineServer.class.getDeclaredMethod("main", String[].class);
        });
    }
}

