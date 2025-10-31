import { useState, useEffect } from 'react';
import { getBackendBaseUrl } from '../services/api.js';

export default function CorporateUsers() {
  const [users, setUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [saving, setSaving] = useState(false);

  const [formData, setFormData] = useState({
    companyName: '',
    name: '',
    email: '',
    phone: '',
    cui: '',
    birthDate: '',
    address: '',
    password: '',
    enabled: true,
    apiKey: ''
  });

  // Cargar usuarios
  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError('');
      const baseUrl = await getBackendBaseUrl();
      const response = await fetch(`${baseUrl}/corporate-users`);
      if (!response.ok) {
        throw new Error('Error al cargar usuarios empresariales');
      }
      const data = await response.json();
      setUsers(data);
      setFilteredUsers(data);
    } catch (err) {
      setError(err.message || 'Error al cargar usuarios empresariales');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Filtrar usuarios
  useEffect(() => {
    if (!searchQuery) {
      setFilteredUsers(users);
    } else {
      const query = searchQuery.toLowerCase();
      setFilteredUsers(users.filter(user =>
        user.companyName?.toLowerCase().includes(query) ||
        user.name?.toLowerCase().includes(query) ||
        user.email?.toLowerCase().includes(query) ||
        user.apiKey?.toLowerCase().includes(query)
      ));
    }
  }, [searchQuery, users]);

  const openCreateModal = () => {
    setEditingUser(null);
    setFormData({
      companyName: '',
      name: '',
      email: '',
      phone: '',
      cui: '',
      birthDate: '',
      address: '',
      password: '',
      enabled: true,
      apiKey: ''
    });
    setShowModal(true);
    setError('');
    setSuccess('');
  };

  const openEditModal = (user) => {
    setEditingUser(user);
    setFormData({
      companyName: user.companyName || '',
      name: user.name || '',
      email: user.email || '',
      phone: user.phone || '',
      cui: user.cui || '',
      birthDate: user.birthDate || '',
      address: user.address || '',
      password: '',
      enabled: user.enabled === 1,
      apiKey: user.apiKey || ''
    });
    setShowModal(true);
    setError('');
    setSuccess('');
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingUser(null);
    setError('');
    setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      
      const baseUrl = await getBackendBaseUrl();

      const userData = {
        companyName: formData.companyName,
        name: formData.name,
        email: formData.email,
        phone: formData.phone,
        cui: formData.cui,
        birthDate: formData.birthDate || null,
        address: formData.address,
        enabled: formData.enabled ? 1 : 0,
      };

      if (editingUser) {
        // Actualizar
        userData.idUser = editingUser.idUser;
        if (formData.password) {
          userData.password = formData.password;
        }

        const response = await fetch(`${baseUrl}/corporate-users/${editingUser.idUser}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(userData)
        });
        
        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          throw new Error(errorData.error || errorData.message || 'Error al actualizar usuario');
        }
        
        setSuccess('✓ Usuario actualizado correctamente');
      } else {
        // Crear
        userData.password = formData.password;

        const response = await fetch(`${baseUrl}/corporate-users`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(userData)
        });
        
        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          throw new Error(errorData.error || errorData.message || 'Error al crear usuario');
        }
        
        const data = await response.json();
        
        if (data.apiKey) {
          await navigator.clipboard.writeText(data.apiKey);
          setSuccess(`✓ Usuario creado. API Key: ${data.apiKey} (copiado al portapapeles)`);
        } else {
          setSuccess('✓ Usuario creado correctamente');
        }
      }

      await fetchUsers();
      closeModal();

      setTimeout(() => setSuccess(''), 5000);
    } catch (err) {
      setError(err.message || 'Error al guardar usuario');
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const regenerateApiKey = async (user) => {
    if (!confirm(`¿Regenerar API Key para ${user.companyName}?\n\nEsto invalidará el API Key actual.`)) {
      return;
    }

    try {
      const baseUrl = await getBackendBaseUrl();
      const response = await fetch(`${baseUrl}/corporate-users/${user.idUser}/regenerate-key`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' }
      });
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || 'Error al regenerar API Key');
      }
      
      const data = await response.json();
      const newApiKey = data.apiKey;

      await navigator.clipboard.writeText(newApiKey);
      setSuccess(`✓ API Key regenerado: ${newApiKey} (copiado al portapapeles)`);

      await fetchUsers();
      setTimeout(() => setSuccess(''), 8000);
    } catch (err) {
      setError(err.message || 'Error al regenerar API Key');
    }
  };

  const toggleStatus = async (user) => {
    const action = user.enabled ? 'desactivar' : 'activar';
    if (!confirm(`¿Estás seguro de ${action} a ${user.companyName}?`)) {
      return;
    }

    try {
      const baseUrl = await getBackendBaseUrl();
      const response = await fetch(`${baseUrl}/corporate-users/${user.idUser}/toggle-status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' }
      });
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || 'Error al cambiar estado');
      }
      
      setSuccess(`✓ Usuario ${user.enabled ? 'desactivado' : 'activado'} correctamente`);
      await fetchUsers();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message || 'Error al cambiar estado');
    }
  };

  const copyApiKey = async (apiKey) => {
    try {
      await navigator.clipboard.writeText(apiKey);
      setSuccess('✓ API Key copiado al portapapeles');
      setTimeout(() => setSuccess(''), 2000);
    } catch (err) {
      setError('Error al copiar API Key');
    }
  };

  const maskApiKey = (apiKey) => {
    if (!apiKey || apiKey.length < 8) return apiKey;
    return apiKey.substring(0, 8) + '•'.repeat(apiKey.length - 12) + apiKey.substring(apiKey.length - 4);
  };

  const getInitials = (name) => {
    if (!name) return '?';
    const words = name.split(' ');
    if (words.length >= 2) {
      return (words[0][0] + words[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  const formatDate = (date) => {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('es-ES', { year: 'numeric', month: 'short', day: 'numeric' });
  };

  const activeCount = users.filter(u => u.enabled === 1).length;

  return (
    <div style={{ minHeight: '100vh', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', padding: '40px 24px' }}>
      <div style={{ maxWidth: '1400px', margin: '0 auto' }}>
        {/* Header */}
        <div style={{ marginBottom: '32px', animation: 'fadeIn 0.5s ease-in' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '8px' }}>
            <div style={{ 
              width: '64px', 
              height: '64px', 
              background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
              borderRadius: '16px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '32px',
              boxShadow: '0 10px 25px rgba(0,0,0,0.2)'
            }}>
              👔
            </div>
            <div>
              <h1 style={{ fontSize: '36px', fontWeight: '800', color: 'white', margin: 0, textShadow: '0 2px 10px rgba(0,0,0,0.2)' }}>
                Usuarios Empresariales
              </h1>
              <p style={{ color: 'rgba(255,255,255,0.9)', margin: '4px 0 0', fontSize: '16px' }}>
                Gestiona agencias de viaje y usuarios corporativos con acceso API
              </p>
            </div>
          </div>
        </div>

        {/* Stats Cards */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '24px' }}>
          <div style={{
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            borderRadius: '16px',
            padding: '24px',
            boxShadow: '0 10px 30px rgba(0,0,0,0.2)',
            border: '1px solid rgba(255,255,255,0.2)',
            backdropFilter: 'blur(10px)'
          }}>
            <div style={{ fontSize: '14px', color: 'rgba(255,255,255,0.8)', marginBottom: '8px', fontWeight: '600' }}>
              TOTAL USUARIOS
            </div>
            <div style={{ fontSize: '40px', fontWeight: '800', color: 'white' }}>{users.length}</div>
          </div>
          <div style={{
            background: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
            borderRadius: '16px',
            padding: '24px',
            boxShadow: '0 10px 30px rgba(0,0,0,0.2)',
            border: '1px solid rgba(255,255,255,0.2)',
            backdropFilter: 'blur(10px)'
          }}>
            <div style={{ fontSize: '14px', color: 'rgba(255,255,255,0.8)', marginBottom: '8px', fontWeight: '600' }}>
              USUARIOS ACTIVOS
            </div>
            <div style={{ fontSize: '40px', fontWeight: '800', color: 'white' }}>{activeCount}</div>
          </div>
          <div style={{
            background: 'linear-gradient(135deg, #fc4a1a 0%, #f7b733 100%)',
            borderRadius: '16px',
            padding: '24px',
            boxShadow: '0 10px 30px rgba(0,0,0,0.2)',
            border: '1px solid rgba(255,255,255,0.2)',
            backdropFilter: 'blur(10px)'
          }}>
            <div style={{ fontSize: '14px', color: 'rgba(255,255,255,0.8)', marginBottom: '8px', fontWeight: '600' }}>
              USUARIOS INACTIVOS
            </div>
            <div style={{ fontSize: '40px', fontWeight: '800', color: 'white' }}>{users.length - activeCount}</div>
          </div>
        </div>

        {/* Actions Bar */}
        <div style={{ 
          background: 'white', 
          borderRadius: '20px', 
          padding: '20px', 
          marginBottom: '24px', 
          boxShadow: '0 10px 40px rgba(0,0,0,0.15)',
          display: 'flex',
          flexWrap: 'wrap',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '16px'
        }}>
          {/* Search */}
          <div style={{ flex: '1', minWidth: '280px', position: 'relative' }}>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="🔍 Buscar por empresa, email o API Key..."
              style={{
                width: '100%',
                padding: '14px 20px 14px 48px',
                border: '2px solid #e5e7eb',
                borderRadius: '12px',
                fontSize: '15px',
                outline: 'none',
                transition: 'all 0.3s',
                background: '#f9fafb'
              }}
              onFocus={(e) => {
                e.target.style.borderColor = '#667eea';
                e.target.style.background = 'white';
              }}
              onBlur={(e) => {
                e.target.style.borderColor = '#e5e7eb';
                e.target.style.background = '#f9fafb';
              }}
            />
            <svg style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', width: '20px', height: '20px', color: '#9ca3af' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
          </div>

          {/* Create Button */}
          <button
            onClick={openCreateModal}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              padding: '14px 28px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              border: 'none',
              borderRadius: '12px',
              fontSize: '15px',
              fontWeight: '600',
              cursor: 'pointer',
              boxShadow: '0 4px 15px rgba(102, 126, 234, 0.4)',
              transition: 'all 0.3s'
            }}
            onMouseEnter={(e) => {
              e.target.style.transform = 'translateY(-2px)';
              e.target.style.boxShadow = '0 6px 20px rgba(102, 126, 234, 0.5)';
            }}
            onMouseLeave={(e) => {
              e.target.style.transform = 'translateY(0)';
              e.target.style.boxShadow = '0 4px 15px rgba(102, 126, 234, 0.4)';
            }}
          >
            <svg style={{ width: '20px', height: '20px' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4"></path>
            </svg>
            Nuevo Usuario
          </button>
        </div>

        {/* Messages */}
        {error && (
          <div style={{
            background: 'linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%)',
            borderRadius: '16px',
            padding: '16px 20px',
            marginBottom: '24px',
            color: 'white',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            boxShadow: '0 4px 15px rgba(255, 107, 107, 0.3)',
            animation: 'slideIn 0.3s ease-out'
          }}>
            <svg style={{ width: '24px', height: '24px', flexShrink: 0 }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <span style={{ fontWeight: '500' }}>{error}</span>
          </div>
        )}

        {success && (
          <div style={{
            background: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
            borderRadius: '16px',
            padding: '16px 20px',
            marginBottom: '24px',
            color: 'white',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            boxShadow: '0 4px 15px rgba(17, 153, 142, 0.3)',
            animation: 'slideIn 0.3s ease-out'
          }}>
            <svg style={{ width: '24px', height: '24px', flexShrink: 0 }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path>
            </svg>
            <span style={{ fontWeight: '500' }}>{success}</span>
          </div>
        )}

        {/* Loading */}
        {loading ? (
          <div style={{
            background: 'white',
            borderRadius: '20px',
            padding: '80px 40px',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            boxShadow: '0 10px 40px rgba(0,0,0,0.15)'
          }}>
            <div style={{ position: 'relative', width: '80px', height: '80px', marginBottom: '24px' }}>
              <div style={{
                width: '100%',
                height: '100%',
                border: '4px solid #f3f4f6',
                borderRadius: '50%'
              }}></div>
              <div style={{
                width: '100%',
                height: '100%',
                border: '4px solid #667eea',
                borderTopColor: 'transparent',
                borderRadius: '50%',
                position: 'absolute',
                top: 0,
                left: 0,
                animation: 'spin 1s linear infinite'
              }}></div>
            </div>
            <h3 style={{ fontSize: '20px', fontWeight: '700', color: '#1f2937', margin: '0 0 8px' }}>
              Cargando usuarios empresariales
            </h3>
            <p style={{ fontSize: '15px', color: '#6b7280', margin: 0 }}>
              Por favor espera un momento...
            </p>
          </div>
        ) : (
          /* Users Cards Grid */
          <div style={{ display: 'grid', gap: '20px' }}>
            {filteredUsers.length === 0 ? (
              <div style={{
                background: 'white',
                borderRadius: '20px',
                padding: '80px 40px',
                textAlign: 'center',
                boxShadow: '0 10px 40px rgba(0,0,0,0.15)'
              }}>
                <svg style={{ width: '80px', height: '80px', color: '#d1d5db', margin: '0 auto 24px' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path>
                </svg>
                <h3 style={{ fontSize: '24px', fontWeight: '700', color: '#1f2937', margin: '0 0 8px' }}>
                  No hay usuarios empresariales
                </h3>
                <p style={{ fontSize: '16px', color: '#6b7280', margin: '0 0 24px' }}>
                  Comienza creando tu primer usuario corporativo
                </p>
                <button
                  onClick={openCreateModal}
                  style={{
                    padding: '12px 24px',
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white',
                    border: 'none',
                    borderRadius: '10px',
                    fontSize: '15px',
                    fontWeight: '600',
                    cursor: 'pointer'
                  }}
                >
                  Crear primer usuario
                </button>
              </div>
            ) : (
              filteredUsers.map((user) => (
                <div key={user.idUser} style={{
                  background: 'white',
                  borderRadius: '20px',
                  padding: '28px',
                  boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
                  transition: 'all 0.3s',
                  border: '2px solid transparent',
                  position: 'relative',
                  overflow: 'hidden'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = 'translateY(-4px)';
                  e.currentTarget.style.boxShadow = '0 12px 35px rgba(0,0,0,0.12)';
                  e.currentTarget.style.borderColor = '#667eea';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = 'translateY(0)';
                  e.currentTarget.style.boxShadow = '0 4px 20px rgba(0,0,0,0.08)';
                  e.currentTarget.style.borderColor = 'transparent';
                }}
                >
                  {/* Status stripe */}
                  <div style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    height: '4px',
                    background: user.enabled ? 'linear-gradient(90deg, #11998e 0%, #38ef7d 100%)' : 'linear-gradient(90deg, #ff6b6b 0%, #ee5a6f 100%)'
                  }}></div>

                  <div style={{ display: 'flex', gap: '24px', flexWrap: 'wrap', alignItems: 'flex-start' }}>
                    {/* Avatar & Company Info */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '20px', flex: '1', minWidth: '300px' }}>
                      <div style={{
                        width: '80px',
                        height: '80px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        borderRadius: '16px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '28px',
                        color: 'white',
                        fontWeight: '700',
                        boxShadow: '0 8px 20px rgba(102, 126, 234, 0.3)',
                        flexShrink: 0
                      }}>
                        {getInitials(user.companyName)}
                      </div>
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <h3 style={{ fontSize: '22px', fontWeight: '700', color: '#1f2937', margin: '0 0 4px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {user.companyName}
                        </h3>
                        <p style={{ fontSize: '14px', color: '#6b7280', margin: '0 0 8px' }}>
                          ID: {user.idUser}
                        </p>
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '6px',
                          padding: '4px 12px',
                          borderRadius: '20px',
                          fontSize: '13px',
                          fontWeight: '600',
                          background: user.enabled ? 'linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%)' : 'linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%)',
                          color: user.enabled ? '#065f46' : '#92400e'
                        }}>
                          {user.enabled ? '✓' : '○'} {user.enabled ? 'Activo' : 'Inactivo'}
                        </span>
                      </div>
                    </div>

                    {/* Contact Info */}
                    <div style={{ flex: '1', minWidth: '250px' }}>
                      <div style={{ display: 'grid', gap: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                          <div style={{ width: '32px', height: '32px', background: '#f3f4f6', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                            👤
                          </div>
                          <div style={{ flex: 1, minWidth: 0 }}>
                            <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '2px' }}>Contacto</div>
                            <div style={{ fontSize: '15px', fontWeight: '600', color: '#1f2937', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user.name}</div>
                          </div>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                          <div style={{ width: '32px', height: '32px', background: '#f3f4f6', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                            📧
                          </div>
                          <div style={{ flex: 1, minWidth: 0 }}>
                            <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '2px' }}>Email</div>
                            <div style={{ fontSize: '14px', fontWeight: '500', color: '#4b5563', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user.email}</div>
                          </div>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                          <div style={{ width: '32px', height: '32px', background: '#f3f4f6', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                            📞
                          </div>
                          <div style={{ flex: 1, minWidth: 0 }}>
                            <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '2px' }}>Teléfono</div>
                            <div style={{ fontSize: '14px', fontWeight: '500', color: '#4b5563' }}>{user.phone}</div>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* API Key */}
                    <div style={{ flex: '1', minWidth: '250px' }}>
                      <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '8px', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                        🔑 API Key
                      </div>
                      <div style={{ 
                        background: '#f9fafb', 
                        borderRadius: '10px', 
                        padding: '12px', 
                        border: '2px dashed #d1d5db',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        marginBottom: '12px'
                      }}>
                        <code style={{ 
                          flex: 1, 
                          fontSize: '13px', 
                          fontFamily: 'monospace', 
                          color: '#374151', 
                          fontWeight: '600',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap'
                        }}>
                          {user.apiKey ? maskApiKey(user.apiKey) : 'N/A'}
                        </code>
                        {user.apiKey && (
                          <button
                            onClick={() => copyApiKey(user.apiKey)}
                            style={{
                              padding: '6px',
                              background: 'white',
                              border: '1px solid #d1d5db',
                              borderRadius: '6px',
                              cursor: 'pointer',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              transition: 'all 0.2s',
                              flexShrink: 0
                            }}
                            onMouseEnter={(e) => {
                              e.target.style.background = '#667eea';
                              e.target.style.borderColor = '#667eea';
                              e.target.querySelector('svg').style.color = 'white';
                            }}
                            onMouseLeave={(e) => {
                              e.target.style.background = 'white';
                              e.target.style.borderColor = '#d1d5db';
                              e.target.querySelector('svg').style.color = '#6b7280';
                            }}
                            title="Copiar API Key"
                          >
                            <svg style={{ width: '16px', height: '16px', color: '#6b7280', transition: 'color 0.2s' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                            </svg>
                          </button>
                        )}
                      </div>

                      <div style={{ fontSize: '11px', color: '#9ca3af', marginBottom: '12px' }}>
                        📅 Registrado: {formatDate(user.createdAt)}
                      </div>

                      {/* Actions */}
                      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                        <button
                          onClick={() => openEditModal(user)}
                          style={{
                            flex: '1',
                            minWidth: '70px',
                            padding: '8px 12px',
                            background: 'white',
                            border: '2px solid #667eea',
                            color: '#667eea',
                            borderRadius: '8px',
                            fontSize: '13px',
                            fontWeight: '600',
                            cursor: 'pointer',
                            transition: 'all 0.2s',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: '6px'
                          }}
                          onMouseEnter={(e) => {
                            e.target.style.background = '#667eea';
                            e.target.style.color = 'white';
                          }}
                          onMouseLeave={(e) => {
                            e.target.style.background = 'white';
                            e.target.style.color = '#667eea';
                          }}
                        >
                          <svg style={{ width: '14px', height: '14px' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                          </svg>
                          Editar
                        </button>
                        <button
                          onClick={() => regenerateApiKey(user)}
                          style={{
                            flex: '1',
                            minWidth: '80px',
                            padding: '8px 12px',
                            background: 'white',
                            border: '2px solid #10b981',
                            color: '#10b981',
                            borderRadius: '8px',
                            fontSize: '13px',
                            fontWeight: '600',
                            cursor: 'pointer',
                            transition: 'all 0.2s',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: '6px'
                          }}
                          onMouseEnter={(e) => {
                            e.target.style.background = '#10b981';
                            e.target.style.color = 'white';
                          }}
                          onMouseLeave={(e) => {
                            e.target.style.background = 'white';
                            e.target.style.color = '#10b981';
                          }}
                        >
                          <svg style={{ width: '14px', height: '14px' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"></path>
                          </svg>
                          Regenerar
                        </button>
                        <button
                          onClick={() => toggleStatus(user)}
                          style={{
                            flex: '1',
                            minWidth: '80px',
                            padding: '8px 12px',
                            background: 'white',
                            border: user.enabled ? '2px solid #f59e0b' : '2px solid #10b981',
                            color: user.enabled ? '#f59e0b' : '#10b981',
                            borderRadius: '8px',
                            fontSize: '13px',
                            fontWeight: '600',
                            cursor: 'pointer',
                            transition: 'all 0.2s',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: '6px'
                          }}
                          onMouseEnter={(e) => {
                            e.target.style.background = user.enabled ? '#f59e0b' : '#10b981';
                            e.target.style.color = 'white';
                          }}
                          onMouseLeave={(e) => {
                            e.target.style.background = 'white';
                            e.target.style.color = user.enabled ? '#f59e0b' : '#10b981';
                          }}
                        >
                          <svg style={{ width: '14px', height: '14px' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                          </svg>
                          {user.enabled ? 'Desactivar' : 'Activar'}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0, 0, 0, 0.6)',
          backdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 50,
          padding: '20px',
          animation: 'fadeIn 0.2s ease-out'
        }}>
          <div style={{
            background: 'white',
            borderRadius: '24px',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
            maxWidth: '800px',
            width: '100%',
            maxHeight: '90vh',
            overflow: 'hidden',
            display: 'flex',
            flexDirection: 'column',
            animation: 'slideUp 0.3s ease-out'
          }}>
            {/* Modal Header */}
            <div style={{
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              padding: '24px 32px',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <h2 style={{ fontSize: '24px', fontWeight: '700', color: 'white', margin: 0 }}>
                {editingUser ? '✏️ Editar Usuario Empresarial' : '➕ Nuevo Usuario Empresarial'}
              </h2>
              <button
                onClick={closeModal}
                style={{
                  background: 'rgba(255, 255, 255, 0.2)',
                  border: 'none',
                  borderRadius: '10px',
                  width: '36px',
                  height: '36px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  cursor: 'pointer',
                  transition: 'background 0.2s'
                }}
                onMouseEnter={(e) => e.target.style.background = 'rgba(255, 255, 255, 0.3)'}
                onMouseLeave={(e) => e.target.style.background = 'rgba(255, 255, 255, 0.2)'}
              >
                <svg style={{ width: '20px', height: '20px', color: 'white' }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>

            {/* Modal Body */}
            <form onSubmit={handleSubmit} style={{ padding: '32px', overflowY: 'auto', flex: 1 }}>
              <div style={{ display: 'grid', gap: '24px' }}>
                {/* Company Name */}
                <div>
                  <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#374151', marginBottom: '8px' }}>
                    🏢 Nombre de la Empresa *
                  </label>
                  <input
                    type="text"
                    value={formData.companyName}
                    onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
                    required
                    style={{
                      width: '100%',
                      padding: '12px 16px',
                      border: '2px solid #e5e7eb',
                      borderRadius: '10px',
                      fontSize: '15px',
                      outline: 'none',
                      transition: 'border-color 0.2s'
                    }}
                    onFocus={(e) => e.target.style.borderColor = '#667eea'}
                    onBlur={(e) => e.target.style.borderColor = '#e5e7eb'}
                    placeholder="Ej: Agencia de Viajes XYZ"
                  />
                </div>

                {/* Contact Name */}
                <div>
                  <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#374151', marginBottom: '8px' }}>
                    👤 Nombre del Contacto *
                  </label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    required
                    style={{
                      width: '100%',
                      padding: '12px 16px',
                      border: '2px solid #e5e7eb',
                      borderRadius: '10px',
                      fontSize: '15px',
                      outline: 'none',
                      transition: 'border-color 0.2s'
                    }}
                    onFocus={(e) => e.target.style.borderColor = '#667eea'}
                    onBlur={(e) => e.target.style.borderColor = '#e5e7eb'}
                    placeholder="Ej: Juan Pérez"
                  />
                </div>

                {/* Grid 2 columns */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
                  {/* Email */}
                  <div>
                    <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#374151', marginBottom: '8px' }}>
                      📧 Email *
                    </label>
                    <input
                      type="email"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      required
                      style={{
                        width: '100%',
                        padding: '12px 16px',
                        border: '2px solid #e5e7eb',
                        borderRadius: '10px',
                        fontSize: '15px',
                        outline: 'none',
                        transition: 'border-color 0.2s'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#667eea'}
                      onBlur={(e) => e.target.style.borderColor = '#e5e7eb'}
                      placeholder="contacto@empresa.com"
                    />
                  </div>

                  {/* Phone */}
                  <div>
                    <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#374151', marginBottom: '8px' }}>
                      📞 Teléfono *
                    </label>
                    <input
                      type="tel"
                      value={formData.phone}
                      onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                      required
                      style={{
                        width: '100%',
                        padding: '12px 16px',
                        border: '2px solid #e5e7eb',
                        borderRadius: '10px',
                        fontSize: '15px',
                        outline: 'none',
                        transition: 'border-color 0.2s'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#667eea'}
                      onBlur={(e) => e.target.style.borderColor = '#e5e7eb'}
                      placeholder="12345678"
                    />
                  </div>
                </div>

                {/* Grid 2 columns */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
                  {/* CUI */}
                  <div>
                    <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#374151', marginBottom: '8px' }}>
                      🆔 CUI/NIT *
                    </label>
                    <input
                      type="text"
                      value={formData.cui}
                      onChange={(e) => setFormData({ ...formData, cui: e.target.value })}
                      required
                      style={{
                        width: '100%',
                        padding: '12px 16px',
                        border: '2px solid #e5e7eb',
                        borderRadius: '10px',
                        fontSize: '15px',
                        outline: 'none',
                        transition: 'border-color 0.2s'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#667eea'}
                      onBlur={(e) => e.target.style.borderColor = '#e5e7eb'}
                      placeholder="1234567890"
                    />
                  </div>

                  {/* Birth Date */}
                  <div>
                    <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#374151', marginBottom: '8px' }}>
                      📅 Fecha de Nacimiento
                    </label>
                    <input
                      type="date"
                      value={formData.birthDate}
                      onChange={(e) => setFormData({ ...formData, birthDate: e.target.value })}
                      style={{
                        width: '100%',
                        padding: '12px 16px',
                        border: '2px solid #e5e7eb',
                        borderRadius: '10px',
                        fontSize: '15px',
                        outline: 'none',
                        transition: 'border-color 0.2s'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#667eea'}
                      onBlur={(e) => e.target.style.borderColor = '#e5e7eb'}
                    />
                  </div>
                </div>

                {/* Address */}
                <div>
                  <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#374151', marginBottom: '8px' }}>
                    📍 Dirección *
                  </label>
                  <textarea
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    required
                    rows="3"
                    style={{
                      width: '100%',
                      padding: '12px 16px',
                      border: '2px solid #e5e7eb',
                      borderRadius: '10px',
                      fontSize: '15px',
                      outline: 'none',
                      transition: 'border-color 0.2s',
                      resize: 'vertical',
                      fontFamily: 'inherit'
                    }}
                    onFocus={(e) => e.target.style.borderColor = '#667eea'}
                    onBlur={(e) => e.target.style.borderColor = '#e5e7eb'}
                    placeholder="Dirección completa de la empresa"
                  ></textarea>
                </div>

                {/* Password */}
                {!editingUser && (
                  <div>
                    <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#374151', marginBottom: '8px' }}>
                      🔒 Contraseña *
                    </label>
                    <input
                      type="password"
                      value={formData.password}
                      onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                      required={!editingUser}
                      minLength="6"
                      style={{
                        width: '100%',
                        padding: '12px 16px',
                        border: '2px solid #e5e7eb',
                        borderRadius: '10px',
                        fontSize: '15px',
                        outline: 'none',
                        transition: 'border-color 0.2s'
                      }}
                      onFocus={(e) => e.target.style.borderColor = '#667eea'}
                      onBlur={(e) => e.target.style.borderColor = '#e5e7eb'}
                      placeholder="Mínimo 6 caracteres"
                    />
                  </div>
                )}

                {/* Enabled */}
                <div style={{
                  background: '#f9fafb',
                  padding: '16px',
                  borderRadius: '10px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px'
                }}>
                  <input
                    type="checkbox"
                    id="enabled"
                    checked={formData.enabled}
                    onChange={(e) => setFormData({ ...formData, enabled: e.target.checked })}
                    style={{
                      width: '20px',
                      height: '20px',
                      cursor: 'pointer',
                      accentColor: '#667eea'
                    }}
                  />
                  <label htmlFor="enabled" style={{ fontSize: '15px', fontWeight: '500', color: '#374151', cursor: 'pointer' }}>
                    Usuario activo (puede usar el sistema)
                  </label>
                </div>

                {/* API Key (show if editing) */}
                {editingUser && formData.apiKey && (
                  <div style={{
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    borderRadius: '12px',
                    padding: '20px',
                    color: 'white'
                  }}>
                    <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', marginBottom: '12px', opacity: 0.9 }}>
                      🔑 API Key Actual
                    </label>
                    <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                      <code style={{
                        flex: 1,
                        fontSize: '14px',
                        background: 'rgba(255,255,255,0.2)',
                        padding: '12px 16px',
                        borderRadius: '8px',
                        fontFamily: 'monospace',
                        fontWeight: '600',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}>
                        {formData.apiKey}
                      </code>
                      <button
                        type="button"
                        onClick={() => copyApiKey(formData.apiKey)}
                        style={{
                          padding: '12px 20px',
                          background: 'rgba(255,255,255,0.2)',
                          border: '2px solid rgba(255,255,255,0.3)',
                          color: 'white',
                          borderRadius: '8px',
                          fontSize: '14px',
                          fontWeight: '600',
                          cursor: 'pointer',
                          transition: 'all 0.2s',
                          whiteSpace: 'nowrap'
                        }}
                        onMouseEnter={(e) => {
                          e.target.style.background = 'rgba(255,255,255,0.3)';
                          e.target.style.borderColor = 'rgba(255,255,255,0.5)';
                        }}
                        onMouseLeave={(e) => {
                          e.target.style.background = 'rgba(255,255,255,0.2)';
                          e.target.style.borderColor = 'rgba(255,255,255,0.3)';
                        }}
                      >
                        Copiar
                      </button>
                    </div>
                    <p style={{ fontSize: '12px', margin: '12px 0 0', opacity: 0.8 }}>
                      💡 Usa el botón "Regenerar API Key" en la lista para crear una nueva clave
                    </p>
                  </div>
                )}

                {/* Actions */}
                <div style={{ display: 'flex', gap: '12px', paddingTop: '8px' }}>
                  <button
                    type="button"
                    onClick={closeModal}
                    style={{
                      flex: '1',
                      padding: '14px 24px',
                      background: 'white',
                      border: '2px solid #e5e7eb',
                      color: '#6b7280',
                      borderRadius: '10px',
                      fontSize: '15px',
                      fontWeight: '600',
                      cursor: 'pointer',
                      transition: 'all 0.2s'
                    }}
                    onMouseEnter={(e) => {
                      e.target.style.background = '#f9fafb';
                      e.target.style.borderColor = '#d1d5db';
                    }}
                    onMouseLeave={(e) => {
                      e.target.style.background = 'white';
                      e.target.style.borderColor = '#e5e7eb';
                    }}
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    disabled={saving}
                    style={{
                      flex: '1',
                      padding: '14px 24px',
                      background: saving ? '#9ca3af' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      border: 'none',
                      color: 'white',
                      borderRadius: '10px',
                      fontSize: '15px',
                      fontWeight: '600',
                      cursor: saving ? 'not-allowed' : 'pointer',
                      transition: 'all 0.2s',
                      boxShadow: saving ? 'none' : '0 4px 15px rgba(102, 126, 234, 0.4)'
                    }}
                    onMouseEnter={(e) => {
                      if (!saving) {
                        e.target.style.boxShadow = '0 6px 20px rgba(102, 126, 234, 0.5)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (!saving) {
                        e.target.style.boxShadow = '0 4px 15px rgba(102, 126, 234, 0.4)';
                      }
                    }}
                  >
                    {saving ? 'Guardando...' : (editingUser ? 'Actualizar Usuario' : 'Crear Usuario')}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; }
          to { opacity: 1; }
        }
        @keyframes slideIn {
          from { transform: translateX(20px); opacity: 0; }
          to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideUp {
          from { transform: translateY(20px); opacity: 0; }
          to { transform: translateY(0); opacity: 1; }
        }
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}
