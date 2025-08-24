package com.sources.app.handlers;

import com.google.gson.Gson;
import com.sources.app.entities.Aircraft;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class AircraftHandler {
    private final Gson gson = new Gson();

    public String listAircrafts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Aircraft> list = session.createQuery("FROM Aircraft", Aircraft.class).getResultList();
            return gson.toJson(list);
        }
    }
}


