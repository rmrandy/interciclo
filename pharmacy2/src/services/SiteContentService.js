import ApiService, { getPharmacyApiUrl } from './ApiService';

function getUserIdHeader() {
  let userId = null;
  try {
    // Intentar obtener del store
    const userStore = require('@/stores/userStore').useUserStore();
    const user = userStore.getUser();
    if (user && user.id) userId = user.id;
    // Si no, intentar de localStorage
    if (!userId && typeof window !== 'undefined') {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const userObj = JSON.parse(userStr);
        if (userObj && userObj.id) userId = userObj.id;
      }
    }
  } catch (e) {
    // Ignorar error al obtener el usuario
  }
  return userId ? { 'User-ID': userId } : {};
}

class SiteContentService {
  constructor() {
    this.baseURL = 'site-content';
    this.baseURLv2 = 'site-content-v2';
    this.contentCache = new Map();
  }

  /**
   * Obtiene todo el contenido del sitio
   */
  async getAllContent() {
    try {
      const response = await ApiService.get(this.baseURL);
      return response.data;
    } catch (error) {
      console.error('Error al obtener contenido del sitio:', error);
      return [];
    }
  }

  /**
   * Obtiene todo el contenido del sitio usando la nueva API v2
   */
  async getAllContentV2() {
    try {
      const response = await ApiService.get(getPharmacyApiUrl(this.baseURLv2));
      return response.data;
    } catch (error) {
      console.error('Error al obtener contenido del sitio v2:', error);
      return [];
    }
  }

  /**
   * Obtiene contenido específico por clave
   */
  async getContentByKey(key) {
    // Verificar cache primero
    if (this.contentCache.has(key)) {
      return this.contentCache.get(key);
    }

    try {
      const response = await ApiService.get(`${this.baseURL}/${key}`);
      const value = response.data.value || '';
      
      // Guardar en cache
      this.contentCache.set(key, value);
      return value;
    } catch (error) {
      console.error(`Error al obtener contenido para clave ${key}:`, error);
      return '';
    }
  }

  /**
   * Obtiene contenido específico por clave usando la nueva API v2
   */
  async getContentByKeyV2(key) {
    if (this.contentCache.has(key)) {
      return this.contentCache.get(key);
    }
    try {
      const response = await ApiService.get(getPharmacyApiUrl(`${this.baseURLv2}/${key}`));
      const value = response.data.value || '';
      this.contentCache.set(key, value);
      return value;
    } catch (error) {
      console.error(`Error al obtener contenido para clave ${key} v2:`, error);
      return '';
    }
  }

  /**
   * Actualiza contenido del sitio (solo administradores)
   */
  async updateContent(key, value) {
    try {
      const headers = getUserIdHeader();
      const response = await ApiService.put(`${this.baseURLv2}/${key}`, { value }, { headers });
      this.contentCache.set(key, value);
      return response.data;
    } catch (error) {
      console.error(`Error al actualizar contenido para clave ${key}:`, error);
      throw error;
    }
  }

  /**
   * Actualiza contenido del sitio usando la nueva API v2 (solo administradores)
   */
  async updateContentV2(key, value) {
    try {
      const headers = getUserIdHeader();
      const response = await ApiService.put(getPharmacyApiUrl(`${this.baseURLv2}/${key}`), { value }, { headers });
      this.contentCache.set(key, value);
      return response.data;
    } catch (error) {
      console.error(`Error al actualizar contenido para clave ${key} v2:`, error);
      throw error;
    }
  }

  /**
   * Crea nuevo contenido del sitio (solo administradores)
   */
  async createContent(key, value, description = '') {
    try {
      const headers = getUserIdHeader();
      const response = await ApiService.post(getPharmacyApiUrl(this.baseURLv2), { key, value, description }, { headers });
      this.contentCache.set(key, value);
      return response.data;
    } catch (error) {
      console.error(`Error al crear contenido para clave ${key}:`, error);
      throw error;
    }
  }

  /**
   * Elimina contenido del sitio (solo administradores)
   */
  async deleteContent(key) {
    try {
      const headers = getUserIdHeader();
      const response = await ApiService.delete(getPharmacyApiUrl(`${this.baseURLv2}/${key}`), { headers });
      this.contentCache.delete(key);
      return response.data;
    } catch (error) {
      console.error(`Error al eliminar contenido para clave ${key}:`, error);
      throw error;
    }
  }

  /**
   * Inicializa contenido por defecto (solo administradores)
   */
  async initializeContent() {
    try {
      const headers = getUserIdHeader();
      const response = await ApiService.post(`${this.baseURLv2}/initialize`, {}, { headers });
      return response.data;
    } catch (error) {
      console.error('Error al inicializar contenido:', error);
      throw error;
    }
  }

