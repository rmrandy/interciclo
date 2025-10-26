package com.sources.app.util;

import com.sources.app.entities.Flight;
import com.sources.app.entities.FlightInventory;
import com.sources.app.entities.FlightFare;
import org.hibernate.Session;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Clase para mostrar la información completa del vuelo 29
 * Combina inventario y precios para mostrar cómo se relacionan
 */
public class ShowFlight29CompleteInfo {
    
    public static void main(String[] args) {
        System.out.println("🔍 Información completa del vuelo 29");
        System.out.println("====================================");
        
        try {
            // Verificar que el vuelo 29 existe
            Flight flight = getFlightById(29);
            if (flight == null) {
                System.out.println("❌ Error: El vuelo 29 no existe");
                return;
            }
            
            System.out.println("✅ Vuelo 29 encontrado: " + flight.getFlightNumber());
            System.out.println();
            
            // Mostrar información completa combinada
            showCompleteFlightInfo(29);
            
        } catch (Exception e) {
            System.err.println("❌ Error mostrando información: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Flight getFlightById(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Flight.class, flightId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static void showCompleteFlightInfo(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            
            // 1. Obtener inventario
            String inventoryHql = "FROM FlightInventory fi WHERE fi.flight.idFlight = :flightId ORDER BY fi.seatCategory";
            List<FlightInventory> inventory = session.createQuery(inventoryHql, FlightInventory.class)
                .setParameter("flightId", flightId)
                .getResultList();
            
            // 2. Obtener precios
            String faresHql = "FROM FlightFare ff WHERE ff.flight.idFlight = :flightId ORDER BY ff.seatCategory";
            List<FlightFare> fares = session.createQuery(faresHql, FlightFare.class)
                .setParameter("flightId", flightId)
                .getResultList();
            
            // 3. Crear mapas para facilitar la búsqueda
            Map<String, FlightInventory> inventoryMap = new HashMap<>();
            Map<String, FlightFare> faresMap = new HashMap<>();
            
            for (FlightInventory item : inventory) {
                inventoryMap.put(item.getSeatCategory(), item);
            }
            
            for (FlightFare fare : fares) {
                faresMap.put(fare.getSeatCategory(), fare);
            }
            
            // 4. Mostrar información combinada
            System.out.println("📊 INFORMACIÓN COMPLETA PARA COMPRA:");
            System.out.println("=".repeat(80));
            System.out.printf("%-15s %-10s %-10s %-10s %-10s %-10s%n", 
                            "CATEGORÍA", "DISPONIBLE", "PRECIO_BASE", "IMPUESTOS", "CARGOS", "PRECIO_FINAL");
            System.out.println("=".repeat(80));
            
            for (FlightInventory item : inventory) {
                String category = item.getSeatCategory();
                FlightFare fare = faresMap.get(category);
                
                if (fare != null) {
                    System.out.printf("%-15s %-10d $%-9.2f $%-9.2f $%-9.2f $%-9.2f%n",
                        category,
                        item.getAvailableSeats(),
                        fare.getBasePrice(),
                        fare.getTaxes(),
                        fare.getFees(),
                        fare.getTotalPrice()
                    );
                } else {
                    System.out.printf("%-15s %-10d %-10s %-10s %-10s %-10s%n",
                        category,
                        item.getAvailableSeats(),
                        "SIN PRECIO",
                        "SIN PRECIO",
                        "SIN PRECIO",
                        "SIN PRECIO"
                    );
                }
            }
            
            System.out.println();
            
            // 5. Mostrar cómo se usaría en el proceso de compra
            System.out.println("🎫 PROCESO DE COMPRA:");
            System.out.println("-".repeat(50));
            
            for (FlightInventory item : inventory) {
                String category = item.getSeatCategory();
                FlightFare fare = faresMap.get(category);
                
                if (fare != null && item.getAvailableSeats() > 0) {
                    System.out.printf("✅ %s: $%.2f (Disponibles: %d)%n",
                        category,
                        fare.getTotalPrice(),
                        item.getAvailableSeats()
                    );
                } else if (item.getAvailableSeats() > 0) {
                    System.out.printf("⚠️  %s: Sin precio configurado (Disponibles: %d)%n",
                        category,
                        item.getAvailableSeats()
                    );
                } else {
                    System.out.printf("❌ %s: No disponible%n", category);
                }
            }
            
            System.out.println();
            
            // 6. Verificar integridad
            System.out.println("🔍 VERIFICACIÓN DE INTEGRIDAD:");
            System.out.println("-".repeat(50));
            System.out.println("📋 Categorías con inventario: " + inventory.size());
            System.out.println("💰 Categorías con precios: " + fares.size());
            
            if (inventory.size() == fares.size()) {
                System.out.println("✅ Todas las categorías tienen inventario y precios");
            } else {
                System.out.println("⚠️  Algunas categorías no tienen precios configurados");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error mostrando información completa: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

