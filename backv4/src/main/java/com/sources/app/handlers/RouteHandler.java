package com.sources.app.handlers;

import com.google.gson.Gson;
import com.sources.app.entities.Route;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class RouteHandler {
    private final Gson gson = new Gson();

    public String listRoutes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Route> routes = session.createQuery("FROM Route", Route.class).getResultList();
            return gson.toJson(routes);
        }
    }
}


