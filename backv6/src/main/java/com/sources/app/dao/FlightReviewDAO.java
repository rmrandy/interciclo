package com.sources.app.dao;

import com.sources.app.entities.FlightReview;
import com.sources.app.entities.Flight;
import com.sources.app.entities.User;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class FlightReviewDAO {

    public FlightReview create(Integer flightId, Integer userId, Integer rating, String comment, Long parentReviewId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            FlightReview r = new FlightReview();
            r.setFlight(session.get(Flight.class, flightId));
            r.setUser(session.get(User.class, userId));
            r.setRating(rating);
            r.setComment(comment);
            r.setParentReviewId(parentReviewId);
            session.persist(r);
            tx.commit();
            return r;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) tx.rollback();
            throw e;
        }
    }

    public List<FlightReview> listByFlight(Integer flightId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM FlightReview fr WHERE fr.flight.idFlight = :fid ORDER BY fr.createdAt DESC";
            return session.createQuery(hql, FlightReview.class)
                    .setParameter("fid", flightId)
                    .getResultList();
        }
    }

    // Construir árbol en memoria
    public List<java.util.Map<String, Object>> listByFlightAsTree(Integer flightId) {
        List<FlightReview> flat = listByFlight(flightId);
        java.util.Map<Long, java.util.Map<String, Object>> nodeById = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> roots = new java.util.ArrayList<>();
        for (FlightReview r : flat) {
            java.util.Map<String, Object> n = new java.util.HashMap<>();
            n.put("idReview", r.getIdReview());
            n.put("userId", r.getUser() != null ? r.getUser().getIdUser() : null);
            // Incluir datos básicos del usuario para mostrar autor en el front
            if (r.getUser() != null) {
                java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                userMap.put("idUser", r.getUser().getIdUser());
                try { userMap.put("firstName", r.getUser().getFirstName()); } catch (Exception ignore) {}
                try { userMap.put("lastName", r.getUser().getLastName()); } catch (Exception ignore) {}
                try { userMap.put("name", r.getUser().getName()); } catch (Exception ignore) {}
                try { userMap.put("email", r.getUser().getEmail()); } catch (Exception ignore) {}
                n.put("user", userMap);
            }
            n.put("rating", r.getRating());
            n.put("reviewText", r.getComment());
            n.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            n.put("parentReviewId", r.getParentReviewId());
            n.put("children", new java.util.ArrayList<>());
            nodeById.put(r.getIdReview(), n);
        }
        for (FlightReview r : flat) {
            java.util.Map<String, Object> n = nodeById.get(r.getIdReview());
            if (r.getParentReviewId() == null) {
                roots.add(n);
            } else {
                java.util.Map<String, Object> p = nodeById.get(r.getParentReviewId());
                if (p != null) {
                    ((java.util.List<java.util.Map<String,Object>>) p.get("children")).add(n);
                } else {
                    roots.add(n); // fallback si padre no existe
                }
            }
        }
        return roots;
    }
}


