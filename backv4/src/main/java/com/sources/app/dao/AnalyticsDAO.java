package com.sources.app.dao;

import com.sources.app.entities.ClickEvent;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AnalyticsDAO {

    public Long saveClick(ClickEvent event) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(event);
            tx.commit();
            return event.getIdClick();
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public java.util.List<ClickEvent> listClicks(String fromIso, String toIso, Integer userId, String pathLike, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM ClickEvent ce WHERE 1=1");
            java.util.Map<String, Object> params = new java.util.HashMap<>();

            if (fromIso != null && !fromIso.isBlank()) {
                hql.append(" AND ce.eventTime >= :fromIso");
                params.put("fromIso", fromIso);
            }
            if (toIso != null && !toIso.isBlank()) {
                hql.append(" AND ce.eventTime <= :toIso");
                params.put("toIso", toIso);
            }
            if (userId != null && userId > 0) {
                hql.append(" AND ce.userId = :uid");
                params.put("uid", userId);
            }
            if (pathLike != null && !pathLike.isBlank()) {
                hql.append(" AND ce.urlPath LIKE :path");
                params.put("path", pathLike);
            }

            hql.append(" ORDER BY ce.eventTime DESC");
            var q = session.createQuery(hql.toString(), ClickEvent.class);
            for (var e : params.entrySet()) q.setParameter(e.getKey(), e.getValue());
            q.setMaxResults(Math.max(1, Math.min(limit <= 0 ? 1000 : limit, 10000)));
            return q.list();
        }
    }
}


