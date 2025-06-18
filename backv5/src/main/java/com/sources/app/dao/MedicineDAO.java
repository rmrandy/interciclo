package com.sources.app.dao;

import com.sources.app.entities.Medicine;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Medicine}.
 * Esta clase proporciona métodos para realizar operaciones CRUD en registros de Medicamentos,
 * que representan los productos farmacéuticos disponibles. Utiliza Hibernate para interacciones con la base de datos.
 */
public class MedicineDAO {
    private final EntityManagerFactory emf;
    private final EntityManager em;

    public MedicineDAO() {
        this.emf = Persistence.createEntityManagerFactory("default");
        this.em = emf.createEntityManager();
    }

    /**
     * Crea un nuevo registro de Medicamento en la base de datos.
     *
     * @param name             El nombre comercial del medicamento.
     * @param activeMedicament El(los) principio(s) activo(s) del medicamento.
     * @param description      Una descripción del medicamento.
     * @param image            URL o ruta a una imagen del medicamento.
     * @param concentration    La concentración del principio activo.
     * @param presentacion     Los detalles de la presentación (p. ej., tamaño del envase, volumen).
     * @param stock            La cantidad actual en stock disponible.
     * @param brand            La marca o fabricante del medicamento.
     * @param prescription     Indica si el medicamento requiere receta (true/false).
     * @param price            El precio unitario del medicamento.
     * @param soldUnits        El número inicial de unidades vendidas (típicamente 0).
     * @return La entidad {@link Medicine} recién creada, o null si ocurrió un error.
     */
    public Medicine create(String name, String activeMedicament, String description, String image,
                           String concentration, Double presentacion, Integer stock, String brand,
                           Boolean prescription, Double price, Integer soldUnits) {
        Transaction tx = null;
        Medicine med = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            med = new Medicine();
            med.setName(name);
            med.setActiveMedicament(activeMedicament);
            med.setDescription(description);
            med.setImage(image);
            med.setConcentration(concentration);
            med.setPresentacion(presentacion);
            med.setStock(stock);
            med.setBrand(brand);
            med.setPrescription(prescription);
            med.setPrice(price);
            med.setSoldUnits(soldUnits);

            session.save(med);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return med;
    }

    /**
     * Recupera todos los registros de Medicamento de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Medicine}, o null si ocurrió un error.
     */
    public List<Medicine> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Medicine> query = session.createQuery("FROM Medicine", Medicine.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Recupera un registro de Medicamento específico por su identificador único.
     *
     * @param id El ID del Medicamento a recuperar.
     * @return La entidad {@link Medicine} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Medicine getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Medicine.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza un registro de Medicamento existente en la base de datos.
     *
     * @param medicine La entidad {@link Medicine} con información actualizada. El ID debe coincidir con un registro existente.
     * @return La entidad {@link Medicine} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public Medicine update(Medicine medicine) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(medicine);
            tx.commit();
            return medicine;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene la lista de principios activos únicos.
     */
    public List<String> getUniqueActivePrinciples() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery("SELECT DISTINCT m.activeMedicament FROM Medicine m", String.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene 5 productos aleatorios.
     */
    public List<Medicine> getRandomMedicines(int count) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Medicine> query = session.createQuery("FROM Medicine ORDER BY RAND()", Medicine.class);
            query.setMaxResults(count);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene los productos más vendidos (ordenados por SOLD_UNITS descendente).
     */
    public List<Medicine> getBestSellers(int count) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Medicine> query = session.createQuery("FROM Medicine m ORDER BY m.soldUnits DESC", Medicine.class);
            query.setMaxResults(count);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Medicine updateStock(Long id, Integer quantity) {
        try {
            em.getTransaction().begin();
            Medicine medicine = em.find(Medicine.class, id);
            if (medicine != null) {
                medicine.setStock(medicine.getStock() - quantity);
                em.merge(medicine);
                em.getTransaction().commit();
                return medicine;
            }
            em.getTransaction().rollback();
            return null;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Elimina un medicamento por su ID.
     * @param id El ID del medicamento a eliminar.
     * @return true si se eliminó correctamente, false si no existe o hubo error.
     */
    public boolean delete(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Medicine medicine = session.get(Medicine.class, id);
            if (medicine != null) {
                session.delete(medicine);
                tx.commit();
                return true;
            } else {
                if (tx != null) tx.rollback();
                return false;
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        em.close();
        emf.close();
    }
}
