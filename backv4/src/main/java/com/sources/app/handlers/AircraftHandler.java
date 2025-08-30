package com.sources.app.handlers;

import com.google.gson.Gson;
import com.sources.app.entities.Aircraft;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AircraftHandler {
    private final Gson gson = new Gson();

    public String listAircrafts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Aircraft> list = session.createQuery("FROM Aircraft", Aircraft.class).getResultList();
            return gson.toJson(list);
        }
    }

    public String createAircraft(String body) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();

            Aircraft aircraft = new Aircraft();
            aircraft.setRegistration(json.get("registration").getAsString());
            aircraft.setModel(json.get("model").getAsString());
            aircraft.setManufacturer(json.get("manufacturer").getAsString());
            aircraft.setSeatCapacity(json.get("seatCapacity").getAsInt());

            session.persist(aircraft);
            tx.commit();
            return gson.toJson(java.util.Map.of("success", true, "aircraft", aircraft));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(java.util.Map.of("success", false, "error", e.getMessage()));
        }
    }

    public String updateAircraft(String id, String body) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Long aircraftId = Long.parseLong(id);
            Aircraft aircraft = session.get(Aircraft.class, aircraftId);
            if (aircraft == null) {
                return gson.toJson(java.util.Map.of("success", false, "error", "Aeronave no encontrada"));
            }

            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
            if (json.has("registration")) aircraft.setRegistration(json.get("registration").getAsString());
            if (json.has("model")) aircraft.setModel(json.get("model").getAsString());
            if (json.has("manufacturer")) aircraft.setManufacturer(json.get("manufacturer").getAsString());
            if (json.has("seatCapacity")) aircraft.setSeatCapacity(json.get("seatCapacity").getAsInt());

            session.merge(aircraft);
            tx.commit();
            return gson.toJson(java.util.Map.of("success", true, "aircraft", aircraft));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(java.util.Map.of("success", false, "error", e.getMessage()));
        }
    }

    public String deleteAircraft(String id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Long aircraftId = Long.parseLong(id);
            Aircraft aircraft = session.get(Aircraft.class, aircraftId);
            if (aircraft == null) {
                return gson.toJson(java.util.Map.of("success", false, "error", "Aeronave no encontrada"));
            }
            session.remove(aircraft);
            tx.commit();
            return gson.toJson(java.util.Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(java.util.Map.of("success", false, "error", e.getMessage()));
        }
    }

    public String getSeatConfig(String aircraftId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<?> rows = session.createNativeQuery(
                "SELECT SEAT_CATEGORY, SEATS, PRICE_MULTIPLIER FROM AIRCRAFT_SEAT_CONFIG WHERE AIRCRAFT_ID = :id ORDER BY SEAT_CATEGORY"
            ).setParameter("id", Long.parseLong(aircraftId)).getResultList();

            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            for (Object row : rows) {
                Object[] r = (Object[]) row;
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("seats", ((Number) r[1]).intValue());
                item.put("priceMultiplier", ((Number) r[2]).doubleValue());
                resp.put((String) r[0], item);
            }
            return gson.toJson(java.util.Map.of("success", true, "config", resp));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(java.util.Map.of("success", false, "error", e.getMessage()));
        }
    }

    public String updateSeatConfig(String aircraftId, String body) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Long id = Long.parseLong(aircraftId);

            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
            com.google.gson.JsonObject cfg = json.has("config") && json.get("config").isJsonObject()
                ? json.getAsJsonObject("config")
                : json; // permitir enviar directo

            String[] cats = new String[] {"ECONOMY","BUSINESS","FIRST_CLASS"};
            for (String cat : cats) {
                if (cfg.has(cat) && cfg.get(cat).isJsonObject()) {
                    com.google.gson.JsonObject o = cfg.getAsJsonObject(cat);
                    int seats = o.has("seats") ? o.get("seats").getAsInt() : 0;
                    double mult = o.has("priceMultiplier") ? o.get("priceMultiplier").getAsDouble() : 1.0;

                    session.createNativeQuery(
                        "MERGE INTO AIRCRAFT_SEAT_CONFIG t USING (SELECT :id AIRCRAFT_ID, :cat SEAT_CATEGORY FROM dual) s " +
                        "ON (t.AIRCRAFT_ID = s.AIRCRAFT_ID AND t.SEAT_CATEGORY = s.SEAT_CATEGORY) " +
                        "WHEN MATCHED THEN UPDATE SET t.SEATS = :seats, t.PRICE_MULTIPLIER = :mult " +
                        "WHEN NOT MATCHED THEN INSERT (AIRCRAFT_ID, SEAT_CATEGORY, SEATS, PRICE_MULTIPLIER) VALUES (:id, :cat, :seats, :mult)"
                    ).setParameter("id", id)
                     .setParameter("cat", cat)
                     .setParameter("seats", seats)
                     .setParameter("mult", mult)
                     .executeUpdate();
                }
            }

            tx.commit();
            return gson.toJson(java.util.Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return gson.toJson(java.util.Map.of("success", false, "error", e.getMessage()));
        }
    }
}


