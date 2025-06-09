package com.sources.app.controllers;

import com.sources.app.dao.MedicineDAO;
import com.sources.app.entities.Medicine;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/medicines")
public class MedicineController {
    private MedicineDAO medicineDAO = new MedicineDAO();

    @GET
    @Path("/actives")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActivePrinciples() {
        List<String> actives = medicineDAO.getUniqueActivePrinciples();
        if (actives == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error("No se pudieron obtener los principios activos"))
                    .build();
        }
        return Response.ok(actives).build();
    }

    @GET
    @Path("/recommended")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecommended() {
        List<Medicine> meds = medicineDAO.getRandomMedicines(5);
        if (meds == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error("No se pudieron obtener los recomendados"))
                    .build();
        }
        return Response.ok(meds).build();
    }

    @GET
    @Path("/bestsellers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBestSellers(@QueryParam("count") @DefaultValue("10") int count) {
        List<Medicine> meds = medicineDAO.getBestSellers(count);
        if (meds == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error("No se pudieron obtener los más vendidos"))
                    .build();
        }
        return Response.ok(meds).build();
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", msg);
        return map;
    }
} 