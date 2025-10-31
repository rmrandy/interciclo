import React, { createContext, useContext, useState, useEffect } from 'react';
import { siteConfigApi } from '../services/api';

const SiteConfigContext = createContext();

export const useSiteConfig = () => {
  const context = useContext(SiteConfigContext);
  if (!context) {
    throw new Error('useSiteConfig debe usarse dentro de SiteConfigProvider');
  }
  return context;
};

export const SiteConfigProvider = ({ children }) => {
  const [config, setConfig] = useState({
    agencyName: 'Agencia de Viajes',
    footer: {
      companyName: 'Agencia de Viajes S.A.',
      address: 'Av. Principal 123, Ciudad',
      phone: '+1 234 567 890',
      email: 'info@agencia.com',
      socialMedia: {
        facebook: '',
        twitter: '',
        instagram: ''
      }
    },
    logo: '',
    primaryColor: '#1e40af',
    secondaryColor: '#3b82f6'
  });
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchConfig = async () => {
    try {
      setLoading(true);
      const response = await siteConfigApi.get();
      if (response?.config) {
        setConfig(response.config);
      }
      setError(null);
    } catch (err) {
      console.error('Error al cargar configuración del sitio:', err);
      setError(err.message || 'Error al cargar la configuración');
    } finally {
      setLoading(false);
    }
  };

  const updateConfig = async (newConfig) => {
    try {
      const response = await siteConfigApi.update(newConfig);
      if (response?.config) {
        setConfig(response.config);
      }
      return response;
    } catch (err) {
      console.error('Error al actualizar configuración:', err);
      throw err;
    }
  };

  useEffect(() => {
    fetchConfig();
  }, []);

  return (
    <SiteConfigContext.Provider
      value={{
        config,
        loading,
        error,
        updateConfig,
        refreshConfig: fetchConfig,
      }}
    >
      {children}
    </SiteConfigContext.Provider>
  );
};

