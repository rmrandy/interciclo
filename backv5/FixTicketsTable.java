package com.sources.app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase para corregir la tabla TICKETS agregando columnas faltantes
 * Resuelve el error ORA-00904: "PAYMENT_STATUS": invalid identifier
 */
public class FixTicketsTable {
    
    // Configuración de la base de datos (igual que en hibernate.cfg.xml)
    private static final String DB_URL = "jdbc:oracle:thin:@//64.225.58.196:1521/XEPDB1";
    private static final String DB_USER = "AEROLINEA";
    private static final String DB_PASSWORD = "123";
    
    public static void main(String[] args) {
        System.out.println("🔧 Iniciando corrección de la tabla TICKETS...");
        System.out.println("================================================");
        
        try {
            // Registrar el driver de Oracle
            Class.forName("oracle.jdbc.driver.OracleDriver");
            
            System.out.println("📡 Conectando a la base de datos Oracle...");
            System.out.println("Host: 64.225.58.196:1521");
            System.out.println("Servicio: XEPDB1");
            System.out.println("Usuario: " + DB_USER);
            System.out.println();
            
            // Conectar a la base de datos
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("✅ Conexión exitosa a la base de datos");
                System.out.println();
                
                // Verificar si la tabla TICKETS existe
                if (!tableExists(connection, "TICKETS")) {
                    System.out.println("❌ Error: La tabla TICKETS no existe");
                    System.out.println("Por favor, crea la tabla TICKETS primero");
                    return;
                }
                
                System.out.println("✅ Tabla TICKETS encontrada");
                
                // Verificar las columnas actuales
                showExistingColumns(connection);
                
                // Agregar columnas faltantes
                addMissingColumns(connection);
                
                // Verificar las columnas después de la modificación
                showFinalColumns(connection);
                
                System.out.println();
                System.out.println("✅ Script ejecutado exitosamente!");
                System.out.println("🎫 La tabla TICKETS ha sido actualizada con las columnas faltantes");
                System.out.println();
                System.out.println("🎉 Ahora deberías poder comprar boletos sin problemas!");
                
            } catch (SQLException e) {
                System.err.println("❌ Error de base de datos: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: Driver de Oracle no encontrado");
            e.printStackTrace();
        }
    }
    
    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName.toUpperCase());
            try (var rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    private static void showExistingColumns(Connection connection) throws SQLException {
        String sql = """
            SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE 
            FROM USER_TAB_COLUMNS 
            WHERE TABLE_NAME = 'TICKETS' 
            ORDER BY COLUMN_ID
        """;
        
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            
            System.out.print("📋 Columnas existentes: ");
            while (rs.next()) {
                System.out.print(rs.getString("COLUMN_NAME") + ", ");
            }
            System.out.println();
            System.out.println();
        }
    }
    
    private static void addMissingColumns(Connection connection) throws SQLException {
        System.out.println("🚀 Agregando columnas faltantes...");
        System.out.println();
        
        // Definir las columnas que necesitamos agregar
        String[][] columnsToAdd = {
            {"PAYMENT_STATUS", "VARCHAR2(20)", "DEFAULT 'PENDING'"},
            {"PAYMENT_METHOD", "VARCHAR2(30)", ""},
            {"TOTAL_AMOUNT", "NUMBER(10,2)", ""},
            {"TAXES", "NUMBER(10,2)", "DEFAULT 0"},
            {"FEES", "NUMBER(10,2)", "DEFAULT 0"},
            {"DISCOUNT_AMOUNT", "NUMBER(10,2)", "DEFAULT 0"},
            {"DISCOUNT_CODE", "VARCHAR2(50)", ""},
            {"CANCELLATION_DATE", "VARCHAR2(30)", ""},
            {"CANCELLATION_REASON", "VARCHAR2(200)", ""},
            {"REFUND_AMOUNT", "NUMBER(10,2)", "DEFAULT 0"},
            {"CREATED_AT", "VARCHAR2(30)", ""},
            {"UPDATED_AT", "VARCHAR2(30)", ""}
        };
        
        try (var stmt = connection.createStatement()) {
            for (String[] column : columnsToAdd) {
                String columnName = column[0];
                String dataType = column[1];
                String defaultValue = column[2];
                
                try {
                    String sql;
                    if (!defaultValue.isEmpty()) {
                        sql = String.format("ALTER TABLE TICKETS ADD %s %s %s", 
                                          columnName, dataType, defaultValue);
                    } else {
                        sql = String.format("ALTER TABLE TICKETS ADD %s %s", 
                                          columnName, dataType);
                    }
                    
                    stmt.execute(sql);
                    System.out.println("✅ Columna " + columnName + " agregada exitosamente");
                    
                } catch (SQLException e) {
                    if (e.getMessage().contains("ORA-01430")) {
                        // Columna ya existe
                        System.out.println("ℹ️  Columna " + columnName + " ya existe");
                    } else {
                        System.out.println("❌ Error agregando " + columnName + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private static void showFinalColumns(Connection connection) throws SQLException {
        // Verificar el total de columnas
        String countSql = "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'TICKETS'";
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery(countSql)) {
            if (rs.next()) {
                int totalColumns = rs.getInt(1);
                System.out.println();
                System.out.println("📊 Total de columnas en TICKETS: " + totalColumns);
            }
        }
        
        // Mostrar todas las columnas
        String sql = """
            SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE, DATA_DEFAULT
            FROM USER_TAB_COLUMNS 
            WHERE TABLE_NAME = 'TICKETS' 
            ORDER BY COLUMN_ID
        """;
        
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            
            System.out.println();
            System.out.println("📋 Columnas finales de la tabla TICKETS:");
            System.out.println("-".repeat(80));
            System.out.printf("%-20s %-15s %-10s %-10s %s%n", 
                            "COLUMNA", "TIPO", "LONGITUD", "NULLABLE", "DEFAULT");
            System.out.println("-".repeat(80));
            
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("DATA_TYPE");
                String dataLength = rs.getString("DATA_LENGTH");
                String nullable = rs.getString("NULLABLE");
                String defaultValue = rs.getString("DATA_DEFAULT");
                
                if (defaultValue == null) defaultValue = "";
                
                System.out.printf("%-20s %-15s %-10s %-10s %s%n", 
                                columnName, dataType, dataLength, nullable, defaultValue);
            }
        }
    }
}

