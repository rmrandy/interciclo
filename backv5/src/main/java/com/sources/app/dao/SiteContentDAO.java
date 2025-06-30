package com.sources.app.dao;

import com.sources.app.entities.SiteContent;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class SiteContentDAO {
    
    /**
     * Obtiene todo el contenido del sitio
     */
    public List<SiteContent> getAllContent() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SiteContent> query = session.createQuery("FROM SiteContent", SiteContent.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene contenido por clave específica
     */
    public Optional<SiteContent> getContentByKey(String contentKey) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SiteContent> query = session.createQuery(
                "FROM SiteContent WHERE contentKey = :key", SiteContent.class);
            query.setParameter("key", contentKey);
            SiteContent result = query.uniqueResult();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Guarda o actualiza contenido del sitio
     */
    public boolean saveOrUpdateContent(SiteContent content) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(content);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Actualiza el valor de contenido por clave. SOLO si existe.
     */
    public boolean updateContentValue(String contentKey, String newValue) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Query query = session.createQuery(
                "UPDATE SiteContent SET contentValue = :value WHERE contentKey = :key");
            query.setParameter("value", newValue);
            query.setParameter("key", contentKey);
            
            int result = query.executeUpdate();
            if (result == 0) {
                // No existía, no se actualiza nada
                System.out.println("[SiteContentDAO] No existe el registro para clave: " + contentKey);
                transaction.rollback();
                return false;
            } else {
                System.out.println("[SiteContentDAO] Actualizado registro para clave: " + contentKey);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Inicializa contenido por defecto si no existe
     */
    public void initializeDefaultContent() {
        // Header content
        if (!getContentByKey("header_title").isPresent()) {
            SiteContent headerTitle = new SiteContent("header_title", "Farmacia Seguros", "Título principal del header");
            saveOrUpdateContent(headerTitle);
        }
        
        if (!getContentByKey("header_subtitle").isPresent()) {
            SiteContent headerSubtitle = new SiteContent("header_subtitle", "Tu salud, nuestra prioridad", "Subtítulo del header");
            saveOrUpdateContent(headerSubtitle);
        }
        
        // Footer content
        if (!getContentByKey("footer_text").isPresent()) {
            SiteContent footerText = new SiteContent("footer_text", "© 2024 Farmacia Seguros. Todos los derechos reservados.", "Texto del footer");
            saveOrUpdateContent(footerText);
        }
        
        if (!getContentByKey("footer_contact").isPresent()) {
            SiteContent footerContact = new SiteContent("footer_contact", "Contacto: info@farmaciaseguros.com | Tel: (502) 1234-5678", "Información de contacto del footer");
            saveOrUpdateContent(footerContact);
        }
    }
} 