  /**
   * Inicializa contenido por defecto usando la nueva API v2 (solo administradores)
   */
  async initializeContentV2() {
    try {
      const headers = getUserIdHeader();
      const response = await ApiService.post(getPharmacyApiUrl(`${this.baseURLv2}/initialize`), {}, { headers });
      return response.data;
    } catch (error) {
      console.error('Error al inicializar contenido v2:', error);
      throw error;
    }
  }

  /**
   * Obtiene el título del header
   */
  async getHeaderTitle() {
    try {
      const all = await this.getAllContentV2();
      const found = all.find(item => item.key === 'header_title');
      return found && found.value ? found.value : 'Farmacia Seguros';
    } catch (e) {
      return 'Farmacia Seguros';
    }
  }

  /**
   * Obtiene el subtítulo del header
   */
  async getHeaderSubtitle() {
    try {
      const all = await this.getAllContentV2();
      const found = all.find(item => item.key === 'header_subtitle');
      return found && found.value ? found.value : '';
    } catch (e) {
      return '';
    }
  }

  /**
   * Obtiene el texto del footer
   */
  async getFooterText() {
    try {
      const all = await this.getAllContentV2();
      const found = all.find(item => item.key === 'footer_text');
      return found && found.value ? found.value : '';
    } catch (e) {
      return '';
    }
  }

  /**
   * Obtiene la información de contacto del footer
   */
  async getFooterContact() {
    try {
      const all = await this.getAllContentV2();
      const found = all.find(item => item.key === 'footer_contact');
      return found && found.value ? found.value : '';
    } catch (e) {
      return '';
    }
  }

  /**
   * Actualiza el título del header
   */
  async updateHeaderTitle(title) {
    return this.updateContentV2('header_title', title);
  }

  /**
   * Actualiza el subtítulo del header
   */
  async updateHeaderSubtitle(subtitle) {
    return this.updateContentV2('header_subtitle', subtitle);
  }

  /**
   * Actualiza el texto del footer
   */
  async updateFooterText(text) {
    return this.updateContentV2('footer_text', text);
  }

  /**
   * Actualiza la información de contacto del footer
   */
  async updateFooterContact(contact) {
    return this.updateContentV2('footer_contact', contact);
  }

  /**
   * Obtiene contenido específico del header
   */
  async getHeaderContent() {
    try {
      const [title, subtitle] = await Promise.all([
        this.getHeaderTitle(),
        this.getHeaderSubtitle()
      ]);
      
      return {
        title: title || 'Farmacia f',
        subtitle: subtitle || 'Tu salud, nuestra prioridad'
      };
    } catch (error) {
      console.error('Error al obtener contenido del header:', error);
      return {
        title: 'Farmacia fds',
        subtitle: 'Tu salud, nuestra prioridad'
      };
    }
  }

  /**
   * Obtiene contenido específico del footer
   */
  async getFooterContent() {
    try {
      const [text, contact] = await Promise.all([
        this.getFooterText(),
        this.getFooterContact()
      ]);
      
      return {
        text: text || '© 2024 Farmacia Seguros. Todos los derechos reservados.',
        contact: contact || 'Contacto: info@farmaciaseguros.com | Tel: (502) 1234-5678'
      };
    } catch (error) {
      console.error('Error al obtener contenido del footer:', error);
      return {
        text: '© 2024 Farmacia Seguros. Todos los derechos reservados.',
        contact: 'Contacto: info@farmaciaseguros.com | Tel: (502) 1234-5678'
      };
    }
  }

  /**
   * Limpia el cache
   */
  clearCache() {
    this.contentCache.clear();
  }

  /**
   * Carga todo el contenido y lo guarda en cache
   */
  async loadAllContent() {
    try {
      const content = await this.getAllContent();
      content.forEach(item => {
        this.contentCache.set(item.contentKey, item.contentValue);
      });
      return content;
    } catch (error) {
      console.error('Error al cargar todo el contenido:', error);
      return [];
    }
  }

  /**
   * Carga todo el contenido usando la nueva API v2 y lo guarda en cache
   */
  async loadAllContentV2() {
    try {
      const content = await this.getAllContentV2();
      if (!Array.isArray(content)) return [];
      content.forEach(item => {
        this.contentCache.set(item.contentKey, item.contentValue);
      });
      return content;
    } catch (error) {
      console.error('Error al cargar todo el contenido v2:', error);
      return [];
    }
  }

  /**
   * Verifica si el contenido existe en cache
   */
  hasCachedContent(key) {
    return this.contentCache.has(key);
  }

  /**
   * Obtiene contenido del cache
   */
  getCachedContent(key) {
    return this.contentCache.get(key);
  }

  /**
   * Actualiza contenido en cache
   */
  setCachedContent(key, value) {
    this.contentCache.set(key, value);
  }
}

export default new SiteContentService(); 