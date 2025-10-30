package com.sources.app.util;

import com.sources.app.entities.Flight;
import com.sources.app.entities.FlightInventory;
import com.sources.app.entities.FlightFare;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.math.BigDecimal;
import java.util.List;

/**
 * Clase para verificar y corregir los precios del vuelo 29
 * Muestra los precios actuales y permite corregirlos
 */
public class CheckFlight29Prices {
    
    public static void main(String[] args) {
        System.out.println("🔍 Verificando precios del vuelo 29...");
        System.out.println("=====================================");
        
        try {
            // Verificar que el vuelo 29 existe
            Flight flight = getFlightById(29);
            if (flight == null) {
                System.out.println("❌ Error: El vuelo 29 no existe");
                return;
            }
            
            System.out.println("✅ Vuelo 29 encontrado: " + flight.getFlightNumber());
            System.out.println();
            
            // Verificar inventario
            checkInventory(29);
            
            // Verificar precios
            checkPrices(29);
            
            System.out.println();
            System.out.println("🎯 Para corregir los precios, ejecuta:");
            System.out.println("   mvn exec:java -Dexec.mainClass=\"com.sources.app.util.CorrectFlight29Prices\"");
            
        } catch (Exception e) {
            System.err.println("❌ Error verificando precios: " + e.getMessage());
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
    
    private static void checkInventory(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("📋 INVENTARIO DE ASIENTOS:");
            System.out.println("-".repeat(50));
            
            String hql = "FROM FlightInventory fi WHERE fi.flight.idFlight = :flightId ORDER BY fi.seatCategory";
            List<FlightInventory> inventory = session.createQuery(hql, FlightInventory.class)
                .setParameter("flightId", flightId)
                .getResultList();
            
            if (inventory.isEmpty()) {
                System.out.println("❌ No hay inventario configurado");
                return;
            }
            
            for (FlightInventory item : inventory) {
                System.out.printf("🪑 %-15s: %d asientos disponibles (Total: %d, Reservados: %d, Vendidos: %d)%n",
                    item.getSeatCategory(),
                    item.getAvailableSeats(),
                    item.getTotalSeats(),
                    item.getReservedSeats(),
                    item.getSoldSeats()
                );
            }
        } catch (Exception e) {
            System.err.println("❌ Error verificando inventario: " + e.getMessage());
        }
    }
    
    private static void checkPrices(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println();
            System.out.println("💰 PRECIOS CONFIGURADOS:");
            System.out.println("-".repeat(50));
            
            String hql = "FROM FlightFare ff WHERE ff.flight.idFlight = :flightId ORDER BY ff.seatCategory";
            List<FlightFare> fares = session.createQuery(hql, FlightFare.class)
                .setParameter("flightId", flightId)
                .getResultList();
            
            if (fares.isEmpty()) {
                System.out.println("❌ No hay precios configurados");
                return;
            }
            
            for (FlightFare fare : fares) {
                System.out.printf("💵 %-15s: Base: $%.2f, Impuestos: $%.2f, Cargos: $%.2f, Total: $%.2f%n",
                    fare.getSeatCategory(),
                    fare.getBasePrice(),
                    fare.getTaxes(),
                    fare.getFees(),
                    fare.getTotalPrice()
                );
            }
        } catch (Exception e) {
            System.err.println("❌ Error verificando precios: " + e.getMessage());
        }
    }
}

